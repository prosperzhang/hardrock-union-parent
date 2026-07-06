package com.hardrockunion.platform.ai.dto;

public class PlatformAiChatResponse {

    private String provider;

    private String model;

    private String content;

    private String finishReason;

    private Integer promptTokens;

    private Integer completionTokens;

    private Integer totalTokens;

    private Integer promptCacheHitTokens;

    private Integer promptCacheMissTokens;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFinishReason() {
        return finishReason;
    }

    public void setFinishReason(String finishReason) {
        this.finishReason = finishReason;
    }

    public Integer getPromptTokens() {
        return promptTokens;
    }

    public void setPromptTokens(Integer promptTokens) {
        this.promptTokens = promptTokens;
    }

    public Integer getCompletionTokens() {
        return completionTokens;
    }

    public void setCompletionTokens(Integer completionTokens) {
        this.completionTokens = completionTokens;
    }

    public Integer getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(Integer totalTokens) {
        this.totalTokens = totalTokens;
    }

    public Integer getPromptCacheHitTokens() {
        return promptCacheHitTokens;
    }

    public void setPromptCacheHitTokens(Integer promptCacheHitTokens) {
        this.promptCacheHitTokens = promptCacheHitTokens;
    }

    public Integer getPromptCacheMissTokens() {
        return promptCacheMissTokens;
    }

    public void setPromptCacheMissTokens(Integer promptCacheMissTokens) {
        this.promptCacheMissTokens = promptCacheMissTokens;
    }
}
