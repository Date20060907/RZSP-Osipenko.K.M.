package org.example.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.example.dao.SubjectDao;
import org.example.dao.SubjectGroupLinkDao;
import org.example.dataclasses.Group;
import org.example.dataclasses.Subject;
import org.example.dataclasses.SubjectGroupLink;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SubjectListController {

    private ObservableList<Subject> subjects;
    private Group group; // Текущая группа

    @FXML
    private Label mainLabel;

    @FXML
    private Button importButton;

    @FXML
    private ListView<Subject> subjectListView;

    /**
     * Загружает ТОЛЬКО те предметы, которые связаны с текущей группой.
     */
    public void loadSubjectsForCurrentGroup() {
        if (group == null) {
            subjects = FXCollections.observableArrayList();
            return;
        }

        SubjectGroupLinkDao linkDao = new SubjectGroupLinkDao();
        SubjectDao subjectDao = new SubjectDao();

        // Получаем ID предметов, связанных с группой
        List<Integer> subjectIds = linkDao.findSubjectIdsByGroupId(group.getId());

        // Получаем сами предметы
        List<Subject> linkedSubjects = subjectIds.stream()
            .map(subjectDao::findById)
            .filter(s -> s != null)
            .collect(Collectors.toList());

        subjects = FXCollections.observableArrayList(linkedSubjects);
    }

    // === КНОПКА "НАЗАД" ===
    @FXML
    private void back() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GroupList.fxml"));
            Parent root = loader.load();

            Scene currentScene = subjectListView.getScene();
            currentScene.setRoot(root);

            Stage stage = (Stage) currentScene.getWindow();
            stage.setTitle("Список групп");
        } catch (IOException e) {
            System.err.println("Ошибка загрузки GroupAssistant.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // === КНОПКА "ДОБАВИТЬ" — ОТКРЫВАЕТ ДИАЛОГ ВЫБОРА ПРЕДМЕТА ===
    @FXML
    private void add() {
        if (group == null) {
            System.err.println("Группа не установлена!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddSubjectPopupMenu.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage popupStage = new Stage();
            popupStage.setTitle("Добавить дисциплину");
            popupStage.setScene(scene);
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.initOwner(subjectListView.getScene().getWindow());
            popupStage.setResizable(false);

            AddSubjectPopupMenuController controller = loader.getController();
            controller.setGroup(group);

            popupStage.showAndWait();

            // После закрытия — обновляем список
            loadSubjectsForCurrentGroup();
            subjectListView.setItems(subjects);

        } catch (IOException e) {
            System.err.println("Ошибка открытия AddSubjectPopupMenu.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // === ИМПОРТ CSV: ДОБАВЛЯЕМ ПРЕДМЕТ И СРАЗУ СВЯЗЫВАЕМ С ГРУППОЙ ===
    @FXML
    private void importSubjects() {
        if (group == null) {
            System.err.println("Невозможно импортировать: группа не выбрана.");
            return;
        }

        Stage currentStage = (Stage) importButton.getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите CSV файл с дисциплинами");
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

        Set<String> uniqueSubjectNames = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String name = line.trim();
                if (!name.isEmpty()) {
                    uniqueSubjectNames.add(name);
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        SubjectDao subjectDao = new SubjectDao();
        SubjectGroupLinkDao linkDao = new SubjectGroupLinkDao();
        int addedCount = 0;

        for (String name : uniqueSubjectNames) {
            // Находим или создаём предмет
            Subject subject = subjectDao.findByName(name);
            if (subject == null) {
                subject = new Subject(name);
                int id = subjectDao.insert(subject);
                if (id == -1) continue;
                subject.setId(id);
            }

            // Проверяем, есть ли уже связь
            if (!linkDao.exists(subject.getId(), group.getId())) {
                linkDao.insert(new SubjectGroupLink(subject.getId(), group.getId()));
                addedCount++;
            }
        }

        loadSubjectsForCurrentGroup();
        subjectListView.setItems(subjects);
        System.out.println("Импорт завершён. Добавлено новых связей: " + addedCount);
    }

    // === ИНИЦИАЛИЗАЦИЯ ===
    @FXML
    public void initialize() {
        subjectListView.setCellFactory(param -> new SubjectElement());
        // Список загрузится в setGroup()
    }

    // === ДВОЙНОЙ КЛИК (можно оставить как заглушку) ===
    @FXML
    public void open(MouseEvent event) {
        if (event.getClickCount() != 2) return;
        Subject selected = subjectListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            System.out.println("Двойной клик по предмету: " + selected.getName());
        }
    }

    // === УСТАНОВКА ГРУППЫ И ЗАГРУЗКА СВЯЗАННЫХ ПРЕДМЕТОВ ===
    public void setGroup(Group group) {
        this.group = group;
        if (group != null) {
            mainLabel.setText("Список дисциплин | " + group.getName());
            loadSubjectsForCurrentGroup();
            subjectListView.setItems(subjects);
        }
    }
}