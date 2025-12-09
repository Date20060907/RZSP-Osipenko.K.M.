package org.example.dataclasses;

/**
 * Представляет занятие (урок), связанное с определённым предметом.
 * Используется как объект передачи данных между слоями приложения.
 */
public class Lesson {

    /**
     * Уникальный идентификатор занятия в базе данных.
     */
    private int id;

    /**
     * Название занятия (например, "Лекция 1", "Практика по SQL").
     */
    private String name;

    /**
     * Идентификатор связанного предмета.
     */
    private int subjectId;

    /**
     * Создаёт пустой объект занятия.
     * Необходим для совместимости с фреймворками, требующими конструктор без параметров.
     */
    public Lesson() {}

    /**
     * Создаёт объект занятия без указания идентификатора.
     *
     * @param name название занятия
     * @param subjectId идентификатор связанного предмета
     */
    public Lesson(String name, int subjectId) {
        this.name = name;
        this.subjectId = subjectId;
    }

    /**
     * Создаёт полный объект занятия с заданным идентификатором.
     *
     * @param id идентификатор занятия
     * @param name название занятия
     * @param subjectId идентификатор связанного предмета
     */
    public Lesson(int id, String name, int subjectId) {
        this.id = id;
        this.name = name;
        this.subjectId = subjectId;
    }

    /**
     * Возвращает идентификатор занятия.
     *
     * @return идентификатор занятия
     */
    public int getId() {
        return id;
    }

    /**
     * Устанавливает идентификатор занятия.
     *
     * @param id идентификатор занятия
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Возвращает название занятия.
     *
     * @return название занятия
     */
    public String getName() {
        return name;
    }

    /**
     * Устанавливает название занятия.
     *
     * @param name название занятия
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Возвращает идентификатор связанного предмета.
     *
     * @return идентификатор предмета
     */
    public int getSubjectId() {
        return subjectId;
    }

    /**
     * Устанавливает идентификатор связанного предмета.
     *
     * @param subjectId идентификатор предмета
     */
    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    /**
     * Возвращает строковое представление объекта занятия.
     *
     * @return строка в формате "Lesson{id=..., name='...', subjectId=...}"
     */
    @Override
    public String toString() {
        return "Lesson{id=" + id + ", name='" + name + "', subjectId=" + subjectId + "}";
    }

    /**
     * Сравнивает данный объект с другим на равенство.
     * Два занятия считаются равными, если их идентификаторы совпадают.
     *
     * @param o объект для сравнения
     * @return {@code true}, если объекты равны, иначе {@code false}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lesson lesson = (Lesson) o;
        return id == lesson.id;
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