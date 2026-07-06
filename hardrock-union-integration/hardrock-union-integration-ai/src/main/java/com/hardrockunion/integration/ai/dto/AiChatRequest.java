package com.hardrockunion.integration.ai.dto;

import java.util.ArrayList;
import java.util.List;

public class AiChatRequest {

    private String model;

    private List<AiMessage> messages = new ArrayList<>();

    private Integer maxTokens;

    private Double temperature;

    public static AiChatRequest ofUserPrompt(String prompt) {
        AiChatRequest request = new AiChatRequest();
        request.setMessages(List.of(AiMessage.user(prompt)));
        return request;
    }

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
