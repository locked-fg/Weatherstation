/*
 * Copyright 2014 Dr. Franz Graf <code@Locked.de>.
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

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * @author Dr. Franz Graf <code@Locked.de>
 */
public class Parser {

    public static final String TRAFFIC = "TRAFFIC";
    public static final String INCOMING = ">>";
    public static final Pattern CODEPATTERN = Pattern.compile("^[a-z0-9]{2}:[a-z0-9]{2}:[a-z0-9]{2}$");

    private final KEvent UNMAPPED = new KEvent(KeyEvent.VK_UNDEFINED, true);

    final HashMap<String, KEvent> eventMap = new HashMap<>();

    public Parser() {
        eventMap.put("04:44:20", new KEvent(KeyEvent.VK_0, true));
        eventMap.put("04:44:21", new KEvent(KeyEvent.VK_1, true));
        eventMap.put("04:44:22", new KEvent(KeyEvent.VK_2, true));
        eventMap.put("04:44:23", new KEvent(KeyEvent.VK_3, true));
        eventMap.put("04:44:24", new KEvent(KeyEvent.VK_4, true));
        eventMap.put("04:44:25", new KEvent(KeyEvent.VK_5, true));
        eventMap.put("04:44:26", new KEvent(KeyEvent.VK_6, true));
        eventMap.put("04:44:27", new KEvent(KeyEvent.VK_7, true));
        eventMap.put("04:44:28", new KEvent(KeyEvent.VK_8, true));
        eventMap.put("04:44:29", new KEvent(KeyEvent.VK_9, true));

        eventMap.put("04:8b:20", new KEvent(KeyEvent.VK_0, false));
        eventMap.put("04:8b:21", new KEvent(KeyEvent.VK_1, false));
        eventMap.put("04:8b:22", new KEvent(KeyEvent.VK_2, false));
        eventMap.put("04:8b:23", new KEvent(KeyEvent.VK_3, false));
        eventMap.put("04:8b:24", new KEvent(KeyEvent.VK_4, false));
        eventMap.put("04:8b:25", new KEvent(KeyEvent.VK_5, false));
        eventMap.put("04:8b:26", new KEvent(KeyEvent.VK_6, false));
        eventMap.put("04:8b:27", new KEvent(KeyEvent.VK_7, false));
        eventMap.put("04:8b:28", new KEvent(KeyEvent.VK_8, false));
        eventMap.put("04:8b:29", new KEvent(KeyEvent.VK_9, false));

        eventMap.put("04:44:30", new KEvent(KeyEvent.VK_PAGE_UP, true));
        eventMap.put("04:8b:30", new KEvent(KeyEvent.VK_PAGE_UP, false));

        eventMap.put("04:44:31", new KEvent(KeyEvent.VK_PAGE_DOWN, true));
        eventMap.put("04:8b:31", new KEvent(KeyEvent.VK_PAGE_DOWN, false));

        eventMap.put("04:8b:04", new KEvent(KeyEvent.VK_RIGHT, true));
        eventMap.put("04:44:04", new KEvent(KeyEvent.VK_RIGHT, false));

        eventMap.put("04:44:03", new KEvent(KeyEvent.VK_LEFT, true));
        eventMap.put("04:8b:03", new KEvent(KeyEvent.VK_LEFT, false));

        eventMap.put("04:44:01", new KEvent(KeyEvent.VK_UP, true));
        eventMap.put("04:8b:01", new KEvent(KeyEvent.VK_UP, false));
        
        eventMap.put("04:44:02", new KEvent(KeyEvent.VK_DOWN, true));
        eventMap.put("04:8b:02", new KEvent(KeyEvent.VK_DOWN, false));
    }

    public KEvent toKeyCode(String line) {
        return cec2KeyCode(line2cecCode(line));
    }

    KEvent cec2KeyCode(String code) {
        KEvent i = eventMap.get(code);
        return i == null ? UNMAPPED : i;
    }

    String line2cecCode(String lineRaw) {
        String line = (lineRaw == null) ? "" : lineRaw.trim();
        if (line.isEmpty()){
            return null;
        }
        if (!line.startsWith(TRAFFIC)) {
            return null;
        }
        if (!line.contains(INCOMING)) {
            return null;
        }
        String[] split = line.split(INCOMING);
        if (split.length != 2) {
            return null;
        }

        String code = split[1].trim();
        if (!CODEPATTERN.matcher(code).matches()) {
            return null;
        }

        return code;
    }
}
