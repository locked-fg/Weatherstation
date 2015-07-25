/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.locked.weatherstation.model;

import de.locked.weatherstation.Measure;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.scene.chart.XYChart;
import org.joda.time.DateTime;

public class BaseModel implements MeasureSink {

    protected static final int TIME_TOLERANCE = 60_000;
    protected static final int LIMIT_HOURS = 48;
    protected final List<MyStat> values = new ArrayList<>(49);
    protected Measure recent = new Measure(new DateTime(0), 0);
    protected double min = Double.MAX_VALUE;
    protected double max = Double.MIN_VALUE;

    @Override
    public void add(Measure m) {
        // check if we can simply ignore the incoming value
        DateTime limit = new DateTime().minusHours(LIMIT_HOURS);
        if (m.getDate().isBefore(limit)) {
            return;
        }

        addOrUpdate(m);
        cleanup(limit);
        updateRecent(m);
        setMinMax();
    }

    protected void setMinMax() {
        min = Double.MAX_VALUE;
        max = Double.MIN_VALUE;
        for (MyStat m : values) {
            min = Math.min(min, m.getMean());
            max = Math.max(max, m.getMean());
        }
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    protected void updateRecent(Measure m) {
        if (m.isAfter(recent)) {
            recent = m;
        }
    }

    protected DateTime addOrUpdate(Measure measure) {
        // get rid of min, sec, msec
        DateTime date = measure.getDate();
        final DateTime key = new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), date.getHourOfDay(), 0);
        double value = measure.getValue();
        {
            Optional<MyStat> optional = values.stream().filter((MyStat s) -> eq(s, key)).findAny();
            if (optional.isPresent()) {
                value = optional.get().add(measure).getMean();
            } else {
                values.add(new MyStat(key, value));
            }
        }
        return key;
    }

    protected boolean eq(XYChart.Data<Long, Double> d, DateTime dt) {
        return eq(d.getXValue().longValue(), dt.getMillis());
    }

    protected boolean eq(MyStat dt, DateTime m) {
        return eq(dt.getDate().getMillis(), m.getMillis());
    }

    protected boolean eq(long a, long b) {
        return Math.abs(a - b) < TIME_TOLERANCE;
    }

    public DateTime minTime() {
        return new DateTime().minusHours(LIMIT_HOURS);
    }

    /**
     * cleanup timed out values
     * @param limit
     */
    protected void cleanup(DateTime limit) {
        for (int i = values.size() - 1; i >= 0; i--) {
            MyStat myStat = values.get(i);
            if (limit.isAfter(myStat.timestamp)) {
                values.remove(i);
            }
        }

    }

    public double getCurrentValue() {
        return recent.getValue();
    }

    public double[] getValues() {
        double[] v = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            v[i] = values.get(i).getMean();
        }
        return v;
    }

}
