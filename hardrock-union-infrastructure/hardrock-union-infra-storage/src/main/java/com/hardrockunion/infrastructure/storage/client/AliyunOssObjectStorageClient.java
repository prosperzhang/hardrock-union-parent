package com.hardrockunion.infrastructure.storage.client;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.infrastructure.storage.config.AliyunOssProperties;
import com.hardrockunion.infrastructure.storage.model.StoragePutObjectRequest;
import com.hardrockunion.infrastructure.storage.model.StoragePutObjectResult;

@Component
public class AliyunOssObjectStorageClient implements ObjectStorageClient {

    private static final String PROVIDER = "ALIYUN_OSS";

    private final AliyunOssProperties properties;

    public AliyunOssObjectStorageClient(AliyunOssProperties properties) {
        this.properties = properties;
    }

    @Override
    public StoragePutObjectResult putObject(StoragePutObjectRequest request) {
        validateProperties();
        validateRequest(request);
        OSS ossClient = new OSSClientBuilder().build(
            StringUtils.trim(properties.getEndpoint()),
            StringUtils.trim(properties.getAccessKeyId()),
            StringUtils.trim(properties.getAccessKeySecret())
        );
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(request.getContentType());
            if (StringUtils.isNotBlank(request.getContentDisposition())) {
                metadata.setContentDisposition(request.getContentDisposition());
            }
            if (request.getContentLength() != null && request.getContentLength() >= 0) {
                metadata.setContentLength(request.getContentLength());
            }
            PutObjectResult putObjectResult = ossClient.putObject(
                properties.getBucketName(),
                request.getObjectKey(),
                request.getInputStream(),
                metadata
            );
            StoragePutObjectResult result = new StoragePutObjectResult();
            result.setProvider(PROVIDER);
            result.setBucketName(properties.getBucketName());
            result.setObjectKey(request.getObjectKey());
            result.setUrl(buildPublicUrl(request.getObjectKey()));
            result.setEtag(putObjectResult == null ? null : putObjectResult.getETag());
            return result;
        } finally {
            ossClient.shutdown();
        }
    }

    private void validateProperties() {
        if (StringUtils.isBlank(properties.getEndpoint())) {
            throw new BusinessException("阿里 OSS endpoint 未配置");
        }
        if (StringUtils.isBlank(properties.getBucketName())) {
            throw new BusinessException("阿里 OSS bucketName 未配置");
        }
        if (StringUtils.isBlank(properties.getAccessKeyId()) || StringUtils.isBlank(properties.getAccessKeySecret())) {
            throw new BusinessException("阿里 OSS 访问密钥未配置");
        }
    }

    private void validateRequest(StoragePutObjectRequest request) {
        if (request == null || StringUtils.isBlank(request.getObjectKey())) {
            throw new BusinessException("存储 objectKey 不能为空");
        }
        if (request.getInputStream() == null) {
            throw new BusinessException("上传文件流不能为空");
        }
    }

    private String buildPublicUrl(String objectKey) {
        if (StringUtils.isNotBlank(properties.getUrlPrefix())) {
            return StringUtils.removeEnd(StringUtils.trim(properties.getUrlPrefix()), "/") + "/" + encodePath(objectKey);
        }
        String endpoint = StringUtils.defaultIfBlank(properties.getPublicEndpoint(), properties.getEndpoint());
        String normalizedEndpoint = StringUtils.removeStart(StringUtils.trim(endpoint), "https://");
        normalizedEndpoint = StringUtils.removeStart(normalizedEndpoint, "http://");
        return "https://" + properties.getBucketName() + "." + normalizedEndpoint + "/" + encodePath(objectKey);
    }

    private String encodePath(String objectKey) {
        String[] parts = StringUtils.split(objectKey, '/');
        if (parts == null || parts.length == 0) {
            return objectKey;
        }
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (!builder.isEmpty()) {
                builder.append('/');
            }
            builder.append(URLEncoder.encode(part, StandardCharsets.UTF_8).replace("+", "%20"));
        }
        return builder.toString();
    }
}
