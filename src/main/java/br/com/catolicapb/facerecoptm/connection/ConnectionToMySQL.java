package br.com.catolicapb.facerecoptm.connection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConnectionToMySQL {
    private static final String URL = "jdbc:mysql://localhost:3306/facerecognition";
    private static final String USER = "root";
    private static final String PASSWORD = "Bl@ck0246";
    private static HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(URL);
        config.setUsername(USER);
        config.setPassword(PASSWORD);
        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void saveEmbedding(String name, float[] embedding) throws SQLException {
        String sql = "INSERT INTO known_faces (name, embedding) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setBytes(2, toByteArray(embedding));
            pstmt.executeUpdate();
        }
    }

    public static ResultSet getEmbeddings(Connection conn) throws SQLException {
        String sql = "SELECT name, embedding FROM known_faces";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        return pstmt.executeQuery();
    }

    private static byte[] toByteArray(float[] floatArray) {
        int len = floatArray.length;
        byte[] byteArray = new byte[len * 4];
        for (int i = 0; i < len; i++) {
            int intBits = Float.floatToIntBits(floatArray[i]);
            byteArray[i * 4] = (byte) (intBits & 0xff);
            byteArray[i * 4 + 1] = (byte) ((intBits >> 8) & 0xff);
            byteArray[i * 4 + 2] = (byte) ((intBits >> 16) & 0xff);
            byteArray[i * 4 + 3] = (byte) ((intBits >> 24) & 0xff);
        }
        return byteArray;
    }

    public static float[] toFloatArray(byte[] byteArray) {
        int len = byteArray.length / 4;
        float[] floatArray = new float[len];
        for (int i = 0; i < len; i++) {
            int intBits = (byteArray[i * 4] & 0xff) |
                    ((byteArray[i * 4 + 1] & 0xff) << 8) |
                    ((byteArray[i * 4 + 2] & 0xff) << 16) |
                    ((byteArray[i * 4 + 3] & 0xff) << 24);
            floatArray[i] = Float.intBitsToFloat(intBits);
        }
        return floatArray;
    }
}
