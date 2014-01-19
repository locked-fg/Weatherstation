package de.locked.weatherstation;

import com.tinkerforge.BrickletAmbientLight;
import com.tinkerforge.BrickletBarometer;
import com.tinkerforge.BrickletHumidity;
import com.tinkerforge.BrickletTemperature;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;
import de.locked.weatherstation.tinkerforge.Connector;
import de.locked.weatherstation.tinkerforge.MyBricklet;
import de.locked.weatherstation.tinkerforge.MyBrickletAmbientLight;
import de.locked.weatherstation.tinkerforge.MyBrickletBarometer;
import de.locked.weatherstation.tinkerforge.MyBrickletHumidity;
import de.locked.weatherstation.tinkerforge.MyBrickletTemperature;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.joda.time.DateTime;

public class MainApp extends Application {

    private static final Logger log = Logger.getLogger(MainApp.class.getName());
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    //
    private final String host = "192.168.178.38";
    private final int port = 4223;
    private final IPConnection ipcon = new IPConnection();
    //
    private final String UID_temperature = "dXC";
    private final String UID_humidity = "hRd";
    private final String UID_ambient = "jzj";
    private final String UID_barometer = "jo7";

    @Override
    public void start(Stage stage) throws Exception {
        initModelsFromCSV();

        log.info("Welcome - starting " + getClass().getName());
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FXMLDocument.fxml"));
        loader.load();
        Parent root = loader.getRoot();

        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/base.css");

        FXMLDocumentController controller = loader.getController();
        scene.addEventHandler(KeyEvent.KEY_PRESSED, (KeyEvent e) -> {
            if (e.getCode() == KeyCode.RIGHT) {
                controller.next();
            } else if (e.getCode() == KeyCode.LEFT) {
                controller.prev();
            }
        });

        scene.addEventHandler(Event.ANY, (Event e) -> {
            log.info(e.toString());
        });

        try {
            if (getParameters().getNamed().containsKey("w")) {
                int w = Integer.parseInt(getParameters().getNamed().get("w"));
                int h = Integer.parseInt(getParameters().getNamed().get("h"));

                log.info("setting width/height to " + w + "/" + h);
                controller.rootPane.setPrefSize(w, h);
                controller.contentPane.setPrefSize(w, h);
            }
        } catch (NumberFormatException e) {
            log.severe(e.getMessage());
        }

        stage.setTitle("JavaFX and Maven");
        stage.setScene(scene);
        stage.show();

        // refresh date 
        scheduler.scheduleAtFixedRate(() -> {
            controller.setDate(new DateTime());
        }, 1, 1, TimeUnit.MINUTES);

        // read data from CSVs
        Platform.runLater(() -> {
            scheduleDiagramSwitch(controller);
            connectBricklets();
        });

    }

    private void scheduleDiagramSwitch(FXMLDocumentController controller) {
        // change view automatically as long as I don't get keyboard events from CEC
        scheduler.scheduleAtFixedRate(() -> {
            try {
                log.info("switch to next diagram");
                controller.next();
            } catch (Throwable t) {
                log.log(Level.SEVERE, t.getMessage(), t);
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application. main() serves only as fallback in case the
     * application can not be launched through deployment artifacts, e.g., in IDEs with limited FX support. NetBeans
     * ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * This is executed in the JavaFX application thread! Not particularly well, but this is just once at startup.
     *
     * @throws IOException
     */
    @SuppressWarnings("LoggerStringConcat")
    private void initModelsFromCSV() throws IOException {
        log.info("init data from CSVs");
        for (Charts chartModel : Charts.values()) {
            Map<String, String> params = getParameters().getNamed();
            String paramName = chartModel.name().toLowerCase() + "-csv";
            String csvPath = params.get(paramName);
            if (csvPath == null) {
                log.warning(paramName + " not given in command line. Ignoring");
                continue;
            }
            File csvFile = new File(csvPath);
            if (!csvFile.exists() || !csvFile.canRead()) {
                log.warning(csvFile.getAbsolutePath() + " cannot be found or read. Ignoring");
                continue;
            }
            log.info("init data from " + csvFile.getName() + " // " + csvFile.getAbsolutePath());
            try (BufferedReader in = new BufferedReader(new FileReader(csvFile))) {
                while (in.ready()) {
                    String line = in.readLine();
                    String[] parts = line.split("\t");
                    if (parts.length != 2) {
                        log.warning("invalid line (Ignoring): " + line);
                        continue;
                    }

                    try {
                        DateTime date = new DateTime(Long.parseLong(parts[0].trim()) * 1000L);
                        double value = Double.parseDouble(parts[1].trim());
                        chartModel.add(date, value);
                    } catch (NumberFormatException e) {
                        log.info("invalid line (Ignoring): " + line);
                    }
                }
            }
            log.info("file done");
        }
    }

    @Override
    public void stop() throws Exception {
        log.info("shutting down application");
        scheduler.shutdownNow();
        try {
            ipcon.disconnect();
        } catch (NotConnectedException ignore) {
        }
    }

    private void connectBricklets() {
        final MyBricklet temp = new MyBrickletTemperature(new BrickletTemperature(UID_temperature, ipcon));
        final MyBricklet humidity = new MyBrickletHumidity(new BrickletHumidity(UID_humidity, ipcon));
        final MyBricklet ambient = new MyBrickletAmbientLight(new BrickletAmbientLight(UID_ambient, ipcon));
        final MyBricklet barometer = new MyBrickletBarometer(new BrickletBarometer(UID_barometer, ipcon));

        scheduler.scheduleAtFixedRate(new Connector(ipcon, host, port), 0, 1, TimeUnit.MINUTES);
        schedulePolling(temp, Charts.TEMPERATURE);
        schedulePolling(humidity, Charts.HUMIDITY);
        schedulePolling(barometer, Charts.BAROMETER);
        schedulePolling(ambient, Charts.AMBIENT);
    }

    private void schedulePolling(MyBricklet bricklet, Charts aChart) {
        scheduler.scheduleAtFixedRate(() -> {
            if (ipcon.getConnectionState() != IPConnection.CONNECTION_STATE_CONNECTED) {
                log.warning("No connection available. Don't query sensor.");
                return;
            }

            try {
                double t = bricklet.getValue();
                log.fine("queried " + aChart.name() + ": " + t);
                Platform.runLater(() -> {
                    aChart.add(t);
                });
            } catch (TimeoutException | NotConnectedException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

}
