package br.com.catolicapb.facerecoptm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.opencv.core.Core;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Carregue a biblioteca nativa do OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/br/com/catolicapb/facerecoptm/main.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("FaceRecOptm");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
