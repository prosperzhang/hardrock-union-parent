package com.hardrockunion.platform.message.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;
import com.hardrockunion.platform.message.dto.MessageQueryRequest;
import com.hardrockunion.platform.message.dto.MessageResponse;
import com.hardrockunion.platform.message.dto.MessageSendRequest;
import com.hardrockunion.platform.message.dto.MessageUnreadCountResponse;
import com.hardrockunion.platform.message.service.MessageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/{appCode}/messages")
@Tag(name = "消息中心", description = "跨 app、跨租户复用的站内消息中心。")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @Operation(summary = "发送消息", description = "向当前租户内的一个或多个成员发送站内消息。")
    @PostMapping
    public Result<MessageResponse> send(@Parameter(description = "应用编码，例如 NEXIS、PRIMELOAD-MARKETPLACE")
                                        @PathVariable("appCode") String appCode,
                                        @RequestBody MessageSendRequest request,
                                        LoginUser loginUser) {
        return Result.success(messageService.send(appCode, request, loginUser));
    }

    @Operation(summary = "我的消息列表", description = "查询当前登录人在当前租户下收到的消息。")
    @GetMapping
    public Result<PageResponse<MessageResponse>> list(@Parameter(description = "应用编码，例如 NEXIS、PRIMELOAD-MARKETPLACE")
                                                      @PathVariable("appCode") String appCode,
                                                      MessageQueryRequest request,
                                                      LoginUser loginUser) {
        return Result.success(messageService.list(appCode, request, loginUser));
    }

    @Operation(summary = "未读消息数", description = "查询当前登录人在当前租户下的未读消息数。")
    @GetMapping("/unread-count")
    public Result<MessageUnreadCountResponse> unreadCount(@Parameter(description = "应用编码，例如 NEXIS、PRIMELOAD-MARKETPLACE")
                                                          @PathVariable("appCode") String appCode,
                                                          LoginUser loginUser) {
        return Result.success(new MessageUnreadCountResponse(messageService.unreadCount(appCode, loginUser)));
    }

    @Operation(summary = "标记单条消息已读", description = "把当前登录人收到的一条消息标记为已读。")
    @PutMapping("/{recipientId}/read")
    public Result<MessageResponse> markRead(@Parameter(description = "应用编码，例如 NEXIS、PRIMELOAD-MARKETPLACE")
                                            @PathVariable("appCode") String appCode,
                                            @Parameter(description = "消息接收记录 ID")
                                            @PathVariable("recipientId") Long recipientId,
                                            LoginUser loginUser) {
        return Result.success(messageService.markRead(appCode, recipientId, loginUser));
    }

    @Operation(summary = "全部标记已读", description = "把当前登录人在当前租户下的所有未读消息标记为已读。")
    @PutMapping("/read-all")
    public Result<Void> markAllRead(@Parameter(description = "应用编码，例如 NEXIS、PRIMELOAD-MARKETPLACE")
                                    @PathVariable("appCode") String appCode,
                                    LoginUser loginUser) {
        messageService.markAllRead(appCode, loginUser);
        return Result.success();
    }
}
