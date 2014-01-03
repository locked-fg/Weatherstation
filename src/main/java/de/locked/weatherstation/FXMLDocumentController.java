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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class FXMLDocumentController {

    static final Logger log = Logger.getLogger(FXMLDocumentController.class.getName());
    private final Locale locale = Locale.GERMAN;

    // masterpane
    @FXML
    private AnchorPane rootPane;
    // header
    @FXML
    private Label title;
    @FXML
    private Label date;
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
        setTitle("Wetter Gaißach");

        setCurrentAmbient(0);
        setCurrentHumidity(0);
        setCurrentPressure(0);
        setCurrentTemp(0);

        setMinMaxAmbient(0);
        setMinMaxHumidity(0);
        setMinMaxPressure(0);
        setMinMaxTemp(0);

        initModel(currentChart);
    }

    @FXML
    public void keyPress(KeyEvent e) {
        if (e.getCode() == KeyCode.RIGHT) {
            initModel(currentChart.next());
        } else if (e.getCode() == KeyCode.LEFT) {
            initModel(currentChart.prev());
        }
    }

    private void initModel(Charts model) {
        currentChart = model;
        // TODO set min/max
        chart.getData().add(new XYChart.Series(currentChart.getValuesModel()));
    }

//<editor-fold defaultstate="collapsed" desc="setter">
    public void setDate(DateTime now) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("EEEEE, dd. MMMMMMMM").withLocale(locale);
        date.setText(fmt.print(now));
    }

    private void setTitle(String str) {
        title.setText(str);
    }

    private void setCurrentAmbient(double d) {
        currentAmbient.setText(String.format(locale, "%.1f Lux", d));
    }

    private void setCurrentHumidity(double d) {
        currentHumidity.setText(String.format(locale, "%.0f%%", d));
    }

    private void setCurrentPressure(double d) {
        currentPressure.setText(String.format(locale, "%.0fmBar", d));
    }

    private void setCurrentTemp(double d) {
        currentTemp.setText(String.format(locale, "%.1f°C", d));
    }

    private void setMinMaxAmbient(double d) {
        minMaxAmbient.setText(String.format(locale, "%.1f Lux", d));
    }

    private void setMinMaxHumidity(double d) {
        minMaxHumidity.setText(String.format(locale, "%.0f%%", d));
    }

    private void setMinMaxPressure(double d) {
        minMaxPressure.setText(String.format(locale, "%.0fmBar", d));
    }

    private void setMinMaxTemp(double d) {
        minMaxTemp.setText(String.format(locale, "%.1f°C", d));
    }
//</editor-fold>
}
