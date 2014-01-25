package de.locked.weatherstation.model;

import de.locked.weatherstation.Measure;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart.Data;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.joda.time.DateTime;
import org.joda.time.ReadableInstant;

class ValuesModel {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final ObservableList<Data<Long, Double>> model = FXCollections.observableArrayList();
    private final List<MyStats> statList = new ArrayList<>(49);

    private static final int TIME_TOLERANCE = 60_000;
    private static final int LIMIT_HOURS = 48;
    private Measure recent = new Measure(new DateTime(0), 0);
    private double min, max;

    public ObservableList<Data<Long, Double>> getModel() {
        return model;
    }

    public void add(Measure m) {
        DateTime limit = new DateTime().minusHours(LIMIT_HOURS);
        // check if we can simply ignore the incoming value
        if (m.getDate().isBefore(limit)) {
            return;
        }

        addOrUpdate(m);
        cleanup(limit);

        updateRecent(m);
        setMinMax();
        pcs.firePropertyChange("UPDATE", 0, 1);
    }

    private void setMinMax() {
        min = Double.MAX_VALUE;
        max = Double.MIN_VALUE;
        for (MyStats m : statList) {
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

    private void updateRecent(Measure m) {
        if (m.isAfter(recent)) {
            recent = m;
        }
    }

    private void addOrUpdate(Measure measure) {
        // get rid of min, sec, msec
        DateTime date = measure.getDate();
        final DateTime key = new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), date.getHourOfDay(), 0);

        double value = measure.getValue();
        {
            Optional<MyStats> optional = statList.stream().filter(s -> eq(s, key)).findAny();
            if (optional.isPresent()) {
                value = optional.get().add(measure).getMean();
            } else {
                statList.add(new MyStats(key, value));
            }
        }
        {
            Optional<Data<Long, Double>> optional = model.stream().filter(m -> eq(m, key)).findAny();
            if (optional.isPresent()) {
                optional.get().setYValue(value);
            } else {
                model.add(new Data<>(key.getMillis(), value));
            }
        }
    }

    private boolean eq(Data<Long, Double> d, DateTime dt) {
        return eq(d.getXValue().longValue(), dt.getMillis());
    }

    private boolean eq(MyStats dt, DateTime m) {
        return eq(dt.getDate().getMillis(), m.getMillis());
    }

    private boolean eq(long a, long b) {
        return Math.abs(a - b) < TIME_TOLERANCE;
    }

    public DateTime minTime() {
        return new DateTime().minusHours(LIMIT_HOURS);
    }

    /**
     * cleanup timed out values
     */
    private void cleanup(DateTime limit) {
        for (int i = statList.size() - 1; i >= 0; i--) {
            MyStats myStat = statList.get(i);
            if (limit.isAfter(myStat.timestamp)) {
                statList.remove(i);
            }
        }

        for (int i = model.size() - 1; i >= 0; i--) {
            if (limit.isAfter((long) model.get(i).XValueProperty().get())) {
                model.remove(i);
            }
        }
    }

    void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    double getCurrentValue() {
        return recent.getValue();
    }

    static class MyStats extends SummaryStatistics {

        final DateTime timestamp;

        private MyStats(DateTime dt, double value) {
            this.timestamp = dt;
            addValue(value);
        }

        private MyStats(DateTime dt, Measure measure) {
            this(dt, measure.getValue());
        }

        public MyStats add(Measure m) {
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
}
