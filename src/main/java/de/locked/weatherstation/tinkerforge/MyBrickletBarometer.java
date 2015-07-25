package de.locked.weatherstation.tinkerforge;

import com.tinkerforge.BrickletBarometer;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;
import static de.locked.weatherstation.tinkerforge.BrickletConfig.UID_barometer;

public class MyBrickletBarometer implements MyBricklet {

    private final BrickletBarometer b;

    public MyBrickletBarometer(BrickletBarometer b) {
        this.b = b;
    }

    public MyBrickletBarometer(String UID_barometer, IPConnection ipcon) {
        this(new BrickletBarometer(UID_barometer, ipcon));
    }

    @Override
    public double getValue() throws TimeoutException, NotConnectedException {
        return b.getAirPressure() / 1000d;
    }

}
