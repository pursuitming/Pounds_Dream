package com.poundsdream.controller;

import com.poundsdream.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Tag(name = "文件管理")
@RestController
@RequestMapping("/api/file")
public class FileController {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_TYPES = {
            "image/jpeg", "image/png", "image/gif", "image/webp"
    };

    @Operation(summary = "上传图片")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "请选择文件");
        }

        if (file.getSize() > MAX_SIZE) {
            return Result.error(400, "文件大小不能超过5MB");
        }

        String contentType = file.getContentType();
        boolean allowed = false;
        for (String type : ALLOWED_TYPES) {
            if (type.equals(contentType)) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            return Result.error(400, "仅支持 jpg/png/gif/webp 格式");
        }

        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf("."))
                : ".jpg";

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String newFileName = UUID.randomUUID().toString().replace("-", "") + ext;

        File destDir = new File(uploadDir, datePath).getAbsoluteFile();
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        try {
            file.transferTo(new File(destDir, newFileName));
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error(500, "文件上传失败");
        }

        String url = "/uploads/" + datePath + "/" + newFileName;
        return Result.success(url);
    }
}
