package com.estudy.web.controller;

import com.estudy.annotation.GlobalInterceptor;
import com.estudy.component.RedisComponent;
import com.estudy.entity.enums.ResultCode;
import com.estudy.entity.vo.Result;
import com.estudy.entity.vo.TaskResult;
import com.estudy.exception.BusinessException;
import com.estudy.service.CozeService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/agent")
public class AgentController {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private CozeService cozeService;

    /**
     * 上传图片并创建任务
     */
    @RequestMapping("/uploadAndParse")
    @GlobalInterceptor
    public Result<String> uploadAndParseWithTask(@RequestHeader(value = "token") String token,
                                                 @NotNull MultipartFile file) {
        // 文件验证
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        if (!isImageFile(file)) {
            throw new BusinessException("只支持图片文件");
        }

        // 在异步处理之前读取文件内容到字节数组
        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            throw new BusinessException("读取文件失败: " + e.getMessage());
        }

        String originalFilename = file.getOriginalFilename();

        // 生成任务ID
        String taskId = UUID.randomUUID().toString();

        // 初始化任务状态
        redisComponent.saveTask(taskId, new TaskResult(false, null, "处理中..."));

        String userId = redisComponent.getUserToken(token).getUserId();

        // 异步处理
        cozeService.uploadAndParseImage(userId, fileBytes, originalFilename)
                .thenAccept(output -> {
                    // 任务成功完成
                    redisComponent.saveTask(taskId, new TaskResult(true, output, null));
                })
                .exceptionally(throwable -> {
                    // 任务失败
                    redisComponent.deleteTask(taskId);
                    return null;
                });

        return Result.success(taskId);
    }

    /**
     * 查询任务结果
     */
    @GetMapping("/task/{taskId}")
    @GlobalInterceptor
    public Result<TaskResult> getTaskResult(@PathVariable String taskId) {
        // 参数验证
        if (taskId == null || taskId.trim().isEmpty()) {
            throw new BusinessException(ResultCode.CODE_400);
        }

        if (!isValidTaskId(taskId)) {
            throw new BusinessException(ResultCode.CODE_400);
        }

        TaskResult result = redisComponent.getTask(taskId);
        if (result == null) {
            return Result.success(null);
        }

        return Result.success(result);
    }

    /**
     * 验证任务ID格式
     */
    private boolean isValidTaskId(String taskId) {
        // UUID格式验证
        return taskId.matches("^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$");
    }

    /**
     * 验证是否为图片文件
     */
    private boolean isImageFile(MultipartFile file) {
        if (file.getContentType() == null) {
            return false;
        }
        return file.getContentType().startsWith("image/");
    }

}
