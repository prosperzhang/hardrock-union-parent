package com.hardrockunion.platform.ai.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.integration.ai.dto.AiChatRequest;
import com.hardrockunion.integration.ai.dto.AiChatResponse;
import com.hardrockunion.integration.ai.service.AiChatService;
import com.hardrockunion.platform.ai.dto.PlatformAiChatRequest;
import com.hardrockunion.platform.ai.dto.PlatformAiChatResponse;

@Service
public class PlatformAiChatService {

    private final AiChatService aiChatService;

    public PlatformAiChatService(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    public PlatformAiChatResponse chat(String appCode, PlatformAiChatRequest request, LoginUser loginUser) {
        validate(appCode, request, loginUser);
        AiChatRequest aiRequest = new AiChatRequest();
        aiRequest.setModel(request.getModel());
        aiRequest.setMessages(request.getMessages());
        aiRequest.setMaxTokens(request.getMaxTokens());
        aiRequest.setTemperature(request.getTemperature());
        AiChatResponse aiResponse = aiChatService.chat(aiRequest);
        return toResponse(aiResponse);
    }

    private void validate(String appCode, PlatformAiChatRequest request, LoginUser loginUser) {
        if (loginUser == null || loginUser.getUserId() == null || loginUser.getTenantId() == null) {
            throw new BusinessException("请先登录");
        }
        if (!StringUtils.equalsIgnoreCase(StringUtils.trimToEmpty(appCode), loginUser.getAppCode())) {
            throw new BusinessException("登录应用与 AI 调用应用不一致");
        }
        if (request == null || request.getMessages() == null || request.getMessages().isEmpty()) {
            throw new BusinessException("AI 消息不能为空");
        }
    }

    private PlatformAiChatResponse toResponse(AiChatResponse aiResponse) {
        PlatformAiChatResponse response = new PlatformAiChatResponse();
        response.setProvider(aiResponse.getProvider());
        response.setModel(aiResponse.getModel());
        response.setContent(aiResponse.getContent());
        response.setFinishReason(aiResponse.getFinishReason());
        response.setPromptTokens(aiResponse.getPromptTokens());
        response.setCompletionTokens(aiResponse.getCompletionTokens());
        response.setTotalTokens(aiResponse.getTotalTokens());
        response.setPromptCacheHitTokens(aiResponse.getPromptCacheHitTokens());
        response.setPromptCacheMissTokens(aiResponse.getPromptCacheMissTokens());
        return response;
    }
}
