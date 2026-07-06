package com.hardrockunion.platform.file.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.file.dto.FileUploadResponse;
import com.hardrockunion.platform.file.service.FileUploadService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "平台-文件", description = "跨 app、跨租户复用的文件上传能力。")
@RestController
@RequestMapping("/api/{appCode}/files")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @Operation(summary = "上传图片", description = "上传 jpg、png、webp、gif 图片，当前存储后端为阿里 OSS。")
    @PostMapping("/images")
    public Result<FileUploadResponse> uploadImage(@PathVariable("appCode") String appCode,
                                                  @RequestParam("file") MultipartFile file,
                                                  LoginUser loginUser) {
        return Result.success(fileUploadService.uploadImage(appCode, file, loginUser));
    }
}
