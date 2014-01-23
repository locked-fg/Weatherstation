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
    private static final String COMMAND[] = {"/usr/local/bin/cec-client", "-d", "8", "-t", "prta", "-o", "Wetter"};
    private final Parser parser = new Parser();
    private Process process;
    private InputStream inputStream;
    private List<CallBackListener> listener = new LinkedList<>();

    public CecListener() {
        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            inputStream = getStream();

            byte[] b = new byte[1];
            StringBuilder sb = new StringBuilder(60);
            while (inputStream.read(b) >= 0) {
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
            log.log(Level.SEVERE, null, ex);
        } finally {
            close();
        }
    }

    InputStream getStream() throws IOException {
        ProcessBuilder pb = new ProcessBuilder(COMMAND);
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
            inputStream.close();
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }
    }
}
