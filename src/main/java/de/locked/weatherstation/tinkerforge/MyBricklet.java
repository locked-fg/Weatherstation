package de.locked.weatherstation.tinkerforge;

import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

public interface MyBricklet {

    double getValue() throws TimeoutException, NotConnectedException;
}