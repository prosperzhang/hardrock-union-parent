package com.hardrockunion.platform.iam.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.platform.iam.dto.IamRoleResponse;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.iam.dto.IamUserCreateRequest;
import com.hardrockunion.platform.iam.dto.IamUserRoleAssignRequest;
import com.hardrockunion.platform.iam.dto.IamUserResponse;
import com.hardrockunion.platform.iam.service.IamUserManageService;

/**
 * app 级员工管理入口。
 *
 * <p>当前主要服务于 `primeload-marketplace` 这类租户后台，用统一 IAM 账号体系管理商户员工。
 */
@RestController
@RequestMapping("/api/{appCode}/users")
public class IamUserController {

    private final IamUserManageService iamUserManageService;

    public IamUserController(IamUserManageService iamUserManageService) {
        this.iamUserManageService = iamUserManageService;
    }

    /**
     * 查询当前 app、当前租户下的员工列表。
     */
    @GetMapping
    public Result<List<IamUserResponse>> list(@PathVariable("appCode") String appCode, LoginUser loginUser) {
        return Result.success(iamUserManageService.listUsers(appCode, loginUser));
    }

    /**
     * 创建员工并分配角色。
     */
    @PostMapping
    public Result<IamUserResponse> create(@PathVariable("appCode") String appCode,
                                          @RequestBody IamUserCreateRequest request,
                                          LoginUser loginUser) {
        return Result.success(iamUserManageService.createUser(appCode, request, loginUser));
    }

    /**
     * 查询指定用户当前已绑定的角色。
     */
    @GetMapping("/{userId}/roles")
    public Result<List<IamRoleResponse>> listUserRoles(@PathVariable("appCode") String appCode,
                                                       @PathVariable("userId") Long userId,
                                                       LoginUser loginUser) {
        return Result.success(iamUserManageService.listUserRoles(appCode, userId, loginUser));
    }

    /**
     * 重置指定用户的角色集合。
     */
    @PutMapping("/{userId}/roles")
    public Result<IamUserResponse> assignUserRoles(@PathVariable("appCode") String appCode,
                                                   @PathVariable("userId") Long userId,
                                                   @RequestBody IamUserRoleAssignRequest request,
                                                   LoginUser loginUser) {
        return Result.success(iamUserManageService.assignUserRoles(appCode, userId, request, loginUser));
    }
}
