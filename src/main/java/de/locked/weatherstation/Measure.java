package de.locked.weatherstation;

import org.joda.time.DateTime;

/**
 * @author Franz
 */
public class Measure {
    private DateTime date;
    private double value;

    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
    
}
