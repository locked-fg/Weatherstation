package de.locked.weatherstation;

import de.locked.weatherstation.model.ChartModel;
import static de.locked.weatherstation.model.ChartModel.*;
import java.net.URL;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class FXMLDocumentController {

    private static final Logger log = Logger.getLogger(FXMLDocumentController.class.getName());
    private final Locale locale = Locale.GERMAN;
    private final String TITLE = "Wetter Gaißach";
    private final DateTimeFormatter fmt = DateTimeFormat.forPattern("EEEEE, dd. MMMMMMMM").withLocale(locale);

    // masterpane
    @FXML
    AnchorPane rootPane;
    @FXML
    GridPane contentPane;

    // Big right top
    @FXML
    private Label bigValue;
    @FXML
    private Label bigChartTitle;

    // header
    @FXML
    private Label title;
    @FXML
    private Label date;
    @FXML
    private Label chartTitle;

    // current
    @FXML
    private Label currentTemp;
    @FXML
    private Label currentHumidity;
    @FXML
    private Label currentPressure;
    @FXML
    private Label currentAmbient;

    // minMax
    @FXML
    private Label minMaxTemp;
    @FXML
    private Label minMaxHumidity;
    @FXML
    private Label minMaxPressure;
    @FXML
    private Label minMaxAmbient;

    // chart
    @FXML
    private LineChart<Long, Double> chart;
    @FXML
    private NumberAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    @FXML   // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    private ChartModel currentChart = TEMPERATURE;
    private Map<ChartModel, SimpleEntry<String, String>> formats = new HashMap<>();

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        // nah is this a nice place for this?
        // I don't want to put the formats into the MODEL classes as this is clearly a VIEW topic
        formats.put(AMBIENT, new AbstractMap.SimpleEntry<>("%.1f Lux", "%.1f / %.1f Lux"));
        formats.put(TEMPERATURE, new AbstractMap.SimpleEntry<>("%.1f°C", "%.1f / %.1f°C"));
        formats.put(HUMIDITY, new AbstractMap.SimpleEntry<>("%.1f%%", "%.1f / %.1f%%"));
        formats.put(BAROMETER, new AbstractMap.SimpleEntry<>("%.0fmBar", "%.0f / %.0fmBar"));

        xAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(xAxis) {

            @Override
            public String toString(Number object) {
                final String ddMM = "dd.MM.";
                DateTime dateTime = new DateTime(object.longValue());

                String today = new DateTime().toString(ddMM);
                String yesterday = new DateTime().minusDays(1).toString(ddMM);
                String current = dateTime.toString(ddMM);

                String pre;
                if (today.equals(current)) {
                    pre = "heute";
                } else if (yesterday.equals(current)) {
                    pre = "gestern";
                } else {
                    pre = current;
                }

                return pre + "\n" + dateTime.toString("H:00");
            }
        });

        setDate(new DateTime());
        title.setText(TITLE);

        initModels();
        initModel(currentChart);
    }

    public void next() {
        Platform.runLater(() -> {
            initModel(currentChart.next());
        });
    }

    public void prev() {
        Platform.runLater(() -> {
            initModel(currentChart.prev());
        });
    }

    public synchronized void setDate(DateTime now) {
        Platform.runLater(() -> {
            date.setText(fmt.print(now));
        });
    }

    private void initModels() {
        AMBIENT.addPropertyChangeListener(e -> {
            update(AMBIENT, currentAmbient, minMaxAmbient);
        });
        TEMPERATURE.addPropertyChangeListener(e -> {
            update(TEMPERATURE, currentTemp, minMaxTemp);
        });
        HUMIDITY.addPropertyChangeListener(e -> {
            update(HUMIDITY, currentHumidity, minMaxHumidity);
        });
        BAROMETER.addPropertyChangeListener(e -> {
            update(BAROMETER, currentPressure, minMaxPressure);
        });
    }

    private void initModel(ChartModel newModel) {
        log.info("now displaying " + newModel.name());
        currentChart = newModel;

        chartTitle.setText(currentChart.title());
        bigChartTitle.setText(currentChart.title());

        chart.getData().setAll(new XYChart.Series(currentChart.getValuesModel()));
        xAxis.setLowerBound(currentChart.getMinTime().getMillis());
        xAxis.setUpperBound(System.currentTimeMillis());

        update(currentChart, null, null);
    }

    private synchronized void update(ChartModel charts, Label current, Label minMax) {
        String fmt1 = formats.get(charts).getKey();
        String fmt2 = formats.get(charts).getValue();

        xAxis.setUpperBound(System.currentTimeMillis() + 15 * 60 * 1000); // +15min
        String curr = String.format(locale, fmt1, charts.getCurrentValue());
        String mm = String.format(locale, fmt2, charts.getMinValue(), charts.getMaxValue());

        if (current != null) {
            current.setText(curr);
        }
        if (minMax != null) {
            minMax.setText(mm);
        }
        if (currentChart == charts) {
            bigValue.setText(curr + "\n" + mm);

            yAxis.setAutoRanging(charts.isAutoRanging());
            double max = charts.getMaxY();
            double min = charts.getMinY();
            yAxis.setUpperBound(max);
            yAxis.setLowerBound(min);
            yAxis.setTickUnit((max - min) / 10);
        }
    }
}
