package com.estudy.entity.vo;

import lombok.Data;

@Data
public class TaskResult {
    private boolean completed;
    private String result;
    private String error;

    public TaskResult() {
    }

    public TaskResult(boolean completed, String result, String error) {
        this.completed = completed;
        this.result = result;
        this.error = error;
    }
}
