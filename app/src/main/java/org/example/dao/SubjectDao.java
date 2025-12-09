package org.example.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.dataclasses.Subject;
import org.example.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс доступа к данным (DAO) для сущности предмета (Subject).
 * Обеспечивает взаимодействие с таблицей 'subjects' в базе данных,
 * предоставляя методы для вставки, поиска, обновления и удаления записей.
 */
public class SubjectDao {

    private static final Logger logger = LogManager.getLogger(SubjectDao.class);
    private static final String TABLE_NAME = "subjects";

    /**
     * Вставляет новый предмет в базу данных.
     *
     * @param subject объект предмета для сохранения
     * @return идентификатор вставленного предмета или -1 в случае ошибки
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
            logger.error("Ошибка при добавлении предмета: {}", e.getMessage(), e);
        }
        return -1;
    }

    /**
     * Находит предмет по его уникальному идентификатору.
     *
     * @param id идентификатор предмета
     * @return объект Subject или null, если запись не найдена
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
            logger.error("Ошибка при поиске предмета по ID {}: {}", id, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Возвращает список всех предметов из базы данных.
     *
     * @return список объектов Subject, возможно пустой
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
            logger.error("Ошибка при получении списка всех предметов: {}", e.getMessage(), e);
        }
        return subjects;
    }

    /**
     * Находит предмет по его названию.
     * Используется, например, для проверки дубликатов перед вставкой.
     *
     * @param name название предмета
     * @return объект Subject или null, если предмет с таким именем не найден
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
            logger.error("Ошибка при поиске предмета по имени '{}': {}", name, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Обновляет данные существующего предмета в базе данных.
     *
     * @param subject объект предмета с обновлёнными данными
     * @return true, если обновление прошло успешно, иначе false
     */
    public boolean update(Subject subject) {
        String sql = "UPDATE " + TABLE_NAME + " SET name = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, subject.getName());
            pstmt.setInt(2, subject.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении предмета с ID {}: {}", subject.getId(), e.getMessage(), e);
        }
        return false;
    }

    /**
     * Удаляет предмет по его уникальному идентификатору.
     * Удаление сопровождается каскадным удалением связанных записей:
     * занятий (lessons) и связей с группами (subject_to_group),
     * за счёт настроек внешних ключей в базе данных (ON DELETE CASCADE).
     *
     * @param id идентификатор предмета
     * @return true, если запись была удалена, иначе false
     */
    public boolean deleteById(int id) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении предмета с ID {}: {}", id, e.getMessage(), e);
        }
        return false;
    }
}