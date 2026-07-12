package com.estudy.service;

import java.util.concurrent.CompletableFuture;

public interface CozeService {

    /**
     * 异步上传并解析文件
     */
    CompletableFuture<String> uploadAndParseImage(String userId, byte[] fileBytes, String fileName);

}
