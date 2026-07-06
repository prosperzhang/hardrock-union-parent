package com.hardrockunion.platform.ai.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.ai.dto.PlatformAiChatRequest;
import com.hardrockunion.platform.ai.dto.PlatformAiChatResponse;
import com.hardrockunion.platform.ai.service.PlatformAiChatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "平台-AI", description = "跨 app、跨租户复用的 AI 调用入口。")
@RestController
@RequestMapping("/api/{appCode}/ai")
public class PlatformAiChatController {

    private final PlatformAiChatService platformAiChatService;

    public PlatformAiChatController(PlatformAiChatService platformAiChatService) {
        this.platformAiChatService = platformAiChatService;
    }

    @Operation(summary = "AI 对话", description = "登录后调用统一 AI 服务。当前后端供应商为 DeepSeek。")
    @PostMapping("/chat")
    public Result<PlatformAiChatResponse> chat(@PathVariable("appCode") String appCode,
                                               @RequestBody PlatformAiChatRequest request,
                                               LoginUser loginUser) {
        return Result.success(platformAiChatService.chat(appCode, request, loginUser));
    }
}
