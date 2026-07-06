package com.hardrockunion.platform.ai.dto;

import java.util.ArrayList;
import java.util.List;

import com.hardrockunion.integration.ai.dto.AiMessage;

public class PlatformAiChatRequest {

    private String model;

    private List<AiMessage> messages = new ArrayList<>();

    private Integer maxTokens;

    private Double temperature;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<AiMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<AiMessage> messages) {
        this.messages = messages;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }
}
