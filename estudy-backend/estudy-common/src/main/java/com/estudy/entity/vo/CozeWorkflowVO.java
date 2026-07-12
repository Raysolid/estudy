package com.estudy.entity.vo;

import lombok.Data;

@Data
public class CozeWorkflowVO {
    private Integer code;
    private String data;
    private String msg;
    private String debugUrl;
    private Usage usage;

    @Data
    public static class Usage {
        private Integer inputCount;
        private Integer tokenCount;
        private Integer outputCount;
    }

}
