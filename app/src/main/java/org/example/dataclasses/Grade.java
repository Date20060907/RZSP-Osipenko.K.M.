package org.example.dataclasses;

import java.time.LocalDate;

/**
 * Представляет оценку студента за занятие.
 * Содержит информацию об идентификаторе, связях со студентом и занятием,
 * значении оценки и дате её выставления.
 */
public class Grade {

    /**
     * Уникальный идентификатор оценки в базе данных.
     */
    private int id;

    /**
     * Идентификатор студента, получившего оценку.
     */
    private int studentId;

    /**
     * Идентификатор занятия, за которое выставлена оценка.
     */
    private int lessonId;

    /**
     * Значение оценки, представленное в виде перечисления {@link GradeValue}.
     */
    private GradeValue gradeValue;

    /**
     * Дата, когда оценка была записана.
     */
    private LocalDate dateRecorded;

    /**
     * Создаёт пустой объект оценки.
     * Используется, например, для десериализации или временного создания.
     */
    public Grade() {}

    /**
     * Создаёт объект оценки без указания идентификатора.
     * Дата записи устанавливается автоматически как текущая дата.
     *
     * @param studentId идентификатор студента
     * @param lessonId идентификатор занятия
     * @param gradeValue значение оценки (не может быть null)
     */
    public Grade(int studentId, int lessonId, GradeValue gradeValue) {
        this.studentId = studentId;
        this.lessonId = lessonId;
        setGradeValue(gradeValue);
        this.dateRecorded = LocalDate.now();
    }

    /**
     * Создаёт полный объект оценки с заданным идентификатором и датой.
     *
     * @param id идентификатор оценки
     * @param studentId идентификатор студента
     * @param lessonId идентификатор занятия
     * @param gradeValue значение оценки (не может быть null)
     * @param dateRecorded дата выставления оценки
     */
    public Grade(int id, int studentId, int lessonId, GradeValue gradeValue, LocalDate dateRecorded) {
        this.id = id;
        this.studentId = studentId;
        this.lessonId = lessonId;
        setGradeValue(gradeValue);
        this.dateRecorded = dateRecorded;
    }

    /**
     * Возвращает идентификатор оценки.
     *
     * @return идентификатор оценки
     */
    public int getId() {
        return id;
    }

    /**
     * Устанавливает идентификатор оценки.
     *
     * @param id идентификатор оценки
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Возвращает идентификатор студента.
     *
     * @return идентификатор студента
     */
    public int getStudentId() {
        return studentId;
    }

    /**
     * Устанавливает идентификатор студента.
     *
     * @param studentId идентификатор студента
     */
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    /**
     * Возвращает идентификатор занятия.
     *
     * @return идентификатор занятия
     */
    public int getLessonId() {
        return lessonId;
    }

    /**
     * Устанавливает идентификатор занятия.
     *
     * @param lessonId идентификатор занятия
     */
    public void setLessonId(int lessonId) {
        this.lessonId = lessonId;
    }

    /**
     * Возвращает значение оценки.
     *
     * @return значение оценки в виде перечисления {@link GradeValue}
     */
    public GradeValue getGradeValue() {
        return gradeValue;
    }

    /**
     * Устанавливает значение оценки.
     * Не допускает присваивание {@code null}.
     *
     * @param gradeValue значение оценки
     * @throws IllegalArgumentException если передано {@code null}
     */
    public void setGradeValue(GradeValue gradeValue) {
        if (gradeValue == null) {
            throw new IllegalArgumentException("Значение оценки не может быть null");
        }
        this.gradeValue = gradeValue;
    }

    /**
     * Возвращает дату, когда оценка была записана.
     *
     * @return дата записи оценки
     */
    public LocalDate getDateRecorded() {
        return dateRecorded;
    }

    /**
     * Устанавливает дату записи оценки.
     *
     * @param dateRecorded дата записи оценки
     */
    public void setDateRecorded(LocalDate dateRecorded) {
        this.dateRecorded = dateRecorded;
    }

    /**
     * Возвращает числовой код значения оценки, соответствующий её перечислению.
     *
     * @return числовой код оценки
     */
    public int getGradeCode() {
        return gradeValue.getCode();
    }

    /**
     * Возвращает строковое представление объекта оценки.
     *
     * @return строка в формате "Grade{id=..., studentId=..., ...}"
     */
    @Override
    public String toString() {
        return "Grade{id=" + id + ", studentId=" + studentId + ", lessonId=" + lessonId +
               ", value=" + gradeValue + ", date=" + dateRecorded + "}";
    }
}