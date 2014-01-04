package de.locked.weatherstation;

import java.beans.PropertyChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import org.joda.time.DateTime;
import org.junit.Test;
import static org.junit.Assert.*;

public class ChartsTest {

    @Test
    public void testNext() {
        Charts[] values = Charts.values();
        for (int i = 0; i < values.length; i++) {
            Charts c = values[i].next();
            if (i < values.length - 1) {
                assertEquals(values[i + 1], c);
            }
            if (i == values.length - 1) {
                assertEquals(values[0], c);
            }
        }
    }

    @Test
    public void testPrev() {
        Charts[] values = Charts.values();
        for (int i = 0; i < values.length; i++) {
            Charts c = values[i].prev();
            if (i > 0 && i < values.length) {
                assertEquals(values[i - 1], c);
            }
            if (i == 0) {
                assertEquals(values[values.length - 1], c);
            }
        }
    }
}
