package br.com.catolicapb.facerecoptm.util;

import br.com.catolicapb.facerecoptm.dao.EmbeddingDao;
import br.com.catolicapb.facerecoptm.dao.PessoaDao;
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
import java.util.Map;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FaceRecognizer {
    private Graph graph;
    private static Session session;
    private static Map<Integer, float[]> knownEmbeddings;
    private double threshold = 0.8;

    public FaceRecognizer() {
        knownEmbeddings = new HashMap<>();
    }

    public boolean hasKnownEmbeddings() {
        return !knownEmbeddings.isEmpty();
    }

    public CompletableFuture<Void> loadModelAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                byte[] graphDef = Files.readAllBytes(Paths.get("src/main/resources/br/com/catolicapb/facerecoptm/TrainingModels/20180408-102900.pb"));
                graph = new Graph();
                graph.importGraphDef(graphDef);
                session = new Session(graph);
                knownEmbeddings = EmbeddingDao.loadKnownEmbeddings();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static Mat preprocessImage(Mat image) {
        Mat processedImage = new Mat();
        if (image.channels() == 3) {
            Imgproc.cvtColor(image, processedImage, Imgproc.COLOR_BGR2RGB);
        } else {
            Imgproc.cvtColor(image, processedImage, Imgproc.COLOR_GRAY2RGB);
        }
        Imgproc.resize(processedImage, processedImage, new Size(160, 160));
        processedImage.convertTo(processedImage, CvType.CV_32F, 1.0 / 255.0);
        return processedImage;
    }

    public static float[] getEmbedding(Mat image) {
        Mat preprocessedImage = preprocessImage(image);
        FloatBuffer floatBuffer = FloatBuffer.allocate(160 * 160 * 3);
        preprocessedImage.get(0, 0, floatBuffer.array());
        Tensor<Float> imageTensor = Tensor.create(new long[]{1, 160, 160, 3}, floatBuffer);
        Tensor<Boolean> phaseTrain = Tensors.create(false);
        List<Tensor<?>> outputs = session.runner()
                .feed("input", imageTensor)
                .feed("phase_train", phaseTrain)
                .fetch("embeddings")
                .run();
        float[][] embeddingArray = new float[1][512];
        outputs.get(0).copyTo(embeddingArray);
        return embeddingArray[0];
    }

    public static void addKnownEmbedding(int pessoaId, Mat image) {
        float[] embedding = getEmbedding(image);
        try {
            EmbeddingDao.saveEmbedding(pessoaId, embedding);
            knownEmbeddings.put(pessoaId, embedding);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int recognizeAndGetId(Mat image) {
        float[] embedding = getEmbedding(image);
        int recognizedId = -1;
        double minDistance = Double.MAX_VALUE;
        for (Map.Entry<Integer, float[]> entry : knownEmbeddings.entrySet()) {
            double distance = calculateDistance(embedding, entry.getValue());
            if (distance < minDistance && distance < threshold) {
                minDistance = distance;
                recognizedId = entry.getKey();
            }
        }
        return recognizedId;
    }

    private double calculateDistance(float[] emb1, float[] emb2) {
        double sum = 0.0;
        for (int i = 0; i < emb1.length; i++) {
            sum += Math.pow(emb1[i] - emb2[i], 2);
        }
        return Math.sqrt(sum);
    }
}
