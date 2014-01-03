package de.locked.weatherstation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.joda.time.DateTime;

public class MainApp extends Application {

    private static final Logger log = Logger.getLogger(MainApp.class.getName());

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/FXMLDocument.fxml"));
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/base.css");

        stage.setTitle("JavaFX and Maven");
        stage.setScene(scene);
        stage.show();

        // init Models
        initModelsFromCSV();
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
    private void initModelsFromCSV() throws IOException {
        Map<String, String> params = getParameters().getNamed();

        Charts chartModel = Charts.TEMPERATURE;
        String paramName = chartModel.name().toLowerCase() + "-csv";
        String csv = params.get(paramName);
        if (csv == null) {
            throw new IllegalArgumentException(paramName + " not given in command line");
        }

        File f = new File(csv);
        if (!f.exists() || !f.canRead()) {
            throw new IOException(f.getAbsolutePath() + " cannot be found or read.");
        }
        try (BufferedReader in = new BufferedReader(new FileReader(f))) {
            while (in.ready()) {
                String[] parts = in.readLine().split("\t");
                if (parts.length != 2) {
                    continue;
                }

                DateTime date = new DateTime(Long.parseLong(parts[0].trim()) * 1000L);
                double value = Double.parseDouble(parts[1].trim());

                chartModel.add(date, value);
            }
        }
    }

}
