package com.hardrockunion.platform.iam.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.iam.domain.entity.AppRegistry;
import com.hardrockunion.platform.iam.dto.AppRegistryCreateRequest;
import com.hardrockunion.platform.iam.dto.AppRegistryUpdateRequest;
import com.hardrockunion.platform.iam.mapper.AppRegistryMapper;

@Service
public class AppRegistryManageService {

    private final AppRegistryMapper appRegistryMapper;
    private final IamRoleQueryService iamRoleQueryService;

    public AppRegistryManageService(AppRegistryMapper appRegistryMapper,
                                    IamRoleQueryService iamRoleQueryService) {
        this.appRegistryMapper = appRegistryMapper;
        this.iamRoleQueryService = iamRoleQueryService;
    }

    public List<AppRegistry> listApps(LoginUser loginUser) {
        ensureWsgmAdmin(loginUser);
        return appRegistryMapper.selectList(new LambdaQueryWrapper<AppRegistry>()
            .eq(AppRegistry::getDeleted, 0)
            .orderByAsc(AppRegistry::getSortNo)
            .orderByAsc(AppRegistry::getId));
    }

    @Transactional(rollbackFor = Exception.class)
    public AppRegistry createApp(AppRegistryCreateRequest request, LoginUser loginUser) {
        ensureWsgmAdmin(loginUser);
        if (request == null || StringUtils.isAnyBlank(request.getAppCode(), request.getAppName())) {
            throw new BusinessException("appCode、appName 不能为空");
        }
        String appCode = normalizeAppCode(request.getAppCode());
        Long count = appRegistryMapper.selectCount(new LambdaQueryWrapper<AppRegistry>()
            .eq(AppRegistry::getAppCode, appCode));
        if (count != null && count > 0) {
            throw new BusinessException("应用编码已存在");
        }

        AppRegistry app = new AppRegistry();
        app.setAppCode(appCode);
        app.setAppName(StringUtils.trim(request.getAppName()));
        app.setAppType(StringUtils.defaultIfBlank(StringUtils.upperCase(StringUtils.trim(request.getAppType())), "SAAS_PLATFORM"));
        app.setHomePath(StringUtils.trimToNull(request.getHomePath()));
        app.setLoginPath(StringUtils.trimToNull(request.getLoginPath()));
        app.setIcon(StringUtils.trimToNull(request.getIcon()));
        app.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
        app.setStatus(normalizeStatus(request.getStatus()));
        app.setDescription(StringUtils.trimToNull(request.getDescription()));
        app.setDeleted(0);
        appRegistryMapper.insert(app);
        return app;
    }

    @Transactional(rollbackFor = Exception.class)
    public AppRegistry updateApp(String appCode, AppRegistryUpdateRequest request, LoginUser loginUser) {
        ensureWsgmAdmin(loginUser);
        if (request == null || StringUtils.isBlank(request.getAppName())) {
            throw new BusinessException("appName 不能为空");
        }
        AppRegistry app = loadApp(appCode);
        app.setAppName(StringUtils.trim(request.getAppName()));
        app.setAppType(StringUtils.defaultIfBlank(StringUtils.upperCase(StringUtils.trim(request.getAppType())), "SAAS_PLATFORM"));
        app.setHomePath(StringUtils.trimToNull(request.getHomePath()));
        app.setLoginPath(StringUtils.trimToNull(request.getLoginPath()));
        app.setIcon(StringUtils.trimToNull(request.getIcon()));
        app.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
        app.setStatus(normalizeStatus(request.getStatus()));
        app.setDescription(StringUtils.trimToNull(request.getDescription()));
        appRegistryMapper.updateById(app);
        return app;
    }

    @Transactional(rollbackFor = Exception.class)
    public AppRegistry updateAppStatus(String appCode, Integer status, LoginUser loginUser) {
        ensureWsgmAdmin(loginUser);
        AppRegistry app = loadApp(appCode);
        app.setStatus(normalizeStatus(status));
        appRegistryMapper.updateById(app);
        return app;
    }

    private void ensureWsgmAdmin(LoginUser loginUser) {
        iamRoleQueryService.ensureWsgmRoleAdmin(loginUser);
    }

    private String normalizeAppCode(String appCode) {
        String normalized = StringUtils.upperCase(StringUtils.trimToEmpty(appCode));
        if (!normalized.matches("^[A-Z][A-Z0-9_-]{1,31}$")) {
            throw new BusinessException("appCode 只能使用大写字母、数字、下划线或中划线，长度 2-32 位，且必须以字母开头");
        }
        return normalized;
    }

    private AppRegistry loadApp(String appCode) {
        String normalizedAppCode = normalizeAppCode(appCode);
        AppRegistry app = appRegistryMapper.selectOne(new LambdaQueryWrapper<AppRegistry>()
            .eq(AppRegistry::getAppCode, normalizedAppCode)
            .eq(AppRegistry::getDeleted, 0)
            .last("limit 1"));
        if (app == null) {
            throw new BusinessException("应用不存在");
        }
        return app;
    }

    private Integer normalizeStatus(Integer status) {
        Integer normalized = status == null ? 1 : status;
        if (!Integer.valueOf(0).equals(normalized) && !Integer.valueOf(1).equals(normalized)) {
            throw new BusinessException("status 只能是 0 或 1");
        }
        return normalized;
    }
}
