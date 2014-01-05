package de.locked.weatherstation;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart.Data;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.joda.time.DateTime;
import org.joda.time.ReadableInstant;
import org.joda.time.Seconds;

public class ValuesModel {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final ObservableList<Data<Long, Double>> model = FXCollections.<Data<Long, Double>>observableArrayList();
    private final List<MyStats> statList = new ArrayList<>(25);

    private Measure recent = new Measure(new DateTime(0), 0);

    public ObservableList<Data<Long, Double>> getModel() {
        return model;
    }

    public void add(Measure m) {
        DateTime limit = new DateTime().minusHours(25);
        // check if we can simply ignore the incoming value
        if (m.getDate().isBefore(limit)) {
            return;
        }

        addOrUpdate(m);
        cleanup(limit);

        updateRecent(m);
        pcs.firePropertyChange("UPDATE", 0, 1);
    }

    public double getMin() {
        DateTime now = new DateTime().minusHours(12);
        double min = Double.MAX_VALUE;
        for (MyStats m : statList) {
            if (m.isAfter(now)) {
                min = Math.min(min, m.getMean());
            }
        }
        min = Math.min(min, recent.getValue());
        return min;
    }

    public double getMax() {
        DateTime now = new DateTime().minusHours(12);
        double min = Double.MIN_VALUE;
        for (MyStats m : statList) {
            if (m.isAfter(now)) {
                min = Math.max(min, m.getMean());
            }
        }
        min = Math.max(min, recent.getValue());
        return min;
    }

    private void updateRecent(Measure m) {
        if (m.isAfter(recent)) {
            recent = m;
        }
    }

    private void addOrUpdate(Measure m) {
        DateTime date = m.getDate();
        // get rid of min, sec, msec
        DateTime key = new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), date.getHourOfDay(), 0);
        long keyMillis = key.getMillis();

        // measure for this key in list and map?
        boolean found = false;
        for (MyStats stat : statList) {
            if (Seconds.secondsBetween(stat.timestamp, key).getSeconds() < 15) {
                found = true;
                stat.add(m.getValue());
                model.stream().filter(d -> d.getXValue() == keyMillis).forEach(d -> {
                    d.setYValue(stat.getMean());
                });
                break;
            }
        }
        if (!found) { // wasn't there
            statList.add(new MyStats(key, m.getValue()));
            model.add(new Data<>(keyMillis, m.getValue()));
        }
    }

    /**
     * cleanup timed out values
     */
    private void cleanup(DateTime limit) {
        for (int i = statList.size() - 1; i >= 0; i--) {
            MyStats myStat = statList.get(i);
            if (myStat.isBefore(limit)) {
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

        private MyStats(DateTime ts, double value) {
            this.timestamp = ts;
            addValue(value);
        }

        public MyStats add(double val) {
            addValue(val);
            return this;
        }

        public boolean isAfter(long instant) {
            return timestamp.isAfter(instant);
        }

        public boolean isAfter(ReadableInstant instant) {
            return timestamp.isAfter(instant);
        }

        public boolean isBefore(ReadableInstant instant) {
            return timestamp.isBefore(instant);
        }
    }
}
