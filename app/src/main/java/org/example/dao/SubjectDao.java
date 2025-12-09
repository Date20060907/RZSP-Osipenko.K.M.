package org.example.dao;

import org.example.dataclasses.Subject;
import org.example.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubjectDao {

    private static final String TABLE_NAME = "subjects";

    /**
     * Добавить новый предмет в БД.
     * @return ID нового предмета или -1 при ошибке
     */
    public int insert(Subject subject) {
        String sql = "INSERT INTO " + TABLE_NAME + " (name) VALUES (?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, subject.getName());
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        subject.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении предмета: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Найти предмет по ID.
     */
    public Subject findById(int id) {
        String sql = "SELECT id, name FROM " + TABLE_NAME + " WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Subject(rs.getInt("id"), rs.getString("name"));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при поиске предмета по ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Получить все предметы.
     */
    public List<Subject> findAll() {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT id, name FROM " + TABLE_NAME;

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                subjects.add(new Subject(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении списка предметов: " + e.getMessage());
        }
        return subjects;
    }

    /**
     * Найти предмет по имени (для проверки дубликатов).
     */
    public Subject findByName(String name) {
        String sql = "SELECT id, name FROM " + TABLE_NAME + " WHERE name = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Subject(rs.getInt("id"), rs.getString("name"));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при поиске предмета по имени: " + e.getMessage());
        }
        return null;
    }

    /**
     * Обновить предмет.
     */
    public boolean update(Subject subject) {
        String sql = "UPDATE " + TABLE_NAME + " SET name = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, subject.getName());
            pstmt.setInt(2, subject.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении предмета: " + e.getMessage());
        }
        return false;
    }

    /**
     * Удалить предмет по ID.
     * (Каскадное удаление: удалятся связанные уроки и связи в subject_to_group)
     */
    public boolean deleteById(int id) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении предмета: " + e.getMessage());
        }
        return false;
    }
}