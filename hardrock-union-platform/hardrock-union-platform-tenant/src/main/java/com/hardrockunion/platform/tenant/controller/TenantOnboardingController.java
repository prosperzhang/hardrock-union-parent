package com.hardrockunion.platform.tenant.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.tenant.dto.TenantOnboardingStatusResponse;
import com.hardrockunion.platform.tenant.service.TenantOnboardingFlowService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "租户引导", description = "注册后创建租户空间、加入租户空间、等待审批、等待分配角色等状态聚合。")
@RequestMapping("/api/{appCode}/onboarding")
public class TenantOnboardingController {

    private final TenantOnboardingFlowService tenantOnboardingFlowService;

    public TenantOnboardingController(TenantOnboardingFlowService tenantOnboardingFlowService) {
        this.tenantOnboardingFlowService = tenantOnboardingFlowService;
    }

    @Operation(summary = "查询当前用户入驻状态", description = "用于前端判断当前用户应进入创建/加入租户空间、等待审批、等待分配角色，还是直接进入系统。")
    @GetMapping("/status")
    public Result<TenantOnboardingStatusResponse> getStatus(@Parameter(description = "应用编码，例如 PMHUB")
                                                            @PathVariable("appCode") String appCode,
                                                            LoginUser loginUser) {
        return Result.success(tenantOnboardingFlowService.getStatus(appCode, loginUser));
    }
}
