package de.locked.weatherstation.model;

import de.locked.weatherstation.Measure;
import java.beans.PropertyChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import org.joda.time.DateTime;

public enum ChartModel {

    TEMPERATURE("Temperatur"), HUMIDITY("Luftfeuchtigkeit", 50, 90), BAROMETER("Luftdruck"), AMBIENT("Lichtstärke");

    private final ValuesModel valuesModel = new ValuesModel();
    private final String title;
    private final double defaultMin;
    private final double defaultMax;

    private ChartModel(String title) {
        this(title, Double.NaN, Double.NaN);
    }

    private ChartModel(String title, double defaultMin, double defaultMax) {
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

    public ChartModel next() {
        ChartModel[] all = ChartModel.values();
        int i = (ordinal() + 1) % all.length;
        return all[i];
    }

    public ChartModel prev() {
        ChartModel[] all = ChartModel.values();
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