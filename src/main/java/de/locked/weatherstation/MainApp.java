package de.locked.weatherstation;

import de.locked.weatherstation.model.ChartModel;
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
import static de.locked.weatherstation.model.ChartModel.*;
import de.locked.weatherstation.model.MeasureSink;
import de.locked.weatherstation.model.ModelReader;
import de.locked.weatherstation.tinkerforge.MyBricklet;
import de.locked.weatherstation.tinkerforge.MyBrickletAmbientLight;
import de.locked.weatherstation.tinkerforge.MyBrickletBarometer;
import de.locked.weatherstation.tinkerforge.MyBrickletHumidity;
import de.locked.weatherstation.tinkerforge.MyBrickletTemperature;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.joda.time.DateTime;
import static de.locked.weatherstation.tinkerforge.BrickletConfig.*;


public class MainApp extends Application {

    private static final Logger log = Logger.getLogger(MainApp.class.getName());
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final IPConnection ipcon = new IPConnection();
    //
    private final int REFRESH_DATE = 1; // m
    private final int POLL_SENSORS = 5; // s
    private FXMLDocumentController controller;
    private CecListener cec;

    @Override
    public void start(Stage stage) throws Exception {
        initLogging();

        log.info("Welcome - starting " + getClass().getName());
        initModelsFromCSV();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/metro.fxml"));
        loader.load();
        Scene scene = new Scene(loader.getRoot());

        controller = loader.getController();
        resize();
        stage.setTitle("My WeatherStation");
        stage.setScene(scene);
        stage.setFullScreen(true);
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

        // Connect to sensors
        scheduler.schedule(() -> {
            if (connectMasterbrick()) {
                connectBricklets();
            }
        }, 30, TimeUnit.SECONDS);

        // refresh date 
        scheduler.scheduleAtFixedRate(() -> {
            controller.setDate(new DateTime());
        }, REFRESH_DATE, REFRESH_DATE, TimeUnit.MINUTES);

        log.info("start finished");
    }

    private void initLogging() {
        try (InputStream is = getClass().getResourceAsStream("/logging.properties")) {
            LogManager.getLogManager().readConfiguration(is);
        } catch (IOException | SecurityException ex) {
            log.log(Level.SEVERE, "Logging properties could not be found.", ex);
        }
    }

    private void connectCEC() {
        cec = new CecListener();
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
            int w = 1920;
            int h = 1080;
            if (getParameters().getNamed().containsKey("w")) {
                w = Integer.parseInt(getParameters().getNamed().get("w"));
            }
            if (getParameters().getNamed().containsKey("h")) {
                h = Integer.parseInt(getParameters().getNamed().get("h"));
            }
            log.info("setting width/height to " + w + "/" + h);
            controller.rootPane.setPrefSize(w, h);
            controller.contentPane.setPrefSize(w, h);
        } catch (NumberFormatException e) {
            log.severe(e.getMessage());
        }
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * This should be executed in the JavaFX application thread as the
     * model.add(MEasure) calls trigger updates of the underlying model. If this
     * should be done off the FX-Appthread, a bulk-insert method in the model
     * would be required.
     *
     * @throws IOException
     */
    @SuppressWarnings("LoggerStringConcat")
    private void initModelsFromCSV() {
        log.info("init data from CSVs");
        Map<String, String> params = getParameters().getNamed();
        for (ChartModel chartModel : ChartModel.values()) {
            String paramName = chartModel.name().toLowerCase() + "-csv";
            String csvPath = params.get(paramName);
            if (csvPath == null) {
                log.warning(paramName + " not given in command line. Set default to localfile.");
                csvPath = chartModel.name().toLowerCase() + ".csv";
            }

            File csvFile = new File(csvPath);
            if (!csvFile.exists() || !csvFile.canRead()) {
                log.warning(csvFile.getAbsolutePath() + " cannot be found or read. Ignoring");
                continue;
            }

            new ModelReader().readFile(csvFile, chartModel);
        }
    }

    @Override
    public void stop() throws Exception {
        log.info("shutting down application");
        scheduler.shutdownNow();
        if (cec != null) {
            cec.interrupt();
        }
        if (ipcon.getConnectionState() == IPConnection.CONNECTION_STATE_CONNECTED) {
            ipcon.disconnect();
        }
    }

    private void connectBricklets() {
        log.info("Connecting bricklets.");
        scheduler.scheduleAtFixedRate(new Runnable() {
            MyBricklet temp = new MyBrickletTemperature(new BrickletTemperature(UID_temperature, ipcon));
            MyBricklet humidity = new MyBrickletHumidity(new BrickletHumidity(UID_humidity, ipcon));
            MyBricklet ambient = new MyBrickletAmbientLight(new BrickletAmbientLight(UID_ambient, ipcon));
            MyBricklet barometer = new MyBrickletBarometer(new BrickletBarometer(UID_barometer, ipcon));

            @Override
            public void run() {
                log.info("Poll sensors");
                if (ipcon.getConnectionState() != IPConnection.CONNECTION_STATE_CONNECTED) {
                    log.warning("No connection available. Don't query sensors.");
                    return;
                }
                // I wanted to push all runLater calls to the controller (or model) yet this would result in 4x the amount of 
                // runLater calls every POLL_SENSORS seconds. I should evaluate if this leads to a noticable effect
                // on the Raspberry.
                // See ValuesModel#add(Measure)
                update(temp, TEMPERATURE);
                update(humidity, HUMIDITY);
                update(ambient, AMBIENTLIGHT);
                update(barometer, BAROMETER);
            }

            private void update(MyBricklet bricklet, ChartModel aChart) {
                try {
                    double t = bricklet.getValue();
                    log.info("queried " + aChart.name() + ": " + t);
                    aChart.add(t);
                } catch (TimeoutException | NotConnectedException e) {
                    log.log(Level.SEVERE, "Getting value from Sensor " + aChart.name() + " failed", e);
                }
            }
        }, POLL_SENSORS, POLL_SENSORS, TimeUnit.SECONDS);
    }

    private boolean connectMasterbrick() {
        log.info("Connecting to Masterbrick");
        try {
            ipcon.setAutoReconnect(true);
            ipcon.connect(host, port);
            return true;
        } catch (IOException | AlreadyConnectedException ex) {
            log.log(Level.SEVERE, "Connection to Bricklets failed!", ex);
            return false;
        }
    }
}
