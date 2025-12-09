package org.example.dataclasses;

public class Lesson {
    private int id;
    private String name;      // Название занятия (например, "Лекция 1", "Практика по SQL")
    private int subjectId;    // ID связанного предмета

    // Конструктор по умолчанию
    public Lesson() {}

    // Конструктор без id
    public Lesson(String name, int subjectId) {
        this.name = name;
        this.subjectId = subjectId;
    }

    // Полный конструктор
    public Lesson(int id, String name, int subjectId) {
        this.id = id;
        this.name = name;
        this.subjectId = subjectId;
    }

    // Геттеры и сеттеры
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    @Override
    public String toString() {
        return "Lesson{id=" + id + ", name='" + name + "', subjectId=" + subjectId + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lesson lesson = (Lesson) o;
        return id == lesson.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}