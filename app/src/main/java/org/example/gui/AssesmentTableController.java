package org.example.gui;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.dataclasses.*;
import org.example.dao.*;

import java.io.IOException;
import java.util.*;

/**
 * Контроллер окна с таблицей оценок студентов по определённому предмету и группе.
 * Отображает ФИО, посещаемость, средний балл и оценки по каждому занятию.
 * Поддерживает редактирование оценок через выпадающие списки.
 */
public class AssesmentTableController {

    private static final Logger logger = LogManager.getLogger(AssesmentTableController.class);

    /**
     * Метка для отображения среднего балла группы.
     */
    @FXML
    private Label midGrade;

    /**
     * Метка для отображения общей посещаемости группы.
     */
    @FXML
    private Label groupAttendance;

    /**
     * Основная заголовочная метка с названием группы и предмета.
     */
    @FXML
    private Label mainLabel;

    /**
     * Таблица с данными студентов и их оценками.
     */
    @FXML
    private TableView<StudentGradeRow> table;

    /**
     * Кнопка возврата к списку дисциплин.
     */
    @FXML
    private Button backButton;

    /**
     * Текущая группа, для которой отображаются оценки.
     */
    private Group currentGroup;

    /**
     * Текущий предмет, по которому отображаются оценки.
     */
    private Subject currentSubject;

    /**
     * Список занятий, связанных с текущим предметом.
     */
    private List<Lesson> lessons;

    /**
     * Внутренний класс, представляющий строку таблицы с данными студента и его оценками.
     */
    public static class StudentGradeRow {

        private final Student student;
        private final Map<Integer, GradeValue> grades;

        /**
         * Создаёт строку таблицы для указанного студента.
         *
         * @param student студент, которому соответствует строка
         */
        public StudentGradeRow(Student student) {
            this.student = student;
            this.grades = new HashMap<>();
        }

        /**
         * Возвращает объект студента.
         *
         * @return студент
         */
        public Student getStudent() {
            return student;
        }

        /**
         * Возвращает карту оценок по идентификаторам занятий.
         *
         * @return карта оценок
         */
        public Map<Integer, GradeValue> getGrades() {
            return grades;
        }

        /**
         * Возвращает ФИО студента.
         *
         * @return полное имя студента
         */
        public String getFio() {
            return student.getFullName();
        }

        /**
         * Рассчитывает и возвращает процент посещаемости студента.
         * Учитываются только пропуски без уважительной причины ("Н").
         *
         * @return строка с процентом посещаемости или пустая строка, если данных нет
         */
        public String getAttendance() {
            int attended = 0;
            int missed = 0;

            for (GradeValue value : grades.values()) {
                if (value == GradeValue.NO_DATA) {
                    continue;
                }
                if (value == GradeValue.ABSENCE_UNEXCUSED) {
                    missed++;
                } else {
                    attended++;
                }
            }

            int total = attended + missed;
            if (total == 0) {
                return "";
            }

            double percentage = (double) attended / total * 100;
            return String.format("%.1f", percentage) + "%";
        }

        /**
         * Рассчитывает и возвращает средний балл студента по числовым оценкам (2–5).
         *
         * @return строка со средним баллом (с двумя знаками после запятой) или пустая строка, если оценок нет
         */
        public String getAverage() {
            List<Integer> validGrades = new ArrayList<>();
            for (GradeValue value : grades.values()) {
                if (value.isGrade()) {
                    validGrades.add(value.getCode());
                }
            }

            if (validGrades.isEmpty()) {
                return "";
            }

            double sum = validGrades.stream().mapToInt(Integer::intValue).sum();
            double average = sum / validGrades.size();
            return String.format("%.2f", average);
        }

        /**
         * Возвращает значение оценки для указанного занятия.
         *
         * @param lessonId идентификатор занятия
         * @return значение оценки или {@link GradeValue#NO_DATA}, если не задано
         */
        public GradeValue getGradeForLesson(int lessonId) {
            return grades.getOrDefault(lessonId, GradeValue.NO_DATA);
        }

        /**
         * Устанавливает значение оценки для указанного занятия.
         *
         * @param lessonId идентификатор занятия
         * @param value значение оценки
         */
        public void setGradeForLesson(int lessonId, GradeValue value) {
            grades.put(lessonId, value);
        }
    }

    /**
     * Устанавливает контекст (группу и предмет) для отображения данных.
     *
     * @param group группа
     * @param subject предмет
     */
    public void setContext(Group group, Subject subject) {
        this.currentGroup = group;
        this.currentSubject = subject;
        if (group != null && subject != null) {
            mainLabel.setText("Оценки | Группа: " + group.getName() + " | Предмет: " + subject.getName());
            loadDataAndInitializeTable();
        }
    }

    /**
     * Рассчитывает и отображает сводные метрики по группе: средний балл и общую посещаемость.
     *
     * @param rows список строк таблицы с данными студентов
     */
    private void calculateGroupMetrics(List<StudentGradeRow> rows) {
        int totalGradeSum = 0;
        int totalGradeCount = 0;
        int totalAttended = 0;
        int totalMissed = 0;

        for (StudentGradeRow row : rows) {
            for (GradeValue value : row.getGrades().values()) {
                if (value.isGrade()) {
                    totalGradeSum += value.getCode();
                    totalGradeCount++;
                }

                if (value == GradeValue.NO_DATA) {
                    continue;
                }
                if (value == GradeValue.ABSENCE_UNEXCUSED) {
                    totalMissed++;
                } else {
                    totalAttended++;
                }
            }
        }

        if (totalGradeCount > 0) {
            double avg = (double) totalGradeSum / totalGradeCount;
            midGrade.setText(String.format("Средний балл группы: %.2f", avg));
        } else {
            midGrade.setText("Средний балл группы: —");
        }

        int totalRelevant = totalAttended + totalMissed;
        if (totalRelevant > 0) {
            double attendance = (double) totalAttended / totalRelevant * 100;
            groupAttendance.setText(String.format("Посещаемость группы: %.1f%%", attendance));
        } else {
            groupAttendance.setText("Посещаемость группы: —");
        }
    }

