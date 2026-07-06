package com.hardrockunion.platform.message.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;
import com.hardrockunion.platform.iam.domain.entity.AppRegistry;
import com.hardrockunion.platform.iam.domain.entity.IamTenantMember;
import com.hardrockunion.platform.iam.mapper.IamTenantMemberMapper;
import com.hardrockunion.platform.iam.service.AppRegistryQueryService;
import com.hardrockunion.platform.message.domain.entity.MessageRecord;
import com.hardrockunion.platform.message.domain.entity.MessageRecipient;
import com.hardrockunion.platform.message.domain.entity.MessageThread;
import com.hardrockunion.platform.message.dto.MessageQueryRequest;
import com.hardrockunion.platform.message.dto.MessageResponse;
import com.hardrockunion.platform.message.dto.MessageSendRequest;
import com.hardrockunion.platform.message.mapper.MessageRecordMapper;
import com.hardrockunion.platform.message.mapper.MessageRecipientMapper;
import com.hardrockunion.platform.message.mapper.MessageThreadMapper;

@Service
public class MessageService {

    private static final String STATUS_ACTIVE = "ACTIVE";

    private final AppRegistryQueryService appRegistryQueryService;
    private final IamTenantMemberMapper iamTenantMemberMapper;
    private final MessageThreadMapper threadMapper;
    private final MessageRecordMapper recordMapper;
    private final MessageRecipientMapper recipientMapper;

    public MessageService(AppRegistryQueryService appRegistryQueryService,
                          IamTenantMemberMapper iamTenantMemberMapper,
                          MessageThreadMapper threadMapper,
                          MessageRecordMapper recordMapper,
                          MessageRecipientMapper recipientMapper) {
        this.appRegistryQueryService = appRegistryQueryService;
        this.iamTenantMemberMapper = iamTenantMemberMapper;
        this.threadMapper = threadMapper;
        this.recordMapper = recordMapper;
        this.recipientMapper = recipientMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public MessageResponse send(String appCode, MessageSendRequest request, LoginUser loginUser) {
        AppRegistry app = ensureAppLogin(appCode, loginUser);
        if (request == null) {
            throw new BusinessException("消息请求不能为空");
        }
        Long tenantId = request.getTenantId() == null ? loginUser.getTenantId() : request.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("tenantId 不能为空");
        }
        if (!tenantId.equals(loginUser.getTenantId())) {
            throw new BusinessException("不能向当前租户之外发送消息");
        }
        List<Long> receiverUserIds = normalizeReceivers(request.getReceiverUserIds());
        if (receiverUserIds.isEmpty()) {
            throw new BusinessException("receiverUserIds 不能为空");
        }
        ensureTenantMembers(app.getId(), tenantId, receiverUserIds);
        if (StringUtils.isBlank(request.getTitle())) {
            throw new BusinessException("title 不能为空");
        }
        String messageType = normalizeMessageType(request.getMessageType());
        String threadType = normalizeThreadType(request.getThreadType(), messageType);

        MessageThread thread = new MessageThread();
        thread.setAppId(app.getId());
        thread.setAppCode(app.getAppCode());
        thread.setTenantId(tenantId);
        thread.setThreadType(threadType);
        thread.setSourceType(StringUtils.upperCase(StringUtils.trimToNull(request.getSourceType())));
        thread.setSourceId(request.getSourceId());
        thread.setTitle(StringUtils.trim(request.getTitle()));
        thread.setThreadStatus(STATUS_ACTIVE);
        thread.setCreatedBy(loginUser.getUserId());
        thread.setDeleted(0);
        threadMapper.insert(thread);

        MessageRecord record = new MessageRecord();
        record.setAppId(app.getId());
        record.setAppCode(app.getAppCode());
        record.setTenantId(tenantId);
        record.setThreadId(thread.getId());
        record.setMessageType(messageType);
        record.setSenderUserId(loginUser.getUserId());
        record.setSenderName(loginUser.getUsername());
        record.setTitle(StringUtils.trim(request.getTitle()));
        record.setContent(StringUtils.trimToEmpty(request.getContent()));
        record.setSourceType(thread.getSourceType());
        record.setSourceId(request.getSourceId());
        record.setActionUrl(StringUtils.trimToNull(request.getActionUrl()));
        record.setRecordStatus(STATUS_ACTIVE);
        record.setDeleted(0);
        recordMapper.insert(record);

        MessageRecipient firstRecipient = null;
        for (Long receiverUserId : receiverUserIds) {
            MessageRecipient recipient = new MessageRecipient();
            recipient.setAppId(app.getId());
            recipient.setAppCode(app.getAppCode());
            recipient.setTenantId(tenantId);
            recipient.setThreadId(thread.getId());
            recipient.setRecordId(record.getId());
            recipient.setReceiverUserId(receiverUserId);
            recipient.setReadFlag(0);
            recipient.setRecipientStatus(STATUS_ACTIVE);
            recipient.setDeleted(0);
            recipientMapper.insert(recipient);
            if (firstRecipient == null) {
                firstRecipient = recipient;
            }
        }
        return toResponse(firstRecipient, record, thread);
    }

