package com.hardrockunion.platform.iam.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.iam.dto.IamRoleCreateRequest;
import com.hardrockunion.platform.iam.dto.IamRoleResponse;
import com.hardrockunion.platform.iam.dto.IamRoleUpdateRequest;
import com.hardrockunion.platform.iam.service.IamRoleManageService;
import com.hardrockunion.platform.iam.service.IamRoleQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "IAM-角色", description = "角色查询与角色管理。只有 WSGM 可以管理 WSGM、NEXIS、PRIMELOAD-MARKETPLACE 三端角色；NEXIS、PRIMELOAD-MARKETPLACE 只能使用角色，不能新增、修改、删除角色。")
@RestController
@RequestMapping("/api/{appCode}/roles")
public class IamRoleController {

    private final IamRoleQueryService iamRoleQueryService;
    private final IamRoleManageService iamRoleManageService;

    public IamRoleController(IamRoleQueryService iamRoleQueryService,
                             IamRoleManageService iamRoleManageService) {
        this.iamRoleQueryService = iamRoleQueryService;
        this.iamRoleManageService = iamRoleManageService;
    }

    @Operation(summary = "查询当前用户角色", description = "返回当前登录用户在当前 app、当前租户下已绑定的角色。")
    @GetMapping
    public Result<List<IamRoleResponse>> list(@Parameter(description = "应用编码，例如 WSGM、NEXIS、PRIMELOAD-MARKETPLACE")
                                              @PathVariable("appCode") String appCode,
                                              LoginUser loginUser) {
        return Result.success(iamRoleQueryService.listRoles(appCode, loginUser));
    }

    @Operation(summary = "查询可分配共享角色", description = "返回当前 app 下可分配的共享角色。NEXIS、PRIMELOAD-MARKETPLACE 虽然不能管理角色，但仍然可以查询自己可分配的角色。")
    @GetMapping("/available")
    public Result<List<IamRoleResponse>> listSharedRoles(@Parameter(description = "应用编码，例如 WSGM、NEXIS、PRIMELOAD-MARKETPLACE")
                                                         @PathVariable("appCode") String appCode,
                                                         LoginUser loginUser) {
        return Result.success(iamRoleQueryService.listSharedRoles(appCode, loginUser));
    }

    @Operation(summary = "查询角色管理列表", description = "返回指定 app 的角色管理列表。只有 WSGM 登录态允许调用。")
    @GetMapping("/manage")
    public Result<List<IamRoleResponse>> listManageRoles(@Parameter(description = "要管理的目标应用编码，例如 WSGM、NEXIS、PRIMELOAD-MARKETPLACE")
                                                         @PathVariable("appCode") String appCode,
                                                         LoginUser loginUser) {
        return Result.success(iamRoleQueryService.listManageRoles(appCode, loginUser));
    }

    @Operation(summary = "查询角色详情", description = "返回指定角色的完整定义。只有 WSGM 登录态允许调用。")
    @GetMapping("/manage/{roleId}")
    public Result<IamRoleResponse> getRoleDetail(@Parameter(description = "要管理的目标应用编码，例如 WSGM、NEXIS、PRIMELOAD-MARKETPLACE")
                                                 @PathVariable("appCode") String appCode,
                                                 @Parameter(description = "角色ID")
                                                 @PathVariable("roleId") Long roleId,
                                                 LoginUser loginUser) {
        return Result.success(iamRoleQueryService.getRoleDetail(appCode, roleId, loginUser));
    }

    @Operation(summary = "创建角色", description = "为指定 app 创建一个共享角色。只有 WSGM 登录态允许调用。")
    @PostMapping("/manage")
    public Result<IamRoleResponse> createRole(@Parameter(description = "要管理的目标应用编码，例如 WSGM、NEXIS、PRIMELOAD-MARKETPLACE")
                                              @PathVariable("appCode") String appCode,
                                              @RequestBody IamRoleCreateRequest request,
                                              LoginUser loginUser) {
        return Result.success(iamRoleManageService.createRole(appCode, request, loginUser));
    }

    @Operation(summary = "更新角色", description = "更新指定角色的名称、状态、是否可分配、是否管理员角色。只有 WSGM 登录态允许调用。")
    @PutMapping("/manage/{roleId}")
    public Result<IamRoleResponse> updateRole(@Parameter(description = "要管理的目标应用编码，例如 WSGM、NEXIS、PRIMELOAD-MARKETPLACE")
                                              @PathVariable("appCode") String appCode,
                                              @Parameter(description = "角色ID")
                                              @PathVariable("roleId") Long roleId,
                                              @RequestBody IamRoleUpdateRequest request,
                                              LoginUser loginUser) {
        return Result.success(iamRoleManageService.updateRole(appCode, roleId, request, loginUser));
    }

    @Operation(summary = "删除角色", description = "逻辑删除指定角色。只有 WSGM 登录态允许调用；如果角色已分配给用户，则不允许删除。")
    @DeleteMapping("/manage/{roleId}")
    public Result<Void> deleteRole(@Parameter(description = "要管理的目标应用编码，例如 WSGM、NEXIS、PRIMELOAD-MARKETPLACE")
                                   @PathVariable("appCode") String appCode,
                                   @Parameter(description = "角色ID")
                                   @PathVariable("roleId") Long roleId,
                                   LoginUser loginUser) {
        iamRoleManageService.deleteRole(appCode, roleId, loginUser);
        return Result.success(null);
    }
}
