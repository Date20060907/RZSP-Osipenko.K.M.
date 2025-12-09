package org.example.gui;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.dao.SubjectDao;
import org.example.dao.SubjectGroupLinkDao;
import org.example.dataclasses.Group;
import org.example.dataclasses.Subject;
import org.example.dataclasses.SubjectGroupLink;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Контроллер списка дисциплин для выбранной группы.
 * Отображает только те предметы, которые привязаны к группе,
 * и предоставляет функции добавления, импорта и перехода к таблице оценок.
 */
public class SubjectListController {

    private static final Logger logger = LogManager.getLogger(SubjectListController.class);

    /**
     * Список отображаемых предметов.
     */
    private ObservableList<Subject> subjects;

    /**
     * Текущая группа, для которой отображаются дисциплины.
     */
    private Group group;

    /**
     * Основная заголовочная метка с названием группы.
     */
    @FXML
    private Label mainLabel;

    /**
     * Кнопка импорта дисциплин из CSV-файла.
     */
    @FXML
    private Button importButton;

    /**
     * Список дисциплин с кастомными ячейками.
     */
    @FXML
    private ListView<Subject> subjectListView;

    /**
     * Загружает из базы данных только те предметы, которые связаны с текущей группой.
     */
    public void loadSubjectsForCurrentGroup() {
        if (group == null) {
            subjects = FXCollections.observableArrayList();
            return;
        }

        SubjectGroupLinkDao linkDao = new SubjectGroupLinkDao();
        SubjectDao subjectDao = new SubjectDao();

        List<Integer> subjectIds = linkDao.findSubjectIdsByGroupId(group.getId());
        List<Subject> linkedSubjects = subjectIds.stream()
                .map(subjectDao::findById)
                .filter(s -> s != null)
                .collect(Collectors.toList());

        subjects = FXCollections.observableArrayList(linkedSubjects);
        logger.debug("Загружено {} дисциплин для группы {}", linkedSubjects.size(), group.getName());
    }

    /**
     * Обработчик нажатия кнопки "Назад".
     * Возвращает пользователя к списку групп.
     */
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
            logger.error("Ошибка при загрузке GroupList.fxml: {}", e.getMessage(), e);
        }
    }

    /**
     * Обработчик нажатия кнопки "Добавить".
     * Открывает модальное окно для выбора или импорта новой дисциплины.
     */
    @FXML
    private void add() {
        if (group == null) {
            logger.warn("Попытка добавить дисциплину без установленной группы");
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

            loadSubjectsForCurrentGroup();
            subjectListView.setItems(subjects);
        } catch (IOException e) {
            logger.error("Ошибка при открытии AddSubjectPopupMenu.fxml: {}", e.getMessage(), e);
        }
    }

    /**
     * Обработчик импорта дисциплин из CSV-файла.
     * Каждая строка файла — название одной дисциплины.
     * Для каждой дисциплины создаётся запись (если не существует) и связь с текущей группой.
     */
    @FXML
    private void importSubjects() {
        if (group == null) {
            logger.warn("Импорт невозможен: группа не выбрана");
            return;
        }

        Stage currentStage = (Stage) importButton.getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите CSV файл с дисциплинами");
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

        Set<String> uniqueSubjectNames = new HashSet<>();
        try (BufferedReader reader = Files.newBufferedReader(selectedFile.toPath(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String name = line.trim();
                if (!name.isEmpty()) {
                    uniqueSubjectNames.add(name);
                }
            }
        } catch (IOException e) {
            logger.error("Ошибка при чтении CSV-файла '{}': {}", selectedFile.getAbsolutePath(), e.getMessage(), e);
            return;
        }

        SubjectDao subjectDao = new SubjectDao();
        SubjectGroupLinkDao linkDao = new SubjectGroupLinkDao();
        int addedCount = 0;

        for (String name : uniqueSubjectNames) {
            Subject subject = subjectDao.findByName(name);
            if (subject == null) {
                subject = new Subject(name);
                int id = subjectDao.insert(subject);
                if (id == -1) {
                    logger.warn("Не удалось создать дисциплину: {}", name);
                    continue;
                }
                subject.setId(id);
            }

            if (!linkDao.exists(subject.getId(), group.getId())) {
                linkDao.insert(new SubjectGroupLink(subject.getId(), group.getId()));
                addedCount++;
            }
        }

        loadSubjectsForCurrentGroup();
        subjectListView.setItems(subjects);
        logger.info("Импорт завершён: добавлено новых связей — {}", addedCount);
    }

    /**
     * Инициализирует контроллер при загрузке FXML.
     * Настройка выполняется в {@link #setGroup(Group)}, так как группа неизвестна на этом этапе.
     */
    @FXML
    public void initialize() {
        // Инициализация отложена до вызова setGroup
        logger.debug("SubjectListController инициализирован");
    }

    /**
     * Устанавливает текущую группу и обновляет интерфейс.
     *
     * @param group группа, для которой будет отображаться список дисциплин
     */
    public void setGroup(Group group) {
        this.group = group;
        if (group != null) {
            mainLabel.setText("Список дисциплин | " + group.getName());
            loadSubjectsForCurrentGroup();
            subjectListView.setCellFactory(param -> new SubjectElement(group.getId()));
            subjectListView.setItems(subjects);
        }
    }

    /**
     * Обработчик двойного клика по дисциплине.
     * Открывает новое окно с таблицей оценок для выбранного предмета и группы.
     *
     * @param event событие мыши
     */
    @FXML
    public void open(MouseEvent event) {
        if (event.getClickCount() != 2) {
            return;
        }

        Subject selected = subjectListView.getSelectionModel().getSelectedItem();
        if (selected == null || group == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AssesmentTable.fxml"));
            Parent root = loader.load();

            AssesmentTableController controller = loader.getController();
            controller.setContext(group, selected);

            Scene scene = new Scene(root);
            Stage currentStage = (Stage) subjectListView.getScene().getWindow();

            Stage newStage = new Stage();
            newStage.setScene(scene);
            newStage.setTitle("Оценки: " + selected.getName());
            newStage.setMaximized(true);
            newStage.show();

            currentStage.close();
        } catch (IOException e) {
            logger.error("Ошибка при открытии AssesmentTable.fxml: {}", e.getMessage(), e);
        }
    }
}