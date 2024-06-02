package br.com.catolicapb.facerecoptm.controller;

import br.com.catolicapb.facerecoptm.dao.PessoaDao;
import br.com.catolicapb.facerecoptm.model.Pessoa;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;


public class SGPViewController {
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
    private AnchorPane currentAnchor;
    @FXML
    private AnchorPane mainAnchor;
    @FXML
    private AnchorPane homeAnchor;
    @FXML
    private AnchorPane managementAnchor;
    @FXML
    private AnchorPane reportAnchor;
    @FXML
    private Button reportBtn;
    @FXML
    private Button updateBtn;
    @FXML
    private Button managementBtn;
    @FXML
    private Button homeBtn;
    @FXML
    private Button addBtn;
    @FXML
    private Button clearBtn;
    @FXML
    private Button deleteBtn;
    private Button currentButton;

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
        Stage stage = (Stage)mainAnchor.getScene().getWindow();
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

        if ((index -1) < -1) {return;}

        IDLbl.setText(String.valueOf(pessoa.getId()));
        nameTf.setText(pessoa.getNome());
        CPFTf.setText(pessoa.getCpf());
        ClassTf.setText(pessoa.getTurma());
        isActiveCheckBox.setSelected(pessoa.getIsActive());
        RegisterDateLbl.setText(pessoa.getRegisterDate().toString());
    }

    private void addPessoa() {
        if (nameTf.getText().isEmpty() || CPFTf.getText().isEmpty() || ClassTf.getText().isEmpty()) {
            showAlert("Os campos Nome, CPF e Turma devem ser preenchidos.", "ERROR");
        } else {
            PessoaDao.addPessoaData(new Pessoa(
                    nameTf.getText(),
                    CPFTf.getText(),
                    ClassTf.getText(),
                    isActiveCheckBox.isSelected()
            ));
            showAlert("A pessoa "+nameTf.getText()+" foi cadastrada com sucesso.", "INFO");
            clearFields();
            showPessoaListData();
        }
    }

    private void updatePessoa() {
        PessoaDao.updatePessoaData(new Pessoa(
                Integer.parseInt(IDLbl.getText()),
                nameTf.getText(),
                CPFTf.getText(),
                ClassTf.getText(),
                isActiveCheckBox.isSelected()
        ));
        showAlert("A pessoa "+nameTf.getText()+" foi atualizada com sucesso.", "INFO");
        clearFields();
        showPessoaListData();
    }

    private void deletePessoa() {
        PessoaDao.deletePessoaData(Integer.parseInt(IDLbl.getText()));
        showAlert("A pessoa com ID "+IDLbl.getText()+" foi excluída com sucesso.", "INFO");
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

}
