package br.com.catolicapb.facerecoptm.controller;

import br.com.catolicapb.facerecoptm.connection.ConnectionToRaspberry;
import br.com.catolicapb.facerecoptm.dao.PessoaDao;
import br.com.catolicapb.facerecoptm.model.Pessoa;
import br.com.catolicapb.facerecoptm.util.FaceRecognizer;
import br.com.catolicapb.facerecoptm.util.ImageCapture;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class SGPViewController {
    @FXML
    private Circle lblLed;
    @FXML
    private Button registerButton;
    @FXML
    private Button captureAndRecognizeButton;
    @FXML
    private Button startStopCameraButton;
    @FXML
    private ImageView camImgView;
    @FXML
    private CheckBox isActiveCheckBox;
    @FXML
    private TableView<Pessoa> pessoaTv;
    @FXML
    private TableColumn<Pessoa, Integer> IDColumn;
    @FXML
    private TableColumn<Pessoa, String> nameColumn;
    @FXML
    private TableColumn<Pessoa, String> CPFColumn;
    @FXML
    private TextField CPFTf;
    @FXML
    private TextField nameTf;
    @FXML
    private Label IDLbl;
    @FXML
    private TextField ClassTf;
    @FXML
    private Label RegisterDateLbl;
    @FXML
    private TextField searchTf;
    @FXML
    private BarChart<?, ?> homeChart;
    @FXML
    private Label homeTotalLbl;
    @FXML
    private Label NameLbl;
    @FXML
    private Label ClassLbl;
    @FXML
    private Label CPFLbl;
    @FXML
    private AnchorPane mainAnchor;
    @FXML
    private AnchorPane homeAnchor;
    @FXML
    private AnchorPane managementAnchor;
    @FXML
    private AnchorPane reportAnchor;
    @FXML
    private AnchorPane identAnchor;
    @FXML
    private AnchorPane registerAnchor;
    @FXML
    private Button reportBtn;
    @FXML
    private Button managementBtn;
    @FXML
    private Button homeBtn;
    @FXML
    private Button updateBtn;
    @FXML
    private Button addBtn;
    @FXML
    private Button clearBtn;
    @FXML
    private Button deleteBtn;
    @FXML
    private Button manageBtn;
    private AnchorPane currentAnchor;
    private Button currentButton;
    private final FaceRecognizer faceRecognizer = new FaceRecognizer();
    private ImageCapture imageCapture = new ImageCapture();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private long lastRecognitionChangeTime;
    private boolean faceRecognized = false;
    private boolean faceDetected = false;
    private long lastVisibilityChangeTime;
    private Color targetColor = Color.GRAY;
    private long lastFrameTime;
    private Color lastSentColor = Color.GRAY;
    private boolean isRecognitionActive = false;
    private AtomicBoolean isCameraActive = new AtomicBoolean(false);
    private volatile boolean stopRequested = false;
    private Image placeholderImage;
    private Integer recognizedPersonId;

    @FXML
    protected void initialize() {
        currentAnchor = homeAnchor;
        currentButton = homeBtn;
        homeAnchor.setVisible(true);
        homeBtn.getStyleClass().add("active");
        managementAnchor.setVisible(false);
        reportAnchor.setVisible(false);
        clearFields();
        showPessoaListData();
        placeholderImage = new Image(getClass().getResource("/br/com/catolicapb/facerecoptm/Images/camera_logo.png").toExternalForm());
        camImgView.setImage(placeholderImage);
        lblLed.setFill(Color.GRAY);
        lblLed.setVisible(false);
        registerAnchor.setVisible(false);
        identAnchor.setVisible(false);
        ConnectionToRaspberry.sendLedCommand("0,0,0");
        captureAndRecognizeButton.setOnAction(event -> toggleRecognition());
        captureAndRecognizeButton.setDisable(true);
        manageBtn.setOnAction(event -> {
            showAnchor(managementAnchor, managementBtn);
            selectPessoaById(recognizedPersonId);
        });
        registerButton.setOnAction(event -> {
            showAnchor(managementAnchor, managementBtn);
        });
        startStopCameraButton.setOnAction(event -> toggleCamera());
    }

    @FXML
    void addBtnAction() {
        addPessoa();
    }

    @FXML
    void clearBtnAction() {
        clearFields();
    }

    @FXML
    void updateBtnAction() {
        updatePessoa();
    }

    @FXML
    void deleteBtnAction() {
        deletePessoa();
        trainModel();
    }

    @FXML
    void closeBtnAction() {
        close();
    }

    @FXML
    void closeBtnIconAction() {
        close();
    }

    @FXML
    void minimizeBtnAction() {
        minimize();
    }

    @FXML
    void homeBtnAction() {
        showAnchor(homeAnchor, homeBtn);
    }

    @FXML
    void managementBtnAction() {
        showAnchor(managementAnchor, managementBtn);
    }

    @FXML
    void reportBtnAction() {
        showAnchor(reportAnchor, reportBtn);
    }

    private void showAnchor(AnchorPane anchorToShow, Button buttonToFocus) {
        if (anchorToShow != currentAnchor) {
            currentAnchor.setVisible(false);
            currentButton.getStyleClass().remove("active");
            anchorToShow.setVisible(true);
            buttonToFocus.getStyleClass().add("active");
            currentAnchor = anchorToShow;
            currentButton = buttonToFocus;
        }
    }

    private void close() {
        System.exit(0);
    }

    private void minimize() {
        Stage stage = (Stage) mainAnchor.getScene().getWindow();
        stage.setIconified(true);
    }

    private void showPessoaListData() {
        IDColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("nome"));
        CPFColumn.setCellValueFactory(new PropertyValueFactory<>("cpf"));
        var pessoaListData = PessoaDao.getPessoaListData();
        pessoaTv.setItems(pessoaListData);
        homeTotalLbl.setText(String.valueOf(PessoaDao.getTotalPessoas()));
    }

    @FXML
    private void selectPessoa() {
        var pessoa = pessoaTv.getSelectionModel().getSelectedItem();
        int index = pessoaTv.getSelectionModel().getSelectedIndex();
        if ((index - 1) < -1) {
            return;
        }
        IDLbl.setText(String.valueOf(pessoa.getId()));
        nameTf.setText(pessoa.getNome());
        CPFTf.setText(pessoa.getCpf());
        ClassTf.setText(pessoa.getTurma());
        isActiveCheckBox.setSelected(pessoa.getIsActive());
        RegisterDateLbl.setText(pessoa.getRegisterDate().toString());
    }

    private void selectPessoaById(Integer id) {
        var pessoa = PessoaDao.getPessoaById(id);
        if (pessoa != null) {
            IDLbl.setText(String.valueOf(pessoa.getId()));
            nameTf.setText(pessoa.getNome());
            CPFTf.setText(pessoa.getCpf());
            ClassTf.setText(pessoa.getTurma());
            isActiveCheckBox.setSelected(pessoa.getIsActive());
            RegisterDateLbl.setText(pessoa.getRegisterDate().toString());
        }
    }

    private void addPessoa() {
        if (nameTf.getText().isEmpty() || CPFTf.getText().isEmpty() || ClassTf.getText().isEmpty()) {
            showAlert("Os campos Nome, CPF e Turma devem ser preenchidos.", "ERROR");
            return;
        }
        if (!isCameraActive.get()) {
            showAlert("A câmera não está ativa. Por favor, ative a câmera antes de prosseguir com o cadastro.", "ERROR");
            return;
        }
        Pessoa pessoa = new Pessoa(
                nameTf.getText(),
                CPFTf.getText(),
                ClassTf.getText(),
                isActiveCheckBox.isSelected()
        );
        int pessoaId = PessoaDao.addPessoaData(pessoa);
        if (pessoaId == -1) {
            showAlert("Erro ao cadastrar a pessoa.", "ERROR");
            return;
        }
        faceRecognizer.loadModelAsync().thenRun(() -> {
            captureImagesForTraining(pessoaId);

            Platform.runLater(() -> {
                showAlert("A pessoa " + nameTf.getText() + " foi cadastrada com sucesso.", "INFO");
                clearFields();
                showPessoaListData();
            });
        });
    }

    private void updatePessoa() {
        PessoaDao.updatePessoaData(new Pessoa(
                Integer.parseInt(IDLbl.getText()),
                nameTf.getText(),
                CPFTf.getText(),
                ClassTf.getText(),
                isActiveCheckBox.isSelected()
        ));
        showAlert("A pessoa " + nameTf.getText() + " foi atualizada com sucesso.", "INFO");
        clearFields();
        showPessoaListData();
    }

    private void deletePessoa() {
        PessoaDao.deletePessoaData(Integer.parseInt(IDLbl.getText()));
        showAlert("A pessoa com ID " + IDLbl.getText() + " foi excluída com sucesso.", "INFO");
        clearFields();
        showPessoaListData();
    }

    private void clearFields() {
        IDLbl.setText("-");
        nameTf.setText("");
        CPFTf.setText("");
        RegisterDateLbl.setText("-");
        ClassTf.setText("");
        isActiveCheckBox.selectedProperty().setValue(false);
        pessoaTv.getSelectionModel().clearSelection();
    }

    private void showAlert(String msg, String type) {
        Alert alert;
        switch (type) {
            case "ERROR":
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error: tente novamente");
                break;
            case "INFO":
                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Informação");
                break;
            default:
                alert = new Alert(Alert.AlertType.NONE);
                break;
        }
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void toggleRecognition() {
        if (isRecognitionActive) {
            stopRecognition();
        } else {
            startRecognition();
        }
    }

    private void startRecognition() {
        stopRequested = false;
        faceRecognizer.loadModelAsync().thenRun(() -> {
            Platform.runLater(() -> {
                if (checkForTrainingImages()) {
                    if (!faceRecognizer.hasKnownEmbeddings()) {
                        trainModel();
                    }
                    isRecognitionActive = true;
                    captureAndRecognizeButton.setText("Parar Tracking");
                    captureAndRecognizeButton.getStyleClass().remove("add-btn");
                    captureAndRecognizeButton.getStyleClass().add("delete-btn");
                    lblLed.setVisible(true);
                    sendLedCommand(Color.GRAY);
                    setTargetColor(Color.GRAY);
                    updateLedColor();
                } else {
                    System.out.println("Nenhum modelo disponível.\n");
                }
            });
        });
    }

    private void stopRecognition() {
        stopRequested = true;
        isRecognitionActive = false;
        Platform.runLater(() -> {
            lblLed.setVisible(false);
            captureAndRecognizeButton.setText("Iniciar Tracking");
            captureAndRecognizeButton.getStyleClass().remove("delete-btn");
            captureAndRecognizeButton.getStyleClass().add("add-btn");
            lblLed.setFill(Color.GRAY);
            identAnchor.setVisible(false);
            registerAnchor.setVisible(false);
            sendLedCommand(Color.BLUE);
        });
        setTargetColor(Color.BLUE);
        faceDetected = false;
        faceRecognized = false;
        lastVisibilityChangeTime = 0;
        updateLedColor();
    }

    private boolean checkForTrainingImages() {
        return faceRecognizer.hasKnownEmbeddings();
    }

    private void trainModel() {
        faceRecognizer.loadModelAsync().thenRun(() -> System.out.println("Modelo treinado com sucesso."));
    }

    private void toggleCamera() {
        if (isCameraActive.get()) {
            stopCamera();
        } else {
            startCamera();
        }
    }

    private void startCamera() {
        if (!imageCapture.isOpened()) {
            imageCapture = new ImageCapture();
        }
        isCameraActive.set(true);
        startStopCameraButton.setText("Parar Camera");
        startStopCameraButton.getStyleClass().remove("add-btn");
        startStopCameraButton.getStyleClass().add("delete-btn");
        captureAndRecognizeButton.setDisable(false);
        executor.submit(() -> {
            while (isCameraActive.get()) {
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

    private void stopCamera() {
        isCameraActive.set(false);
        imageCapture.release();
        Platform.runLater(() -> {
            stopRecognition();
            camImgView.setImage(placeholderImage);
            identAnchor.setVisible(false);
            registerAnchor.setVisible(false);
            stopRequested = true;
            captureAndRecognizeButton.setDisable(true);
            captureAndRecognizeButton.getStyleClass().remove("delete-btn");
            captureAndRecognizeButton.getStyleClass().add("add-btn");
            startStopCameraButton.setText("Iniciar Camera");
            startStopCameraButton.getStyleClass().remove("delete-btn");
            startStopCameraButton.getStyleClass().add("add-btn");
        });
        updateLedColor();
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
        if (!isRecognitionActive || stopRequested) {
            return;
        }
        Mat grayFrame = imageCapture.captureGrayscaleImage();
        Rect[] facesArray = imageCapture.detectFaces(grayFrame);
        faceDetected = facesArray.length > 0;
        for (Rect face : facesArray) {
            Mat faceMat = new Mat(grayFrame, face);
            Integer personId = faceRecognizer.recognizeAndGetId(faceMat);
            String label;
            if (personId != -1) {
                //System.out.println("Pessoa reconhecida: " + personId);
                setTargetColor(Color.GREEN);
                faceRecognized = true;
                recognizedPersonId = personId;
                Pessoa pessoa = PessoaDao.getPessoaById(personId);
                label = pessoa != null ? pessoa.getNome() : "Desconhecido";
            } else {
                //System.out.println("Pessoa não reconhecida");
                setTargetColor(Color.RED);
                faceRecognized = false;
                recognizedPersonId = null;
                label = "Desconhecido";
            }
            Imgproc.putText(frame, label, face.tl(), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 255, 0), 2);
            Imgproc.rectangle(frame, face.tl(), face.br(), new Scalar(0, 255, 0), 2);
        }
        if (facesArray.length == 0) {
            //System.out.println("Nenhum rosto detectado");
            setTargetColor(Color.GRAY);
            faceRecognized = false;
            recognizedPersonId = null;
        }
        updateLedColor();
        displayFrame(frame);
    }

    private void updateAnchorVisibility() {
        if (System.currentTimeMillis() - lastVisibilityChangeTime > 400 || !isRecognitionActive) {
            Platform.runLater(() -> {
                if (faceDetected) {
                    if (faceRecognized) {
                        Pessoa pessoa = PessoaDao.getPessoaById(recognizedPersonId);
                        if (pessoa != null) {
                            updateRecognizedPersonDetails(pessoa);
                        } else {
                            updateUnrecognizedPersonDetails();
                        }
                    } else {
                        updateUnrecognizedPersonDetails();
                    }
                } else {
                    identAnchor.setVisible(false);
                    registerAnchor.setVisible(false);
                }
                lastVisibilityChangeTime = System.currentTimeMillis();
            });
        }
    }

    private void updateRecognizedPersonDetails(Pessoa pessoa) {
        NameLbl.setText(pessoa.getNome());
        CPFLbl.setText(pessoa.getCpf());
        ClassLbl.setText(pessoa.getTurma());
        identAnchor.setVisible(true);
        registerAnchor.setVisible(false);
    }

    private void updateUnrecognizedPersonDetails() {
        NameLbl.setText("Desconhecido");
        CPFLbl.setText("N/A");
        ClassLbl.setText("N/A");
        identAnchor.setVisible(false);
        registerAnchor.setVisible(true);
    }

    private void displayFrame(Mat frame) {
        Image imageToShow = mat2Image(frame);
        Platform.runLater(() -> camImgView.setImage(imageToShow));
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
                    updateAnchorVisibility();
                    if (!targetColor.equals(lastSentColor)) {
                        sendLedCommand(targetColor);
                        lastSentColor = targetColor;
                    }
                } else {
                    lblLed.setVisible(false);
                    if (!lastSentColor.equals(Color.GRAY)) {
                        ConnectionToRaspberry.sendLedCommand("0,0,0");
                        lastSentColor = Color.GRAY;
                    }
                    identAnchor.setVisible(false);
                    registerAnchor.setVisible(false);
                }
            });
        }
    }

    private void sendLedCommand(Color color) {
        String command = "";
        if (color.equals(Color.GRAY)) {
            command = "0,0,1";
        } else if (color.equals(Color.GREEN)) {
            command = "0,1,0";
        } else if (color.equals(Color.RED)) {
            command = "1,0,0";
        } else {
            command = "0,0,0";
        }
        ConnectionToRaspberry.sendLedCommand(command);
    }

    private Image mat2Image(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    private void captureImagesForTraining(int pessoaId) {
        int numImages = 7;
        for (int i = 0; i < numImages; i++) {
            Mat frame = imageCapture.captureGrayscaleImage();
            Rect[] facesArray = imageCapture.detectFaces(frame);
            if (facesArray.length == 1) {
                Mat face = new Mat(frame, facesArray[0]);
                faceRecognizer.addKnownEmbedding(pessoaId, face);
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
