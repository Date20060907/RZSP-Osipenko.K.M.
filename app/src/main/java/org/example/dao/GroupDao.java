package org.example.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.dataclasses.Group;
import org.example.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс доступа к данным (DAO) для сущности группы (Group).
 * Обеспечивает взаимодействие с таблицей 'groups' в базе данных,
 * предоставляя методы для вставки, поиска, обновления и удаления записей.
 */
public class GroupDao {

    private static final Logger logger = LogManager.getLogger(GroupDao.class);
    private static final String TABLE_NAME = "groups";

    /**
     * Вставляет новую группу в базу данных.
     *
     * @param group объект группы для сохранения
     * @return идентификатор вставленной группы или -1 в случае ошибки
     */
    public int insert(Group group) {
        String sql = "INSERT INTO " + TABLE_NAME + " (name) VALUES (?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, group.getName());
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        group.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка при добавлении группы: {}", e.getMessage(), e);
        }
        return -1;
    }

    /**
     * Находит группу по её уникальному идентификатору.
     *
     * @param id идентификатор группы
     * @return объект Group или null, если запись не найдена
     */
    public Group findById(int id) {
        String sql = "SELECT id, name FROM " + TABLE_NAME + " WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Group(rs.getInt("id"), rs.getString("name"));
            }
        } catch (SQLException e) {
            logger.error("Ошибка при поиске группы по ID {}: {}", id, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Возвращает список всех групп из базы данных.
     *
     * @return список объектов Group, возможно пустой
     */
    public List<Group> findAll() {
        List<Group> groups = new ArrayList<>();
        String sql = "SELECT id, name FROM " + TABLE_NAME;

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                groups.add(new Group(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            logger.error("Ошибка при получении списка всех групп: {}", e.getMessage(), e);
        }
        return groups;
    }

    /**
     * Обновляет данные существующей группы в базе данных.
     *
     * @param group объект группы с обновлёнными данными
     * @return true, если обновление прошло успешно, иначе false
     */
    public boolean update(Group group) {
        String sql = "UPDATE " + TABLE_NAME + " SET name = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, group.getName());
            pstmt.setInt(2, group.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении группы с ID {}: {}", group.getId(), e.getMessage(), e);
        }
        return false;
    }

    /**
     * Удаляет группу по её уникальному идентификатору.
     * Удаление сопровождается каскадным удалением связанных записей
     * в других таблицах за счёт настроек внешних ключей (ON DELETE CASCADE).
     *
     * @param id идентификатор группы
     * @return true, если запись была удалена, иначе false
     */
    public boolean deleteById(int id) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении группы с ID {}: {}", id, e.getMessage(), e);
        }
        return false;
    }
}