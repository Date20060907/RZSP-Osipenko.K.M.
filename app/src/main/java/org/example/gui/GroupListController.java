package org.example.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.example.dao.GroupDao;
import org.example.dao.StudentDao;
import org.example.dataclasses.Group;
import org.example.dataclasses.Student;

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

public class GroupListController {

    private ObservableList<Group> groups;

    @FXML
    private Button importButton;

    @FXML
    private ListView<Group> groupListView;

    /**
     * Загружает все группы из базы данных.
     */
    public void loadGroupsFromDatabase() {
        GroupDao groupDao = new GroupDao();
        groups = FXCollections.observableArrayList(groupDao.findAll());
    }

    /**
     * Обработчик кнопки импорта групп из CSV.
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
            System.out.println("Выбор файла отменён.");
            return;
        }

        if (!selectedFile.getName().toLowerCase().endsWith(".csv")) {
            System.err.println("Пожалуйста, выберите файл с расширением .csv");
            return;
        }

        // Для эффективности: кэшируем ID групп по названию
        Map<String, Integer> groupNameToId = new HashMap<>();
        GroupDao groupDao = new GroupDao();
        StudentDao studentDao = new StudentDao();

        int groupAddedCount = 0;
        int studentAddedCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile, StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;
                if (firstLine) {
                    firstLine = false;
                    continue; // пропускаем заголовок
                }

                String[] parts = line.split(",", -1);
                if (parts.length < 2) {
                    System.err.println("Пропущена некорректная строка: " + line);
                    continue;
                }

                String fullName = parts[0].trim();
                String groupName = parts[1].trim();

                if (fullName.isEmpty() || groupName.isEmpty()) {
                    continue;
                }

                // --- 1. Убедимся, что группа существует ---
                Integer groupId = groupNameToId.get(groupName);
                if (groupId == null) {
                    // Проверяем, есть ли уже такая группа
                    List<Group> allGroups = groupDao.findAll();
                    Group existingGroup = allGroups.stream()
                            .filter(g -> g.getName().equals(groupName))
                            .findFirst()
                            .orElse(null);

                    if (existingGroup == null) {
                        // Создаём новую группу
                        Group newGroup = new Group(groupName);
                        int id = groupDao.insert(newGroup);
                        if (id != -1) {
                            newGroup.setId(id);
                            groupId = id;
                            groupNameToId.put(groupName, groupId);
                            groupAddedCount++;
                        } else {
                            System.err.println("Не удалось создать группу: " + groupName);
                            continue;
                        }
                    } else {
                        groupId = existingGroup.getId();
                        groupNameToId.put(groupName, groupId);
                    }
                }

                // --- 2. Добавляем студента ---
                // Проверяем, существует ли уже студент с таким ФИО в этой группе
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
            System.err.println("Ошибка чтения файла: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Обновляем список групп в интерфейсе
        loadGroupsFromDatabase();
        groupListView.setItems(groups);

        System.out.println("Импорт завершён.");
        System.out.println("Добавлено групп: " + groupAddedCount);
        System.out.println("Добавлено студентов: " + studentAddedCount);
    }

    /**
     * Инициализация контроллера (вызывается автоматически).
     */
    @FXML
    public void initialize() {
        // Устанавливаем кастомную ячейку для отображения групп
        groupListView.setCellFactory(param -> new GroupElement());

        // Загружаем данные
        loadGroupsFromDatabase();
        groupListView.setItems(groups);
    }

    /**
     * Обработчик двойного клика по группе — открывает SubjectList.
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
            System.err.println("Ошибка загрузки SubjectList.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
}