package br.com.catolicapb.facerecoptm.util;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

public class ImageCapture {
    private VideoCapture capture;
    private CascadeClassifier faceDetector;

    public ImageCapture() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        capture = new VideoCapture(0);
        faceDetector = new CascadeClassifier("C:\\Users\\jeffe\\IdeaProjects\\FaceRecOptm\\src\\main\\resources\\br\\com\\catolicapb\\facerecoptm\\haarcascade_frontalface_default.xml");
    }

    public Mat captureImage() {
        Mat frame = new Mat();
        if (capture.isOpened()) {
            capture.read(frame);
        }
        return frame;
    }

    public Mat captureGrayscaleImage() {
        Mat frame = captureImage();
        Mat grayFrame = new Mat(frame.size(), CvType.CV_8UC1);
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        return grayFrame;
    }

    public Rect[] detectFaces(Mat frame) {
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(frame, faceDetections);
        return faceDetections.toArray();
    }

    public void release() {
        capture.release();
    }
}
