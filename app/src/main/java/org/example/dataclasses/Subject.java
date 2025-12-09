package org.example.dataclasses;

/**
 * Представляет учебный предмет.
 * Используется как объект передачи данных между слоями приложения.
 */
public class Subject {

    /**
     * Уникальный идентификатор предмета в базе данных.
     */
    private int id;

    /**
     * Название предмета (например, "Математика", "Программирование").
     */
    private String name;

    /**
     * Создаёт пустой объект предмета.
     * Необходим для совместимости с фреймворками, требующими конструктор без параметров.
     */
    public Subject() {}

    /**
     * Создаёт объект предмета без указания идентификатора.
     *
     * @param name название предмета
     */
    public Subject(String name) {
        this.name = name;
    }

    /**
     * Создаёт полный объект предмета с заданным идентификатором.
     *
     * @param id идентификатор предмета
     * @param name название предмета
     */
    public Subject(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Возвращает идентификатор предмета.
     *
     * @return идентификатор предмета
     */
    public int getId() {
        return id;
    }

    /**
     * Устанавливает идентификатор предмета.
     *
     * @param id идентификатор предмета
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Возвращает название предмета.
     *
     * @return название предмета
     */
    public String getName() {
        return name;
    }

    /**
     * Устанавливает название предмета.
     *
     * @param name название предмета
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Возвращает строковое представление предмета.
     * По умолчанию возвращается только название.
     *
     * @return название предмета
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Сравнивает данный объект с другим на равенство.
     * Два предмета считаются равными, если их идентификаторы совпадают.
     *
     * @param o объект для сравнения
     * @return {@code true}, если объекты равны, иначе {@code false}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subject subject = (Subject) o;
        return id == subject.id;
    }

    /**
     * Возвращает хеш-код объекта, основанный на его идентификаторе.
     *
     * @return хеш-код
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}