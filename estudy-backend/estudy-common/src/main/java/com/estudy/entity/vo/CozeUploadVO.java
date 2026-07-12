package com.estudy.entity.vo;

import lombok.Data;

@Data
public class CozeUploadVO {
    private Integer code;
    private UploadData data;
    private String msg;

    @Data
    public static class UploadData {
        private Long bytes;
        private Long createdAt;
        private String fileName;
        private String id;
    }

}
