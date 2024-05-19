package br.com.catolicapb.facerecoptm.util;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

public class ImageCapture {
    private VideoCapture capture;

    public ImageCapture() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        capture = new VideoCapture(0);
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

    public void release() {
        capture.release();
    }
}