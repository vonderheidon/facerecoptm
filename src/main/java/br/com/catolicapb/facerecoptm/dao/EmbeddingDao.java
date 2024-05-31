package br.com.catolicapb.facerecoptm.dao;

import br.com.catolicapb.facerecoptm.connection.ConnectionToMySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class EmbeddingDao {

    public static void saveEmbedding(String name, float[] embedding) throws SQLException {
        String sql = "INSERT INTO known_faces (name, embedding) VALUES (?, ?)";

        try (Connection conn = ConnectionToMySQL.getConnection();
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

    public static byte[] toByteArray(float[] floatArray) {
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

    public static Map<String, float[]> loadKnownEmbeddings() {
        Map<String, float[]> knownEmbeddings = new HashMap<>();
        try (Connection conn = ConnectionToMySQL.getConnection();
             ResultSet rs = getEmbeddings(conn)) {
            while (rs.next()) {
                String name = rs.getString("name");
                float[] embedding = toFloatArray(rs.getBytes("embedding"));
                knownEmbeddings.put(name, embedding);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return knownEmbeddings;
    }
}
