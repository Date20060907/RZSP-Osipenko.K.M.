package org.example.gui;

import org.example.dataclasses.*;
import org.example.dao.*;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.util.*;

public class AssesmentTableController {

	@FXML
	private Label midGrade;

	@FXML
	private Label groupAttendance;

	@FXML
	private Label mainLabel;

	@FXML
	private TableView<StudentGradeRow> table;

	@FXML
	private Button backButton;

	private Group currentGroup;
	private Subject currentSubject;
	private List<Lesson> lessons;

	// Внутренний класс для строки таблицы
	public static class StudentGradeRow {
		private final Student student;
		private final Map<Integer, GradeValue> grades;

		public StudentGradeRow(Student student) {
			this.student = student;
			this.grades = new HashMap<>();
		}

		public Student getStudent() {
			return student;
		}

		public Map<Integer, GradeValue> getGrades() {
			return grades;
		}

		public String getFio() {
			return student.getFullName();
		}

		public String getAttendance() {
			int attended = 0; // Посещённые (все, кроме Н и " ")
			int missed = 0; // Пропущенные (только "Н")

			for (GradeValue value : grades.values()) {
				if (value == GradeValue.NO_DATA) {
					// Игнорируем "нет данных"
					continue;
				}
				if (value == GradeValue.ABSENCE_UNEXCUSED) {
					missed++; // "Н" — пропуск
				} else {
					attended++; // всё остальное — посещение
				}
			}

			int total = attended + missed;
			if (total == 0) {
				return ""; // нет данных для расчёта
			}

			double percentage = (double) attended / total * 100;
			return String.format("%.1f", percentage) + "%";
		}

		// === НОВЫЙ МЕТОД: расчёт среднего балла ===
		public String getAverage() {
			List<Integer> validGrades = new ArrayList<>();
			for (GradeValue value : grades.values()) {
				if (value.isGrade()) { // true только для GRADE_2, GRADE_3, GRADE_4, GRADE_5
					validGrades.add(value.getCode());
				}
			}

			if (validGrades.isEmpty()) {
				return ""; // нет оценок → пусто
			}

			double sum = validGrades.stream().mapToInt(Integer::intValue).sum();
			double average = sum / validGrades.size();

			// Округляем до 2 знаков после запятой
			return String.format("%.2f", average);
		}

		public GradeValue getGradeForLesson(int lessonId) {
			return grades.getOrDefault(lessonId, GradeValue.NO_DATA);
		}

		public void setGradeForLesson(int lessonId, GradeValue value) {
			grades.put(lessonId, value);
		}
	}

	public void setContext(Group group, Subject subject) {
		this.currentGroup = group;
		this.currentSubject = subject;
		if (group != null && subject != null) {
			mainLabel.setText("Оценки | Группа: " + group.getName() + " | Предмет: " + subject.getName());
			loadDataAndInitializeTable();
		}
	}

