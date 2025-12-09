package org.example.dataclasses;

public class Subject {
    private int id;
    private String name;

    // Конструктор по умолчанию
    public Subject() {}

    // Конструктор с именем
    public Subject(String name) {
        this.name = name;
    }

    // Конструктор с id и именем
    public Subject(int id, String name) {
        this.id = id;
        this.name = name;
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

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subject subject = (Subject) o;
        return id == subject.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}