package de.locked.weatherstation;

import static de.locked.weatherstation.Charts.*;
import java.net.URL;
import java.util.Locale;
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

    @FXML   // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    private Charts currentChart = TEMPERATURE;

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
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

    public synchronized void next() {
        Platform.runLater(() -> {
            initModel(currentChart.next());
        });
    }

    public synchronized void prev() {
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
            update("%.1f Lux", "%.1f / %.1f Lux", AMBIENT, currentAmbient, minMaxAmbient);
        });
        TEMPERATURE.addPropertyChangeListener(e -> {
            update("%.1f°C", "%.1f / %.1f°C", TEMPERATURE, currentTemp, minMaxTemp);
        });
        HUMIDITY.addPropertyChangeListener(e -> {
            update("%.1f%%", "%.1f / %.1f%%", HUMIDITY, currentHumidity, minMaxHumidity);
        });
        BAROMETER.addPropertyChangeListener(e -> {
            update("%.0fmBar", "%.0f / %.0fmBar", BAROMETER, currentPressure, minMaxPressure);
        });
    }

    private void initModel(Charts model) {
        log.info("now displaying " + model.name());
        currentChart = model;

        chartTitle.setText(currentChart.title());
        bigChartTitle.setText(currentChart.title());

        chart.getData().clear();
        chart.getData().add(new XYChart.Series(currentChart.getValuesModel()));
        xAxis.setLowerBound(currentChart.getMinTime().getMillis());
        xAxis.setUpperBound(System.currentTimeMillis());
        chart.requestLayout();
    }

    private void update(String fmt1, String fmt2, Charts c, Label current, Label minMax) {
        String curr = String.format(locale, fmt1, c.getCurrentValue());
        String mm = String.format(locale, fmt2, c.getMinValue(), c.getMaxValue());

        current.setText(curr);
        minMax.setText(mm);
        if (currentChart == c) {
            bigValue.setText(curr + "\n" + mm);
        }
    }
}
