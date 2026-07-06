package com.hardrockunion.solution.wsgm.service;

import java.time.LocalDate;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.solution.wsgm.domain.entity.WsgmCustomer;
import com.hardrockunion.solution.wsgm.domain.entity.WsgmOpportunity;
import com.hardrockunion.solution.wsgm.dto.WsgmOpportunityCreateRequest;
import com.hardrockunion.solution.wsgm.dto.WsgmOpportunityResponse;
import com.hardrockunion.solution.wsgm.mapper.WsgmCustomerMapper;
import com.hardrockunion.solution.wsgm.mapper.WsgmOpportunityMapper;

@Service
public class WsgmOpportunityService {

    private final WsgmCustomerMapper customerMapper;
    private final WsgmOpportunityMapper opportunityMapper;
    private final WsgmAccessGuard wsgmAccessGuard;

    public WsgmOpportunityService(
        WsgmCustomerMapper customerMapper,
        WsgmOpportunityMapper opportunityMapper,
        WsgmAccessGuard wsgmAccessGuard
    ) {
        this.customerMapper = customerMapper;
        this.opportunityMapper = opportunityMapper;
        this.wsgmAccessGuard = wsgmAccessGuard;
    }

    public List<WsgmOpportunityResponse> list(Long customerId, LoginUser loginUser) {
        WsgmCustomer customer = loadCustomer(customerId, loginUser);
        return opportunityMapper.selectList(new LambdaQueryWrapper<WsgmOpportunity>()
                .eq(WsgmOpportunity::getTenantId, customer.getTenantId())
                .eq(WsgmOpportunity::getCustomerId, customerId)
                .eq(WsgmOpportunity::getDeleted, 0)
                .orderByDesc(WsgmOpportunity::getId))
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public WsgmOpportunityResponse getById(Long id, LoginUser loginUser) {
        ensureLogin(loginUser);
        WsgmOpportunity opportunity = opportunityMapper.selectOne(new LambdaQueryWrapper<WsgmOpportunity>()
            .eq(WsgmOpportunity::getId, id)
            .eq(WsgmOpportunity::getTenantId, loginUser.getTenantId())
            .eq(WsgmOpportunity::getDeleted, 0)
            .last("limit 1"));
        if (opportunity == null) {
            throw new BusinessException("商机不存在");
        }
        return toResponse(opportunity);
    }

    public WsgmOpportunityResponse create(Long customerId, WsgmOpportunityCreateRequest request, LoginUser loginUser) {
        WsgmCustomer customer = loadCustomer(customerId, loginUser);
        if (request == null || StringUtils.isBlank(request.getOpportunityName())) {
            throw new BusinessException("opportunityName 不能为空");
        }

        WsgmOpportunity opportunity = new WsgmOpportunity();
        opportunity.setTenantId(customer.getTenantId());
        opportunity.setCustomerId(customerId);
        opportunity.setOpportunityName(request.getOpportunityName());
        opportunity.setStageCode(StringUtils.defaultIfBlank(request.getStageCode(), "INITIAL"));
        opportunity.setExpectedAmount(request.getExpectedAmount());
        opportunity.setExpectedSignDate(parseDate(request.getExpectedSignDate()));
        opportunity.setRemark(request.getRemark());
        opportunity.setStatus(1);
        opportunity.setDeleted(0);
        opportunity.setCreatedBy(loginUser.getUserId());
        opportunityMapper.insert(opportunity);
        return getById(opportunity.getId(), loginUser);
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

    private void ensureLogin(LoginUser loginUser) {
        wsgmAccessGuard.ensureLogin(loginUser);
    }

    private LocalDate parseDate(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return LocalDate.parse(value);
    }

    private WsgmOpportunityResponse toResponse(WsgmOpportunity opportunity) {
        WsgmOpportunityResponse response = new WsgmOpportunityResponse();
        response.setId(opportunity.getId());
        response.setTenantId(opportunity.getTenantId());
        response.setCustomerId(opportunity.getCustomerId());
        response.setOpportunityName(opportunity.getOpportunityName());
        response.setStageCode(opportunity.getStageCode());
        response.setExpectedAmount(opportunity.getExpectedAmount());
        response.setExpectedSignDate(opportunity.getExpectedSignDate() == null ? null : opportunity.getExpectedSignDate().toString());
        response.setRemark(opportunity.getRemark());
        response.setStatus(opportunity.getStatus());
        return response;
    }
}
