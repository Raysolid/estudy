package com.estudy.entity.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserDto implements Serializable {
    private static final long serialVersionUID = -3884282148585392527L;

    private String userId;
    private String nickname;
    private String avatar;
    private String email;
    private Integer points;
    private String preference;
    private String token;

}
