package org.example.dataclasses;

public class Student {
    private int id;
    private String fullName; // ФИО
    private int groupId;     // ID группы

    // Конструктор по умолчанию
    public Student() {}

    // Конструктор без id
    public Student(String fullName, int groupId) {
        this.fullName = fullName;
        this.groupId = groupId;
    }

    // Полный конструктор
    public Student(int id, String fullName, int groupId) {
        this.id = id;
        this.fullName = fullName;
        this.groupId = groupId;
    }

    // Геттеры и сеттеры
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    @Override
    public String toString() {
        return "Student{id=" + id + ", fullName='" + fullName + "', groupId=" + groupId + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return id == student.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}