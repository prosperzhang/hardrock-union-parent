package com.hardrockunion.platform.file.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "hardrock.file.image")
public class FileUploadProperties {

    private Long maxSize = 5 * 1024 * 1024L;

    private List<String> allowedContentTypes = List.of("image/jpeg", "image/png", "image/webp", "image/gif");

    public Long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Long maxSize) {
        this.maxSize = maxSize;
    }

    public List<String> getAllowedContentTypes() {
        return allowedContentTypes;
    }

    public void setAllowedContentTypes(List<String> allowedContentTypes) {
        this.allowedContentTypes = allowedContentTypes;
    }
}
