package de.locked.weatherstation;

import java.beans.PropertyChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import org.joda.time.DateTime;

public enum Charts {

    TEMPERATURE("Temperatur"), HUMIDITY("Luftfeuchtigkeit"), BAROMETER("Luftdruck"), AMBIENT("Lichtstärke");

    private final ValuesModel valuesModel = new ValuesModel();
    private final String title;

    private Charts(String title) {
        this.title = title;
    }

    public ObservableList<XYChart.Data<Long, Double>> getValuesModel() {
        return valuesModel.getModel();
    }

    public DateTime getMinTime() {
        return valuesModel.minTime();
    }

    public void add(double value) {
        add(new Measure(new DateTime(), value));
    }

    public void add(Measure m) {
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
