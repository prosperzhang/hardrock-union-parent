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
import com.hardrockunion.platform.tenant.dto.TenantJoinRequestCreateRequest;
import com.hardrockunion.platform.tenant.dto.TenantJoinRequestResponse;
import com.hardrockunion.platform.tenant.dto.TenantJoinRequestReviewRequest;
import com.hardrockunion.platform.tenant.service.TenantJoinRequestFlowService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "租户加入申请", description = "租户空间加入申请、审批与撤销。")
@RequestMapping("/api/{appCode}/tenant-join-requests")
public class TenantJoinRequestController {

    private final TenantJoinRequestFlowService tenantJoinRequestFlowService;

    public TenantJoinRequestController(TenantJoinRequestFlowService tenantJoinRequestFlowService) {
        this.tenantJoinRequestFlowService = tenantJoinRequestFlowService;
    }

    @Operation(summary = "提交加入租户空间申请", description = "按 tenantId 或 tenantKeyword 提交加入当前 app 租户空间申请。")
    @PostMapping
    public Result<TenantJoinRequestResponse> create(@Parameter(description = "应用编码，例如 NEXIS、PRIMELOAD-MARKETPLACE")
                                                    @PathVariable("appCode") String appCode,
                                                    @RequestBody TenantJoinRequestCreateRequest request,
                                                    LoginUser loginUser) {
        return Result.success(tenantJoinRequestFlowService.create(appCode, request, loginUser));
    }

    @Operation(summary = "撤销加入申请", description = "撤销当前用户自己提交且仍处于待审批状态的申请。")
    @PostMapping("/{requestId}/cancel")
    public Result<TenantJoinRequestResponse> cancel(@Parameter(description = "应用编码，例如 NEXIS、PRIMELOAD-MARKETPLACE")
                                                    @PathVariable("appCode") String appCode,
                                                    @Parameter(description = "加入申请ID")
                                                    @PathVariable("requestId") Long requestId,
                                                    LoginUser loginUser) {
        return Result.success(tenantJoinRequestFlowService.cancel(appCode, requestId, loginUser));
    }

    @Operation(summary = "查询租户空间加入申请列表", description = "返回指定租户空间下的加入申请列表，仅管理员可查看。")
    @GetMapping("/tenants/{tenantId}")
    public Result<List<TenantJoinRequestResponse>> listByTenantRegistry(@Parameter(description = "应用编码，例如 NEXIS、PRIMELOAD-MARKETPLACE")
                                                                @PathVariable("appCode") String appCode,
                                                                @Parameter(description = "租户空间ID")
                                                                @PathVariable("tenantId") Long tenantId,
                                                                LoginUser loginUser) {
        return Result.success(tenantJoinRequestFlowService.listByTenantRegistry(appCode, tenantId, loginUser));
    }

    @Operation(summary = "审批通过加入申请", description = "审批通过后会创建或激活租户成员关系。")
    @PostMapping("/tenants/{tenantId}/{requestId}/approve")
    public Result<TenantJoinRequestResponse> approve(@Parameter(description = "应用编码，例如 NEXIS、PRIMELOAD-MARKETPLACE")
                                                     @PathVariable("appCode") String appCode,
                                                     @Parameter(description = "租户空间ID")
                                                     @PathVariable("tenantId") Long tenantId,
                                                     @Parameter(description = "加入申请ID")
                                                     @PathVariable("requestId") Long requestId,
                                                     @RequestBody(required = false) TenantJoinRequestReviewRequest request,
                                                     LoginUser loginUser) {
        return Result.success(tenantJoinRequestFlowService.approve(appCode, tenantId, requestId, request, loginUser));
    }

    @Operation(summary = "拒绝加入申请", description = "拒绝指定的租户空间加入申请。")
    @PostMapping("/tenants/{tenantId}/{requestId}/reject")
    public Result<TenantJoinRequestResponse> reject(@Parameter(description = "应用编码，例如 NEXIS、PRIMELOAD-MARKETPLACE")
                                                    @PathVariable("appCode") String appCode,
                                                    @Parameter(description = "租户空间ID")
                                                    @PathVariable("tenantId") Long tenantId,
                                                    @Parameter(description = "加入申请ID")
                                                    @PathVariable("requestId") Long requestId,
                                                    @RequestBody(required = false) TenantJoinRequestReviewRequest request,
                                                    LoginUser loginUser) {
        return Result.success(tenantJoinRequestFlowService.reject(appCode, tenantId, requestId, request, loginUser));
    }
}
