package com.hardrockunion.platform.iam.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.iam.dto.IamPermissionMatrixResponse;
import com.hardrockunion.platform.iam.service.IamPermissionMatrixQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "IAM-权限矩阵", description = "面向前端权限配置页的部门、角色、菜单权限聚合视图。")
@RestController
@RequestMapping("/api/{appCode}/permission-matrix")
public class IamPermissionMatrixController {

    private final IamPermissionMatrixQueryService iamPermissionMatrixQueryService;

    public IamPermissionMatrixController(IamPermissionMatrixQueryService iamPermissionMatrixQueryService) {
        this.iamPermissionMatrixQueryService = iamPermissionMatrixQueryService;
    }

    @Operation(summary = "查询权限矩阵", description = "返回当前 app 的菜单权限树，以及部门 -> 角色 -> 已绑定菜单权限。只有 WSGM 权限管理员可调用。")
    @GetMapping
    public Result<IamPermissionMatrixResponse> detail(@Parameter(description = "应用编码，例如 WSGM、NEXIS、PRIMELOAD-MARKETPLACE")
                                                      @PathVariable("appCode") String appCode,
                                                      LoginUser loginUser) {
        return Result.success(iamPermissionMatrixQueryService.getPermissionMatrix(appCode, loginUser));
    }
}
