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
import junit.framework.TestCase;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Dr. Franz Graf <code@Locked.de>
 */
public class ParserTest extends TestCase {

    @Test
    public void testGetCecCode() {
        Parser p = new Parser();
        String s, cec;

        s = "TRAFFIC: [            5614]     >> 01:44:21";
        assertEquals("01:44:21", p.line2cecCode(s));

        s = "DEBUG:   [            5615]     >> TV (0) -> Recorder 1 (1): user control pressed (44)";
        assertNull(p.line2cecCode(s));

        s = "DEBUG:   [            5615]     key pressed: 1 (21)";
        assertNull(p.line2cecCode(s));

        s = "DEBUG:   [            5615]     making Recorder 1 (1) the active source";
        assertNull(p.line2cecCode(s));

        s = "NOTICE:  [            5615]     >> source activated: Recorder 1 (1)";
        assertNull(p.line2cecCode(s));

        s = "TRAFFIC: [            5845]     >> 01:8b:21";
        assertEquals("01:8b:21", p.line2cecCode(s));

        s = "DEBUG:   [            5846]     >> TV (0) -> Recorder 1 (1): vendor remote button up (8B)";
        assertNull(p.line2cecCode(s));

        s = "DEBUG:   [            5846]     key released: 1 (21)";
        assertNull(p.line2cecCode(s));

        s = "TRAFFIC: [           11770]     >> 01:44:22";
        assertEquals("01:44:22", p.line2cecCode(s));

        s = "DEBUG:   [           11771]     >> TV (0) -> Recorder 1 (1): user control pressed (44)";
        assertNull(p.line2cecCode(s));
    }

    @Test
    public void testCecCode() {
        Parser p = new Parser();
        p.eventMap.clear();

        p.eventMap.put("01:44:01", new KEvent(KeyEvent.VK_UP, true));
        p.eventMap.put("01:8b:01", new KEvent(KeyEvent.VK_UP, false));

        assertEquals(KeyEvent.VK_UP, p.cec2KeyCode("01:44:01").code);
        assertTrue(p.cec2KeyCode("01:44:01").pressed);
        assertFalse(p.cec2KeyCode("01:8b:01").pressed);

        assertEquals(KeyEvent.VK_UNDEFINED, p.cec2KeyCode("00:8b:01").code);
    }

    @Test
    public void testToKeyCode() {
        String s;
        Parser p = new Parser();

        s = "TRAFFIC: [            5845]     >> 01:8b:21";
        assertEquals(KeyEvent.VK_1, p.toKeyCode(s).code);
        assertFalse(p.toKeyCode(s).pressed);

        s = "TRAFFIC: [            5614]     >> 01:44:21";
        assertEquals(KeyEvent.VK_1, p.toKeyCode(s).code);
        assertTrue(p.toKeyCode(s).pressed);

        s = "DEBUG:   [            5615]     key pressed: 1 (21)";
        assertEquals(KeyEvent.VK_UNDEFINED, p.toKeyCode(s).code);
    }
}
