package de.locked.weatherstation.model;

import de.locked.weatherstation.Measure;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTime;

public class ModelReader {

    private static final Logger log = Logger.getLogger(ModelReader.class.getName());

    public void readFile(File csvFile, MeasureSink sink) {
        // read line by line to avoid having one huge file in memory
        log.info("init data from " + csvFile.getName() + " // " + csvFile.getAbsolutePath());
        try (BufferedReader in = new BufferedReader(new FileReader(csvFile))) {
            DateTime ignoreBefore = new DateTime().minusDays(3);
            int i = 0;
            while (in.ready()) {
                String line = in.readLine();
                String[] parts = line.split("\t");
                if (parts.length != 2) {
                    log.warning("invalid line (Ignoring): " + line);
                    continue;
                }

                try {
                    DateTime date = new DateTime(Long.parseLong(parts[0].trim()) * 1000L);
                    if (date.isAfter(ignoreBefore)) {
                        double value = Double.parseDouble(parts[1].trim());
                        sink.add(new Measure(date, value));
                    }
                } catch (NumberFormatException e) {
                    log.info("invalid line (Ignoring): " + line);
                }
                
                if (i++ % 100 == 0) {
                    System.gc();
                }
            }
            log.info("file done");
        } catch (IOException e) {
            log.log(Level.WARNING, "IO Exception: ", e);
        }
    }

}
