package de.locked.weatherstation.tinkerforge;

import com.tinkerforge.BrickletAmbientLight;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

public class MyBrickletAmbientLight implements MyBricklet {

    private final BrickletAmbientLight b;

    public MyBrickletAmbientLight(BrickletAmbientLight b) {
        this.b = b;
    }

    public MyBrickletAmbientLight(String UID_ambient, IPConnection ipcon) {
        this(new BrickletAmbientLight(UID_ambient, ipcon));
    }

    @Override
    public double getValue() throws TimeoutException, NotConnectedException {
        return b.getIlluminance()/ 10d;
    }

}
