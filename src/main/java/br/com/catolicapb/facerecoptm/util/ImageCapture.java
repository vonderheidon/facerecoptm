package br.com.catolicapb.facerecoptm.util;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class ImageCapture {
    private VideoCapture capture;
    private CascadeClassifier faceDetector;

    public ImageCapture() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        capture = new VideoCapture(0);
        capture.set(Videoio.CAP_PROP_FRAME_WIDTH, 640);
        capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480);
        faceDetector = new CascadeClassifier("src/main/resources/br/com/catolicapb/facerecoptm/TrainingModels/haarcascade_frontalface_default.xml");
    }

    public Mat captureImage() {
        Mat frame = new Mat();
        if (capture.isOpened()) {
            capture.read(frame);
            Core.flip(frame, frame, 1);
        }
        return frame;
    }

    public Mat captureGrayscaleImage() {
        Mat frame = captureImage();
        Mat grayFrame = new Mat(frame.size(), CvType.CV_8UC1);
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(grayFrame, grayFrame, new Size(5, 5), 0);
        return grayFrame;
    }

    public Rect[] detectFaces(Mat frame) {
        MatOfRect faceDetections = new MatOfRect();
        //valores padrões
        //scaleFactor = 1.1
        //minNeighbors = 3 a 5
        //minSize = 30x30
        //maxSize = 2x2
        faceDetector.detectMultiScale(frame, faceDetections, 1.1, 7, 0, new Size(50, 50), new Size());
        return faceDetections.toArray();
    }

    public void release() {
        capture.release();
    }
}
