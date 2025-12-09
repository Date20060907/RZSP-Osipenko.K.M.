package org.example.dataclasses;

/**
 * Представляет связь между предметом и группой.
 * Используется для хранения данных из промежуточной таблицы "subject_to_group".
 */
public class SubjectGroupLink {

    /**
     * Уникальный идентификатор записи связи в базе данных.
     */
    private int id;

    /**
     * Идентификатор связанного предмета.
     */
    private int subjectId;

    /**
     * Идентификатор связанной группы.
     */
    private int groupId;

    /**
     * Создаёт пустой объект связи.
     * Необходим для совместимости с фреймворками, требующими конструктор без параметров.
     */
    public SubjectGroupLink() {}

    /**
     * Создаёт объект связи без указания идентификатора записи.
     *
     * @param subjectId идентификатор предмета
     * @param groupId идентификатор группы
     */
    public SubjectGroupLink(int subjectId, int groupId) {
        this.subjectId = subjectId;
        this.groupId = groupId;
    }

    /**
     * Создаёт полный объект связи с заданным идентификатором записи.
     *
     * @param id идентификатор записи в таблице связи
     * @param subjectId идентификатор предмета
     * @param groupId идентификатор группы
     */
    public SubjectGroupLink(int id, int subjectId, int groupId) {
        this.id = id;
        this.subjectId = subjectId;
        this.groupId = groupId;
    }

    /**
     * Возвращает идентификатор записи связи.
     *
     * @return идентификатор записи
     */
    public int getId() {
        return id;
    }

    /**
     * Устанавливает идентификатор записи связи.
     *
     * @param id идентификатор записи
     */
    public void setId(int id) {
        this.id = id;
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
     * Возвращает идентификатор связанной группы.
     *
     * @return идентификатор группы
     */
    public int getGroupId() {
        return groupId;
    }

    /**
     * Устанавливает идентификатор связанной группы.
     *
     * @param groupId идентификатор группы
     */
    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    /**
     * Возвращает строковое представление объекта связи.
     *
     * @return строка в формате "SubjectGroupLink{id=..., subjectId=..., groupId=...}"
     */
    @Override
    public String toString() {
        return "SubjectGroupLink{id=" + id + ", subjectId=" + subjectId + ", groupId=" + groupId + "}";
    }

    /**
     * Сравнивает данный объект с другим на равенство.
     * Две связи считаются равными, если совпадают их subjectId и groupId.
     *
     * @param o объект для сравнения
     * @return {@code true}, если объекты равны, иначе {@code false}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubjectGroupLink that = (SubjectGroupLink) o;
        return subjectId == that.subjectId && groupId == that.groupId;
    }

    /**
     * Возвращает хеш-код объекта, основанный на идентификаторах предмета и группы.
     *
     * @return хеш-код
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(subjectId, groupId);
    }
}