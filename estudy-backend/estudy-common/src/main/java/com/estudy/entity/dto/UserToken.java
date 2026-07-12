package com.estudy.entity.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserToken implements Serializable {

    private static final long serialVersionUID = 4716151372895484234L;

    private String token;
    private String userId;
    private String email;
    private String nickName;

}
