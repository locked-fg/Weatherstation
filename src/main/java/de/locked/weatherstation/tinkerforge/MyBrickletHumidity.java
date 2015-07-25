package de.locked.weatherstation.tinkerforge;

import com.tinkerforge.BrickletHumidity;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

public class MyBrickletHumidity implements MyBricklet {

    private final BrickletHumidity b;

    public MyBrickletHumidity(BrickletHumidity b) {
        this.b = b;
    }

    public MyBrickletHumidity(String UID_humidity, IPConnection ipcon) {
        this(new BrickletHumidity(UID_humidity, ipcon));
    }

    @Override
    public double getValue() throws TimeoutException, NotConnectedException {
        return b.getHumidity() / 10d;
    }

}