    @Transactional(rollbackFor = Exception.class)
    public MessageResponse sendSystem(String appCode, MessageSendRequest request) {
        AppRegistry app = appRegistryQueryService.getEnabledAppByCode(appCode);
        if (request == null) {
            throw new BusinessException("消息请求不能为空");
        }
        Long tenantId = request.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("tenantId 不能为空");
        }
        List<Long> receiverUserIds = normalizeReceivers(request.getReceiverUserIds());
        if (receiverUserIds.isEmpty()) {
            throw new BusinessException("receiverUserIds 不能为空");
        }
        ensureTenantMembers(app.getId(), tenantId, receiverUserIds);
        if (StringUtils.isBlank(request.getTitle())) {
            throw new BusinessException("title 不能为空");
        }
        String messageType = normalizeMessageType(request.getMessageType());
        String threadType = normalizeThreadType(request.getThreadType(), messageType);

        MessageThread thread = new MessageThread();
        thread.setAppId(app.getId());
        thread.setAppCode(app.getAppCode());
        thread.setTenantId(tenantId);
        thread.setThreadType(threadType);
        thread.setSourceType(StringUtils.upperCase(StringUtils.trimToNull(request.getSourceType())));
        thread.setSourceId(request.getSourceId());
        thread.setTitle(StringUtils.trim(request.getTitle()));
        thread.setThreadStatus(STATUS_ACTIVE);
        thread.setCreatedBy(null);
        thread.setDeleted(0);
        threadMapper.insert(thread);

        MessageRecord record = new MessageRecord();
        record.setAppId(app.getId());
        record.setAppCode(app.getAppCode());
        record.setTenantId(tenantId);
        record.setThreadId(thread.getId());
        record.setMessageType(messageType);
        record.setSenderUserId(null);
        record.setSenderName("SYSTEM");
        record.setTitle(StringUtils.trim(request.getTitle()));
        record.setContent(StringUtils.trimToEmpty(request.getContent()));
        record.setSourceType(thread.getSourceType());
        record.setSourceId(request.getSourceId());
        record.setActionUrl(StringUtils.trimToNull(request.getActionUrl()));
        record.setRecordStatus(STATUS_ACTIVE);
        record.setDeleted(0);
        recordMapper.insert(record);

