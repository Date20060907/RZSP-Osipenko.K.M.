package org.example.dataclasses;

/**
 * Представляет студента учебного заведения.
 * Содержит информацию об идентификаторе, ФИО и принадлежности к группе.
 */
public class Student {

    /**
     * Уникальный идентификатор студента в базе данных.
     */
    private int id;

    /**
     * Полное имя студента (ФИО).
     */
    private String fullName;

    /**
     * Идентификатор группы, к которой принадлежит студент.
     */
    private int groupId;

    /**
     * Создаёт пустой объект студента.
     * Необходим для совместимости с фреймворками, требующими конструктор без параметров (например, JavaFX, Jackson).
     */
    public Student() {}

    /**
     * Создаёт объект студента без указания идентификатора.
     *
     * @param fullName полное имя студента (ФИО)
     * @param groupId идентификатор группы
     */
    public Student(String fullName, int groupId) {
        this.fullName = fullName;
        this.groupId = groupId;
    }

    /**
     * Создаёт полный объект студента с заданным идентификатором.
     *
     * @param id идентификатор студента
     * @param fullName полное имя студента (ФИО)
     * @param groupId идентификатор группы
     */
    public Student(int id, String fullName, int groupId) {
        this.id = id;
        this.fullName = fullName;
        this.groupId = groupId;
    }

    /**
     * Возвращает идентификатор студента.
     *
     * @return идентификатор студента
     */
    public int getId() {
        return id;
    }

    /**
     * Устанавливает идентификатор студента.
     *
     * @param id идентификатор студента
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Возвращает полное имя студента (ФИО).
     *
     * @return полное имя студента
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Устанавливает полное имя студента (ФИО).
     *
     * @param fullName полное имя студента
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Возвращает идентификатор группы, к которой принадлежит студент.
     *
     * @return идентификатор группы
     */
    public int getGroupId() {
        return groupId;
    }

    /**
     * Устанавливает идентификатор группы студента.
     *
     * @param groupId идентификатор группы
     */
    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    /**
     * Возвращает строковое представление объекта студента.
     *
     * @return строка в формате "Student{id=..., fullName='...', groupId=...}"
     */
    @Override
    public String toString() {
        return "Student{id=" + id + ", fullName='" + fullName + "', groupId=" + groupId + "}";
    }

    /**
     * Сравнивает данный объект с другим на равенство.
     * Два студента считаются равными, если их идентификаторы совпадают.
     *
     * @param o объект для сравнения
     * @return {@code true}, если объекты равны, иначе {@code false}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return id == student.id;
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