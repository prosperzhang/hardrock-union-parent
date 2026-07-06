package com.hardrockunion.platform.file.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.storage.client.ObjectStorageClient;
import com.hardrockunion.infrastructure.storage.model.StoragePutObjectRequest;
import com.hardrockunion.infrastructure.storage.model.StoragePutObjectResult;
import com.hardrockunion.platform.file.config.FileUploadProperties;
import com.hardrockunion.platform.file.dto.FileUploadResponse;

@Service
public class FileUploadService {

    private static final DateTimeFormatter DATE_PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final ObjectStorageClient objectStorageClient;
    private final FileUploadProperties fileUploadProperties;

    public FileUploadService(ObjectStorageClient objectStorageClient, FileUploadProperties fileUploadProperties) {
        this.objectStorageClient = objectStorageClient;
        this.fileUploadProperties = fileUploadProperties;
    }

    public FileUploadResponse uploadImage(String appCode, MultipartFile file, LoginUser loginUser) {
        validateLogin(appCode, loginUser);
        validateImage(file);
        String objectKey = buildObjectKey(appCode, loginUser, file);
        StoragePutObjectRequest request = new StoragePutObjectRequest();
        request.setObjectKey(objectKey);
        request.setContentType(file.getContentType());
        request.setContentLength(file.getSize());
        request.setContentDisposition("inline");
        try {
            request.setInputStream(file.getInputStream());
        } catch (IOException exception) {
            throw new BusinessException("读取上传文件失败");
        }
        StoragePutObjectResult storageResult = objectStorageClient.putObject(request);
        return toResponse(file, storageResult);
    }

    private void validateLogin(String appCode, LoginUser loginUser) {
        if (loginUser == null || loginUser.getUserId() == null || loginUser.getTenantId() == null) {
            throw new BusinessException("请先登录");
        }
        if (!StringUtils.equalsIgnoreCase(StringUtils.trimToEmpty(appCode), loginUser.getAppCode())) {
            throw new BusinessException("登录应用与上传应用不一致");
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传图片不能为空");
        }
        if (fileUploadProperties.getMaxSize() != null
            && fileUploadProperties.getMaxSize() > 0
            && file.getSize() > fileUploadProperties.getMaxSize()) {
            throw new BusinessException("上传图片超过大小限制");
        }
        String contentType = StringUtils.lowerCase(StringUtils.trimToEmpty(file.getContentType()), Locale.ROOT);
        boolean allowed = fileUploadProperties.getAllowedContentTypes() != null
            && fileUploadProperties.getAllowedContentTypes().stream()
                .filter(StringUtils::isNotBlank)
                .map(type -> StringUtils.lowerCase(StringUtils.trim(type), Locale.ROOT))
                .anyMatch(type -> StringUtils.equals(type, contentType));
        if (!allowed) {
            throw new BusinessException("仅支持上传 jpg、png、webp、gif 图片");
        }
    }

    private String buildObjectKey(String appCode, LoginUser loginUser, MultipartFile file) {
        String extension = resolveExtension(file);
        String datePath = LocalDate.now().format(DATE_PATH_FORMATTER);
        return StringUtils.lowerCase(appCode, Locale.ROOT)
            + "/tenant-" + loginUser.getTenantId()
            + "/user-" + loginUser.getUserId()
            + "/images/" + datePath
            + "/" + UUID.randomUUID().toString().replace("-", "")
            + extension;
    }

    private String resolveExtension(MultipartFile file) {
        String originalFilename = StringUtils.defaultString(file.getOriginalFilename());
        String extension = StringUtils.lowerCase(StringUtils.substringAfterLast(originalFilename, "."), Locale.ROOT);
        if (StringUtils.isBlank(extension)) {
            extension = switch (StringUtils.lowerCase(StringUtils.trimToEmpty(file.getContentType()), Locale.ROOT)) {
                case "image/jpeg" -> "jpg";
                case "image/png" -> "png";
                case "image/webp" -> "webp";
                case "image/gif" -> "gif";
                default -> "";
            };
        }
        if (!StringUtils.equalsAny(extension, "jpg", "jpeg", "png", "webp", "gif")) {
            throw new BusinessException("图片文件扩展名不支持");
        }
        return "." + extension;
    }

    private FileUploadResponse toResponse(MultipartFile file, StoragePutObjectResult storageResult) {
        FileUploadResponse response = new FileUploadResponse();
        response.setProvider(storageResult.getProvider());
        response.setBucketName(storageResult.getBucketName());
        response.setObjectKey(storageResult.getObjectKey());
        response.setUrl(storageResult.getUrl());
        response.setOriginalFilename(file.getOriginalFilename());
        response.setContentType(file.getContentType());
        response.setSize(file.getSize());
        response.setEtag(storageResult.getEtag());
        return response;
    }
}
