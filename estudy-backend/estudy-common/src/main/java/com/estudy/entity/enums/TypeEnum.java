package com.estudy.entity.enums;

public enum TypeEnum {
    SINGLE(0, "单选题", 5),
    MULTIPLE(1, "多选题", 10),
    TURE_FALSE(2, "判断题", 5);

    private final Integer type;
    private final String name;
    private final Integer score;

    TypeEnum(Integer type, String name, Integer score) {
        this.type = type;
        this.name = name;
        this.score = score;
    }

    public static TypeEnum getEnumByType(Integer type) {
        for (TypeEnum typeEnum : TypeEnum.values()) {
            if (typeEnum.type.equals(type)) {
                return typeEnum;
            }
        }
        return null;
    }

    public static Integer getScoreByType(Integer type) {
        for (TypeEnum typeEnum : TypeEnum.values()) {
            if (typeEnum.type.equals(type)) {
                return typeEnum.score;
            }
        }
        return 0;
    }

}
