package de.locked.webexporter;

import com.google.gson.Gson;
import com.tinkerforge.AlreadyConnectedException;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;
import de.locked.weatherstation.Measure;
import de.locked.weatherstation.model.BaseModel;
import de.locked.weatherstation.model.ChartModel;
import de.locked.weatherstation.model.ModelReader;
import static de.locked.weatherstation.tinkerforge.BrickletConfig.UID_ambient;
import static de.locked.weatherstation.tinkerforge.BrickletConfig.UID_barometer;
import static de.locked.weatherstation.tinkerforge.BrickletConfig.UID_humidity;
import static de.locked.weatherstation.tinkerforge.BrickletConfig.UID_temperature;
import de.locked.weatherstation.tinkerforge.MyBricklet;
import de.locked.weatherstation.tinkerforge.MyBrickletAmbientLight;
import de.locked.weatherstation.tinkerforge.MyBrickletBarometer;
import de.locked.weatherstation.tinkerforge.MyBrickletHumidity;
import de.locked.weatherstation.tinkerforge.MyBrickletTemperature;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.stream.Collectors.toMap;
import static java.util.function.Function.identity;
import static de.locked.weatherstation.model.ChartModel.*;
import de.locked.weatherstation.tinkerforge.BrickletConfig;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Exporter {

    private static final Logger log = Logger.getLogger(Exporter.class.getName());
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<ChartModel, BaseModel> models;
    private final IPConnection ipcon = new IPConnection();
    //
    private final Path tablePath = Paths.get("table.json");
    private final Path seriesPath = Paths.get("series.json");
    private final int POLL_SENSORS_INTERVAL = 15; // seconds
    private final int WRITE_INTERVAL = 15;

    
    public Exporter() {
        log.info("Starting exporter");
        Map<ChartModel, BaseModel> collect = Arrays.stream(ChartModel.values())
                .collect(toMap(identity(), v -> new BaseModel()));
        models = Collections.unmodifiableMap(collect);
    }

    private Exporter readFiles() {
        for (ChartModel m : ChartModel.values()) {
            File csvFile = new File(m.name().toLowerCase() + ".csv");
            log.info("reading: " + csvFile.getAbsolutePath());
            new ModelReader().readFile(csvFile, models.get(m));
        }
        return this;
    }

    private boolean isConnected() {
        return ipcon.getConnectionState() == IPConnection.CONNECTION_STATE_CONNECTED;
    }

    private Exporter connectBricklets() throws IOException, UnknownHostException, AlreadyConnectedException {
        log.info("schedule bricklet connection");
        ipcon.addConnectedListener(reason -> {
            log.info("Connection to masterbrick set up! start polling");
            scheduler.scheduleWithFixedDelay(new Runnable() {
                final MyBricklet temp = new MyBrickletTemperature(UID_temperature, ipcon);
                final MyBricklet humidity = new MyBrickletHumidity(UID_humidity, ipcon);
                final MyBricklet ambient = new MyBrickletAmbientLight(UID_ambient, ipcon);
                final MyBricklet barometer = new MyBrickletBarometer(UID_barometer, ipcon);

                @Override
                public void run() {
                    try {
                        if (isConnected()) {
                            log.info("fetching data from bricklets");
                            models.get(TEMPERATURE).add(new Measure(temp.getValue()));
                            models.get(HUMIDITY).add(new Measure(humidity.getValue()));
                            models.get(AMBIENTLIGHT).add(new Measure(ambient.getValue()));
                            models.get(BAROMETER).add(new Measure(barometer.getValue()));
                        } else {
                            log.info("not connected to bricklets");
                        }
                    } catch (TimeoutException | NotConnectedException ex) {
                        log.log(Level.SEVERE, null, ex);
                    }
                }

            }, 0, POLL_SENSORS_INTERVAL, TimeUnit.SECONDS);
        });
        ipcon.connect(BrickletConfig.host, BrickletConfig.port);
        return this;
    }
    
    private void setFromModel(TableValue value, BaseModel model) {
        value.min = model.getMin();
        value.max = model.getMax();
        value.current = model.getCurrentValue();
    }

    private Table createTable() {
        Table t = new Table();
        setFromModel(t.airpressure, models.get(BAROMETER));
        setFromModel(t.ambientlight, models.get(AMBIENTLIGHT));
        setFromModel(t.humidity, models.get(HUMIDITY));
        setFromModel(t.temperature, models.get(TEMPERATURE));
        return t;
    }

    private Series createSeries() {
        Series s = new Series();
        s.airpressure = models.get(BAROMETER).getValues();
        s.ambientlight = models.get(AMBIENTLIGHT).getValues();
        s.humidity = models.get(HUMIDITY).getValues();
        s.temperature = models.get(TEMPERATURE).getValues();
        return s;
    }

    private void startWriter() {
        log.info("schedule writer");
        scheduler.scheduleAtFixedRate(() -> {
            if (!isConnected()) {
                return;
            }
            Gson g = new Gson();
            try (BufferedWriter writer = Files.newBufferedWriter(tablePath)) {
                log.info("writing table");
                writer.write(g.toJson(createTable()));
            } catch (IOException ex) {
                log.log(Level.SEVERE, null, ex);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(seriesPath)) {
                log.info("writing series");
                writer.write(g.toJson(createSeries()));
            } catch (IOException ex) {
                log.log(Level.SEVERE, null, ex);
            }

        }, 0, WRITE_INTERVAL, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws Exception {
        new Exporter()
                .readFiles()
                .connectBricklets()
                .startWriter();
    }

}
