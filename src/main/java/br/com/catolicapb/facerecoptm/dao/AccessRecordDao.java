package br.com.catolicapb.facerecoptm.dao;

import br.com.catolicapb.facerecoptm.connection.ConnectionToMySQL;
import br.com.catolicapb.facerecoptm.model.AccessRecord;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AccessRecordDao {

    public static void recordEntry(int pessoaId, LocalDateTime entryTime) {
        String sql = "INSERT INTO access_records (pessoa_id, entry_time) VALUES (?, ?)";

        try (Connection conn = ConnectionToMySQL.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pessoaId);
            pstmt.setTimestamp(2, Timestamp.valueOf(entryTime));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void recordExit(int pessoaId, LocalDateTime exitTime) {
        String sql = "UPDATE access_records SET exit_time = ? WHERE pessoa_id = ? AND DATE(entry_time) = ? AND exit_time IS NULL";

        try (Connection conn = ConnectionToMySQL.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(exitTime));
            pstmt.setInt(2, pessoaId);
            pstmt.setDate(3, Date.valueOf(exitTime.toLocalDate()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ObservableList<AccessRecord> getAccessRecordsByDate(LocalDate date) {
        ObservableList<AccessRecord> records = FXCollections.observableArrayList();
        String sql = "SELECT * FROM access_records WHERE DATE(entry_time) = ?";
        try (Connection conn = ConnectionToMySQL.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, date.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                AccessRecord record = new AccessRecord(
                        rs.getInt("id"),
                        rs.getInt("pessoa_id"),
                        rs.getTimestamp("entry_time").toLocalDateTime(),
                        rs.getTimestamp("exit_time") != null ? rs.getTimestamp("exit_time").toLocalDateTime() : null
                );
                records.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    public static boolean hasEntryForToday(int pessoaId) {
        String sql = "SELECT COUNT(*) FROM access_records WHERE pessoa_id = ? AND DATE(entry_time) = ?";

        try (Connection conn = ConnectionToMySQL.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pessoaId);
            pstmt.setDate(2, Date.valueOf(LocalDate.now()));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean hasExitForToday(int pessoaId) {
        String sql = "SELECT COUNT(*) FROM access_records WHERE pessoa_id = ? AND DATE(entry_time) = ? AND exit_time IS NOT NULL";

        try (Connection conn = ConnectionToMySQL.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pessoaId);
            pstmt.setDate(2, Date.valueOf(LocalDate.now()));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}