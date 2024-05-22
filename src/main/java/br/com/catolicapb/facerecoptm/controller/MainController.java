package br.com.catolicapb.facerecoptm.controller;

import br.com.catolicapb.facerecoptm.util.FaceRecognizer;
import br.com.catolicapb.facerecoptm.util.ImageCapture;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainController {
    @FXML
    private ImageView imageView;
    @FXML
    private Circle lblLed;

    @FXML
    private TextArea logArea;

    @FXML
    private Button registerButton;

    private FaceRecognizer faceRecognizer;
    private ImageCapture imageCapture;
    private ExecutorService executor;

    @FXML
    private void initialize() {
        faceRecognizer = new FaceRecognizer();
        imageCapture = new ImageCapture();

        if (checkForTrainingImages()) {
            trainModel();
        } else {
            logArea.appendText("Nenhum modelo disponível.\n");
        }

        startCamera();

        registerButton.setOnAction(event -> registerNewUser());
    }

    private boolean checkForTrainingImages() {
        return faceRecognizer.hasKnownEmbeddings();
    }

    private void trainModel() {
        faceRecognizer.loadKnownEmbeddings();
        logArea.appendText("Modelo treinado com sucesso.\n");
    }

    private void startCamera() {
        executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                captureAndRecognize();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    private void captureAndRecognize() {
        Mat frame = imageCapture.captureImage();
        Mat grayFrame = imageCapture.captureGrayscaleImage();
        Rect[] facesArray = imageCapture.detectFaces(grayFrame);

        for (Rect face : facesArray) {
            Mat faceMat = new Mat(grayFrame, face);
            String label = faceRecognizer.recognize(faceMat);
            Imgproc.putText(frame, label, face.tl(), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 255, 0), 2);
            Imgproc.rectangle(frame, face.tl(), face.br(), new Scalar(0, 255, 0), 2);
        }

        Image imageToShow = mat2Image(frame);
        Platform.runLater(() -> imageView.setImage(imageToShow));
    }

    private Image mat2Image(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    private void registerNewUser() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Registrar Novo Usuário");
        dialog.setHeaderText("Insira o nome da pessoa:");
        dialog.setContentText("Nome:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            captureImagesForTraining(name);
        });
    }

    private void captureImagesForTraining(String name) {
        int numImages = 15;
        for (int i = 0; i < numImages; i++) {
            Mat frame = imageCapture.captureGrayscaleImage();
            Rect[] facesArray = imageCapture.detectFaces(frame);

            if (facesArray.length == 1) {
                Mat face = new Mat(frame, facesArray[0]);
                faceRecognizer.addKnownEmbedding(name, face);
                logArea.appendText("Imagem capturada para: " + name + "\n");
            } else {
                i--;
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        trainModel();
    }

    @FXML
    public void stop() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
        imageCapture.release();
        faceRecognizer.close();
    }
}
