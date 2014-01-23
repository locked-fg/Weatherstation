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

/**
 *
 * @author Dr. Franz Graf <code@Locked.de>
 */
public class KEvent {

    public final int code;
    public final boolean pressed;

    public KEvent(int code, boolean pressed) {
        this.code = code;
        this.pressed = pressed;
    }

    public boolean isUnmapped() {
        return code == KeyEvent.VK_UNDEFINED;
    }

    public int getCode() {
        return code;
    }

    public boolean isPressed() {
        return pressed;
    }

    @Override
    public String toString() {
        return "KEvent{" + "code=" + code + ", pressed=" + pressed + ", unmapped=" + isUnmapped() + '}';
    }
}
