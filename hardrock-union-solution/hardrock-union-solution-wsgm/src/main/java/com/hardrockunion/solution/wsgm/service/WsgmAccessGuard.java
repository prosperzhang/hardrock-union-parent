package com.hardrockunion.solution.wsgm.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;

@Component
public class WsgmAccessGuard {

    public void ensureLogin(LoginUser loginUser) {
        if (loginUser == null || loginUser.getTenantId() == null) {
            throw new BusinessException("未登录或登录已失效");
        }
        if (!StringUtils.equals("WSGM", StringUtils.trimToEmpty(loginUser.getAppCode()).toUpperCase())) {
            throw new BusinessException("当前登录态不属于 WSGM");
        }
    }
}
