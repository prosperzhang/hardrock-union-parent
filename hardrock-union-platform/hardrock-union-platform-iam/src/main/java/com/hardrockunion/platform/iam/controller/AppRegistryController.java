package com.hardrockunion.platform.iam.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.platform.iam.dto.AppRegistryResponse;
import com.hardrockunion.platform.iam.service.AppRegistryQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/app-registry")
@Tag(name = "App注册表", description = "App注册表查询。")
public class AppRegistryController {

    private final AppRegistryQueryService appRegistryQueryService;

    public AppRegistryController(AppRegistryQueryService appRegistryQueryService) {
        this.appRegistryQueryService = appRegistryQueryService;
    }

    @GetMapping
    @Operation(summary = "查询应用列表", description = "返回当前系统中启用的App注册表。")
    public Result<List<AppRegistryResponse>> list() {
        return Result.success(appRegistryQueryService.listEnabledApps()
            .stream()
            .map(this::toResponse)
            .toList());
    }

    @GetMapping("/{appCode}")
    @Operation(summary = "查询应用详情", description = "根据应用编码查询启用中的App注册表。")
    public Result<AppRegistryResponse> detail(@PathVariable("appCode") String appCode) {
        return Result.success(toResponse(appRegistryQueryService.getEnabledAppByCode(appCode)));
    }

    private AppRegistryResponse toResponse(com.hardrockunion.platform.iam.domain.entity.AppRegistry app) {
        AppRegistryResponse response = new AppRegistryResponse();
        response.setId(app.getId());
        response.setAppCode(app.getAppCode());
        response.setAppName(app.getAppName());
        response.setAppType(app.getAppType());
        response.setHomePath(app.getHomePath());
        response.setLoginPath(app.getLoginPath());
        response.setIcon(app.getIcon());
        response.setSortNo(app.getSortNo());
        response.setStatus(app.getStatus());
        response.setDescription(app.getDescription());
        return response;
    }
}
