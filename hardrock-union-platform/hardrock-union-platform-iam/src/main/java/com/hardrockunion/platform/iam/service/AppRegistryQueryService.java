package com.hardrockunion.platform.iam.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.platform.iam.domain.entity.AppRegistry;
import com.hardrockunion.platform.iam.mapper.AppRegistryMapper;

@Service
public class AppRegistryQueryService {

    private final AppRegistryMapper appRegistryMapper;

    public AppRegistryQueryService(AppRegistryMapper appRegistryMapper) {
        this.appRegistryMapper = appRegistryMapper;
    }

    public List<AppRegistry> listEnabledApps() {
        return appRegistryMapper.selectList(new LambdaQueryWrapper<AppRegistry>()
            .eq(AppRegistry::getDeleted, 0)
            .eq(AppRegistry::getStatus, 1)
            .orderByAsc(AppRegistry::getSortNo)
            .orderByAsc(AppRegistry::getId));
    }

    public AppRegistry getEnabledAppByCode(String appCode) {
        String normalized = normalizeAppCode(appCode);
        AppRegistry app = appRegistryMapper.selectOne(new LambdaQueryWrapper<AppRegistry>()
            .eq(AppRegistry::getAppCode, normalized)
            .eq(AppRegistry::getDeleted, 0)
            .eq(AppRegistry::getStatus, 1)
            .last("limit 1"));
        if (app == null) {
            throw new BusinessException("应用不存在");
        }
        return app;
    }

    public AppRegistry getAppByCode(String appCode) {
        String normalized = normalizeRegisteredAppCode(appCode);
        AppRegistry app = appRegistryMapper.selectOne(new LambdaQueryWrapper<AppRegistry>()
            .eq(AppRegistry::getAppCode, normalized)
            .eq(AppRegistry::getDeleted, 0)
            .last("limit 1"));
        if (app == null) {
            throw new BusinessException("应用不存在");
        }
        return app;
    }

    public Long getEnabledAppIdByCode(String appCode) {
        return getEnabledAppByCode(appCode).getId();
    }

    public AppRegistry getEnabledAppById(Long appId) {
        if (appId == null) {
            throw new BusinessException("appId 不能为空");
        }
        AppRegistry app = appRegistryMapper.selectOne(new LambdaQueryWrapper<AppRegistry>()
            .eq(AppRegistry::getId, appId)
            .eq(AppRegistry::getDeleted, 0)
            .eq(AppRegistry::getStatus, 1)
            .last("limit 1"));
        if (app == null) {
            throw new BusinessException("应用不存在");
        }
        return app;
    }

    public AppRegistry getAppById(Long appId) {
        if (appId == null) {
            throw new BusinessException("appId 不能为空");
        }
        AppRegistry app = appRegistryMapper.selectOne(new LambdaQueryWrapper<AppRegistry>()
            .eq(AppRegistry::getId, appId)
            .eq(AppRegistry::getDeleted, 0)
            .last("limit 1"));
        if (app == null) {
            throw new BusinessException("应用不存在");
        }
        return app;
    }

    public String normalizeAppCode(String appCode) {
        String normalized = StringUtils.trimToEmpty(appCode).toUpperCase();
        if (StringUtils.isBlank(normalized)) {
            throw new BusinessException("appCode 不能为空");
        }
        if (appRegistryMapper.selectCount(new LambdaQueryWrapper<AppRegistry>()
            .eq(AppRegistry::getAppCode, normalized)
            .eq(AppRegistry::getDeleted, 0)
            .eq(AppRegistry::getStatus, 1)) == 0) {
            throw new BusinessException("不支持的 appCode");
        }
        return normalized;
    }

    public String normalizeRegisteredAppCode(String appCode) {
        String normalized = StringUtils.trimToEmpty(appCode).toUpperCase();
        if (StringUtils.isBlank(normalized)) {
            throw new BusinessException("appCode 不能为空");
        }
        if (appRegistryMapper.selectCount(new LambdaQueryWrapper<AppRegistry>()
            .eq(AppRegistry::getAppCode, normalized)
            .eq(AppRegistry::getDeleted, 0)) == 0) {
            throw new BusinessException("不支持的 appCode");
        }
        return normalized;
    }

    public boolean existsEnabledApp(String appCode) {
        try {
            normalizeAppCode(appCode);
            return true;
        } catch (BusinessException ex) {
            return false;
        }
    }
}
