package de.locked.weatherstation.model;

import de.locked.weatherstation.Measure;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import org.joda.time.DateTime;

class ValuesModel extends BaseModel {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final ObservableList<Data<Long, Double>> model = FXCollections.observableArrayList();

    @Override
    public void add(Measure m) {
        // Now it's clear that we need to do process the call. The following methods must be called on the
        // FXApplicationThread as the add/remove/update calls will trigger an update of the UI
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> {
                super.add(m);
            });
            return;
        }
        super.add(m);
        pcs.firePropertyChange("UPDATE", 0, 1);
    }

    @Override
    protected DateTime addOrUpdate(Measure measure) {
        DateTime key = super.addOrUpdate(measure);

        double value = measure.getValue();
        Optional<XYChart.Data<Long, Double>> optional = model.stream().filter((XYChart.Data<Long, Double> m) -> eq(m, key)).findAny();
        if (optional.isPresent()) {
            optional.get().setYValue(value);
        } else {
            model.add(new XYChart.Data<>(key.getMillis(), value));
        }
        return key;
    }

    @Override
    protected void cleanup(DateTime limit) {
        super.cleanup(limit);
        for (int i = model.size() - 1; i >= 0; i--) {
            if (limit.isAfter((long) model.get(i).XValueProperty().get())) {
                model.remove(i);
            }
        }
    }

    public ObservableList<Data<Long, Double>> getModel() {
        return model;
    }

    void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

}
