package br.com.catolicapb.facerecoptm.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionToMySQL {
    private static final String URL = "jdbc:mysql://localhost:3306/facerecognition";
    private static final String USER = "root";
    private static final String PASSWORD = "Bl@ck0246";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
