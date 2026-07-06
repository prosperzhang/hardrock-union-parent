package com.hardrockunion.platform.tenant.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.tenant.dto.TenantMemberActionRequest;
import com.hardrockunion.platform.tenant.dto.TenantMemberAssignRequest;
import com.hardrockunion.platform.tenant.dto.TenantMemberResponse;
import com.hardrockunion.platform.tenant.service.TenantMemberFlowService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "租户成员", description = "租户空间成员列表、移出与部门角色分配。")
@RequestMapping("/api/{appCode}/tenants/{tenantId}/members")
public class TenantMemberController {

    private final TenantMemberFlowService tenantMemberFlowService;

    public TenantMemberController(TenantMemberFlowService tenantMemberFlowService) {
        this.tenantMemberFlowService = tenantMemberFlowService;
    }

    @Operation(summary = "查询租户空间成员列表", description = "返回指定租户空间下的成员、部门和角色信息。")
    @GetMapping
    public Result<List<TenantMemberResponse>> list(@Parameter(description = "应用编码，例如 NEXIS")
                                                   @PathVariable("appCode") String appCode,
                                                   @Parameter(description = "租户空间ID")
                                                   @PathVariable("tenantId") Long tenantId,
                                                   LoginUser loginUser) {
        return Result.success(tenantMemberFlowService.listMembers(appCode, tenantId, loginUser));
    }

    @Operation(summary = "分配成员部门和角色", description = "管理员先选部门，再从该部门可分配角色中选择角色，最终写入租户成员部门角色关系。")
    @PutMapping("/{memberId}/department-roles")
    public Result<TenantMemberResponse> assignDepartmentRoles(@Parameter(description = "应用编码，例如 NEXIS")
                                                              @PathVariable("appCode") String appCode,
                                                              @Parameter(description = "租户空间ID")
                                                              @PathVariable("tenantId") Long tenantId,
                                                              @Parameter(description = "成员ID")
                                                              @PathVariable("memberId") Long memberId,
                                                              @RequestBody TenantMemberAssignRequest request,
                                                              LoginUser loginUser) {
        return Result.success(tenantMemberFlowService.assignDepartmentRoles(
            appCode,
            tenantId,
            memberId,
            request,
            loginUser
        ));
    }

    @Operation(summary = "移出租户空间成员", description = "把指定成员移出租户空间，并停用其部门角色绑定。")
    @PostMapping("/{memberId}/remove")
    public Result<TenantMemberResponse> remove(@Parameter(description = "应用编码，例如 NEXIS")
                                               @PathVariable("appCode") String appCode,
                                               @Parameter(description = "租户空间ID")
                                               @PathVariable("tenantId") Long tenantId,
                                               @Parameter(description = "成员ID")
                                               @PathVariable("memberId") Long memberId,
                                               @RequestBody(required = false) TenantMemberActionRequest request,
                                               LoginUser loginUser) {
        return Result.success(tenantMemberFlowService.removeMember(
            appCode,
            tenantId,
            memberId,
            request == null ? null : request.getRemark(),
            loginUser
        ));
    }
}
