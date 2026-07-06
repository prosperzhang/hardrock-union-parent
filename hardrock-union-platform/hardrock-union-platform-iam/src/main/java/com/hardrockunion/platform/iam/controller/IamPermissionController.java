package com.hardrockunion.platform.iam.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.iam.dto.IamPermissionCreateRequest;
import com.hardrockunion.platform.iam.dto.IamPermissionResponse;
import com.hardrockunion.platform.iam.dto.IamPermissionUpdateRequest;
import com.hardrockunion.platform.iam.service.IamPermissionManageService;
import com.hardrockunion.platform.iam.service.IamPermissionQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "IAM-权限", description = "权限主数据。只有 WSGM 可以管理权限；PMHUB、PRIMELOAD-MARKETPLACE 通过角色继承权限，不直接创建权限。")
@RestController
@RequestMapping("/api/{appCode}/permissions")
public class IamPermissionController {

    private final IamPermissionQueryService iamPermissionQueryService;
    private final IamPermissionManageService iamPermissionManageService;

    public IamPermissionController(IamPermissionQueryService iamPermissionQueryService,
                                   IamPermissionManageService iamPermissionManageService) {
        this.iamPermissionQueryService = iamPermissionQueryService;
        this.iamPermissionManageService = iamPermissionManageService;
    }

    @Operation(summary = "查询权限列表", description = "返回当前 app 下的权限列表。只有 WSGM 登录态允许调用。")
    @GetMapping
    public Result<List<IamPermissionResponse>> list(@Parameter(description = "应用编码，例如 WSGM、PMHUB、PRIMELOAD-MARKETPLACE")
                                                    @PathVariable("appCode") String appCode,
                                                    LoginUser loginUser) {
        return Result.success(iamPermissionQueryService.listPermissions(appCode, loginUser));
    }

    @Operation(summary = "查询权限树", description = "返回当前 app 下的权限树。只有 WSGM 登录态允许调用。")
    @GetMapping("/tree")
    public Result<List<IamPermissionResponse>> tree(@Parameter(description = "应用编码，例如 WSGM、PMHUB、PRIMELOAD-MARKETPLACE")
                                                    @PathVariable("appCode") String appCode,
                                                    LoginUser loginUser) {
        return Result.success(iamPermissionQueryService.listPermissionTree(appCode, loginUser));
    }

    @Operation(summary = "查询权限详情", description = "返回指定权限的完整定义。只有 WSGM 登录态允许调用。")
    @GetMapping("/{permissionId}")
    public Result<IamPermissionResponse> detail(@Parameter(description = "应用编码，例如 WSGM、PMHUB、PRIMELOAD-MARKETPLACE")
                                                @PathVariable("appCode") String appCode,
                                                @Parameter(description = "权限ID")
                                                @PathVariable("permissionId") Long permissionId,
                                                LoginUser loginUser) {
        return Result.success(iamPermissionQueryService.getPermissionDetail(appCode, permissionId, loginUser));
    }

    @Operation(summary = "创建权限", description = "为指定 app 创建一个权限。只有 WSGM 登录态允许调用。")
    @PostMapping
    public Result<IamPermissionResponse> create(@Parameter(description = "应用编码，例如 WSGM、PMHUB、PRIMELOAD-MARKETPLACE")
                                                @PathVariable("appCode") String appCode,
                                                @RequestBody IamPermissionCreateRequest request,
                                                LoginUser loginUser) {
        return Result.success(iamPermissionManageService.createPermission(appCode, request, loginUser));
    }

    @Operation(summary = "更新权限", description = "更新权限名称、类型、父级、路径、组件、状态与排序。只有 WSGM 登录态允许调用。")
    @PutMapping("/{permissionId}")
    public Result<IamPermissionResponse> update(@Parameter(description = "应用编码，例如 WSGM、PMHUB、PRIMELOAD-MARKETPLACE")
                                                @PathVariable("appCode") String appCode,
                                                @Parameter(description = "权限ID")
                                                @PathVariable("permissionId") Long permissionId,
                                                @RequestBody IamPermissionUpdateRequest request,
                                                LoginUser loginUser) {
        return Result.success(iamPermissionManageService.updatePermission(appCode, permissionId, request, loginUser));
    }

    @Operation(summary = "删除权限", description = "逻辑删除指定权限。若权限仍有子权限或已分配给角色，则不允许删除。")
    @DeleteMapping("/{permissionId}")
    public Result<Void> delete(@Parameter(description = "应用编码，例如 WSGM、PMHUB、PRIMELOAD-MARKETPLACE")
                               @PathVariable("appCode") String appCode,
                               @Parameter(description = "权限ID")
                               @PathVariable("permissionId") Long permissionId,
                               LoginUser loginUser) {
        iamPermissionManageService.deletePermission(appCode, permissionId, loginUser);
        return Result.success(null);
    }
}
