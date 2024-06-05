package br.com.catolicapb.facerecoptm.util;

import javafx.scene.image.Image;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.io.ByteArrayInputStream;

public class ImageCapture {
    private static VideoCapture capture;
    private static CascadeClassifier faceDetector;

    public ImageCapture() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        capture = new VideoCapture(0);
        capture.set(Videoio.CAP_PROP_FRAME_WIDTH, 640);
        capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480);
        faceDetector = new CascadeClassifier("src/main/resources/br/com/catolicapb/facerecoptm/TrainingModels/haarcascade_frontalface_default.xml");
    }

    public static Mat captureImage() {
        Mat frame = new Mat();
        if (capture.isOpened()) {
            capture.read(frame);
            Core.flip(frame, frame, 1);
        }
        return frame;
    }

    public static Mat captureGrayscaleImage() {
        Mat frame = captureImage();
        Mat grayFrame = new Mat(frame.size(), CvType.CV_8UC1);
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(grayFrame, grayFrame, new Size(5, 5), 0);
        return grayFrame;
    }

    public static Rect[] detectFaces(Mat frame) {
        MatOfRect faceDetections = new MatOfRect();
        //valores padrões
        //scaleFactor = 1.1
        //minNeighbors = 3 a 5
        //minSize = 30x30
        //maxSize = 2x2
        faceDetector.detectMultiScale(frame, faceDetections, 1.1, 7, 0, new Size(50, 50), new Size());
        return faceDetections.toArray();
    }

    public static Image mat2Image(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    public static void captureImagesForTraining(int pessoaId) {
        int numImages = 7;
        for (int i = 0; i < numImages; i++) {
            Mat frame = captureGrayscaleImage();
            Rect[] facesArray = detectFaces(frame);
            if (facesArray.length == 1) {
                Mat face = new Mat(frame, facesArray[0]);
                FaceRecognizer.addKnownEmbedding(pessoaId, face);
            } else {
                i--;
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void release() {
        capture.release();
    }

    public boolean isOpened() {
        return capture.isOpened();
    }

}
