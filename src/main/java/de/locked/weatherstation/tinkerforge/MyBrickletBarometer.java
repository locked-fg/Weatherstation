package de.locked.weatherstation.tinkerforge;

import com.tinkerforge.BrickletBarometer;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

public class MyBrickletBarometer implements MyBricklet {

    private final BrickletBarometer b;

    public MyBrickletBarometer(BrickletBarometer b) {
        this.b = b;
    }

    @Override
    public double getValue() throws TimeoutException, NotConnectedException {
        return b.getAirPressure() / 1000d;
    }

}
