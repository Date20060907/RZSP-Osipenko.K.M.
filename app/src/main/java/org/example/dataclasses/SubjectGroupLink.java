package org.example.dataclasses;

public class SubjectGroupLink {
    private int id;
    private int subjectId;
    private int groupId;

    // Конструктор по умолчанию
    public SubjectGroupLink() {}

    // Основной конструктор
    public SubjectGroupLink(int subjectId, int groupId) {
        this.subjectId = subjectId;
        this.groupId = groupId;
    }

    // Полный конструктор (с id)
    public SubjectGroupLink(int id, int subjectId, int groupId) {
        this.id = id;
        this.subjectId = subjectId;
        this.groupId = groupId;
    }

    // Геттеры и сеттеры
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    @Override
    public String toString() {
        return "SubjectGroupLink{id=" + id + ", subjectId=" + subjectId + ", groupId=" + groupId + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubjectGroupLink that = (SubjectGroupLink) o;
        return subjectId == that.subjectId && groupId == that.groupId;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(subjectId, groupId);
    }
}