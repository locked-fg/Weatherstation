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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Dr. Franz Graf <info@Locked.de>
 */
public class CecListener extends Thread implements AutoCloseable {

    private static final Logger log = Logger.getLogger(CecListener.class.getName());
    private static final String COMMAND = "/usr/local/bin/cec-client -d 8 -t prta -o Wetter";
    private static final int SLEEP_BETWEEN_RETRIES = 1000;

    private final Parser parser = new Parser();
    private final List<CallBackListener> listener = new LinkedList<>();
    private Process process;
    private InputStream inputStream;
    int maxRetryCount = 10;
    boolean keepAlive = true;

    public CecListener() {
        setDaemon(true);
    }

    @Override
    public void run() {
        int retryCount = 1;
        do {
            try {
                log.info("Starting cec-listener on command line. Attempt " + retryCount);
                inputStream = openStream();

                byte[] b = new byte[1];
                StringBuilder sb = new StringBuilder(60);
                while (inputStream.read(b) >= 0) {
                    retryCount = 1;
                    String s = new String(b);
                    if (s.equals("\n")) {
                        KEvent keyCode = parser.toKeyCode(sb.toString());
                        fireEvent(keyCode);
                        sb = new StringBuilder(60);
                    } else {
                        sb.append(s);
                    }
                }
            } catch (IOException ex) {
                log.warning("Error reading stream from commandline!");
            } finally {
                close();
            }

            // set asleep after an error. Maybe the call is simply invalid. In this case I want to avoid going into an
            // infinite loop
            try {
                sleep(SLEEP_BETWEEN_RETRIES);
            } catch (InterruptedException ex) {
                log.info("Interrupted CECListener shutting down");
                return;
            }
        } while (keepAlive && !isInterrupted() && retryCount++ < maxRetryCount);
    }

    InputStream openStream() throws IOException {
        ProcessBuilder pb = new ProcessBuilder(COMMAND.split(" "));
        process = pb.start();
        return process.getInputStream();
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
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }
    }
}
