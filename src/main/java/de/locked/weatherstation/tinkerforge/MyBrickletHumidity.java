package de.locked.weatherstation.tinkerforge;

import com.tinkerforge.BrickletHumidity;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

public class MyBrickletHumidity implements MyBricklet {

    private final BrickletHumidity b;

    public MyBrickletHumidity(BrickletHumidity b) {
        this.b = b;
    }

    @Override
    public double getValue() throws TimeoutException, NotConnectedException {
        return b.getHumidity() / 10d;
    }

}
