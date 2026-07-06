package com.hardrockunion.platform.iam.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.iam.domain.entity.AppRegistry;
import com.hardrockunion.platform.iam.dto.AppRegistryCreateRequest;
import com.hardrockunion.platform.iam.dto.AppRegistryResponse;
import com.hardrockunion.platform.iam.dto.AppRegistryStatusUpdateRequest;
import com.hardrockunion.platform.iam.dto.AppRegistryUpdateRequest;
import com.hardrockunion.platform.iam.service.AppRegistryManageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/wsgm/platform/apps")
@Tag(name = "WSGM-应用管理", description = "总部系统中的应用注册表管理。")
public class WsgmAppManagementController {

    private final AppRegistryManageService appRegistryManageService;

    public WsgmAppManagementController(AppRegistryManageService appRegistryManageService) {
        this.appRegistryManageService = appRegistryManageService;
    }

    @GetMapping
    @Operation(summary = "查询应用管理列表", description = "由 WSGM 管理员查询全部未删除App注册表，包含启用与禁用状态。")
    public Result<List<AppRegistryResponse>> list(LoginUser loginUser) {
        return Result.success(appRegistryManageService.listApps(loginUser)
            .stream()
            .map(this::toResponse)
            .toList());
    }

    @PostMapping
    @Operation(summary = "创建应用", description = "由 WSGM 管理员创建一个App注册表。")
    public Result<AppRegistryResponse> create(@RequestBody AppRegistryCreateRequest request,
                                              LoginUser loginUser) {
        return Result.success(toResponse(appRegistryManageService.createApp(request, loginUser)));
    }

    @PutMapping("/{appCode}")
    @Operation(summary = "更新应用", description = "由 WSGM 管理员更新App注册表。应用编码不允许修改。")
    public Result<AppRegistryResponse> update(@PathVariable("appCode") String appCode,
                                              @RequestBody AppRegistryUpdateRequest request,
                                              LoginUser loginUser) {
        return Result.success(toResponse(appRegistryManageService.updateApp(appCode, request, loginUser)));
    }

    @PatchMapping("/{appCode}/status")
    @Operation(summary = "更新应用状态", description = "由 WSGM 管理员启用或禁用App注册表。")
    public Result<AppRegistryResponse> updateStatus(@PathVariable("appCode") String appCode,
                                                    @RequestBody AppRegistryStatusUpdateRequest request,
                                                    LoginUser loginUser) {
        return Result.success(toResponse(appRegistryManageService.updateAppStatus(appCode, request == null ? null : request.getStatus(), loginUser)));
    }

    private AppRegistryResponse toResponse(AppRegistry app) {
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
