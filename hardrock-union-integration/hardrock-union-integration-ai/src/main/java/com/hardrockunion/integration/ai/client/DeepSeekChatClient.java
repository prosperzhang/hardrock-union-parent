package com.hardrockunion.integration.ai.client;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.integration.ai.config.DeepSeekProperties;
import com.hardrockunion.integration.ai.dto.AiChatRequest;
import com.hardrockunion.integration.ai.dto.AiChatResponse;
import com.hardrockunion.integration.ai.dto.AiMessage;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class DeepSeekChatClient implements AiChatClient {

    private static final String PROVIDER = "DEEPSEEK";
    private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";

    private final RestClient deepSeekRestClient;
    private final DeepSeekProperties properties;

    public DeepSeekChatClient(RestClient deepSeekRestClient, DeepSeekProperties properties) {
        this.deepSeekRestClient = deepSeekRestClient;
        this.properties = properties;
    }

    @Override
    public AiChatResponse chat(AiChatRequest request) {
        validate(request);
        DeepSeekChatCompletionRequest providerRequest = toProviderRequest(request);
        DeepSeekChatCompletionResponse providerResponse = deepSeekRestClient.post()
            .uri(CHAT_COMPLETIONS_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + StringUtils.trim(properties.getApiKey()))
            .body(providerRequest)
            .retrieve()
            .body(DeepSeekChatCompletionResponse.class);
        return toResponse(providerRequest, providerResponse);
    }

    private void validate(AiChatRequest request) {
        if (!Boolean.TRUE.equals(properties.getEnabled())) {
            throw new BusinessException("DeepSeek AI 集成未启用");
        }
        if (StringUtils.isBlank(properties.getApiKey())) {
            throw new BusinessException("DeepSeek API Key 未配置");
        }
        if (request == null || request.getMessages() == null || request.getMessages().isEmpty()) {
            throw new BusinessException("AI 消息不能为空");
        }
        boolean hasBlankMessage = request.getMessages().stream()
            .anyMatch(message -> message == null
                || StringUtils.isBlank(message.getRole())
                || StringUtils.isBlank(message.getContent()));
        if (hasBlankMessage) {
            throw new BusinessException("AI 消息 role 和 content 不能为空");
        }
    }

    private DeepSeekChatCompletionRequest toProviderRequest(AiChatRequest request) {
        DeepSeekChatCompletionRequest providerRequest = new DeepSeekChatCompletionRequest();
        providerRequest.setModel(StringUtils.defaultIfBlank(request.getModel(), properties.getDefaultModel()));
        providerRequest.setMessages(request.getMessages());
        providerRequest.setMaxTokens(request.getMaxTokens() == null ? properties.getDefaultMaxTokens() : request.getMaxTokens());
        providerRequest.setTemperature(request.getTemperature() == null ? properties.getDefaultTemperature() : request.getTemperature());
        providerRequest.setStream(false);
        return providerRequest;
    }

    private AiChatResponse toResponse(DeepSeekChatCompletionRequest providerRequest,
                                      DeepSeekChatCompletionResponse providerResponse) {
        if (providerResponse == null || providerResponse.getChoices() == null || providerResponse.getChoices().isEmpty()) {
            throw new BusinessException("DeepSeek 返回结果为空");
        }
        DeepSeekChoice choice = providerResponse.getChoices().getFirst();
        AiChatResponse response = new AiChatResponse();
        response.setProvider(PROVIDER);
        response.setModel(StringUtils.defaultIfBlank(providerResponse.getModel(), providerRequest.getModel()));
        response.setContent(choice.getMessage() == null ? null : choice.getMessage().getContent());
        response.setFinishReason(choice.getFinishReason());
        response.setProviderRequestId(providerResponse.getId());
        if (providerResponse.getUsage() != null) {
            response.setPromptTokens(providerResponse.getUsage().getPromptTokens());
            response.setCompletionTokens(providerResponse.getUsage().getCompletionTokens());
            response.setTotalTokens(providerResponse.getUsage().getTotalTokens());
            response.setPromptCacheHitTokens(providerResponse.getUsage().getPromptCacheHitTokens());
            response.setPromptCacheMissTokens(providerResponse.getUsage().getPromptCacheMissTokens());
        }
        return response;
    }

    private static final class DeepSeekChatCompletionRequest {
        private String model;
        private List<AiMessage> messages;
        @JsonProperty("max_tokens")
        private Integer maxTokens;
        private Double temperature;
        private Boolean stream;

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

        public Boolean getStream() {
            return stream;
        }

        public void setStream(Boolean stream) {
            this.stream = stream;
        }
    }

    private static final class DeepSeekChatCompletionResponse {
        private String id;
        private String model;
        private List<DeepSeekChoice> choices;
        private DeepSeekUsage usage;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public List<DeepSeekChoice> getChoices() {
            return choices;
        }

        public void setChoices(List<DeepSeekChoice> choices) {
            this.choices = choices;
        }

        public DeepSeekUsage getUsage() {
            return usage;
        }

        public void setUsage(DeepSeekUsage usage) {
            this.usage = usage;
        }
    }

    private static final class DeepSeekChoice {
        private Integer index;
        private AiMessage message;
        @JsonProperty("finish_reason")
        private String finishReason;

        public Integer getIndex() {
            return index;
        }

        public void setIndex(Integer index) {
            this.index = index;
        }

        public AiMessage getMessage() {
            return message;
        }

        public void setMessage(AiMessage message) {
            this.message = message;
        }

        public String getFinishReason() {
            return finishReason;
        }

        public void setFinishReason(String finishReason) {
            this.finishReason = finishReason;
        }
    }

    private static final class DeepSeekUsage {
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;
        @JsonProperty("completion_tokens")
        private Integer completionTokens;
        @JsonProperty("total_tokens")
        private Integer totalTokens;
        @JsonProperty("prompt_cache_hit_tokens")
        private Integer promptCacheHitTokens;
        @JsonProperty("prompt_cache_miss_tokens")
        private Integer promptCacheMissTokens;

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
}
