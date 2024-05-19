package br.com.catolicapb.facerecoptm.controller;

import br.com.catolicapb.facerecoptm.util.FaceRecognizer;
import br.com.catolicapb.facerecoptm.util.ImageCapture;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

public class MainController {
    @FXML
    private ImageView imageView;

    @FXML
    private Button recognizeButton;

    @FXML
    private Button trainButton;

    @FXML
    private TextArea logArea;

    private FaceRecognizer faceRecognizer;
    private ImageCapture imageCapture;

    @FXML
    private void initialize() {
        faceRecognizer = new FaceRecognizer();
        imageCapture = new ImageCapture();

        trainButton.setOnAction(event -> trainModel());
        recognizeButton.setOnAction(event -> recognizeFace());
    }

    private void trainModel() {
        // Simulação de treinamento (em um cenário real, você deve carregar imagens reais e etiquetas)
        List<Mat> trainingImages = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        // Adicionar imagens de treinamento e suas etiquetas
        Mat img1 = Imgcodecs.imread("src/TrainingData/face_1716147398148.png", Imgcodecs.IMREAD_GRAYSCALE);
        Mat img3 = Imgcodecs.imread("src/TrainingData/face_1716143733300.png", Imgcodecs.IMREAD_GRAYSCALE);
        Mat img4 = Imgcodecs.imread("src/TrainingData/face_1716143752588.png", Imgcodecs.IMREAD_GRAYSCALE);
        Mat img5 = Imgcodecs.imread("src/TrainingData/face_1716143765271.png", Imgcodecs.IMREAD_GRAYSCALE);
        Mat img6 = Imgcodecs.imread("src/TrainingData/face_1716143806371.png", Imgcodecs.IMREAD_GRAYSCALE);

        trainingImages.add(img1);
        trainingImages.add(img3);
        trainingImages.add(img4);
        trainingImages.add(img5);
        trainingImages.add(img6);
        labels.add("Jefferson");
        labels.add("Albert");
        labels.add("Trump");
        labels.add("Joana");
        labels.add("Obama");

        faceRecognizer.train(trainingImages, labels);
        logArea.appendText("Modelo treinado com sucesso.\n");
    }

    private void recognizeFace() {
        Mat frame = imageCapture.captureGrayscaleImage();
        Image imageToShow = mat2Image(frame);
        imageView.setImage(imageToShow);

        String label = faceRecognizer.recognize(frame);
        logArea.appendText("Face reconhecida: " + label + "\n");
    }

    private Image mat2Image(Mat frame) {
        // Converter Mat para Image
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }
}