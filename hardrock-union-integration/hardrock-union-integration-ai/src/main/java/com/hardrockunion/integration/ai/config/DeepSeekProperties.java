package com.hardrockunion.integration.ai.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "hardrock.integration.ai.deepseek")
public class DeepSeekProperties {

    private Boolean enabled = true;

    private String baseUrl = "https://api.deepseek.com";

    private String apiKey;

    private String defaultModel = "deepseek-v4-flash";

    private Duration connectTimeout = Duration.ofSeconds(5);

    private Duration readTimeout = Duration.ofSeconds(120);

    private Integer defaultMaxTokens = 2048;

    private Double defaultTemperature = 0.2D;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getDefaultModel() {
        return defaultModel;
    }

    public void setDefaultModel(String defaultModel) {
        this.defaultModel = defaultModel;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Integer getDefaultMaxTokens() {
        return defaultMaxTokens;
    }

    public void setDefaultMaxTokens(Integer defaultMaxTokens) {
        this.defaultMaxTokens = defaultMaxTokens;
    }

    public Double getDefaultTemperature() {
        return defaultTemperature;
    }

    public void setDefaultTemperature(Double defaultTemperature) {
        this.defaultTemperature = defaultTemperature;
    }
}
