package org.example.dataclasses;

/**
 * Перечисление допустимых значений оценок и статусов посещаемости.
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

	private final int code;
	private final String displayName;

	GradeValue(int code, String displayName) {
		this.code = code;
		this.displayName = displayName;
	}

	public int getCode() {
		return code;
	}

	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Получить краткое текстовое представление для отображения в таблице.
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
	 * Получить GradeValue по числовому коду.
	 * 
	 * @throws IllegalArgumentException если код недопустим
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
	 * Проверить, является ли значение оценкой (2–5).
	 */
	public boolean isGrade() {
		return this == GRADE_2 || this == GRADE_3 || this == GRADE_4 || this == GRADE_5;
	}

	/**
	 * Проверить, является ли значение пропуском.
	 */
	public boolean isAbsence() {
		return this == ABSENCE_UNEXCUSED || this == ABSENCE_EXCUSED;
	}

	@Override
	public String toString() {
		return getShortSymbol();
	}
}