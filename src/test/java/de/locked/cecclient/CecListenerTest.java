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
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Dr. Franz Graf <code@Locked.de>
 */
public class CecListenerTest {

    @Test
    public void testRun() throws Exception {
        CecListener c = new CecListener() {
            int i = 0;

            @Override
            void fireEvent(KEvent key) {
                i++;
                System.out.println("process line #" + i);
                if (i < 68 || (i > 68 && i < 73)) {
                    assertTrue(key.isUnmapped());
                } else if (i == 68) {
                    assertFalse(key.isUnmapped());
                    assertEquals(KeyEvent.VK_1, key.getCode());
                    assertTrue(key.isPressed());
                } else if (i == 73) {
                    assertFalse(key.isUnmapped());
                    assertFalse(key.isPressed());
                }
            }

            @Override
            InputStream getStream() throws IOException {
                // Inject test stream
                return getClass().getResourceAsStream("/input.log");
            }
        };
        c.run();
        c.close();
    }

}