    /**
     * Обработчик нажатия кнопки "Назад".
     * Возвращает пользователя к списку дисциплин для текущей группы.
     */
    @FXML
    private void back() {
        if (currentGroup == null) {
            ((Stage) backButton.getScene().getWindow()).close();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SubjectList.fxml"));
            Parent root = loader.load();

            SubjectListController controller = loader.getController();
            controller.setGroup(currentGroup);

            Scene scene = new Scene(root, 600, 800);
            Stage newStage = new Stage();
            newStage.setScene(scene);
            newStage.setTitle("Список дисциплин | " + currentGroup.getName());
            newStage.show();

            ((Stage) backButton.getScene().getWindow()).close();
        } catch (IOException e) {
            logger.error("Ошибка при возврате к списку дисциплин: {}", e.getMessage(), e);
        }
    }

    /**
     * Загружает данные из базы и инициализирует таблицу оценок.
     */
    private void loadDataAndInitializeTable() {
        table.getColumns().clear();
        table.getItems().clear();

        StudentDao studentDao = new StudentDao();
        LessonDao lessonDao = new LessonDao();
        GradeDao gradeDao = new GradeDao();

        List<Student> students = studentDao.findByGroupId(currentGroup.getId());
        lessons = lessonDao.findBySubjectId(currentSubject.getId());

        Map<String, Grade> gradeCache = new HashMap<>();
        for (Student student : students) {
            List<Grade> grades = gradeDao.findByStudentId(student.getId());
            for (Grade grade : grades) {
                gradeCache.put(student.getId() + "_" + grade.getLessonId(), grade);
            }
        }

        TableColumn<StudentGradeRow, String> fioCol = new TableColumn<>("ФИО студента");
        fioCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFio()));
        fioCol.setMinWidth(200);

        TableColumn<StudentGradeRow, String> attendanceCol = new TableColumn<>("Посещаемость, %");
        attendanceCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getAttendance()));
        attendanceCol.setMinWidth(120);

        TableColumn<StudentGradeRow, String> averageCol = new TableColumn<>("Средний балл");
        averageCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getAverage()));
        averageCol.setMinWidth(120);

        table.getColumns().addAll(fioCol, attendanceCol, averageCol);

        for (int i = 0; i < lessons.size(); i++) {
            Lesson lesson = lessons.get(i);

            Label headerLabel = new Label("Занятие " + (i + 1));
            headerLabel.setTooltip(new Tooltip(lesson.getName()));
            headerLabel.setStyle("-fx-font-weight: bold;");

            TableColumn<StudentGradeRow, GradeValue> lessonCol = new TableColumn<>();
            lessonCol.setGraphic(headerLabel);
            lessonCol.setMinWidth(80);

            lessonCol.setCellValueFactory(cell -> {
                GradeValue value = cell.getValue().getGradeForLesson(lesson.getId());
                return new SimpleObjectProperty<>(value);
            });

            lessonCol.setCellFactory(col -> new TableCell<>() {
                private final ComboBox<GradeValue> comboBox = new ComboBox<>();

                {
                    comboBox.getItems().addAll(GradeValue.values());
                    comboBox.setOnAction(e -> {
                        StudentGradeRow row = getTableView().getItems().get(getIndex());
                        if (row != null) {
                            GradeValue selected = comboBox.getValue();
                            row.setGradeForLesson(lesson.getId(), selected);
                            saveGradeToDatabase(row.getStudent().getId(), lesson.getId(), selected);
                        }
                    });
                }

                @Override
                protected void updateItem(GradeValue item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        comboBox.setValue(item);
                        setGraphic(comboBox);
                    }
                }
            });

            table.getColumns().add(lessonCol);
        }

        ObservableList<StudentGradeRow> rows = FXCollections.observableArrayList();
        for (Student student : students) {
            StudentGradeRow row = new StudentGradeRow(student);
            for (Lesson lesson : lessons) {
                String key = student.getId() + "_" + lesson.getId();
                Grade grade = gradeCache.get(key);
                GradeValue value = (grade != null) ? grade.getGradeValue() : GradeValue.NO_DATA;
                row.setGradeForLesson(lesson.getId(), value);
            }
            rows.add(row);
        }

        rows.sort((r1, r2) -> r1.getFio().compareToIgnoreCase(r2.getFio()));
        table.setItems(rows);

        calculateGroupMetrics(rows);
    }

    /**
     * Сохраняет значение оценки в базу данных.
     *
     * @param studentId идентификатор студента
     * @param lessonId идентификатор занятия
     * @param value новое значение оценки
     */
    private void saveGradeToDatabase(int studentId, int lessonId, GradeValue value) {
        GradeDao gradeDao = new GradeDao();

        Grade existing = gradeDao.findByStudentAndLesson(studentId, lessonId);
        if (existing != null) {
            existing.setGradeValue(value);
            gradeDao.update(existing);
        } else {
            Grade newGrade = new Grade(studentId, lessonId, value);
            gradeDao.insert(newGrade);
        }
        logger.info("Сохранена оценка: студент={}, занятие={}, значение={}", studentId, lessonId, value);
    }
}