package org.example.dao;

import org.example.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.example.dataclasses.Group;

public class GroupDao {

    private static final String TABLE_NAME = "groups";

    /**
     * Добавить новую группу в БД.
     * @return ID новой группы, или -1 при ошибке
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
            System.err.println("Ошибка при добавлении группы: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Получить группу по ID.
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
            System.err.println("Ошибка при поиске группы по ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Получить все группы.
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
            System.err.println("Ошибка при получении списка групп: " + e.getMessage());
        }
        return groups;
    }

    /**
     * Обновить данные группы.
     */
    public boolean update(Group group) {
        String sql = "UPDATE " + TABLE_NAME + " SET name = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, group.getName());
            pstmt.setInt(2, group.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении группы: " + e.getMessage());
        }
        return false;
    }

    /**
     * Удалить группу по ID.
     * (Каскадное удаление из других таблиц поддерживается за счёт FOREIGN KEY ON DELETE CASCADE)
     */
    public boolean deleteById(int id) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении группы: " + e.getMessage());
        }
        return false;
    }
}