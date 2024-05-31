package br.com.catolicapb.facerecoptm.controller;

import br.com.catolicapb.facerecoptm.connection.ConnectionToRaspberry;
import br.com.catolicapb.facerecoptm.util.FaceRecognizer;
import br.com.catolicapb.facerecoptm.util.ImageCapture;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
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
    @FXML
    private Button captureAndRecognizeButton;
    private FaceRecognizer faceRecognizer;
    private ImageCapture imageCapture;
    private long lastRecognitionChangeTime;
    private Color targetColor = Color.GRAY;
    private long lastFrameTime;
    private Color lastSentColor = Color.GRAY;
    private boolean isRecognitionActive = false;

    @FXML
    private void initialize() {
        faceRecognizer = new FaceRecognizer();
        imageCapture = new ImageCapture();
        lblLed.setFill(Color.GRAY);
        lblLed.setVisible(false);
        ConnectionToRaspberry.sendLedCommand(0, 0, 0);
        startCamera();
        captureAndRecognizeButton.setOnAction(event -> toggleRecognition());
        registerButton.setOnAction(event -> registerNewUser());
    }

    private void toggleRecognition() {
        if (isRecognitionActive) {
            isRecognitionActive = false;
            captureAndRecognizeButton.setText("Iniciar Reconhecimento");
            lblLed.setFill(Color.GRAY);
            setTargetColor(Color.GRAY);
            updateLedColor();
            lblLed.setVisible(false);
            ConnectionToRaspberry.sendLedCommand(0, 0, 0);
        } else {
            faceRecognizer.loadModelAsync().thenRun(() -> {
                Platform.runLater(() -> {
                    if (checkForTrainingImages()) {
                        // Carregue os embeddings conhecidos apenas se não estiverem carregados
                        if (faceRecognizer.hasKnownEmbeddings()) {
                            logArea.appendText("Embeddings conhecidos já carregados.\n");
                        } else {
                            trainModel();
                        }
                    } else {
                        logArea.appendText("Nenhum modelo disponível.\n");
                    }
                    isRecognitionActive = true;
                    captureAndRecognizeButton.setText("Parar Reconhecimento");
                    lblLed.setVisible(true);
                });
            });
        }
    }


    private boolean checkForTrainingImages() {
        return faceRecognizer.hasKnownEmbeddings();
    }

    private void trainModel() {
        faceRecognizer.loadModelAsync().thenRun(() -> {
            logArea.appendText("Modelo treinado com sucesso.\n");
        });
    }

    private void startCamera() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastFrameTime >= 50) {
                    captureAndProcessFrame();
                    lastFrameTime = currentTime;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    private void captureAndProcessFrame() {
        Mat frame = imageCapture.captureImage();
        if (isRecognitionActive) {
            captureAndRecognize(frame);
        } else {
            displayFrame(frame);
        }
    }

    private void captureAndRecognize(Mat frame) {
        Mat grayFrame = imageCapture.captureGrayscaleImage();
        Rect[] facesArray = imageCapture.detectFaces(grayFrame);
        boolean recognized = false;
        for (Rect face : facesArray) {
            Mat faceMat = new Mat(grayFrame, face);
            String label = faceRecognizer.recognize(faceMat);
            Imgproc.putText(frame, label, face.tl(), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 255, 0), 2);
            Imgproc.rectangle(frame, face.tl(), face.br(), new Scalar(0, 255, 0), 2);
            if (!label.equals("Desconhecido")) {
                recognized = true;
                setTargetColor(Color.GREEN);
            }
        }
        if (!recognized) {
            if (facesArray.length > 0) {
                setTargetColor(Color.RED);
            } else {
                setTargetColor(Color.GRAY);
            }
        }
        updateLedColor();
        displayFrame(frame);
    }

    private void displayFrame(Mat frame) {
        Image imageToShow = mat2Image(frame);
        Platform.runLater(() -> imageView.setImage(imageToShow));
    }

    private void setTargetColor(Color color) {
        if (!targetColor.equals(color)) {
            targetColor = color;
            lastRecognitionChangeTime = System.currentTimeMillis();
        }
    }

    private void updateLedColor() {
        if (System.currentTimeMillis() - lastRecognitionChangeTime > 400 || !isRecognitionActive) {
            Platform.runLater(() -> {
                if (isRecognitionActive) {
                    lblLed.setFill(targetColor);
                } else {
                    lblLed.setVisible(false);
                    ConnectionToRaspberry.sendLedCommand(0, 0, 0);
                }
                if (!targetColor.equals(lastSentColor) || !isRecognitionActive) {
                    sendLedCommand(targetColor);
                    lastSentColor = targetColor;
                }
            });
        }
    }

    private void sendLedCommand(Color color) {
        if (color.equals(Color.GRAY)) {
            ConnectionToRaspberry.sendLedCommand(0, 1, 0);
        } else if (color.equals(Color.GREEN)) {
            ConnectionToRaspberry.sendLedCommand(1, 0, 0);
        } else if (color.equals(Color.RED)) {
            ConnectionToRaspberry.sendLedCommand(0, 0, 1);
        }
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
        result.ifPresent(name -> captureImagesForTraining(name));
    }

    private void captureImagesForTraining(String name) {
        int numImages = 7;
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
}
