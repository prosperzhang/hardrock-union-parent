package com.hardrockunion.platform.tenant.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.tenant.dto.TenantCreateRequest;
import com.hardrockunion.platform.tenant.dto.TenantCreateResponse;
import com.hardrockunion.platform.tenant.dto.TenantSummaryResponse;
import com.hardrockunion.platform.tenant.dto.TenantWorkspaceAttachParentRequest;
import com.hardrockunion.platform.tenant.dto.TenantWorkspaceMyResponse;
import com.hardrockunion.platform.tenant.service.TenantWorkspaceFlowService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "租户空间", description = "租户空间创建与查询。NEXIS 支持集团、公司和项目；集团仅由平台开通。")
@RequestMapping({"/api/{appCode}/tenants/workspaces", "/api/{appCode}/tenants/projects"})
public class TenantWorkspaceController {

    private final TenantWorkspaceFlowService tenantWorkspaceFlowService;

    public TenantWorkspaceController(TenantWorkspaceFlowService tenantWorkspaceFlowService) {
        this.tenantWorkspaceFlowService = tenantWorkspaceFlowService;
    }

    @Operation(summary = "查询当前可见租户空间列表", description = "返回当前账号可见的租户空间列表。NEXIS 公司可看到直属项目，项目只看到自身。PRIMELOAD-MARKETPLACE 返回商户。")
    @GetMapping
    public Result<List<TenantSummaryResponse>> list(@Parameter(description = "应用编码，例如 NEXIS、PRIMELOAD-MARKETPLACE")
                                                    @PathVariable("appCode") String appCode,
                                                    LoginUser loginUser) {
        return Result.success(tenantWorkspaceFlowService.list(appCode, loginUser));
    }

    @Operation(summary = "查询我的空间分组", description = "返回当前账号直接加入的公司、独立项目、已归属项目，以及所在公司下的直属项目。用于前端空间选择页。")
    @GetMapping("/my")
    public Result<TenantWorkspaceMyResponse> listMy(@Parameter(description = "应用编码，例如 NEXIS")
                                                    @PathVariable("appCode") String appCode,
                                                    LoginUser loginUser) {
        return Result.success(tenantWorkspaceFlowService.listMyWorkspaces(appCode, loginUser));
    }

    @Operation(summary = "查询租户空间详情", description = "按 tenantId 返回租户空间详情。")
    @GetMapping("/detail/{tenantId}")
    public Result<TenantSummaryResponse> get(@Parameter(description = "应用编码，例如 NEXIS、PRIMELOAD-MARKETPLACE")
                                             @PathVariable("appCode") String appCode,
                                             @Parameter(description = "租户空间ID")
                                             @PathVariable("tenantId") Long tenantId,
                                             LoginUser loginUser) {
        return Result.success(tenantWorkspaceFlowService.getById(appCode, tenantId, loginUser));
    }

    @Operation(summary = "搜索可绑定公司", description = "搜索 NEXIS 中可用于参建单位绑定的公司租户。不会返回项目租户。")
    @GetMapping("/bindable-organizations")
    public Result<List<TenantSummaryResponse>> searchBindableOrganizations(@Parameter(description = "应用编码，例如 NEXIS")
                                                                           @PathVariable("appCode") String appCode,
                                                                           @Parameter(description = "关键词，匹配名称、编码、负责人或手机号")
                                                                           @RequestParam("keyword") String keyword,
                                                                           LoginUser loginUser) {
        return Result.success(tenantWorkspaceFlowService.searchBindableOrganizations(appCode, keyword, loginUser));
    }

    @Operation(summary = "创建租户空间", description = "NEXIS 普通用户可创建 COMPANY、PROJECT；GROUP 仅允许 WSGM 平台超级管理员代开。公司可挂集团，项目可独立创建或挂到集团/公司。")
    @PostMapping
    public Result<TenantCreateResponse> create(@Parameter(description = "应用编码，例如 NEXIS、PRIMELOAD-MARKETPLACE")
                                                      @PathVariable("appCode") String appCode,
                                                      @RequestBody TenantCreateRequest request,
                                                      LoginUser loginUser) {
        return Result.success(tenantWorkspaceFlowService.create(appCode, request, loginUser));
    }

    @Operation(summary = "调整项目组织归属", description = "把独立项目归入集团或公司，或传空 parentTenantId 将项目转回独立项目。操作人必须同时具备项目和目标组织管理权限。")
    @PostMapping("/{projectTenantId}/parent")
    public Result<TenantSummaryResponse> attachParent(@Parameter(description = "应用编码，例如 NEXIS")
                                                      @PathVariable("appCode") String appCode,
                                                      @Parameter(description = "项目租户ID")
                                                      @PathVariable("projectTenantId") Long projectTenantId,
                                                      @RequestBody TenantWorkspaceAttachParentRequest request,
                                                      LoginUser loginUser) {
        return Result.success(tenantWorkspaceFlowService.attachParent(appCode, projectTenantId, request, loginUser));
    }
}