	private void calculateGroupMetrics(List<StudentGradeRow> rows) {
		int totalGradeSum = 0;
		int totalGradeCount = 0;
		int totalAttended = 0;
		int totalMissed = 0;

		for (StudentGradeRow row : rows) {
			for (GradeValue value : row.getGrades().values()) {
				// Средний балл (только 2-5)
				if (value.isGrade()) {
					totalGradeSum += value.getCode();
					totalGradeCount++;
				}

				// Посещаемость
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

		// Средний балл группы
		if (totalGradeCount > 0) {
			double avg = (double) totalGradeSum / totalGradeCount;
			midGrade.setText(String.format("Средний балл группы: %.2f", avg));
		} else {
			midGrade.setText("Средний балл группы: —");
		}

		// Посещаемость группы
		int totalRelevant = totalAttended + totalMissed;
		if (totalRelevant > 0) {
			double attendance = (double) totalAttended / totalRelevant * 100;
			groupAttendance.setText(String.format("Посещаемость группы: %.1f%%", attendance));
		} else {
			groupAttendance.setText("Посещаемость группы: —");
		}
	}

	@FXML
	private void back() {
		if (currentGroup == null) {
			// На всякий случай — просто закрыть
			((Stage) backButton.getScene().getWindow()).close();
			return;
		}

		try {
			// Загружаем SubjectList.fxml
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/SubjectList.fxml"));
			Parent root = loader.load();

			// Передаём группу в контроллер
			SubjectListController controller = loader.getController();
			controller.setGroup(currentGroup); // ← восстанавливаем контекст

			// Создаём новую сцену и окно
			Scene scene = new Scene(root, 600, 800);
			Stage newStage = new Stage();
			newStage.setScene(scene);
			newStage.setTitle("Список дисциплин | " + currentGroup.getName());
			newStage.show();

			// Закрываем текущее окно
			((Stage) backButton.getScene().getWindow()).close();

		} catch (IOException e) {
			System.err.println("Ошибка при возврате к списку дисциплин: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void loadDataAndInitializeTable() {
		table.getColumns().clear();
		table.getItems().clear();

		// Загружаем данные
		StudentDao studentDao = new StudentDao();
		LessonDao lessonDao = new LessonDao();
		GradeDao gradeDao = new GradeDao();

		List<Student> students = studentDao.findByGroupId(currentGroup.getId());
		lessons = lessonDao.findBySubjectId(currentSubject.getId());

		// Загружаем оценки для всех студентов и занятий
		Map<String, Grade> gradeCache = new HashMap<>(); // ключ: "studentId_lessonId"
		for (Student student : students) {
			List<Grade> grades = gradeDao.findByStudentId(student.getId());
			for (Grade grade : grades) {
				gradeCache.put(student.getId() + "_" + grade.getLessonId(), grade);
			}
		}

		// === 1. Колонка: ФИО ===
		TableColumn<StudentGradeRow, String> fioCol = new TableColumn<>("ФИО студента");
		fioCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFio()));
		fioCol.setMinWidth(200);

		// === 2. Колонка: Посещаемость ===
		TableColumn<StudentGradeRow, String> attendanceCol = new TableColumn<>("Посещаемость, %");
		attendanceCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getAttendance()));
		attendanceCol.setMinWidth(120);

		// === 3. Колонка: Средний балл ===
		TableColumn<StudentGradeRow, String> averageCol = new TableColumn<>("Средний балл");
		averageCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getAverage()));
		averageCol.setMinWidth(120);

		// Добавляем первые 3 колонки
		table.getColumns().addAll(fioCol, attendanceCol, averageCol);

		// === 4+. Колонки занятий ===
		// === Колонки занятий ===
		for (int i = 0; i < lessons.size(); i++) {
			Lesson lesson = lessons.get(i);

			// Заголовок с подсказкой
			Label headerLabel = new Label("Занятие " + (i + 1));
			headerLabel.setTooltip(new Tooltip(lesson.getName()));
			headerLabel.setStyle("-fx-font-weight: bold;"); // опционально: выделить жирным

			TableColumn<StudentGradeRow, GradeValue> lessonCol = new TableColumn<>();
			lessonCol.setGraphic(headerLabel);
			lessonCol.setMinWidth(80);

			lessonCol.setCellValueFactory(cell -> {
				GradeValue value = cell.getValue().getGradeForLesson(lesson.getId());
				return new javafx.beans.property.SimpleObjectProperty<>(value);
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
		// === Заполняем и сортируем строки ===
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

		// 🔼 Сортировка по ФИО (по возрастанию)
		rows.sort((r1, r2) -> r1.getFio().compareToIgnoreCase(r2.getFio()));

		table.setItems(rows);

		calculateGroupMetrics(rows);
	}

	private void saveGradeToDatabase(int studentId, int lessonId, GradeValue value) {
		GradeDao gradeDao = new GradeDao();

		// Проверяем, существует ли уже оценка
		Grade existing = gradeDao.findByStudentAndLesson(studentId, lessonId);
		if (existing != null) {
			// Обновляем
			existing.setGradeValue(value);
			gradeDao.update(existing);
		} else {
			// Создаём новую
			Grade newGrade = new Grade(studentId, lessonId, value);
			gradeDao.insert(newGrade);
		}
		System.out.println("✅ Сохранено: студент=" + studentId + ", занятие=" + lessonId + ", значение=" + value);
	}
}