package br.com.catolicapb.facerecoptm.util;

import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.face.LBPHFaceRecognizer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FaceRecognizer {
    private LBPHFaceRecognizer recognizer;
    private boolean isTrained;
    private Map<Integer, String> labelMap; // Mapa de etiquetas para nomes

    public FaceRecognizer() {
        recognizer = LBPHFaceRecognizer.create();
        isTrained = false;
        labelMap = new HashMap<>();
    }

    public void train(List<Mat> images, List<String> labels) {
        int[] labelIds = new int[labels.size()];
        Map<String, Integer> nameToIdMap = new HashMap<>();
        int currentId = 0;

        for (int i = 0; i < labels.size(); i++) {
            String name = labels.get(i);
            if (!nameToIdMap.containsKey(name)) {
                nameToIdMap.put(name, currentId);
                labelMap.put(currentId, name);
                currentId++;
            }
            labelIds[i] = nameToIdMap.get(name);
        }

        recognizer.train(images, new MatOfInt(labelIds));
        isTrained = true;
    }

    public String recognize(Mat image) {
        if (!isTrained) {
            throw new IllegalStateException("O modelo LBPH não foi treinado. Chame o método train antes de reconhecer faces.");
        }
        int[] label = new int[1];
        double[] confidence = new double[1];
        recognizer.predict(image, label, confidence);
        return labelMap.get(label[0]);
    }
}