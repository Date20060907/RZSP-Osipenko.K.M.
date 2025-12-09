package org.example.gui;

import org.example.dataclasses.Group;
import org.example.dataclasses.Lesson;
import org.example.dataclasses.Subject;
import org.example.dataclasses.SubjectGroupLink;
import org.example.dao.LessonDao;
import org.example.dao.SubjectDao;
import org.example.dao.SubjectGroupLinkDao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AddSubjectPopupMenuController {

    @FXML
    private ComboBox<Subject> subjectList;

    @FXML
    private Button cancelButton;

    @FXML
    private Button applyButton;

    @FXML
    private Button addButton;

    private Group currentGroup; // ← Группа, с которой будем связывать

    @FXML
    public void initialize() {
        SubjectDao subjectDao = new SubjectDao();
        List<Subject> subjects = subjectDao.findAll();
        subjectList.getItems().addAll(subjects);
    }

    // Метод для передачи группы извне
    public void setGroup(Group group) {
        this.currentGroup = group;
    }

    @FXML
    private void apply() {
        Subject selected = subjectList.getValue();
        if (selected != null && currentGroup != null) {
            // Создаём связь: предмет <-> группа
            SubjectGroupLinkDao linkDao = new SubjectGroupLinkDao();
            linkDao.insert(new SubjectGroupLink(selected.getId(), currentGroup.getId()));
            System.out.println("✅ Связь создана: " + selected.getName() + " ↔ " + currentGroup.getName());
        }
        closeWindow();
    }

    @FXML
    private void cancel() {
        closeWindow();
    }

    @FXML
    private void add() {
        Stage currentStage = (Stage) subjectList.getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите CSV-файл с дисциплиной");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV файлы", "*.csv"));

        File selectedFile = fileChooser.showOpenDialog(currentStage);
        if (selectedFile == null) {
            System.out.println("Выбор файла отменён.");
            return;
        }

        if (!selectedFile.getName().toLowerCase().endsWith(".csv")) {
            System.err.println("Выбран файл не с расширением .csv");
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
                if (name.isEmpty())
                    continue;

                if (first) {
                    subjectName = name;
                    first = false;
                } else {
                    lessonNames.add(name);
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        if (subjectName == null || subjectName.isEmpty()) {
            System.err.println("Название дисциплины не найдено.");
            return;
        }

        SubjectDao subjectDao = new SubjectDao();
        LessonDao lessonDao = new LessonDao(); // ← добавили

        Subject existing = subjectDao.findByName(subjectName);
        if (existing != null) {
            System.out.println("Предмет \"" + subjectName + "\" уже существует.");
            refreshSubjectListAndSelect(existing);
            return;
        }

        // --- 1. Создаём предмет ---
        Subject newSubject = new Subject(subjectName);
        int subjectId = subjectDao.insert(newSubject);
        if (subjectId == -1) {
            System.err.println("❌ Не удалось добавить предмет: " + subjectName);
            return;
        }
        newSubject.setId(subjectId);
        System.out.println("✅ Предмет добавлен: " + subjectName);

        // --- 2. Создаём занятия ---
        int lessonCount = 0;
        for (String lessonName : lessonNames) {
            Lesson lesson = new Lesson(lessonName, subjectId);
            if (lessonDao.insert(lesson) != -1) {
                lessonCount++;
            }
        }
        System.out.println("✅ Добавлено занятий: " + lessonCount);

        // Обновляем список
        refreshSubjectListAndSelect(newSubject);
    }

    private void refreshSubjectListAndSelect(Subject subjectToSelect) {
        SubjectDao subjectDao = new SubjectDao();
        List<Subject> subjects = subjectDao.findAll();

        ObservableList<Subject> observableSubjects = FXCollections.observableArrayList(subjects);
        subjectList.setItems(observableSubjects);
        subjectList.getSelectionModel().select(subjectToSelect);
    }

    private void closeWindow() {
        Stage stage = (Stage) applyButton.getScene().getWindow();
        stage.close();
    }
}