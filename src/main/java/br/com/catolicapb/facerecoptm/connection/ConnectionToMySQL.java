package br.com.catolicapb.facerecoptm.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionToMySQL {
    private static final String URL = "jdbc:mysql://localhost:3306/facerecognition";
    private static final String USER = "root";
    private static final String PASSWORD = "Bl@ck0246";

    public static Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexão realizada com sucesso.");
        } catch (SQLException ex) {
            System.err.println("Erro ao conectar ao banco de dados.\n".concat(ex.getMessage()));
        }
        return connection;
    }
}
