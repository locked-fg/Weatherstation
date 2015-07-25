package de.locked.weatherstation.tinkerforge;

import com.tinkerforge.BrickletTemperature;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

public class MyBrickletTemperature implements MyBricklet {

    private final BrickletTemperature b;

    public MyBrickletTemperature(BrickletTemperature b) {
        this.b = b;
    }

    public MyBrickletTemperature(String UID_temperature, IPConnection ipcon) {
        this(new BrickletTemperature(UID_temperature, ipcon));
    }

    @Override
    public double getValue() throws TimeoutException, NotConnectedException {
        return b.getTemperature() / 100d;
    }

}
