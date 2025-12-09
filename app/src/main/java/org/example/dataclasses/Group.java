package org.example.dataclasses;

/**
 * Представляет учебную группу.
 * Используется как объект передачи данных (POJO) для взаимодействия с базой данных и интерфейсом.
 */
public class Group {

    /**
     * Уникальный идентификатор группы в базе данных.
     */
    private int id;

    /**
     * Название группы (например, "ИС-201").
     */
    private String name;

    /**
     * Создаёт пустой объект группы.
     * Необходим для корректной работы некоторых фреймворков (например, JavaFX, Jackson, JPA-подобных решений).
     */
    public Group() {}

    /**
     * Создаёт объект группы без указания идентификатора.
     *
     * @param name название группы
     */
    public Group(String name) {
        this.name = name;
    }

    /**
     * Создаёт объект группы с заданным идентификатором и названием.
     * Обычно используется при загрузке данных из базы данных.
     *
     * @param id идентификатор группы
     * @param name название группы
     */
    public Group(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Возвращает идентификатор группы.
     *
     * @return идентификатор группы
     */
    public int getId() {
        return id;
    }

    /**
     * Устанавливает идентификатор группы.
     *
     * @param id идентификатор группы
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Возвращает название группы.
     *
     * @return название группы
     */
    public String getName() {
        return name;
    }

    /**
     * Устанавливает название группы.
     *
     * @param name название группы
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Возвращает строковое представление объекта группы.
     *
     * @return строка в формате "Group{id=..., name='...'}"
     */
    @Override
    public String toString() {
        return "Group{id=" + id + ", name='" + name + "'}";
    }
}