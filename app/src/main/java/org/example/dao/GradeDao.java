package org.example.dao;

import org.example.dataclasses.Grade;
import org.example.dataclasses.GradeValue;
import org.example.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GradeDao {

    private static final String TABLE_NAME = "grades";

    public int insert(Grade grade) {
        String sql = "INSERT INTO " + TABLE_NAME + " (student_id, lesson_id, grade, date_recorded) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, grade.getStudentId());
            pstmt.setInt(2, grade.getLessonId());
            pstmt.setInt(3, grade.getGradeValue().getCode()); // ← сохраняем код enum
            pstmt.setDate(4, Date.valueOf(grade.getDateRecorded()));
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        grade.setId(rs.getInt(1));
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении оценки: " + e.getMessage());
        }
        return -1;
    }

    public Grade findById(int id) {
        String sql = "SELECT id, student_id, lesson_id, grade, date_recorded FROM " + TABLE_NAME + " WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Grade(
                    rs.getInt("id"),
                    rs.getInt("student_id"),
                    rs.getInt("lesson_id"),
                    GradeValue.fromCode(rs.getInt("grade")), // ← преобразуем из int в enum
                    rs.getDate("date_recorded").toLocalDate()
                );
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при поиске оценки по ID: " + e.getMessage());
        }
        return null;
    }

    public List<Grade> findAll() {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT id, student_id, lesson_id, grade, date_recorded FROM " + TABLE_NAME;

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                grades.add(new Grade(
                    rs.getInt("id"),
                    rs.getInt("student_id"),
                    rs.getInt("lesson_id"),
                    GradeValue.fromCode(rs.getInt("grade")),
                    rs.getDate("date_recorded").toLocalDate()
                ));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении списка оценок: " + e.getMessage());
        }
        return grades;
    }

    public List<Grade> findByStudentId(int studentId) {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT id, student_id, lesson_id, grade, date_recorded FROM " + TABLE_NAME + " WHERE student_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    grades.add(new Grade(
                        rs.getInt("id"),
                        rs.getInt("student_id"),
                        rs.getInt("lesson_id"),
                        GradeValue.fromCode(rs.getInt("grade")),
                        rs.getDate("date_recorded").toLocalDate()
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении оценок студента " + studentId + ": " + e.getMessage());
        }
        return grades;
    }

    public List<Grade> findByLessonId(int lessonId) {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT id, student_id, lesson_id, grade, date_recorded FROM " + TABLE_NAME + " WHERE lesson_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, lessonId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    grades.add(new Grade(
                        rs.getInt("id"),
                        rs.getInt("student_id"),
                        rs.getInt("lesson_id"),
                        GradeValue.fromCode(rs.getInt("grade")),
                        rs.getDate("date_recorded").toLocalDate()
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении оценок занятия " + lessonId + ": " + e.getMessage());
        }
        return grades;
    }

    public Grade findByStudentAndLesson(int studentId, int lessonId) {
        String sql = "SELECT id, student_id, lesson_id, grade, date_recorded FROM " + TABLE_NAME +
                     " WHERE student_id = ? AND lesson_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            pstmt.setInt(2, lessonId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Grade(
                    rs.getInt("id"),
                    rs.getInt("student_id"),
                    rs.getInt("lesson_id"),
                    GradeValue.fromCode(rs.getInt("grade")),
                    rs.getDate("date_recorded").toLocalDate()
                );
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при поиске оценки: " + e.getMessage());
        }
        return null;
    }

    public boolean update(Grade grade) {
        String sql = "UPDATE " + TABLE_NAME + " SET grade = ?, date_recorded = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, grade.getGradeValue().getCode());
            pstmt.setDate(2, Date.valueOf(grade.getDateRecorded()));
            pstmt.setInt(3, grade.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении оценки: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteById(int id) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении оценки: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteByStudentId(int studentId) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE student_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении оценок студента " + studentId + ": " + e.getMessage());
        }
        return false;
    }

    public boolean deleteByLessonId(int lessonId) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE lesson_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, lessonId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении оценок занятия " + lessonId + ": " + e.getMessage());
        }
        return false;
    }
}