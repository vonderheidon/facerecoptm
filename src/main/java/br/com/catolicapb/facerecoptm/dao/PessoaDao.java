package br.com.catolicapb.facerecoptm.dao;

import br.com.catolicapb.facerecoptm.connection.ConnectionToMySQL;
import br.com.catolicapb.facerecoptm.model.Pessoa;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.SQLException;

public class PessoaDao {
    private static final Connection connection = ConnectionToMySQL.getConnection();

    public static ObservableList<Pessoa> getPessoaListData() {
        ObservableList<Pessoa> listData = FXCollections.observableArrayList();
        String sql = "SELECT id, nome, cpf FROM pessoa";

        try (var stmt = connection.prepareStatement(sql);
             var result = stmt.executeQuery()) {
            while (result.next()) {
                Pessoa pessoa = new Pessoa(
                        result.getInt("id"),
                        result.getString("nome"),
                        result.getString("cpf")
                );
                listData.add(pessoa);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return listData;
    }

    public static void addPessoaData(Pessoa pessoa) {
        String sql = "INSERT INTO pessoa (nome,cpf) VALUES(?,?);";

        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, pessoa.getNome());
            stmt.setString(2, pessoa.getCpf());
            stmt.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void updatePessoaData(Pessoa pessoa) {
        String sql = "UPDATE pessoa SET nome = ?, cpf = ? WHERE id = ?";

        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, pessoa.getNome());
            stmt.setString(2, pessoa.getCpf());
            stmt.setLong(3, pessoa.getId());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void deletePessoaData(Pessoa pessoa) {
        String sql = "DELETE FROM pessoa WHERE id = ?";

        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, pessoa.getId());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static int getTotalPessoas() {
        String sql = "SELECT COUNT(*) AS total FROM pessoa";
        int total = 0;

        try (var stmt = connection.prepareStatement(sql);
             var result = stmt.executeQuery()) {
            if (result.next()) {
                total = result.getInt("total");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return total;
    }
}
