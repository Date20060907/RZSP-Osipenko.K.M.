package org.example.dataclasses;

/**
 * Перечисление, представляющее допустимые значения оценок и статусов посещаемости.
 * Каждое значение имеет числовой код и отображаемое имя.
 */
public enum GradeValue {

    NO_DATA(0, "Нет данных"),
    ABSENCE_UNEXCUSED(1, "Пропуск (неуваж.)"),
    GRADE_2(2, "2"),
    GRADE_3(3, "3"),
    GRADE_4(4, "4"),
    GRADE_5(5, "5"),
    ABSENCE_EXCUSED(6, "Пропуск (уваж.)"),
    PRESENT(7, "Присутствовал");

    /**
     * Числовой код значения, используемый для хранения в базе данных.
     */
    private final int code;

    /**
     * Отображаемое имя значения, предназначенное для показа пользователю.
     */
    private final String displayName;

    /**
     * Создаёт экземпляр перечисления с заданным кодом и отображаемым именем.
     *
     * @param code числовой код значения
     * @param displayName отображаемое имя
     */
    GradeValue(int code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    /**
     * Возвращает числовой код значения.
     *
     * @return числовой код
     */
    public int getCode() {
        return code;
    }

    /**
     * Возвращает отображаемое имя значения.
     *
     * @return отображаемое имя
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Возвращает краткое текстовое представление значения для отображения в таблицах или компактных интерфейсах.
     *
     * @return краткий символ (например, "2", "+", "Н", "У" и т.д.)
     */
    public String getShortSymbol() {
        switch (this) {
            case NO_DATA:
                return " ";
            case ABSENCE_UNEXCUSED:
                return "Н";
            case GRADE_2:
                return "2";
            case GRADE_3:
                return "3";
            case GRADE_4:
                return "4";
            case GRADE_5:
                return "5";
            case ABSENCE_EXCUSED:
                return "У";
            case PRESENT:
                return "+";
            default:
                return "?";
        }
    }

    /**
     * Возвращает соответствующий элемент перечисления по заданному числовому коду.
     *
     * @param code числовой код
     * @return соответствующее значение {@link GradeValue}
     * @throws IllegalArgumentException если код не соответствует ни одному значению
     */
    public static GradeValue fromCode(int code) {
        for (GradeValue value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Недопустимый код оценки: " + code);
    }

    /**
     * Проверяет, является ли текущее значение оценкой (2, 3, 4 или 5).
     *
     * @return {@code true}, если значение — оценка, иначе {@code false}
     */
    public boolean isGrade() {
        return this == GRADE_2 || this == GRADE_3 || this == GRADE_4 || this == GRADE_5;
    }

    /**
     * Проверяет, является ли текущее значение пропуском (уважительным или неуважительным).
     *
     * @return {@code true}, если значение — пропуск, иначе {@code false}
     */
    public boolean isAbsence() {
        return this == ABSENCE_UNEXCUSED || this == ABSENCE_EXCUSED;
    }

    /**
     * Возвращает краткое текстовое представление значения (см. {@link #getShortSymbol()}).
     *
     * @return краткий символ
     */
    @Override
    public String toString() {
        return getShortSymbol();
    }
}