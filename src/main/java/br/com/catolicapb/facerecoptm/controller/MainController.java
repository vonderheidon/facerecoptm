package br.com.catolicapb.facerecoptm.controller;

import br.com.catolicapb.facerecoptm.util.FaceRecognizer;
import br.com.catolicapb.facerecoptm.util.ImageCapture;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Optional;

public class MainController {
    @FXML
    private ImageView imageView;

    @FXML
    private TextArea logArea;

    @FXML
    private Button registerButton;

    private FaceRecognizer faceRecognizer;
    private ImageCapture imageCapture;
    private Timeline timeline;

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
        File trainingDir = new File("src/TrainingData");
        return trainingDir.exists() && trainingDir.isDirectory() && trainingDir.list().length > 0;
    }

    private void trainModel() {
        File trainingDir = new File("src/TrainingData");
        File[] userDirectories = trainingDir.listFiles(File::isDirectory);

        if (userDirectories != null) {
            for (File userDir : userDirectories) {
                String label = userDir.getName();
                File[] imageFiles = userDir.listFiles((dir, name) -> name.endsWith(".png") || name.endsWith(".jpg"));

                if (imageFiles != null) {
                    for (File file : imageFiles) {
                        Mat image = Imgcodecs.imread(file.getAbsolutePath());
                        faceRecognizer.addKnownEmbedding(label, image);
                    }
                }
            }
            logArea.appendText("Modelo treinado com sucesso.\n");
        }
    }

    private void startCamera() {
        timeline = new Timeline(new KeyFrame(Duration.millis(100), event -> captureAndRecognize()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void captureAndRecognize() {
        Mat frame = imageCapture.captureImage();
        new Thread(() -> {
            Mat grayFrame = imageCapture.captureGrayscaleImage();
            Rect[] facesArray = imageCapture.detectFaces(grayFrame);

            for (Rect face : facesArray) {
                if (face.width < 30 || face.height < 30) { // Ignorar detecções muito pequenas
                    continue;
                }

                Mat faceMat = new Mat(grayFrame, face);
                String label = faceRecognizer.recognize(faceMat);
                Imgproc.putText(frame, label, face.tl(), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 255, 0), 2);
                Imgproc.rectangle(frame, face.tl(), face.br(), new Scalar(0, 255, 0), 2);
                faceMat.release(); // Liberação de recursos
            }

            Image imageToShow = mat2Image(frame);
            Platform.runLater(() -> imageView.setImage(imageToShow));

            frame.release(); // Liberação de recursos
            grayFrame.release(); // Liberação de recursos
        }).start();
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
        File userDir = new File("src/TrainingData/" + name);
        if (!userDir.exists()) {
            userDir.mkdirs();
        }

        int numImages = 15;
        for (int i = 0; i < numImages; i++) {
            Mat frame = imageCapture.captureGrayscaleImage();
            Rect[] facesArray = imageCapture.detectFaces(frame);

            if (facesArray.length == 1) {
                Mat face = new Mat(frame, facesArray[0]);
                String filePath = userDir.getAbsolutePath() + "/" + name + "_" + System.currentTimeMillis() + ".png";
                Imgcodecs.imwrite(filePath, face);
                logArea.appendText("Imagem capturada: " + filePath + "\n");
            } else {
                i--; // Tentar novamente se não houver exatamente um rosto detectado
            }

            try {
                Thread.sleep(200); // Pequeno atraso entre capturas
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Treinar o modelo com as novas imagens
        trainModel();
    }
}
