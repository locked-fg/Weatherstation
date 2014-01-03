package de.locked.weatherstation;

import java.util.HashMap;
import java.util.function.LongFunction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart.Data;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.joda.time.DateTime;

public class ValuesModel {

    private final ObservableList<Data<Long, Double>> model = FXCollections.<Data<Long, Double>>observableArrayList();
    private final HashMap<Long, MyStats> map = new HashMap<>();

    public ObservableList<Data<Long, Double>> getModel() {
        return model;
    }

    public void add(Measure m) {
        final long limit = new DateTime().minusHours(25).getMillis();
        // check if we can simply ignore the incoming value
        if (m.getDate().getMillis() < limit) {
            return;
        }

        Long tmpKey = m.getDate().getMillis();
        final Long key = tmpKey - m.getDate().getMillis() % 3600_000; // get rid of minutes, sec, msec

        if (!map.containsKey(key)) { // add
            map.put(key, new MyStats(m.getValue()));
            model.add(new Data<>(key, m.getValue()));
        } else { // update
            final MyStats stats = map.get(key).add(m.getValue());
            model.stream().filter(d -> d.getXValue() == key).forEach(d -> {
                d.setYValue(stats.getMean());
            });
        }

        // cleanup timed out values
        for (Long timestamp : map.keySet()) {
            if (timestamp < limit) {
                map.remove(timestamp);
                for (int i = 0; i < model.size(); i++) {
                    if (model.get(i).XValueProperty().get() == timestamp) {
                        model.remove(i);
                    }
                }
            }
        }
    }

    class MyStats extends SummaryStatistics {

        public MyStats(double val) {
            addValue(val);
        }

        public MyStats add(double val) {
            addValue(val);
            return this;
        }
    }
}
