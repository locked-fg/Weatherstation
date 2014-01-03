package de.locked.weatherstation;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

//    @Override
//    public void start(Stage stage) throws Exception {
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FXMLDocument.fxml"));
//        Parent root = (Parent) loader.load();
//
//        FXMLDocumentController controller = loader.getController();
//        
//        
//        Scene scene = new Scene(root);
//        scene.getStylesheets().add("/styles/base.css");
//        stage.setTitle("Weatherstation");
//        stage.setScene(scene);
//        stage.show();
//    }
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/FXMLDocument.fxml"));
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/base.css");

        stage.setTitle("JavaFX and Maven");
        stage.setScene(scene);
        stage.show();

        ValuesModel model = Charts.TEMPERATURE.getModel();
        // add values to the model(s)
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

}
