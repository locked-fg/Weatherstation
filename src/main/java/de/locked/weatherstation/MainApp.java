package de.locked.weatherstation;

import com.tinkerforge.BrickletHumidity;
import com.tinkerforge.BrickletTemperature;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;
import de.locked.weatherstation.tinkerforge.Connector;
import de.locked.weatherstation.tinkerforge.MyBricklet;
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
    private final String UID_temperature = "dXC";
    private final String UID_humidity = "hRd";

    @Override
    public void start(Stage stage) throws Exception {

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
                controller.rootPane.setPrefSize(w, h);
                controller.rootPane.setMaxSize(w, h);
                controller.rootPane.setMinSize(w, h);
                
                // set Grid Pane size as well!!
            }
        } catch (Exception e) {
            log.severe(e.getMessage());
        }

        stage.setTitle("JavaFX and Maven");
        stage.setScene(scene);
        stage.show();

        // refresh date 
        scheduler.scheduleAtFixedRate(() -> {
            controller.setDate(new DateTime());
        }, 5, 1, TimeUnit.MINUTES);

        Platform.runLater(() -> {
            try {
                initModelsFromCSV();
                listenTemp();
            } catch (IOException ex) {
                log.log(Level.SEVERE, null, ex);
            }
        });
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
                        log.info("invalid line (Ignoring): " + line);
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

    private void listenTemp() {
        final MyBricklet temp = new MyBrickletTemperature(new BrickletTemperature(UID_temperature, ipcon));
        final MyBricklet humidity = new MyBrickletHumidity(new BrickletHumidity(UID_humidity, ipcon));

        scheduler.scheduleAtFixedRate(new Connector(ipcon, host, port), 0, 1, TimeUnit.MINUTES);
        schedulePolling(temp, Charts.TEMPERATURE);
        schedulePolling(humidity, Charts.HUMIDITY);
    }

    private void schedulePolling(MyBricklet bricklet, Charts aChart) {
        scheduler.scheduleAtFixedRate(() -> {
            if (ipcon.getConnectionState() != IPConnection.CONNECTION_STATE_CONNECTED) {
                return;
            }

            log.info("pull for " + aChart.name());
            try {
                double t = bricklet.getValue();
                Platform.runLater(() -> {
                    aChart.add(t);
                });
            } catch (TimeoutException | NotConnectedException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

}
