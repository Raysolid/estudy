package com.estudy.component;

import com.estudy.config.AppConfig;
import com.estudy.entity.constants.Constants;
import com.estudy.entity.enums.ResultCode;
import com.estudy.exception.BusinessException;
import com.estudy.utils.StrUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Component
@Slf4j
public class FileComponent {

    @Resource
    private AppConfig appConfig;

    public void saveAvatar(MultipartFile avatarFile, String fileName) throws IOException {
        if (avatarFile != null) {
            String baseFolder = appConfig.getProjectFolder() + Constants.FILE_FOLDER;
            File targetFileFolder = new File(baseFolder + Constants.AVATAR_FOLDER);
            if (!targetFileFolder.exists()) {
                targetFileFolder.mkdirs();
            }
            String filePath = targetFileFolder.getPath() + "/" + fileName + Constants.IMAGE_SUFFIX;
            avatarFile.transferTo(new File(filePath));
        }
    }

    public void getResource(HttpServletResponse response, String sourceName) {
        if (sourceName.contains("../") || sourceName.contains("..\\")) {
            throw new BusinessException(ResultCode.CODE_400);
        }
        String suffix = StrUtils.getFileSuffix(sourceName);
        response.setContentType("image/" + suffix.replace(".", ""));
        response.setHeader("Cache-Control", "max-age=2592000");
        readFile(response, sourceName);
    }

    /**
     * 读取文件
     */
    protected void readFile(HttpServletResponse response, String filePath) {
        File file = new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER + filePath);
        if (!file.exists()) {
            return;
        }
        try (OutputStream out = response.getOutputStream();
             FileInputStream in = new FileInputStream(file)) {
            byte[] byteData = new byte[1024];
            int len = 0;
            while ((len = in.read(byteData)) != -1) {
                out.write(byteData, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            log.error("读取文件异常", e);
        }
    }

}
