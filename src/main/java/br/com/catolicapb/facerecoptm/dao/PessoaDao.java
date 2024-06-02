package br.com.catolicapb.facerecoptm.dao;

import br.com.catolicapb.facerecoptm.connection.ConnectionToMySQL;
import br.com.catolicapb.facerecoptm.model.Pessoa;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PessoaDao {

    public static ObservableList<Pessoa> getPessoaListData() {
        ObservableList<Pessoa> listData = FXCollections.observableArrayList();
        String sql = "SELECT * FROM pessoa";
        try (Connection connection = ConnectionToMySQL.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet result = pstmt.executeQuery()) {

            while (result.next()) {
                Pessoa pessoa = new Pessoa(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getString("cpf"),
                        result.getString("turma"),
                        result.getDate("registerDate"),
                        result.getBoolean("isActive")

                );
                listData.add(pessoa);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return listData;
    }

    public static void addPessoaData(Pessoa pessoa) {
        String sql = "INSERT INTO pessoa (name, cpf, turma, isActive, registerDate) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = ConnectionToMySQL.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, pessoa.getNome());
            pstmt.setString(2, pessoa.getCpf());
            pstmt.setString(3, pessoa.getTurma());
            pstmt.setBoolean(4, pessoa.getIsActive());
            pstmt.setTimestamp(5, getCurrentTimestamp());
            pstmt.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static Timestamp getCurrentTimestamp() {
        return Timestamp.valueOf(LocalDateTime.now());
    }

    public static void updatePessoaData(Pessoa pessoa) {
        String sql = "UPDATE pessoa SET name = ?, cpf = ?, turma = ?, isActive = ? WHERE id = ?";

        try (Connection connection = ConnectionToMySQL.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, pessoa.getNome());
            pstmt.setString(2, pessoa.getCpf());
            pstmt.setString(3, pessoa.getTurma());
            pstmt.setBoolean(4, pessoa.getIsActive());
            pstmt.setLong(5, pessoa.getId());
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void deletePessoaData(Integer id) {
        String sql = "DELETE FROM pessoa WHERE id = ?";

        try (Connection connection = ConnectionToMySQL.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static int getTotalPessoas() {
        String sql = "SELECT COUNT(*) AS total FROM pessoa";
        int total = 0;

        try (Connection connection = ConnectionToMySQL.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet result = pstmt.executeQuery()) {

            if (result.next()) {
                total = result.getInt("total");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return total;
    }
}
