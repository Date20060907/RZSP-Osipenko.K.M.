package org.example.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.dataclasses.Lesson;
import org.example.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс доступа к данным (DAO) для сущности занятия (Lesson).
 * Обеспечивает взаимодействие с таблицей 'lessons' в базе данных,
 * предоставляя методы для вставки, поиска, обновления и удаления записей.
 */
public class LessonDao {

    private static final Logger logger = LogManager.getLogger(LessonDao.class);
    private static final String TABLE_NAME = "lessons";

    /**
     * Вставляет новое занятие в базу данных.
     *
     * @param lesson объект занятия для сохранения
     * @return идентификатор вставленного занятия или -1 в случае ошибки
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
            logger.error("Ошибка при добавлении занятия: {}", e.getMessage(), e);
        }
        return -1;
    }

    /**
     * Находит занятие по его уникальному идентификатору.
     *
     * @param id идентификатор занятия
     * @return объект Lesson или null, если запись не найдена
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
            logger.error("Ошибка при поиске занятия по ID {}: {}", id, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Возвращает список всех занятий из базы данных.
     *
     * @return список объектов Lesson, возможно пустой
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
            logger.error("Ошибка при получении списка всех занятий: {}", e.getMessage(), e);
        }
        return lessons;
    }

    /**
     * Находит все занятия, связанные с указанным предметом.
     *
     * @param subjectId идентификатор предмета
     * @return список занятий по данному предмету, возможно пустой
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
            logger.error("Ошибка при получении занятий для предмета {}: {}", subjectId, e.getMessage(), e);
        }
        return lessons;
    }

    /**
     * Обновляет данные существующего занятия в базе данных.
     *
     * @param lesson объект занятия с обновлёнными данными
     * @return true, если обновление прошло успешно, иначе false
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
            logger.error("Ошибка при обновлении занятия с ID {}: {}", lesson.getId(), e.getMessage(), e);
        }
        return false;
    }

    /**
     * Удаляет занятие по его уникальному идентификатору.
     * При удалении автоматически удаляются связанные оценки
     * благодаря каскадному удалению (ON DELETE CASCADE) в БД.
     *
     * @param id идентификатор занятия
     * @return true, если запись была удалена, иначе false
     */
    public boolean deleteById(int id) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении занятия с ID {}: {}", id, e.getMessage(), e);
        }
        return false;
    }

    /**
     * Удаляет все занятия, связанные с указанным предметом.
     * Обычно вызывается при удалении самого предмета.
     *
     * @param subjectId идентификатор предмета
     * @return true, если хотя бы одна запись была удалена, иначе false
     */
    public boolean deleteBySubjectId(int subjectId) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE subject_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, subjectId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении занятий предмета {}: {}", subjectId, e.getMessage(), e);
        }
        return false;
    }
}