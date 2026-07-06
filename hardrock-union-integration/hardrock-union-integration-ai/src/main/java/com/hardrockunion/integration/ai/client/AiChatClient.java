package com.hardrockunion.integration.ai.client;

import com.hardrockunion.integration.ai.dto.AiChatRequest;
import com.hardrockunion.integration.ai.dto.AiChatResponse;

public interface AiChatClient {

    AiChatResponse chat(AiChatRequest request);
}