        MessageRecipient firstRecipient = null;
        for (Long receiverUserId : receiverUserIds) {
            MessageRecipient recipient = new MessageRecipient();
            recipient.setAppId(app.getId());
            recipient.setAppCode(app.getAppCode());
            recipient.setTenantId(tenantId);
            recipient.setThreadId(thread.getId());
            recipient.setRecordId(record.getId());
            recipient.setReceiverUserId(receiverUserId);
            recipient.setReadFlag(0);
            recipient.setRecipientStatus(STATUS_ACTIVE);
            recipient.setDeleted(0);
            recipientMapper.insert(recipient);
            if (firstRecipient == null) {
                firstRecipient = recipient;
            }
        }
        return toResponse(firstRecipient, record, thread);
    }

    public PageResponse<MessageResponse> list(String appCode, MessageQueryRequest request, LoginUser loginUser) {
        AppRegistry app = ensureAppLogin(appCode, loginUser);
        MessageQueryRequest query = request == null ? new MessageQueryRequest() : request;
        LambdaQueryWrapper<MessageRecipient> wrapper = new LambdaQueryWrapper<MessageRecipient>()
            .eq(MessageRecipient::getAppId, app.getId())
            .eq(MessageRecipient::getTenantId, loginUser.getTenantId())
            .eq(MessageRecipient::getReceiverUserId, loginUser.getUserId())
            .eq(MessageRecipient::getDeleted, 0);
        if (query.getReadFlag() != null) {
            wrapper.eq(MessageRecipient::getReadFlag, query.getReadFlag());
        }
        wrapper.orderByAsc(MessageRecipient::getReadFlag)
            .orderByDesc(MessageRecipient::getId);
        Page<MessageRecipient> page = recipientMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()), wrapper);
        List<MessageResponse> records = page.getRecords().stream()
            .map(this::toResponse)
            .filter(Objects::nonNull)
            .filter(response -> matchQuery(response, query))
            .toList();
        Page<MessageResponse> responsePage = Page.of(page.getCurrent(), page.getSize(), page.getTotal());
        responsePage.setRecords(records);
        return PageResponse.from(responsePage);
    }

    public long unreadCount(String appCode, LoginUser loginUser) {
        AppRegistry app = ensureAppLogin(appCode, loginUser);
        return recipientMapper.selectCount(new LambdaQueryWrapper<MessageRecipient>()
            .eq(MessageRecipient::getAppId, app.getId())
            .eq(MessageRecipient::getTenantId, loginUser.getTenantId())
            .eq(MessageRecipient::getReceiverUserId, loginUser.getUserId())
            .eq(MessageRecipient::getReadFlag, 0)
            .eq(MessageRecipient::getDeleted, 0));
    }

    @Transactional(rollbackFor = Exception.class)
    public MessageResponse markRead(String appCode, Long recipientId, LoginUser loginUser) {
        AppRegistry app = ensureAppLogin(appCode, loginUser);
        MessageRecipient recipient = recipientMapper.selectOne(new LambdaQueryWrapper<MessageRecipient>()
            .eq(MessageRecipient::getAppId, app.getId())
            .eq(MessageRecipient::getTenantId, loginUser.getTenantId())
            .eq(MessageRecipient::getReceiverUserId, loginUser.getUserId())
            .eq(MessageRecipient::getId, recipientId)
            .eq(MessageRecipient::getDeleted, 0)
            .last("limit 1"));
        if (recipient == null) {
            throw new BusinessException("消息不存在");
        }
        if (!Integer.valueOf(1).equals(recipient.getReadFlag())) {
            recipient.setReadFlag(1);
            recipient.setReadAt(LocalDateTime.now());
            recipientMapper.updateById(recipient);
        }
        return toResponse(recipient);
    }

    @Transactional(rollbackFor = Exception.class)
    public void markAllRead(String appCode, LoginUser loginUser) {
        AppRegistry app = ensureAppLogin(appCode, loginUser);
        List<MessageRecipient> unreadRecipients = recipientMapper.selectList(new LambdaQueryWrapper<MessageRecipient>()
            .eq(MessageRecipient::getAppId, app.getId())
            .eq(MessageRecipient::getTenantId, loginUser.getTenantId())
            .eq(MessageRecipient::getReceiverUserId, loginUser.getUserId())
            .eq(MessageRecipient::getReadFlag, 0)
            .eq(MessageRecipient::getDeleted, 0));
        LocalDateTime now = LocalDateTime.now();
        for (MessageRecipient recipient : unreadRecipients) {
            recipient.setReadFlag(1);
            recipient.setReadAt(now);
            recipientMapper.updateById(recipient);
        }
    }

    private AppRegistry ensureAppLogin(String appCode, LoginUser loginUser) {
        AppRegistry app = appRegistryQueryService.getEnabledAppByCode(appCode);
        if (loginUser == null || loginUser.getUserId() == null || loginUser.getTenantId() == null) {
            throw new BusinessException("未登录或登录已失效");
        }
        if (!StringUtils.equalsIgnoreCase(app.getAppCode(), loginUser.getAppCode())) {
            throw new BusinessException("当前登录态不属于该 app");
        }
        return app;
    }

    private List<Long> normalizeReceivers(List<Long> receiverUserIds) {
        if (receiverUserIds == null) {
            return List.of();
        }
        return receiverUserIds.stream()
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    }

    private void ensureTenantMembers(Long appId, Long tenantId, List<Long> receiverUserIds) {
        long activeMemberCount = iamTenantMemberMapper.selectCount(new LambdaQueryWrapper<IamTenantMember>()
            .eq(IamTenantMember::getAppId, appId)
            .eq(IamTenantMember::getTenantId, tenantId)
            .in(IamTenantMember::getUserId, receiverUserIds)
            .eq(IamTenantMember::getMemberStatus, STATUS_ACTIVE)
            .eq(IamTenantMember::getDeleted, 0));
        if (activeMemberCount != receiverUserIds.size()) {
            throw new BusinessException("接收人不属于当前租户");
        }
    }

    private String normalizeMessageType(String messageType) {
        String normalized = StringUtils.upperCase(StringUtils.defaultIfBlank(StringUtils.trimToNull(messageType), "BUSINESS"));
        if (!List.of("SYSTEM", "BUSINESS", "APPROVAL", "CHAT", "COMMENT").contains(normalized)) {
            throw new BusinessException("不支持的消息类型");
        }
        return normalized;
    }

    private String normalizeThreadType(String threadType, String messageType) {
        return StringUtils.upperCase(StringUtils.defaultIfBlank(StringUtils.trimToNull(threadType), messageType));
    }

    private MessageResponse toResponse(MessageRecipient recipient) {
        if (recipient == null) {
            return null;
        }
        MessageRecord record = recordMapper.selectById(recipient.getRecordId());
        MessageThread thread = record == null ? null : threadMapper.selectById(record.getThreadId());
        return toResponse(recipient, record, thread);
    }

    private MessageResponse toResponse(MessageRecipient recipient, MessageRecord record, MessageThread thread) {
        if (recipient == null || record == null) {
            return null;
        }
        MessageResponse response = new MessageResponse();
        response.setId(recipient.getId());
        response.setThreadId(recipient.getThreadId());
        response.setRecordId(recipient.getRecordId());
        response.setTenantId(recipient.getTenantId());
        response.setThreadType(thread == null ? null : thread.getThreadType());
        response.setMessageType(record.getMessageType());
        response.setSenderUserId(record.getSenderUserId());
        response.setSenderName(record.getSenderName());
        response.setTitle(record.getTitle());
        response.setContent(record.getContent());
        response.setSourceType(record.getSourceType());
        response.setSourceId(record.getSourceId());
        response.setActionUrl(record.getActionUrl());
        response.setReadFlag(recipient.getReadFlag());
        response.setReadAt(recipient.getReadAt());
        response.setCreatedAt(record.getCreatedAt());
        return response;
    }

    private boolean matchQuery(MessageResponse response, MessageQueryRequest query) {
        if (StringUtils.isNotBlank(query.getMessageType())
            && !StringUtils.equalsIgnoreCase(response.getMessageType(), query.getMessageType())) {
            return false;
        }
        if (StringUtils.isBlank(query.getKeyword())) {
            return true;
        }
        String keyword = StringUtils.trim(query.getKeyword());
        return StringUtils.containsIgnoreCase(response.getTitle(), keyword)
            || StringUtils.containsIgnoreCase(response.getContent(), keyword);
    }
}
