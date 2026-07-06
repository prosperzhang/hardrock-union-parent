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
import com.hardrockunion.platform.tenant.service.TenantWorkspaceFlowService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "租户空间", description = "租户空间创建与查询。PMHUB 支持集团、公司和项目；PRIMELOAD-MARKETPLACE 中 tenant 即 merchant。")
@RequestMapping({"/api/{appCode}/tenants/workspaces", "/api/{appCode}/tenants/projects"})
public class TenantWorkspaceController {

    private final TenantWorkspaceFlowService tenantWorkspaceFlowService;

    public TenantWorkspaceController(TenantWorkspaceFlowService tenantWorkspaceFlowService) {
        this.tenantWorkspaceFlowService = tenantWorkspaceFlowService;
    }

    @Operation(summary = "查询当前可见租户空间列表", description = "返回当前账号可见的租户空间列表。PMHUB 返回集团、公司或项目；公司登录态可看到直属项目。PRIMELOAD-MARKETPLACE 返回商户。")
    @GetMapping
    public Result<List<TenantSummaryResponse>> list(@Parameter(description = "应用编码，例如 PMHUB、PRIMELOAD-MARKETPLACE")
                                                    @PathVariable("appCode") String appCode,
                                                    LoginUser loginUser) {
        return Result.success(tenantWorkspaceFlowService.list(appCode, loginUser));
    }

    @Operation(summary = "查询租户空间详情", description = "按 tenantId 返回租户空间详情。")
    @GetMapping("/{tenantId}")
    public Result<TenantSummaryResponse> get(@Parameter(description = "应用编码，例如 PMHUB、PRIMELOAD-MARKETPLACE")
                                             @PathVariable("appCode") String appCode,
                                             @Parameter(description = "租户空间ID")
                                             @PathVariable("tenantId") Long tenantId,
                                             LoginUser loginUser) {
        return Result.success(tenantWorkspaceFlowService.getById(appCode, tenantId, loginUser));
    }

    @Operation(summary = "创建租户空间", description = "创建当前 app 的租户空间，并自动把创建者加入默认决策部管理员角色。PMHUB 可通过 tenantType 创建 GROUP、COMPANY、PROJECT，项目可通过 parentTenantId 挂到公司或集团下。")
    @PostMapping
    public Result<TenantCreateResponse> create(@Parameter(description = "应用编码，例如 PMHUB、PRIMELOAD-MARKETPLACE")
                                                      @PathVariable("appCode") String appCode,
                                                      @RequestBody TenantCreateRequest request,
                                                      LoginUser loginUser) {
        return Result.success(tenantWorkspaceFlowService.create(appCode, request, loginUser));
    }
}
