package de.locked.weatherstation;

import com.tinkerforge.AlreadyConnectedException;
import com.tinkerforge.BrickletAmbientLight;
import com.tinkerforge.BrickletBarometer;
import com.tinkerforge.BrickletHumidity;
import com.tinkerforge.BrickletTemperature;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;
import de.locked.cecclient.CecListener;
import de.locked.cecclient.KEvent;
import static de.locked.weatherstation.Charts.*;
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
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.joda.time.DateTime;

public class MainApp extends Application {

    private static final Logger log = Logger.getLogger(MainApp.class.getName());
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    //
    private final String host = "192.168.178.38";
    private final int port = 4223;
    private final IPConnection ipcon = new IPConnection();
    //
    private final String UID_temperature = "dXC";
    private final String UID_humidity = "hRd";
    private final String UID_ambient = "jzj";
    private final String UID_barometer = "jo7";
    //
    private final int AUTO_SWITCH_DIAG = 15; // s
    private final int REFRESH_DATE = 60; // s
    private final int POLL_SENSORS = 5; // s
    private FXMLDocumentController controller;

    @Override
    public void start(Stage stage) throws Exception {
        log.info("Welcome - starting " + getClass().getName());
        initModelsFromCSV();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FXMLDocument.fxml"));
        loader.load();
        Scene scene = new Scene(loader.getRoot());
        scene.getStylesheets().add("/styles/base.css");

        controller = loader.getController();
        resize();
        stage.setTitle("JavaFX and Maven");
        stage.setScene(scene);
        stage.show();

        // on the CLI we won't get any of those
        scene.addEventHandler(KeyEvent.KEY_PRESSED, (KeyEvent e) -> {
            if (e.getCode() == KeyCode.RIGHT) {
                controller.next();
            } else if (e.getCode() == KeyCode.LEFT) {
                controller.prev();
            }
        });

        connectCEC();
        connectBricklets();

        // refresh date 
        scheduler.scheduleAtFixedRate(() -> {
            controller.setDate(new DateTime());
        }, REFRESH_DATE, REFRESH_DATE, TimeUnit.SECONDS);
    }

    private void connectCEC() {
        CecListener cec = new CecListener();
        cec.addCallBackListener((KEvent e) -> {
            if (!e.isUnmapped() && e.isPressed()) {
                if (e.getCode() == java.awt.event.KeyEvent.VK_RIGHT) {
                    controller.next();
                } else if (e.getCode() == java.awt.event.KeyEvent.VK_LEFT) {
                    controller.prev();
                }
            }
        });
        cec.start();
    }

    private void resize() {
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
    private void initModelsFromCSV() {
        log.info("init data from CSVs");
        Map<String, String> params = getParameters().getNamed();
        for (Charts chartModel : Charts.values()) {
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
                        chartModel.add(new Measure(date, value));
                    } catch (NumberFormatException e) {
                        log.info("invalid line (Ignoring): " + line);
                    }
                }
                log.info("file done");
            } catch (IOException e) {
                log.log(Level.WARNING, "IO Exception: ", e);
            }
        }
    }

    @Override
    public void stop() throws Exception {
        log.info("shutting down application");
        scheduler.shutdownNow();
        if (ipcon.getConnectionState() == IPConnection.CONNECTION_STATE_CONNECTED) {
            ipcon.disconnect();
        }
    }

    private void connectBricklets() {
        try {
            ipcon.setAutoReconnect(true);
            ipcon.connect(host, port);
        } catch (IOException | AlreadyConnectedException ex) {
            log.log(Level.SEVERE, null, ex);
            return;
        }

        scheduler.scheduleAtFixedRate(new Runnable() {
            MyBricklet temp = new MyBrickletTemperature(new BrickletTemperature(UID_temperature, ipcon));
            MyBricklet humidity = new MyBrickletHumidity(new BrickletHumidity(UID_humidity, ipcon));
            MyBricklet ambient = new MyBrickletAmbientLight(new BrickletAmbientLight(UID_ambient, ipcon));
            MyBricklet barometer = new MyBrickletBarometer(new BrickletBarometer(UID_barometer, ipcon));

            @Override
            public void run() {
                if (ipcon.getConnectionState() != IPConnection.CONNECTION_STATE_CONNECTED) {
                    log.warning("No connection available. Don't query sensors.");
                    return;
                }
                update(temp, TEMPERATURE);
                update(humidity, HUMIDITY);
                update(ambient, AMBIENT);
                update(barometer, BAROMETER);
            }

            private void update(MyBricklet bricklet, Charts aChart) {
                try {
                    double t = bricklet.getValue();
                    log.fine("queried " + aChart.name() + ": " + t);
                    Platform.runLater(() -> {
                        aChart.add(t);
                    });
                } catch (TimeoutException | NotConnectedException e) {
                    log.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }, POLL_SENSORS, POLL_SENSORS, TimeUnit.SECONDS);
    }

}
