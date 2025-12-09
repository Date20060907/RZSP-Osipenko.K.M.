package org.example.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.dataclasses.Group;
import org.example.dataclasses.Lesson;
import org.example.dataclasses.Subject;
import org.example.dataclasses.SubjectGroupLink;
import org.example.dao.LessonDao;
import org.example.dao.SubjectDao;
import org.example.dao.SubjectGroupLinkDao;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Контроллер всплывающего окна для добавления предмета к группе.
 * Поддерживает выбор существующего предмета из списка или импорт нового из CSV-файла.
 */
public class AddSubjectPopupMenuController {

    private static final Logger logger = LogManager.getLogger(AddSubjectPopupMenuController.class);

    /**
     * Выпадающий список доступных предметов.
     */
    @FXML
    private ComboBox<Subject> subjectList;

    /**
     * Кнопка отмены действия.
     */
    @FXML
    private Button cancelButton;

    /**
     * Кнопка подтверждения выбора предмета.
     */
    @FXML
    private Button applyButton;

    /**
     * Кнопка добавления нового предмета через импорт CSV-файла.
     */
    @FXML
    private Button addButton;

    /**
     * Текущая группа, к которой будет привязан выбранный или созданный предмет.
     */
    private Group currentGroup;

    /**
     * Инициализирует контроллер: загружает список всех предметов и отображает их в выпадающем списке.
     */
    @FXML
    public void initialize() {
        SubjectDao subjectDao = new SubjectDao();
        List<Subject> subjects = subjectDao.findAll();
        subjectList.getItems().addAll(subjects);
        logger.debug("Инициализация контроллера: загружено {} предметов", subjects.size());
    }

    /**
     * Устанавливает группу, с которой будет связываться предмет.
     *
     * @param group группа, переданная из родительского окна
     */
    public void setGroup(Group group) {
        this.currentGroup = group;
        logger.debug("Установлена группа для привязки предмета: {}", group.getName());
    }

    /**
     * Обработчик нажатия кнопки "Применить".
     * Создаёт связь между выбранным предметом и текущей группой.
     */
    @FXML
    private void apply() {
        Subject selected = subjectList.getValue();
        if (selected != null && currentGroup != null) {
            SubjectGroupLinkDao linkDao = new SubjectGroupLinkDao();
            linkDao.insert(new SubjectGroupLink(selected.getId(), currentGroup.getId()));
            logger.info("Создана связь: предмет '{}' ↔ группа '{}'", selected.getName(), currentGroup.getName());
        } else {
            logger.warn("Невозможно создать связь: предмет или группа не выбраны");
        }
        closeWindow();
    }

    /**
     * Обработчик нажатия кнопки "Отмена".
     * Закрывает окно без сохранения изменений.
     */
    @FXML
    private void cancel() {
        logger.debug("Действие отменено пользователем");
        closeWindow();
    }

    /**
     * Обработчик нажатия кнопки "Добавить".
     * Позволяет пользователю выбрать CSV-файл с описанием нового предмета и его занятий,
     * затем создаёт предмет и связанные занятия в базе данных.
     */
    @FXML
    private void add() {
        Stage currentStage = (Stage) subjectList.getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите CSV-файл с дисциплиной");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV файлы", "*.csv"));

        File selectedFile = fileChooser.showOpenDialog(currentStage);
        if (selectedFile == null) {
            logger.info("Выбор файла отменён пользователем");
            return;
        }

        if (!selectedFile.getName().toLowerCase().endsWith(".csv")) {
            logger.error("Выбран файл с недопустимым расширением: {}", selectedFile.getName());
            return;
        }

        String subjectName = null;
        List<String> lessonNames = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(selectedFile), StandardCharsets.UTF_8))) {

            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                String name = line.trim();
                if (name.isEmpty()) continue;

                if (first) {
                    subjectName = name;
                    first = false;
                } else {
                    lessonNames.add(name);
                }
            }
        } catch (IOException e) {
            logger.error("Ошибка при чтении CSV-файла '{}': {}", selectedFile.getAbsolutePath(), e.getMessage(), e);
            return;
        }

        if (subjectName == null || subjectName.isEmpty()) {
            logger.error("Название предмета не указано в файле '{}'", selectedFile.getName());
            return;
        }

        SubjectDao subjectDao = new SubjectDao();
        LessonDao lessonDao = new LessonDao();

        Subject existing = subjectDao.findByName(subjectName);
        if (existing != null) {
            logger.info("Предмет '{}' уже существует в базе данных", subjectName);
            refreshSubjectListAndSelect(existing);
            return;
        }

        // Создание нового предмета
        Subject newSubject = new Subject(subjectName);
        int subjectId = subjectDao.insert(newSubject);
        if (subjectId == -1) {
            logger.error("Не удалось добавить предмет в базу данных: {}", subjectName);
            return;
        }
        newSubject.setId(subjectId);
        logger.info("Добавлен новый предмет: {}", subjectName);

        // Создание занятий
        int lessonCount = 0;
        for (String lessonName : lessonNames) {
            Lesson lesson = new Lesson(lessonName, subjectId);
            if (lessonDao.insert(lesson) != -1) {
                lessonCount++;
            }
        }
        logger.info("Добавлено {} занятий для предмета '{}'", lessonCount, subjectName);

        refreshSubjectListAndSelect(newSubject);
    }

    /**
     * Обновляет список предметов в интерфейсе и выбирает указанный предмет.
     *
     * @param subjectToSelect предмет, который должен быть выбран после обновления
     */
    private void refreshSubjectListAndSelect(Subject subjectToSelect) {
        SubjectDao subjectDao = new SubjectDao();
        List<Subject> subjects = subjectDao.findAll();

        ObservableList<Subject> observableSubjects = FXCollections.observableArrayList(subjects);
        subjectList.setItems(observableSubjects);
        subjectList.getSelectionModel().select(subjectToSelect);
        logger.debug("Список предметов обновлён; выбран предмет: {}", subjectToSelect.getName());
    }

    /**
     * Закрывает текущее окно.
     */
    private void closeWindow() {
        Stage stage = (Stage) applyButton.getScene().getWindow();
        stage.close();
        logger.debug("Окно добавления предмета закрыто");
    }
}