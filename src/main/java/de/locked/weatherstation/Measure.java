package de.locked.weatherstation;

import org.joda.time.DateTime;

/**
 * @author Franz
 */
public class Measure {

    private final DateTime date;
    private final double value;

    public Measure(double value) {
        this(DateTime.now(), value);
    }
    
    public Measure(DateTime date, double value) {
        this.date = date;
        this.value = value;
    }

    public DateTime getDate() {
        return date;
    }

    public double getValue() {
        return value;
    }

    public boolean isAfter(Measure m) {
        return this.date.isAfter(m.getDate());
    }

    @Override
    public String toString() {
        return "Measure{" + "date=" + date + ", value=" + value + '}';
    }
}
