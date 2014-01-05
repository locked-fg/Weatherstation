package de.locked.weatherstation;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;
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

    // masterpane
    @FXML
    AnchorPane rootPane;
    @FXML
    private GridPane contentPane;
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

    private Charts currentChart = Charts.TEMPERATURE;

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        xAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(xAxis) {

            @Override
            public String toString(Number object) {
                final String ddMM = "dd. MM.";
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

                return pre + " " + dateTime.toString("HH") + " Uhr";
            }
        });

        setDate(new DateTime());
        title.setText("Wetter Gaißach");

        setCurrentAmbient(0);
        setCurrentHumidity(0);
        setCurrentPressure(0);
        setCurrentTemp(0);

        setMinMaxAmbient(0, 0);
        setMinMaxHumidity(0, 0);
        setMinMaxPressure(0, 0);
        setMinMaxTemp(0, 0);

        initModels();
        initModel(currentChart);
    }

    public void next() {
        initModel(currentChart.next());
    }

    public void prev() {
        initModel(currentChart.prev());
    }

    private void initModels() {
        for (Charts aChart : Charts.values()) {
            aChart.addPropertyChangeListener(e -> {
                switch (aChart) {
                    case AMBIENT:
                        setCurrentAmbient(aChart.getCurrentValue());
                        setMinMaxAmbient(aChart.getMinValue(), aChart.getMaxValue());
                        break;
                    case TEMPERATURE:
                        setCurrentTemp(aChart.getCurrentValue());
                        setMinMaxTemp(aChart.getMinValue(), aChart.getMaxValue());
                        break;
                    case HUMIDITY:
                        setCurrentHumidity(aChart.getCurrentValue());
                        setMinMaxHumidity(aChart.getMinValue(), aChart.getMaxValue());
                        break;
                    case PRESSURE:
                        setCurrentPressure(aChart.getCurrentValue());
                        setMinMaxPressure(aChart.getMinValue(), aChart.getMaxValue());
                        break;
                }
            });
        }
    }

    private void initModel(Charts model) {
        log.info("now displaying " + model.name());
        currentChart = model;

        chartTitle.setText(currentChart.title());
        chart.getData().clear();
        chart.getData().add(new XYChart.Series(currentChart.getValuesModel()));
        chart.requestLayout();
    }

//<editor-fold defaultstate="collapsed" desc="setter">
    public void setDate(DateTime now) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("EEEEE, dd. MMMMMMMM").withLocale(locale);
        date.setText(fmt.print(now));
    }

    private void setCurrentAmbient(double d) {
        currentAmbient.setText(String.format(locale, "%.1f Lux", d));
    }

    private void setCurrentHumidity(double d) {
        currentHumidity.setText(String.format(locale, "%.1f%%", d));
    }

    private void setCurrentPressure(double d) {
        currentPressure.setText(String.format(locale, "%.0fmBar", d));
    }

    private void setCurrentTemp(double d) {
        currentTemp.setText(String.format(locale, "%.1f°C", d));
    }

    private void setMinMaxAmbient(double min, double max) {
        minMaxAmbient.setText(String.format(locale, "%.1f / %.1f Lux", min, max));
    }

    private void setMinMaxHumidity(double min, double max) {
        minMaxHumidity.setText(String.format(locale, "%.1f / %.1f%%", min, max));
    }

    private void setMinMaxPressure(double min, double max) {
        minMaxPressure.setText(String.format(locale, "%.0f / %.0fmBar", min, max));
    }

    private void setMinMaxTemp(double min, double max) {
        minMaxTemp.setText(String.format(locale, "%.1f / %.1f°C", min, max));
    }
//</editor-fold>

}
