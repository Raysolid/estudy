package com.estudy.entity.enums;

public enum DifficultyEnum {
    EASY(1, "简单"),
    MEDIUM(2, "中等"),
    HARD(3, "困难");

    private final Integer type;
    private final String name;

    DifficultyEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    public static DifficultyEnum getDifficultByType(Integer type) {
        for (DifficultyEnum difficulty : DifficultyEnum.values()) {
            if (difficulty.type.equals(type)) {
                return difficulty;
            }
        }
        return null;
    }

}
