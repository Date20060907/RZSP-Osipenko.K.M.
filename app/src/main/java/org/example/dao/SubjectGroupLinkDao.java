package org.example.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.dataclasses.SubjectGroupLink;
import org.example.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс доступа к данным (DAO) для связи между предметами и группами (SubjectGroupLink).
 * Обеспечивает взаимодействие с таблицей 'subject_to_group' в базе данных,
 * предоставляя методы для вставки, проверки существования, поиска и удаления связей.
 */
public class SubjectGroupLinkDao {

    private static final Logger logger = LogManager.getLogger(SubjectGroupLinkDao.class);
    private static final String TABLE_NAME = "subject_to_group";

    /**
     * Вставляет новую связь между предметом и группой.
     * Перед вставкой проверяется, не существует ли уже такая связь.
     *
     * @param link объект связи для сохранения
     * @return идентификатор вставленной связи или -1, если связь уже существует или произошла ошибка
     */
    public int insert(SubjectGroupLink link) {
        if (exists(link.getSubjectId(), link.getGroupId())) {
            logger.warn("Попытка вставки дублирующей связи: subjectId={}, groupId={}", 
                        link.getSubjectId(), link.getGroupId());
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
            logger.error("Ошибка при добавлении связи предмет-группа: {}", e.getMessage(), e);
        }
        return -1;
    }

    /**
     * Проверяет, существует ли связь между указанным предметом и группой.
     *
     * @param subjectId идентификатор предмета
     * @param groupId идентификатор группы
     * @return true, если связь существует, иначе false
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
            logger.error("Ошибка при проверке существования связи (subjectId={}, groupId={}): {}", 
                         subjectId, groupId, e.getMessage(), e);
        }
        return false;
    }

    /**
     * Возвращает список всех связей между предметами и группами.
     *
     * @return список объектов SubjectGroupLink, возможно пустой
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
                    rs.getInt("group_id")
                ));
            }
        } catch (SQLException e) {
            logger.error("Ошибка при получении всех связей: {}", e.getMessage(), e);
        }
        return links;
    }

    /**
     * Возвращает идентификаторы всех предметов, связанных с указанной группой.
     *
     * @param groupId идентификатор группы
     * @return список идентификаторов предметов, возможно пустой
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
            logger.error("Ошибка при получении предметов для группы {}: {}", groupId, e.getMessage(), e);
        }
        return subjectIds;
    }

    /**
     * Возвращает идентификаторы всех групп, связанных с указанным предметом.
     *
     * @param subjectId идентификатор предмета
     * @return список идентификаторов групп, возможно пустой
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
            logger.error("Ошибка при получении групп для предмета {}: {}", subjectId, e.getMessage(), e);
        }
        return groupIds;
    }

    /**
     * Удаляет связь по её уникальному идентификатору.
     *
     * @param id идентификатор связи
     * @return true, если запись была удалена, иначе false
     */
    public boolean deleteById(int id) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении связи по ID {}: {}", id, e.getMessage(), e);
        }
        return false;
    }

    /**
     * Удаляет все связи, связанные с указанной группой.
     *
     * @param groupId идентификатор группы
     * @return true, если хотя бы одна запись была удалена, иначе false
     */
    public boolean deleteByGroupId(int groupId) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE group_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, groupId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении связей для группы {}: {}", groupId, e.getMessage(), e);
        }
        return false;
    }

    /**
     * Удаляет все связи, связанные с указанным предметом.
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
            logger.error("Ошибка при удалении связей для предмета {}: {}", subjectId, e.getMessage(), e);
        }
        return false;
    }

    /**
     * Удаляет связь по идентификатору группы и идентификатору предмета.
     *
     * @param groupId идентификатор группы
     * @param subjectId идентификатор предмета
     * @return true, если запись была удалена, иначе false
     */
    public boolean deleteByGroupIdAndSubjectId(int groupId, int subjectId) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE group_id = ? AND subject_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, groupId);
            pstmt.setInt(2, subjectId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении связи (groupId={}, subjectId={}): {}", 
                         groupId, subjectId, e.getMessage(), e);
            return false;
        }
    }
}