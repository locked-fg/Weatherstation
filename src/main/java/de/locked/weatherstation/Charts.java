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

    public void add(double value) {
        add(new DateTime(), value);
    }

    public void add(DateTime d, double value) {
        valuesModel.add(new Measure(d, value));
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        valuesModel.addPropertyChangeListener(listener);
    }

    Charts next() {
        Charts[] all = Charts.values();
        int i = (ordinal() + 1) % all.length;
        return all[i];
    }

    Charts prev() {
        Charts[] all = Charts.values();
        int i = (ordinal() + all.length - 1) % all.length;
        return all[i];
    }

    double getCurrentValue() {
        return valuesModel.getCurrentValue();
    }

    double getMinValue() {
        return valuesModel.getMin();
    }

    double getMaxValue() {
        return valuesModel.getMax();
    }

    String title() {
        return title;
    }
}
