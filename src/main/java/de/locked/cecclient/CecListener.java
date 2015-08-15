/*
 * Copyright 2014 Dr. Franz Graf <info@Locked.de>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.locked.cecclient;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Dr. Franz Graf <info@Locked.de>
 */
public class CecListener extends Thread implements AutoCloseable {

    private static final Logger log = Logger.getLogger(CecListener.class.getName());
    private static final int SLEEP_BETWEEN_RETRIES = 1000;
    private static final int EMPTY_STREAM_DELAY = 500;

    private final String command;
    private final Parser parser = new Parser();
    private final List<CallBackListener> listener = new LinkedList<>();
    private Process process;
    InputStream inputStream;
    InputStream errorStream;
    int maxRetryCount = 10;
    boolean keepAlive = true;

    public CecListener(String command) {
        this.command = command;
        setDaemon(true);
    }

    void openStreams() throws IOException {
        ProcessBuilder pb = new ProcessBuilder(command.split(" "));
        process = pb.start();
        inputStream = process.getInputStream();
        errorStream = process.getErrorStream();
    }

    @Override
    public void run() {
        int retryCount = 1;
        do {
            try {
                log.log(Level.INFO, "Starting cec-listener on command line. Attempt {0}", retryCount);
                openStreams();
                CecStreamConsumer is = new CecStreamConsumer("InputStream", inputStream, s -> fireEvent(parser.toKeyCode(s)));
                CecStreamConsumer es = new CecStreamConsumer("ErrorStream", errorStream);
                do {
                    is.maybeReadStream();
                    es.maybeReadStream();
                    sleep(EMPTY_STREAM_DELAY); // don't hammer the poor raspi down
                } while (keepAlive && !isInterrupted());

            } catch (IOException ex) {
                log.warning("Error reading CEC stream from commandline!");
            } catch (InterruptedException ex) {
                log.log(Level.SEVERE, "Thread interrupted", ex);
            } finally {
                close();
            }

            // set asleep after an error. Maybe the call is simply invalid. In this case I want to avoid going
            // into an infinite loop
            try {
                sleep(SLEEP_BETWEEN_RETRIES);
            } catch (InterruptedException ex) {
                log.info("Interrupted CECListener shutting down");
                return;
            }
        } while (keepAlive && !isInterrupted() && retryCount++ < maxRetryCount);
    }

    void fireEvent(KEvent key) {
        log.fine(key.toString());
        for (CallBackListener l : listener) {
            l.processEvent(key);
        }
    }

    public void addCallBackListener(CallBackListener l) {
        listener.add(l);
    }

    public void removeCallBackListener(CallBackListener l) {
        listener.remove(l);
    }

    @Override
    public void close() {
        if (process != null) {
            process.destroy();
        }
        closeStream(inputStream);
        closeStream(errorStream);
    }

    private void closeStream(InputStream is) {
        try {
            if (is != null) {
                is.close();
            }
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }
    }
}

class CecStreamConsumer {

    private static final Logger log = Logger.getLogger(CecStreamConsumer.class.getName());
    private final String label;
    private final InputStream is;
    private final Consumer<String> consumer;
    private final byte[] b = new byte[1];

    CecStreamConsumer(String label, InputStream stream, Consumer<String> consumer) {
        this.label = label;
        this.is = stream;
        this.consumer = consumer;
    }

    CecStreamConsumer(String label, InputStream stream) {
        this(label, stream, i -> {
        });
    }

    void maybeReadStream() throws IOException {
        if (is.available() > 0) {
            StringBuilder sb = new StringBuilder(60);
            while (is.available() > 0 && is.read(b) >= 0) {
                String s = new String(b).intern();
                if (s.equals("\n")) {
                    if(sb.length() > 0){
                        log.log(Level.FINE, "CEC command ''{0}'': {1}", new Object[]{label, s});
                        consumer.accept(sb.toString().trim());
                        sb = new StringBuilder(60);
                    }
                } else {
                    sb.append(s);
                }
            }
        }
    }
}
