package org.example.dao;

import org.example.dataclasses.Lesson;
import org.example.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LessonDao {

    private static final String TABLE_NAME = "lessons";

    /**
     * Добавить занятие в БД.
     * @return ID нового занятия или -1 при ошибке
     */
    public int insert(Lesson lesson) {
        String sql = "INSERT INTO " + TABLE_NAME + " (name, subject_id) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, lesson.getName());
            pstmt.setInt(2, lesson.getSubjectId());
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        lesson.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении занятия: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Найти занятие по ID.
     */
    public Lesson findById(int id) {
        String sql = "SELECT id, name, subject_id FROM " + TABLE_NAME + " WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Lesson(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("subject_id")
                );
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при поиске занятия по ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Получить все занятия.
     */
    public List<Lesson> findAll() {
        List<Lesson> lessons = new ArrayList<>();
        String sql = "SELECT id, name, subject_id FROM " + TABLE_NAME;

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lessons.add(new Lesson(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("subject_id")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении списка занятий: " + e.getMessage());
        }
        return lessons;
    }

    /**
     * Получить все занятия по ID предмета.
     */
    public List<Lesson> findBySubjectId(int subjectId) {
        List<Lesson> lessons = new ArrayList<>();
        String sql = "SELECT id, name, subject_id FROM " + TABLE_NAME + " WHERE subject_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, subjectId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lessons.add(new Lesson(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("subject_id")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении занятий для предмета " + subjectId + ": " + e.getMessage());
        }
        return lessons;
    }

    /**
     * Обновить занятие.
     */
    public boolean update(Lesson lesson) {
        String sql = "UPDATE " + TABLE_NAME + " SET name = ?, subject_id = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, lesson.getName());
            pstmt.setInt(2, lesson.getSubjectId());
            pstmt.setInt(3, lesson.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении занятия: " + e.getMessage());
        }
        return false;
    }

    /**
     * Удалить занятие по ID (каскадно удалятся и оценки).
     */
    public boolean deleteById(int id) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении занятия: " + e.getMessage());
        }
        return false;
    }

    /**
     * Удалить все занятия по ID предмета (например, при удалении предмета).
     */
    public boolean deleteBySubjectId(int subjectId) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE subject_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, subjectId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении занятий предмета " + subjectId + ": " + e.getMessage());
        }
        return false;
    }
}