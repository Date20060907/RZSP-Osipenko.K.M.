package org.example.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Утилитарный класс для управления подключениями к базе данных SQLite.
 * Обеспечивает инициализацию схемы базы данных и предоставляет методы для получения новых соединений.
 */
public class DatabaseManager {

    private static final Logger logger = LogManager.getLogger(DatabaseManager.class);
    private static final String DB_URL = "jdbc:sqlite:education.db";

    static {
        initializeDatabase();
    }

    /**
     * Закрытый конструктор для предотвращения создания экземпляров класса.
     */
    private DatabaseManager() {}

    /**
     * Устанавливает новое соединение с базой данных.
     * При каждом вызове возвращается отдельное соединение.
     * Автоматически включает поддержку внешних ключей для SQLite.
     *
     * @return новое соединение с базой данных
     * @throws SQLException если не удаётся установить соединение
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    /**
     * Инициализирует базу данных: создаёт необходимые таблицы, если они ещё не существуют.
     * Выполняется один раз при загрузке класса.
     */
    public static void initializeDatabase() {
        String[] createTableSQLs = {
            "CREATE TABLE IF NOT EXISTS groups (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL);",
            "CREATE TABLE IF NOT EXISTS subjects (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL);",
            "CREATE TABLE IF NOT EXISTS lessons (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, subject_id INTEGER NOT NULL, FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE);",
            "CREATE TABLE IF NOT EXISTS subject_to_group (id INTEGER PRIMARY KEY AUTOINCREMENT, subject_id INTEGER NOT NULL, group_id INTEGER NOT NULL, FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE, FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE);",
            "CREATE TABLE IF NOT EXISTS students (id INTEGER PRIMARY KEY AUTOINCREMENT, full_name TEXT NOT NULL, group_id INTEGER NOT NULL, FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE);",
            "CREATE TABLE IF NOT EXISTS grades (id INTEGER PRIMARY KEY AUTOINCREMENT, student_id INTEGER NOT NULL, lesson_id INTEGER NOT NULL, grade INTEGER NOT NULL CHECK (grade >= 0 AND grade <= 10), date_recorded DATE NOT NULL DEFAULT CURRENT_DATE, FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE, FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE);"
        };

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            for (String sql : createTableSQLs) {
                stmt.execute(sql);
            }
            logger.info("Таблицы базы данных успешно созданы или уже существуют");
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка инициализации базы данных", e);
        }
    }
}