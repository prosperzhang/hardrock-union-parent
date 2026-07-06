package com.hardrockunion.integration.ai.service;

import org.springframework.stereotype.Service;

import com.hardrockunion.integration.ai.client.AiChatClient;
import com.hardrockunion.integration.ai.dto.AiChatRequest;
import com.hardrockunion.integration.ai.dto.AiChatResponse;

@Service
public class AiChatService {

    private final AiChatClient aiChatClient;

    public AiChatService(AiChatClient aiChatClient) {
        this.aiChatClient = aiChatClient;
    }

    public AiChatResponse chat(AiChatRequest request) {
        return aiChatClient.chat(request);
    }
}
