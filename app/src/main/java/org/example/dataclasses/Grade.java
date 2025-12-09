package org.example.dataclasses;

import java.time.LocalDate;

public class Grade {
    private int id;
    private int studentId;
    private int lessonId;
    private GradeValue gradeValue;       // ← Используем enum вместо int
    private LocalDate dateRecorded;

    public Grade() {}

    // Конструктор без id
    public Grade(int studentId, int lessonId, GradeValue gradeValue) {
        this.studentId = studentId;
        this.lessonId = lessonId;
        this.gradeValue = gradeValue;
        this.dateRecorded = LocalDate.now();
    }

    // Полный конструктор
    public Grade(int id, int studentId, int lessonId, GradeValue gradeValue, LocalDate dateRecorded) {
        this.id = id;
        this.studentId = studentId;
        this.lessonId = lessonId;
        this.gradeValue = gradeValue;
        this.dateRecorded = dateRecorded;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getLessonId() { return lessonId; }
    public void setLessonId(int lessonId) { this.lessonId = lessonId; }

    public GradeValue getGradeValue() { return gradeValue; }
    public void setGradeValue(GradeValue gradeValue) {
        if (gradeValue == null) {
            throw new IllegalArgumentException("Значение оценки не может быть null");
        }
        this.gradeValue = gradeValue;
    }

    public LocalDate getDateRecorded() { return dateRecorded; }
    public void setDateRecorded(LocalDate dateRecorded) { this.dateRecorded = dateRecorded; }

    // Удобный метод для получения числового кода (для отображения или сохранения)
    public int getGradeCode() {
        return gradeValue.getCode();
    }

    @Override
    public String toString() {
        return "Grade{id=" + id + ", studentId=" + studentId + ", lessonId=" + lessonId +
               ", value=" + gradeValue + ", date=" + dateRecorded + "}";
    }
}