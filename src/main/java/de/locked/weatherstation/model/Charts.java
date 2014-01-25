package de.locked.weatherstation.model;

import de.locked.weatherstation.Measure;
import java.beans.PropertyChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import org.joda.time.DateTime;

public enum Charts {

    TEMPERATURE("Temperatur"), HUMIDITY("Luftfeuchtigkeit", 40, 90), BAROMETER("Luftdruck", 925, 965), AMBIENT("Lichtstärke");

    private final ValuesModel valuesModel = new ValuesModel();
    private final String title;
    private final double defaultMin;
    private final double defaultMax;

    private Charts(String title) {
        this(title, Double.NaN, Double.NaN);
    }

    private Charts(String title, double defaultMin, double defaultMax) {
        this.title = title;
        this.defaultMin = defaultMin;
        this.defaultMax = defaultMax;
    }

    public ObservableList<XYChart.Data<Long, Double>> getValuesModel() {
        return valuesModel.getModel();
    }

    public DateTime getMinTime() {
        return valuesModel.minTime();
    }

    public synchronized void add(double value) {
        add(new Measure(new DateTime(), value));
    }

    public synchronized void add(Measure m) {
        valuesModel.add(m);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        valuesModel.addPropertyChangeListener(listener);
    }

    public Charts next() {
        Charts[] all = Charts.values();
        int i = (ordinal() + 1) % all.length;
        return all[i];
    }

    public Charts prev() {
        Charts[] all = Charts.values();
        int i = (ordinal() + all.length - 1) % all.length;
        return all[i];
    }

    public double getCurrentValue() {
        return valuesModel.getCurrentValue();
    }

    public boolean isAutoRanging() {
        return Double.isNaN(defaultMin);
    }

    public double getMinY() {
        return defaultMin;
    }

    public double getMaxY() {
        return defaultMax;
    }

    public double getMinValue() {
        return valuesModel.getMin();
    }

    public double getMaxValue() {
        return valuesModel.getMax();
    }

    public String title() {
        return title;
    }
}