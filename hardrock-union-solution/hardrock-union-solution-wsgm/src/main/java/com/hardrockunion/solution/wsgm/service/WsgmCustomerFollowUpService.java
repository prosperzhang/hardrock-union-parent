package com.hardrockunion.solution.wsgm.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.solution.wsgm.domain.entity.WsgmCustomer;
import com.hardrockunion.solution.wsgm.domain.entity.WsgmCustomerFollowUp;
import com.hardrockunion.solution.wsgm.dto.WsgmCustomerFollowUpCreateRequest;
import com.hardrockunion.solution.wsgm.dto.WsgmCustomerFollowUpResponse;
import com.hardrockunion.solution.wsgm.mapper.WsgmCustomerFollowUpMapper;
import com.hardrockunion.solution.wsgm.mapper.WsgmCustomerMapper;

@Service
public class WsgmCustomerFollowUpService {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final WsgmCustomerMapper customerMapper;
    private final WsgmCustomerFollowUpMapper followUpMapper;
    private final WsgmAccessGuard wsgmAccessGuard;

    public WsgmCustomerFollowUpService(
        WsgmCustomerMapper customerMapper,
        WsgmCustomerFollowUpMapper followUpMapper,
        WsgmAccessGuard wsgmAccessGuard
    ) {
        this.customerMapper = customerMapper;
        this.followUpMapper = followUpMapper;
        this.wsgmAccessGuard = wsgmAccessGuard;
    }

    public List<WsgmCustomerFollowUpResponse> list(Long customerId, LoginUser loginUser) {
        WsgmCustomer customer = loadCustomer(customerId, loginUser);
        return followUpMapper.selectList(new LambdaQueryWrapper<WsgmCustomerFollowUp>()
                .eq(WsgmCustomerFollowUp::getTenantId, customer.getTenantId())
                .eq(WsgmCustomerFollowUp::getCustomerId, customerId)
                .eq(WsgmCustomerFollowUp::getDeleted, 0)
                .orderByDesc(WsgmCustomerFollowUp::getId))
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public WsgmCustomerFollowUpResponse create(Long customerId, WsgmCustomerFollowUpCreateRequest request, LoginUser loginUser) {
        WsgmCustomer customer = loadCustomer(customerId, loginUser);
        if (request == null || StringUtils.isBlank(request.getFollowUpContent())) {
            throw new BusinessException("followUpContent 不能为空");
        }

        WsgmCustomerFollowUp followUp = new WsgmCustomerFollowUp();
        followUp.setTenantId(customer.getTenantId());
        followUp.setCustomerId(customerId);
        followUp.setFollowUpType(StringUtils.defaultIfBlank(request.getFollowUpType(), "PHONE"));
        followUp.setFollowUpContent(request.getFollowUpContent());
        followUp.setNextAction(request.getNextAction());
        followUp.setNextFollowUpAt(parseDateTime(request.getNextFollowUpAt()));
        followUp.setCreatedBy(loginUser.getUserId());
        followUp.setDeleted(0);
        followUpMapper.insert(followUp);
        return toResponse(followUp);
    }

    private WsgmCustomer loadCustomer(Long customerId, LoginUser loginUser) {
        wsgmAccessGuard.ensureLogin(loginUser);
        WsgmCustomer customer = customerMapper.selectOne(new LambdaQueryWrapper<WsgmCustomer>()
            .eq(WsgmCustomer::getId, customerId)
            .eq(WsgmCustomer::getTenantId, loginUser.getTenantId())
            .eq(WsgmCustomer::getDeleted, 0)
            .last("limit 1"));
        if (customer == null) {
            throw new BusinessException("客户不存在");
        }
        return customer;
    }

    private LocalDateTime parseDateTime(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return LocalDateTime.parse(value, DATETIME_FORMATTER);
    }

    private WsgmCustomerFollowUpResponse toResponse(WsgmCustomerFollowUp followUp) {
        WsgmCustomerFollowUpResponse response = new WsgmCustomerFollowUpResponse();
        response.setId(followUp.getId());
        response.setCustomerId(followUp.getCustomerId());
        response.setFollowUpType(followUp.getFollowUpType());
        response.setFollowUpContent(followUp.getFollowUpContent());
        response.setNextAction(followUp.getNextAction());
        response.setNextFollowUpAt(followUp.getNextFollowUpAt() == null ? null : followUp.getNextFollowUpAt().format(DATETIME_FORMATTER));
        response.setCreatedBy(followUp.getCreatedBy());
        return response;
    }
}
