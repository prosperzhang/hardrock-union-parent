package com.hardrockunion.solution.wsgm.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.solution.wsgm.domain.entity.WsgmCustomer;
import com.hardrockunion.solution.wsgm.dto.WsgmCustomerCreateRequest;
import com.hardrockunion.solution.wsgm.dto.WsgmCustomerResponse;
import com.hardrockunion.solution.wsgm.mapper.WsgmCustomerMapper;

@Service
public class WsgmCustomerService {

    private final WsgmCustomerMapper customerMapper;
    private final WsgmAccessGuard wsgmAccessGuard;

    public WsgmCustomerService(WsgmCustomerMapper customerMapper, WsgmAccessGuard wsgmAccessGuard) {
        this.customerMapper = customerMapper;
        this.wsgmAccessGuard = wsgmAccessGuard;
    }

    public List<WsgmCustomerResponse> list(LoginUser loginUser) {
        wsgmAccessGuard.ensureLogin(loginUser);
        return customerMapper.selectList(new LambdaQueryWrapper<WsgmCustomer>()
                .eq(WsgmCustomer::getTenantId, loginUser.getTenantId())
                .eq(WsgmCustomer::getDeleted, 0)
                .orderByDesc(WsgmCustomer::getId))
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public WsgmCustomerResponse getById(Long id, LoginUser loginUser) {
        wsgmAccessGuard.ensureLogin(loginUser);
        WsgmCustomer customer = customerMapper.selectOne(new LambdaQueryWrapper<WsgmCustomer>()
            .eq(WsgmCustomer::getId, id)
            .eq(WsgmCustomer::getTenantId, loginUser.getTenantId())
            .eq(WsgmCustomer::getDeleted, 0)
            .last("limit 1"));
        if (customer == null) {
            throw new BusinessException("客户不存在");
        }
        return toResponse(customer);
    }

    public WsgmCustomerResponse create(WsgmCustomerCreateRequest request, LoginUser loginUser) {
        wsgmAccessGuard.ensureLogin(loginUser);
        if (request == null || StringUtils.isBlank(request.getCustomerName())) {
            throw new BusinessException("customerName 不能为空");
        }

        WsgmCustomer customer = new WsgmCustomer();
        customer.setTenantId(loginUser.getTenantId());
        customer.setCustomerName(request.getCustomerName());
        customer.setContactName(request.getContactName());
        customer.setContactPhone(request.getContactPhone());
        customer.setLevelCode(StringUtils.defaultIfBlank(request.getLevelCode(), "A"));
        customer.setSourceCode(StringUtils.defaultIfBlank(request.getSourceCode(), "MANUAL"));
        customer.setRemark(request.getRemark());
        customer.setStatus(1);
        customer.setDeleted(0);
        customer.setCreatedBy(loginUser.getUserId());
        customerMapper.insert(customer);
        return getById(customer.getId(), loginUser);
    }

    private WsgmCustomerResponse toResponse(WsgmCustomer customer) {
        WsgmCustomerResponse response = new WsgmCustomerResponse();
        response.setId(customer.getId());
        response.setTenantId(customer.getTenantId());
        response.setCustomerName(customer.getCustomerName());
        response.setContactName(customer.getContactName());
        response.setContactPhone(customer.getContactPhone());
        response.setLevelCode(customer.getLevelCode());
        response.setSourceCode(customer.getSourceCode());
        response.setRemark(customer.getRemark());
        response.setStatus(customer.getStatus());
        return response;
    }
}
