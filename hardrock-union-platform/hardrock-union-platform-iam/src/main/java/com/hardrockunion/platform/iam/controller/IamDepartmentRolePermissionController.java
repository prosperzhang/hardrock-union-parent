package com.hardrockunion.platform.iam.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.iam.dto.IamDepartmentRolePermissionAssignRequest;
import com.hardrockunion.platform.iam.dto.IamPermissionResponse;
import com.hardrockunion.platform.iam.service.IamPermissionManageService;
import com.hardrockunion.platform.iam.service.IamPermissionQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "IAM-部门角色权限", description = "部门角色与权限的绑定关系。只有 WSGM 可以管理部门角色权限。")
@RestController
@RequestMapping("/api/{appCode}/departments/{departmentId}/roles/{roleId}/permissions")
public class IamDepartmentRolePermissionController {

    private final IamPermissionQueryService iamPermissionQueryService;
    private final IamPermissionManageService iamPermissionManageService;

    public IamDepartmentRolePermissionController(IamPermissionQueryService iamPermissionQueryService,
                                                 IamPermissionManageService iamPermissionManageService) {
        this.iamPermissionQueryService = iamPermissionQueryService;
        this.iamPermissionManageService = iamPermissionManageService;
    }

    @Operation(summary = "查询部门角色权限", description = "返回指定部门角色当前已绑定的权限。只有 WSGM 登录态允许调用。")
    @GetMapping
    public Result<List<IamPermissionResponse>> list(@Parameter(description = "应用编码，例如 WSGM、NEXIS、PRIMELOAD-MARKETPLACE")
                                                    @PathVariable("appCode") String appCode,
                                                    @Parameter(description = "部门ID")
                                                    @PathVariable("departmentId") Long departmentId,
                                                    @Parameter(description = "角色ID")
                                                    @PathVariable("roleId") Long roleId,
                                                    LoginUser loginUser) {
        return Result.success(iamPermissionQueryService.listPermissionsByDepartmentRole(appCode, departmentId, roleId, loginUser));
    }

    @Operation(summary = "更新部门角色权限", description = "覆盖式更新部门角色绑定的权限集合。只有 WSGM 登录态允许调用。")
    @PutMapping
    public Result<List<IamPermissionResponse>> assign(@Parameter(description = "应用编码，例如 WSGM、NEXIS、PRIMELOAD-MARKETPLACE")
                                                      @PathVariable("appCode") String appCode,
                                                      @Parameter(description = "部门ID")
                                                      @PathVariable("departmentId") Long departmentId,
                                                      @Parameter(description = "角色ID")
                                                      @PathVariable("roleId") Long roleId,
                                                      @RequestBody IamDepartmentRolePermissionAssignRequest request,
                                                      LoginUser loginUser) {
        return Result.success(iamPermissionManageService.assignDepartmentRolePermissions(appCode, departmentId, roleId, request, loginUser));
    }
}
