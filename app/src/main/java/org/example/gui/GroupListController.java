package org.example.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.dao.GroupDao;
import org.example.dao.StudentDao;
import org.example.dataclasses.Group;
import org.example.dataclasses.Student;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Контроллер главного окна приложения — списка групп.
 * Отображает группы из базы данных, поддерживает импорт из CSV и переход к списку предметов по двойному клику.
 */
public class GroupListController {

    private static final Logger logger = LogManager.getLogger(GroupListController.class);

    /**
     * Список групп, отображаемых в интерфейсе.
     */
    private ObservableList<Group> groups;

    /**
     * Кнопка импорта групп и студентов из CSV-файла.
     */
    @FXML
    private Button importButton;

    /**
     * Список групп с кастомными ячейками.
     */
    @FXML
    private ListView<Group> groupListView;

    /**
     * Загружает все группы из базы данных и обновляет внутренний список.
     */
    public void loadGroupsFromDatabase() {
        GroupDao groupDao = new GroupDao();
        groups = FXCollections.observableArrayList(groupDao.findAll());
        logger.debug("Загружено {} групп из базы данных", groups.size());
    }

    /**
     * Обработчик нажатия кнопки импорта.
     * Позволяет пользователю выбрать CSV-файл и импортировать в него группы и студентов.
     */
    @FXML
    private void importGroups() {
        Stage currentStage = (Stage) importButton.getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите CSV файл с группами и студентами");
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

        Map<String, Integer> groupNameToId = new HashMap<>();
        GroupDao groupDao = new GroupDao();
        StudentDao studentDao = new StudentDao();

        int groupAddedCount = 0;
        int studentAddedCount = 0;

        try (BufferedReader reader = Files.newBufferedReader(selectedFile.toPath(), StandardCharsets.UTF_8)) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] parts = line.split(",", -1);
                if (parts.length < 2) {
                    logger.warn("Пропущена некорректная строка в CSV: {}", line);
                    continue;
                }

                String fullName = parts[0].trim();
                String groupName = parts[1].trim();

                if (fullName.isEmpty() || groupName.isEmpty()) {
                    continue;
                }

                Integer groupId = groupNameToId.get(groupName);
                if (groupId == null) {
                    List<Group> allGroups = groupDao.findAll();
                    Group existingGroup = allGroups.stream()
                            .filter(g -> g.getName().equals(groupName))
                            .findFirst()
                            .orElse(null);

                    if (existingGroup == null) {
                        Group newGroup = new Group(groupName);
                        int id = groupDao.insert(newGroup);
                        if (id != -1) {
                            newGroup.setId(id);
                            groupId = id;
                            groupNameToId.put(groupName, groupId);
                            groupAddedCount++;
                        } else {
                            logger.error("Не удалось создать группу: {}", groupName);
                            continue;
                        }
                    } else {
                        groupId = existingGroup.getId();
                        groupNameToId.put(groupName, groupId);
                    }
                }

                List<Student> studentsInGroup = studentDao.findByGroupId(groupId);
                boolean studentExists = studentsInGroup.stream()
                        .anyMatch(s -> s.getFullName().equals(fullName));

                if (!studentExists) {
                    Student newStudent = new Student(fullName, groupId);
                    int studentId = studentDao.insert(newStudent);
                    if (studentId != -1) {
                        studentAddedCount++;
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Ошибка при чтении CSV-файла '{}': {}", selectedFile.getAbsolutePath(), e.getMessage(), e);
            return;
        }

        loadGroupsFromDatabase();
        groupListView.setItems(groups);

        logger.info("Импорт завершён: добавлено групп — {}, студентов — {}", groupAddedCount, studentAddedCount);
    }

    /**
     * Инициализирует контроллер при загрузке FXML.
     * Настраивает кастомные ячейки и загружает данные.
     */
    @FXML
    public void initialize() {
        groupListView.setCellFactory(param -> new GroupElement());
        loadGroupsFromDatabase();
        groupListView.setItems(groups);
        logger.debug("Контроллер GroupListController инициализирован");
    }

    /**
     * Обработчик двойного клика по элементу списка.
     * Открывает окно списка предметов для выбранной группы.
     *
     * @param event событие мыши
     */
    @FXML
    public void open(MouseEvent event) {
        if (event.getClickCount() != 2) {
            return;
        }

        Group selectedGroup = groupListView.getSelectionModel().getSelectedItem();
        if (selectedGroup == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SubjectList.fxml"));
            Parent root = loader.load();

            SubjectListController controller = loader.getController();
            controller.setGroup(selectedGroup);

            Scene currentScene = groupListView.getScene();
            currentScene.setRoot(root);

            Stage stage = (Stage) currentScene.getWindow();
            stage.setTitle("Предметы группы: " + selectedGroup.getName());
        } catch (IOException e) {
            logger.error("Ошибка при загрузке SubjectList.fxml: {}", e.getMessage(), e);
        }
    }
}