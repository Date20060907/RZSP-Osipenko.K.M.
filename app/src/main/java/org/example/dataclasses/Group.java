package org.example.dataclasses;

// Простой POJO-класс для таблицы groups
public class Group {
    private int id;
    private String name;

    // Конструктор по умолчанию (обязателен для многих фреймворков и удобства)
    public Group() {}

    // Основной конструктор
    public Group(String name) {
        this.name = name;
    }

    // Конструктор с id (для загрузки из БД)
    public Group(int id, String name) {
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
        return "Group{id=" + id + ", name='" + name + "'}";
    }
}