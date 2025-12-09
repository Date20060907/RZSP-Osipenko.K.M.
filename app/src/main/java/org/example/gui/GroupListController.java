package org.example.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.example.dao.GroupDao;
import org.example.dataclasses.Group;

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
        fileChooser.setTitle("Выберите CSV файл с группами");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV файлы", "*.csv")
        );

        File selectedFile = fileChooser.showOpenDialog(currentStage);
        if (selectedFile == null) {
            System.out.println("Выбор файла отменён.");
            return;
        }

        if (!selectedFile.getName().toLowerCase().endsWith(".csv")) {
            System.err.println("Пожалуйста, выберите файл с расширением .csv");
            return;
        }

        Set<String> uniqueGroupNames = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile, StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                // Пропускаем заголовок (первая строка)
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                // Разделяем по запятой (CSV)
                String[] parts = line.split(",", -1);
                if (parts.length < 2) {
                    System.err.println("Пропущена некорректная строка: " + line);
                    continue;
                }

                String groupName = parts[1].trim();
                if (!groupName.isEmpty()) {
                    uniqueGroupNames.add(groupName);
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Добавляем новые группы в БД
        GroupDao groupDao = new GroupDao();
        int addedCount = 0;

        for (String groupName : uniqueGroupNames) {
            boolean exists = groupDao.findAll().stream()
                .anyMatch(g -> g.getName().equals(groupName));

            if (!exists) {
                Group newGroup = new Group(groupName);
                int id = groupDao.insert(newGroup);
                if (id != -1) {
                    addedCount++;
                }
            }
        }

        // Обновляем список в интерфейсе
        loadGroupsFromDatabase();
        groupListView.setItems(groups);

        System.out.println("Импорт завершён. Добавлено новых групп: " + addedCount);
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