package com.estudy.service.impl;

import com.estudy.component.RedisComponent;
import com.estudy.config.AppConfig;
import com.estudy.entity.dto.SettingDto;
import com.estudy.entity.po.User;
import com.estudy.entity.query.QueryParams;
import com.estudy.entity.vo.CozeUploadVO;
import com.estudy.entity.vo.CozeWorkflowVO;
import com.estudy.exception.BusinessException;
import com.estudy.mappers.UserMapper;
import com.estudy.service.CozeService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class CozeServiceImpl implements CozeService {

    @Resource
    private AppConfig appConfig;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private RestTemplate restTemplate;

    private SettingDto settingDto;

    @Resource
    private UserMapper<User, QueryParams> userMapper;

    /**
     * 异步上传并解析图片
     */
    @Async("cozeTaskExecutor")
    public CompletableFuture<String> uploadAndParseImage(String userId, byte[] fileBytes, String fileName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("开始处理图片: {} ({} bytes)", fileName, fileBytes.length);

                settingDto = redisComponent.getSetting();

                // 1. 上传文件到Coze
                String fileId = uploadFileToCoze(fileBytes, fileName);
                log.info("文件上传成功, fileId: {}", fileId);

                // 2. 调用工作流解析文件
                String output = parseFileWithWorkflow(fileId);
                log.info("文件解析成功, 输出长度: {}", output.length());

                // 扣减用户积分
                userMapper.updateUserPoints(userId, -50);

                return output;

            } catch (Exception e) {
                log.error("图片处理失败: {}", e.getMessage(), e);
                throw new BusinessException("图片处理失败: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Coze上传文件
     */
    private String uploadFileToCoze(byte[] fileBytes, String fileName){
        String formattedToken = formatToken(settingDto.getCozeToken());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", "Bearer " + formattedToken);
        headers.set("User-Agent", "MyApp/1.0");

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return fileName;
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);

        log.info("开始上传文件到Coze: {} ({} bytes)", fileName, fileBytes.length);

        try {
            ResponseEntity<CozeUploadVO> response = restTemplate.exchange(
                    appConfig.getUploadUrl(),
                    HttpMethod.POST,
                    requestEntity,
                    CozeUploadVO.class
            );

            return handleUploadResponse(response);

        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("认证失败，请检查Token配置");
            throw new BusinessException("Coze API认证失败: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            log.error("网络连接异常: {}", e.getMessage());
            throw new BusinessException("网络连接异常，请稍后重试", e);
        } catch (Exception e) {
            log.error("文件上传异常: {}", e.getMessage());
            throw new BusinessException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 格式化 Token（移除多余的 Bearer 前缀等）
     */
    private String formatToken(String rawToken) {
        if (rawToken == null) return null;

        String token = rawToken.trim();

        // 如果已经包含 Bearer 前缀，移除它
        if (token.toLowerCase().startsWith("bearer ")) {
            token = token.substring(7).trim();
        }

        return token;
    }

    private String handleUploadResponse(ResponseEntity<CozeUploadVO> response) {
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            CozeUploadVO uploadResponse = response.getBody();
            if (uploadResponse.getCode() == 0) {
                return uploadResponse.getData().getId();
            } else {
                throw new BusinessException("Coze上传API返回错误: " + uploadResponse.getMsg());
            }
        } else {
            throw new BusinessException("上传失败: " + response.getStatusCode());
        }
    }

    /**
     * 解析文件工作流
     */
    private String parseFileWithWorkflow(String fileId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + settingDto.getCozeToken());

        // 构建请求参数
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("input", Collections.singletonList("{\"file_id\":\"" + fileId + "\"}"));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("workflow_id", settingDto.getCozeWorkflowId());
        requestBody.put("parameters", parameters);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        log.info("调用Coze工作流并传入fileId: {}", fileId);

        try {
            ResponseEntity<CozeWorkflowVO> response = restTemplate.exchange(
                    appConfig.getWorkflowUrl(),
                    HttpMethod.POST,
                    requestEntity,
                    CozeWorkflowVO.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                CozeWorkflowVO workflowResponse = response.getBody();
                if (workflowResponse.getCode() == 0) {
                    log.info("工作流执行成功");
                    return extractOutputFromResponse(workflowResponse.getData());
                } else {
                    throw new BusinessException("Coze工作流API错误: " + workflowResponse.getMsg());
                }
            } else {
                throw new BusinessException("工作流请求失败: " + response.getStatusCode());
            }

        } catch (ResourceAccessException e) {
            if (e.getMessage().contains("Read timed out")) {
                throw new BusinessException("工作流请求超时 - 服务响应时间过长");
            }
            throw new BusinessException("工作流请求失败: " + e.getMessage(), e);
        } catch (HttpClientErrorException e) {
            log.error("工作流API错误: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException("工作流API错误: " + e.getStatusCode());
        }
    }

    /**
     * 从响应数据中提取output
     */
    private String extractOutputFromResponse(String responseData) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(responseData);
            return rootNode.path("output").asText();
        } catch (Exception e) {
            log.error("解析工作流响应失败: {}", responseData, e);
            throw new BusinessException("解析工作流输出失败");
        }
    }
}
