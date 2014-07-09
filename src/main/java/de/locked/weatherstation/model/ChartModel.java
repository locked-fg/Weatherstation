package de.locked.weatherstation.model;

import de.locked.weatherstation.Measure;
import java.beans.PropertyChangeListener;
import java.util.function.Function;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;
import org.joda.time.DateTime;

public enum ChartModel {

    TEMPERATURE("Temperatur"), 
    HUMIDITY("Luftfeuchtigkeit", 50, 90), 
    BAROMETER("Luftdruck"),
    AMBIENTLIGHT("Lichtstärke", e -> Math.log(Math.max(1,e)), e -> Math.pow(Math.E, e));

    private final String title;
    private final ValuesModel valuesModel = new ValuesModel();
    private final double defaultMin;
    private final double defaultMax;
    private Function<Double, Double> in;
    private Function<Double, Double> out;

    private ChartModel(String title) {
        this(title, Double.NaN, Double.NaN);
    }
    
    private ChartModel(String title, Function<Double, Double> in, Function<Double, Double> out) {
        this(title, Double.NaN, Double.NaN, in, out);
    }

    private ChartModel(String title, double defaultMin, double defaultMax) {
        this(title, defaultMin, defaultMax, e -> e, e -> e);
    }
    
    private ChartModel(String title, double defaultMin, double defaultMax, Function<Double, Double> in, Function<Double, Double> out) {
        this.title = title;
        this.defaultMin = defaultMin;
        this.defaultMax = defaultMax;
        this.in = in;
        this.out = out;
    }

    public StringConverter<Number> getTickLabelFormatter() {
        return new NumberStringConverter("#") {

                @Override
                public String toString(Number value) {
                    if (value != null && value instanceof Double) {
                        value = out.apply((Double) value);
                    }
                    return super.toString(value);
                }

            };
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
        Measure measure = new Measure(m.getDate(), in.apply(m.getValue()));
        valuesModel.add(measure);
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
        return out.apply(valuesModel.getCurrentValue());
    }

    public boolean isAutoRanging() {
        return Double.isNaN(defaultMin);
    }

    public double getMinY() {
        if (!Double.isNaN(defaultMin)) {
            return Math.min(defaultMin, getMinValue());
        } else {
            return getMinValue();
        }
    }

    public double getMaxY() {
        if (!Double.isNaN(defaultMax)) {
            return Math.max(defaultMax, getMaxValue());
        } else {
            return getMaxValue();
        }
    }

    public double getMinValue() {
        return out.apply(valuesModel.getMin());
    }

    public double getMaxValue() {
        return out.apply(valuesModel.getMax());
    }

    public String title() {
        return title;
    }
}

