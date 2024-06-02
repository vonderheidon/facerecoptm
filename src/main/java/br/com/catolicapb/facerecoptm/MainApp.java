package br.com.catolicapb.facerecoptm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.opencv.core.Core;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        FXMLLoader loader1 = new FXMLLoader(getClass().getResource("/br/com/catolicapb/facerecoptm/FXML/main.fxml"));
        FXMLLoader loader2 = new FXMLLoader(getClass().getResource("/br/com/catolicapb/facerecoptm/FXML/sgp-view.fxml"));
        Scene scene = new Scene(loader2.load());
        primaryStage.setTitle("FaceRecOptm");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
