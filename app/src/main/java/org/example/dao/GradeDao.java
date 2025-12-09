package org.example.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.dataclasses.Grade;
import org.example.dataclasses.GradeValue;
import org.example.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс доступа к данным (DAO) для сущности оценки (Grade).
 * Обеспечивает взаимодействие с таблицей 'grades' в базе данных,
 * предоставляя методы для вставки, поиска, обновления и удаления записей.
 */
public class GradeDao {

    private static final Logger logger = LogManager.getLogger(GradeDao.class);
    private static final String TABLE_NAME = "grades";

    /**
     * Вставляет новую оценку в базу данных и возвращает сгенерированный идентификатор.
     *
     * @param grade объект оценки для сохранения
     * @return идентификатор вставленной записи или -1 в случае ошибки
     */
    public int insert(Grade grade) {
        String sql = "INSERT INTO " + TABLE_NAME + " (student_id, lesson_id, grade, date_recorded) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, grade.getStudentId());
            pstmt.setInt(2, grade.getLessonId());
            pstmt.setInt(3, grade.getGradeValue().getCode());
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
            logger.error("Ошибка при добавлении оценки: {}", e.getMessage(), e);
        }
        return -1;
    }

    /**
     * Находит оценку по её уникальному идентификатору.
     *
     * @param id идентификатор оценки
     * @return объект Grade или null, если запись не найдена
     */
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
                    GradeValue.fromCode(rs.getInt("grade")),
                    rs.getDate("date_recorded").toLocalDate()
                );
            }
        } catch (SQLException e) {
            logger.error("Ошибка при поиске оценки по ID {}: {}", id, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Возвращает список всех оценок из базы данных.
     *
     * @return список объектов Grade, возможно пустой
     */
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
            logger.error("Ошибка при получении списка всех оценок: {}", e.getMessage(), e);
        }
        return grades;
    }

    /**
     * Находит все оценки, связанные с указанным студентом.
     *
     * @param studentId идентификатор студента
     * @return список оценок студента, возможно пустой
     */
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
            logger.error("Ошибка при получении оценок студента {}: {}", studentId, e.getMessage(), e);
        }
        return grades;
    }

    /**
     * Находит все оценки, связанные с указанным занятием (уроком).
     *
     * @param lessonId идентификатор занятия
     * @return список оценок занятия, возможно пустой
     */
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
            logger.error("Ошибка при получении оценок занятия {}: {}", lessonId, e.getMessage(), e);
        }
        return grades;
    }

    /**
     * Находит оценку по идентификатору студента и занятия.
     *
     * @param studentId идентификатор студента
     * @param lessonId идентификатор занятия
     * @return объект Grade или null, если запись не найдена
     */
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
            logger.error("Ошибка при поиске оценки для студента {} и занятия {}: {}", studentId, lessonId, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Обновляет существующую оценку в базе данных.
     *
     * @param grade объект оценки с обновлёнными данными
     * @return true, если обновление прошло успешно, иначе false
     */
    public boolean update(Grade grade) {
        String sql = "UPDATE " + TABLE_NAME + " SET grade = ?, date_recorded = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, grade.getGradeValue().getCode());
            pstmt.setDate(2, Date.valueOf(grade.getDateRecorded()));
            pstmt.setInt(3, grade.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении оценки с ID {}: {}", grade.getId(), e.getMessage(), e);
        }
        return false;
    }

    /**
     * Удаляет оценку по её уникальному идентификатору.
     *
     * @param id идентификатор оценки
     * @return true, если запись была удалена, иначе false
     */
    public boolean deleteById(int id) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении оценки с ID {}: {}", id, e.getMessage(), e);
        }
        return false;
    }

    /**
     * Удаляет все оценки, связанные с указанным студентом.
     *
     * @param studentId идентификатор студента
     * @return true, если хотя бы одна запись была удалена, иначе false
     */
    public boolean deleteByStudentId(int studentId) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE student_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении оценок студента {}: {}", studentId, e.getMessage(), e);
        }
        return false;
    }

    /**
     * Удаляет все оценки, связанные с указанным занятием.
     *
     * @param lessonId идентификатор занятия
     * @return true, если хотя бы одна запись была удалена, иначе false
     */
    public boolean deleteByLessonId(int lessonId) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE lesson_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, lessonId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении оценок занятия {}: {}", lessonId, e.getMessage(), e);
        }
        return false;
    }
}