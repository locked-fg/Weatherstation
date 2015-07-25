package de.locked.weatherstation.model;

import de.locked.weatherstation.Measure;

public interface MeasureSink {

    void add(Measure m);

}
