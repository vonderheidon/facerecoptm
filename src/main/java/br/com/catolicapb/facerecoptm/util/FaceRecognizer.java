package br.com.catolicapb.facerecoptm.util;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.Tensors;

import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FaceRecognizer {
    private Graph graph;
    private Session session;
    private Map<String, float[]> knownEmbeddings;
    private double threshold = 0.9; // Ajuste conforme necessário

    public FaceRecognizer() {
        try {
            byte[] graphDef = Files.readAllBytes(Paths.get("C:\\Users\\jeffe\\IdeaProjects\\FaceRecOptm\\src\\main\\resources\\br\\com\\catolicapb\\facerecoptm\\20180408-102900.pb"));
            graph = new Graph();
            graph.importGraphDef(graphDef);
            session = new Session(graph);
            knownEmbeddings = new HashMap<>();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Mat preprocessImage(Mat image) {
        Mat processedImage = new Mat();
        // Verificar o número de canais
        if (image.channels() == 3) {
            Imgproc.cvtColor(image, processedImage, Imgproc.COLOR_BGR2RGB);
        } else {
            Imgproc.cvtColor(image, processedImage, Imgproc.COLOR_GRAY2RGB);
        }

        Imgproc.resize(processedImage, processedImage, new Size(160, 160));
        processedImage.convertTo(processedImage, CvType.CV_32F, 1.0 / 255.0);
        return processedImage;
    }

    public float[] getEmbedding(Mat image) {
        Mat preprocessedImage = preprocessImage(image);
        FloatBuffer floatBuffer = FloatBuffer.allocate(160 * 160 * 3);
        preprocessedImage.get(0, 0, floatBuffer.array());
        Tensor<Float> imageTensor = Tensor.create(new long[]{1, 160, 160, 3}, floatBuffer);

        Tensor<Boolean> phaseTrain = Tensors.create(false); // Falso para inferência

        List<Tensor<?>> outputs = session.runner()
                .feed("input", imageTensor)
                .feed("phase_train", phaseTrain)
                .fetch("embeddings")
                .run();

        float[][] embeddingArray = new float[1][512];
        outputs.get(0).copyTo(embeddingArray);

        return embeddingArray[0];
    }

    private Map<String, float[]> embeddingsCache = new HashMap<>();

    public void addKnownEmbedding(String label, Mat image) {
        float[] embedding = embeddingsCache.get(label);
        if (embedding == null) {
            embedding = getEmbedding(image);
            embeddingsCache.put(label, embedding);
        }
        knownEmbeddings.put(label, embedding);
    }

    public String recognize(Mat image) {
        float[] embedding = getEmbedding(image);
        String recognizedLabel = "Desconhecido";
        double minDistance = Double.MAX_VALUE;

        for (Map.Entry<String, float[]> entry : knownEmbeddings.entrySet()) {
            double distance = calculateDistance(embedding, entry.getValue());
            if (distance < minDistance && distance < threshold) {
                minDistance = distance;
                recognizedLabel = entry.getKey();
            }
        }

        return recognizedLabel;
    }

    private double calculateDistance(float[] emb1, float[] emb2) {
        double sum = 0.0;
        for (int i = 0; i < emb1.length; i++) {
            sum += Math.pow(emb1[i] - emb2[i], 2);
        }
        return Math.sqrt(sum);
    }

    public void close() {
        session.close();
        graph.close();
    }
}
