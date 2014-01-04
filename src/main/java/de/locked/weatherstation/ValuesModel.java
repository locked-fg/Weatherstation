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

public class ValuesModel {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final ObservableList<Data<Long, Double>> model = FXCollections.<Data<Long, Double>>observableArrayList();
    private final List<MyStats> map = new ArrayList<>(25);

    private Measure recent = new Measure(new DateTime(0), 0);

    public ObservableList<Data<Long, Double>> getModel() {
        return model;
    }

    public void add(Measure m) {
        final long limit = new DateTime().minusHours(25).getMillis();

        // check if we can simply ignore the incoming value
        if (m.getDate().getMillis() < limit) {
            return;
        }

        long key = m.getDate().getMillis();
        key = key - m.getDate().getMillis() % 3600_000; // get rid of minutes, sec, msec

        addOrUpdate(key, m);
        cleanup(limit);

        updateRecent(m);
        pcs.firePropertyChange("UPDATE", 0, 1);
    }

    public double getMin() {
        if (map.isEmpty()) {
            return 0d;
        }
        double min = Double.MAX_VALUE;
        for (MyStats m : map) {
            min = Math.min(min, m.getMean());
        }
        min = Math.min(min, recent.getValue());
        return min;
    }

    public double getMax() {
        if (map.isEmpty()) {
            return 0d;
        }
        double min = Double.MIN_VALUE;
        for (MyStats m : map) {
            min = Math.max(min, m.getMean());
        }
        min = Math.max(min, recent.getValue());
        return min;
    }

    private void updateRecent(Measure m) {
        if (m.isAfter(recent)) {
            recent = m;
        }
    }

    private void addOrUpdate(long key, Measure m) {
        // measure for this key in list and map?
        boolean found = false;
        for (MyStats stat : map) {
            if (stat.timestamp == key) { // yes there it is!
                found = true;
                stat.add(m.getValue());
                model.stream().filter(d -> d.getXValue() == key).forEach(d -> {
                    d.setYValue(stat.getMean());
                });
                break;
            }
        }
        if (!found) { // wasn't there
            map.add(new MyStats(key, m.getValue()));
            model.add(new Data<>(key, m.getValue()));
        }
    }

    /**
     * cleanup timed out values
     */
    private void cleanup(final long limit) {
        for (int i = map.size() - 1; i >= 0; i--) {
            MyStats myStat = map.get(i);
            if (myStat.timestamp < limit) {
                map.remove(i);
                for (int j = 0; j < model.size(); j++) { // clean ObservableList
                    if (model.get(j).XValueProperty().get() == myStat.timestamp) {
                        model.remove(j);
                    }
                }
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

        final long timestamp;

        private MyStats(Long ts, double value) {
            this.timestamp = ts;
            addValue(value);
        }

        public MyStats add(double val) {
            addValue(val);
            return this;
        }
    }
}
