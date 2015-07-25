/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.locked.weatherstation.model;

import de.locked.weatherstation.Measure;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.joda.time.DateTime;
import org.joda.time.ReadableInstant;

public class MyStat extends SummaryStatistics {

    final DateTime timestamp;

    MyStat(DateTime dt, double value) {
        this.timestamp = dt;
        addValue(value);
    }

    MyStat add(Measure m) {
        addValue(m.getValue());
        return this;
    }

    public DateTime getDate() {
        return timestamp;
    }

    public boolean isAfter(ReadableInstant instant) {
        return timestamp.isAfter(instant);
    }

    public boolean isBefore(ReadableInstant instant) {
        return timestamp.isBefore(instant);
    }
}