package de.locked.weatherstation;

import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import org.joda.time.DateTime;

public enum Charts {

    TEMPERATURE, HUMIDITY, PRESSURE, AMBIENT;

    final ValuesModel valuesModel = new ValuesModel();

    public ObservableList<XYChart.Data<Long, Double>> getValuesModel() {
        return valuesModel.getModel();
    }

    public void add(DateTime d, double value) {
        valuesModel.add(new Measure(d, value));
    }

//    public ValuesModel getModel() {
//        return valuesModel;
//    }
    Charts next() {
        Charts[] all = Charts.values();
        int i = ordinal() + 1 % all.length;
        return all[i];
    }

    Charts prev() {
        Charts[] all = Charts.values();
        int i = ordinal() + all.length - 1 % all.length;
        return all[i];
    }

}
