package org.example.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.dataclasses.Student;
import org.example.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс доступа к данным (DAO) для сущности студента (Student).
 * Обеспечивает взаимодействие с таблицей 'students' в базе данных,
 * предоставляя методы для вставки, поиска, обновления и удаления записей.
 */
public class StudentDao {

    private static final Logger logger = LogManager.getLogger(StudentDao.class);
    private static final String TABLE_NAME = "students";

    /**
     * Вставляет нового студента в базу данных.
     *
     * @param student объект студента для сохранения
     * @return идентификатор вставленного студента или -1 в случае ошибки
     */
    public int insert(Student student) {
        String sql = "INSERT INTO " + TABLE_NAME + " (full_name, group_id) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, student.getFullName());
            pstmt.setInt(2, student.getGroupId());
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        student.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка при добавлении студента: {}", e.getMessage(), e);
        }
        return -1;
    }

    /**
     * Находит студента по его уникальному идентификатору.
     *
     * @param id идентификатор студента
     * @return объект Student или null, если запись не найдена
     */
    public Student findById(int id) {
        String sql = "SELECT id, full_name, group_id FROM " + TABLE_NAME + " WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Student(
                    rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getInt("group_id")
                );
            }
        } catch (SQLException e) {
            logger.error("Ошибка при поиске студента по ID {}: {}", id, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Возвращает список всех студентов из базы данных.
     *
     * @return список объектов Student, возможно пустой
     */
    public List<Student> findAll() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT id, full_name, group_id FROM " + TABLE_NAME;

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                students.add(new Student(
                    rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getInt("group_id")
                ));
            }
        } catch (SQLException e) {
            logger.error("Ошибка при получении списка всех студентов: {}", e.getMessage(), e);
        }
        return students;
    }

    /**
     * Находит всех студентов, принадлежащих указанной группе.
     *
     * @param groupId идентификатор группы
     * @return список студентов из данной группы, возможно пустой
     */
    public List<Student> findByGroupId(int groupId) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT id, full_name, group_id FROM " + TABLE_NAME + " WHERE group_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, groupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    students.add(new Student(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getInt("group_id")
                    ));
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка при получении студентов для группы {}: {}", groupId, e.getMessage(), e);
        }
        return students;
    }

    /**
     * Обновляет данные существующего студента в базе данных.
     *
     * @param student объект студента с обновлёнными данными
     * @return true, если обновление прошло успешно, иначе false
     */
    public boolean update(Student student) {
        String sql = "UPDATE " + TABLE_NAME + " SET full_name = ?, group_id = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, student.getFullName());
            pstmt.setInt(2, student.getGroupId());
            pstmt.setInt(3, student.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении студента с ID {}: {}", student.getId(), e.getMessage(), e);
        }
        return false;
    }

    /**
     * Удаляет студента по его уникальному идентификатору.
     * Связанные оценки автоматически удаляются благодаря каскадному удалению (ON DELETE CASCADE) в БД.
     *
     * @param id идентификатор студента
     * @return true, если запись была удалена, иначе false
     */
    public boolean deleteById(int id) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении студента с ID {}: {}", id, e.getMessage(), e);
        }
        return false;
    }

    /**
     * Удаляет всех студентов, принадлежащих указанной группе.
     * Обычно вызывается при удалении самой группы.
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
            logger.error("Ошибка при удалении студентов группы {}: {}", groupId, e.getMessage(), e);
        }
        return false;
    }
}