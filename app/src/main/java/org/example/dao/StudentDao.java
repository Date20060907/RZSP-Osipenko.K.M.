package org.example.dao;

import org.example.dataclasses.Student;
import org.example.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDao {

    private static final String TABLE_NAME = "students";

    /**
     * Добавить студента в БД.
     * @return ID нового студента или -1 при ошибке
     */
    public int insert(Student student) {
        String sql = "INSERT INTO " + TABLE_NAME + " (full_name, group_id) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, student.getFullName());
            pstmt.setInt(2, student.getGroupId());
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        student.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении студента: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Найти студента по ID.
     */
    public Student findById(int id) {
        String sql = "SELECT id, full_name, group_id FROM " + TABLE_NAME + " WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Student(
                    rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getInt("group_id")
                );
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при поиске студента по ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Получить всех студентов.
     */
    public List<Student> findAll() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT id, full_name, group_id FROM " + TABLE_NAME;

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                students.add(new Student(
                    rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getInt("group_id")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении списка студентов: " + e.getMessage());
        }
        return students;
    }

    /**
     * Получить студентов по ID группы.
     */
    public List<Student> findByGroupId(int groupId) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT id, full_name, group_id FROM " + TABLE_NAME + " WHERE group_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, groupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    students.add(new Student(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getInt("group_id")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении студентов для группы " + groupId + ": " + e.getMessage());
        }
        return students;
    }

    /**
     * Обновить студента.
     */
    public boolean update(Student student) {
        String sql = "UPDATE " + TABLE_NAME + " SET full_name = ?, group_id = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, student.getFullName());
            pstmt.setInt(2, student.getGroupId());
            pstmt.setInt(3, student.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении студента: " + e.getMessage());
        }
        return false;
    }

    /**
     * Удалить студента по ID (каскадно удалятся и оценки).
     */
    public boolean deleteById(int id) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении студента: " + e.getMessage());
        }
        return false;
    }

    /**
     * Удалить всех студентов из группы (например, при удалении группы).
     */
    public boolean deleteByGroupId(int groupId) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE group_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, groupId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении студентов группы " + groupId + ": " + e.getMessage());
        }
        return false;
    }
}