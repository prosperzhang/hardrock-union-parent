package com.hardrockunion.platform.tenant.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
@Tag(name = "租户空间", description = "租户空间创建与查询。NEXIS 支持集团、公司和项目；PRIMELOAD-MARKETPLACE 中 tenant 即 merchant。")
@RequestMapping({"/api/{appCode}/tenants/workspaces", "/api/{appCode}/tenants/projects"})
public class TenantWorkspaceController {

    private final TenantWorkspaceFlowService tenantWorkspaceFlowService;

    public TenantWorkspaceController(TenantWorkspaceFlowService tenantWorkspaceFlowService) {
        this.tenantWorkspaceFlowService = tenantWorkspaceFlowService;
    }

    @Operation(summary = "查询当前可见租户空间列表", description = "返回当前账号可见的租户空间列表。NEXIS 返回集团、公司或项目；公司登录态可看到直属项目。PRIMELOAD-MARKETPLACE 返回商户。")
    @GetMapping
    public Result<List<TenantSummaryResponse>> list(@Parameter(description = "应用编码，例如 NEXIS、PRIMELOAD-MARKETPLACE")
                                                    @PathVariable("appCode") String appCode,
                                                    LoginUser loginUser) {
        return Result.success(tenantWorkspaceFlowService.list(appCode, loginUser));
    }

    @Operation(summary = "查询我的空间分组", description = "返回当前账号直接加入的集团/公司、独立项目、已归属项目，以及所在集团/公司下的直属项目。用于前端空间选择页。")
    @GetMapping("/my")
    public Result<TenantWorkspaceMyResponse> listMy(@Parameter(description = "应用编码，例如 NEXIS")
                                                    @PathVariable("appCode") String appCode,
                                                    LoginUser loginUser) {
        return Result.success(tenantWorkspaceFlowService.listMyWorkspaces(appCode, loginUser));
    }

    @Operation(summary = "查询租户空间详情", description = "按 tenantId 返回租户空间详情。")
    @GetMapping("/{tenantId}")
    public Result<TenantSummaryResponse> get(@Parameter(description = "应用编码，例如 NEXIS、PRIMELOAD-MARKETPLACE")
                                             @PathVariable("appCode") String appCode,
                                             @Parameter(description = "租户空间ID")
                                             @PathVariable("tenantId") Long tenantId,
                                             LoginUser loginUser) {
        return Result.success(tenantWorkspaceFlowService.getById(appCode, tenantId, loginUser));
    }

    @Operation(summary = "创建租户空间", description = "创建当前 app 的租户空间，并自动把创建者加入默认决策部管理员角色。NEXIS 可通过 tenantType 创建 GROUP、COMPANY、PROJECT，项目可通过 parentTenantId 挂到公司或集团下。")
    @PostMapping
    public Result<TenantCreateResponse> create(@Parameter(description = "应用编码，例如 NEXIS、PRIMELOAD-MARKETPLACE")
                                                      @PathVariable("appCode") String appCode,
                                                      @RequestBody TenantCreateRequest request,
                                                      LoginUser loginUser) {
        return Result.success(tenantWorkspaceFlowService.create(appCode, request, loginUser));
    }

    @Operation(summary = "调整项目公司/集团归属", description = "把独立项目归入公司/集团，或传空 parentTenantId 将项目转回独立项目。操作人必须是项目管理员；归入公司/集团时也必须是目标公司/集团管理员。")
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
