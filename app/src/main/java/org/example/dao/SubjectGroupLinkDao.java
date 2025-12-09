package org.example.dao;

import org.example.dataclasses.SubjectGroupLink;
import org.example.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubjectGroupLinkDao {

    private static final String TABLE_NAME = "subject_to_group";

    /**
     * Добавить связь между предметом и группой.
     * 
     * @return ID новой связи или -1 при ошибке
     */
    public int insert(SubjectGroupLink link) {
        // Проверяем, не существует ли уже такая связь
        if (exists(link.getSubjectId(), link.getGroupId())) {
            System.out.println(
                    "Связь уже существует: subjectId=" + link.getSubjectId() + ", groupId=" + link.getGroupId());
            return -1;
        }

        String sql = "INSERT INTO " + TABLE_NAME + " (subject_id, group_id) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, link.getSubjectId());
            pstmt.setInt(2, link.getGroupId());
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        link.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении связи предмет-группа: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Проверить, существует ли связь.
     */
    public boolean exists(int subjectId, int groupId) {
        String sql = "SELECT 1 FROM " + TABLE_NAME + " WHERE subject_id = ? AND group_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, subjectId);
            pstmt.setInt(2, groupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при проверке существования связи: " + e.getMessage());
        }
        return false;
    }

    /**
     * Получить все связи.
     */
    public List<SubjectGroupLink> findAll() {
        List<SubjectGroupLink> links = new ArrayList<>();
        String sql = "SELECT id, subject_id, group_id FROM " + TABLE_NAME;

        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                links.add(new SubjectGroupLink(
                        rs.getInt("id"),
                        rs.getInt("subject_id"),
                        rs.getInt("group_id")));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении всех связей: " + e.getMessage());
        }
        return links;
    }

    /**
     * Получить все ID предметов, связанных с группой.
     */
    public List<Integer> findSubjectIdsByGroupId(int groupId) {
        List<Integer> subjectIds = new ArrayList<>();
        String sql = "SELECT subject_id FROM " + TABLE_NAME + " WHERE group_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, groupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    subjectIds.add(rs.getInt("subject_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении предметов для группы " + groupId + ": " + e.getMessage());
        }
        return subjectIds;
    }

    /**
     * Получить все ID групп, связанных с предметом.
     */
    public List<Integer> findGroupIdsBySubjectId(int subjectId) {
        List<Integer> groupIds = new ArrayList<>();
        String sql = "SELECT group_id FROM " + TABLE_NAME + " WHERE subject_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, subjectId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    groupIds.add(rs.getInt("group_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении групп для предмета " + subjectId + ": " + e.getMessage());
        }
        return groupIds;
    }

    /**
     * Удалить связь по ID.
     */
    public boolean deleteById(int id) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении связи по ID: " + e.getMessage());
        }
        return false;
    }

    /**
     * Удалить все связи для заданной группы.
     */
    public boolean deleteByGroupId(int groupId) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE group_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, groupId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении связей для группы " + groupId + ": " + e.getMessage());
        }
        return false;
    }

    /**
     * Удалить все связи для заданного предмета.
     */
    public boolean deleteBySubjectId(int subjectId) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE subject_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, subjectId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении связей для предмета " + subjectId + ": " + e.getMessage());
        }
        return false;
    }

    /**
     * Удалить связь по ID группы и ID предмета.
     */
    public boolean deleteByGroupIdAndSubjectId(int groupId, int subjectId) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE group_id = ? AND subject_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, groupId);
            pstmt.setInt(2, subjectId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении связи: " + e.getMessage());
            return false;
        }
    }
}