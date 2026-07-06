package com.hardrockunion.platform.iam.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;

import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.iam.dto.CurrentUserResponse;
import com.hardrockunion.platform.iam.dto.IamDepartmentResponse;
import com.hardrockunion.platform.iam.dto.IamTenantMemberResponse;
import com.hardrockunion.platform.iam.dto.IamTenantMemberSwitchRequest;
import com.hardrockunion.platform.iam.dto.IamUserProfileUpdateRequest;
import com.hardrockunion.platform.iam.dto.LoginRequest;
import com.hardrockunion.platform.iam.dto.LoginResponse;
import com.hardrockunion.platform.iam.dto.RegisterRequest;
import com.hardrockunion.platform.iam.dto.IamUserDepartmentResponse;
import com.hardrockunion.platform.iam.dto.IamUserDepartmentSwitchRequest;
import com.hardrockunion.platform.iam.service.IamAuthService;
import com.hardrockunion.platform.iam.service.IamDepartmentManageService;
import com.hardrockunion.platform.iam.service.IamDepartmentQueryService;
import com.hardrockunion.platform.iam.service.AppRegistryQueryService;
import com.hardrockunion.platform.iam.service.IamUserQueryService;
import com.hardrockunion.platform.iam.service.IamUserInfoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "IAM-认证", description = "登录、当前用户信息以及当前用户部门切换。")
@RestController
@RequestMapping("/api/{appCode}/auth")
public class IamAuthController {

    private final IamAuthService iamAuthService;
    private final IamUserQueryService iamUserQueryService;
    private final IamDepartmentQueryService iamDepartmentQueryService;
    private final IamDepartmentManageService iamDepartmentManageService;
    private final IamUserInfoService iamUserInfoService;
    private final AppRegistryQueryService appRegistryQueryService;

    public IamAuthController(IamAuthService iamAuthService,
                             IamUserQueryService iamUserQueryService,
                             IamDepartmentQueryService iamDepartmentQueryService,
                             IamDepartmentManageService iamDepartmentManageService,
                             IamUserInfoService iamUserInfoService,
                             AppRegistryQueryService appRegistryQueryService) {
        this.iamAuthService = iamAuthService;
        this.iamUserQueryService = iamUserQueryService;
        this.iamDepartmentQueryService = iamDepartmentQueryService;
        this.iamDepartmentManageService = iamDepartmentManageService;
        this.iamUserInfoService = iamUserInfoService;
        this.appRegistryQueryService = appRegistryQueryService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@PathVariable("appCode") String appCode, @RequestBody LoginRequest request) {
        return Result.success(iamAuthService.login(appCode, request));
    }

    @Operation(summary = "注册账号", description = "在指定 app 下注册账号。注册只创建 IAM 账号，不加入租户、不分配部门角色。")
    @PostMapping("/register")
    public Result<LoginResponse> register(@PathVariable("appCode") String appCode, @RequestBody RegisterRequest request) {
        return Result.success(iamAuthService.register(appCode, request));
    }

    @GetMapping("/me")
    public Result<CurrentUserResponse> currentUser(@PathVariable("appCode") String appCode, LoginUser loginUser) {
        return Result.success(iamUserQueryService.currentUser(appCode, loginUser));
    }

    @Operation(summary = "更新当前用户资料", description = "注册成功后立即完善昵称和头像。昵称必填，头像可为空。")
    @PutMapping("/me/profile")
    public Result<CurrentUserResponse> updateCurrentUserProfile(@PathVariable("appCode") String appCode,
                                                                @RequestBody IamUserProfileUpdateRequest request,
                                                                LoginUser loginUser) {
        Long appId = appRegistryQueryService.getAppByCode(appCode).getId();
        iamUserInfoService.saveRequiredProfile(appId, loginUser.getUserId(), request.getNickName(), request.getAvatarUrl());
        return Result.success(iamUserQueryService.currentUser(appCode, loginUser));
    }

    @Operation(summary = "查询当前用户所属租户列表", description = "返回当前登录用户在该 app 下的所有租户成员关系，包含主租户标记。")
    @GetMapping("/me/tenants")
    public Result<List<IamTenantMemberResponse>> currentUserTenants(@PathVariable("appCode") String appCode,
                                                                    LoginUser loginUser) {
        return Result.success(iamAuthService.listCurrentUserTenants(appCode, loginUser.getUserId()));
    }

    @Operation(summary = "切换当前用户主租户", description = "把当前登录用户的主租户切换到已加入的另一个租户，并返回新的 token。")
    @PutMapping("/me/tenant")
    public Result<LoginResponse> switchCurrentUserTenant(@PathVariable("appCode") String appCode,
                                                         @RequestBody IamTenantMemberSwitchRequest request,
                                                         LoginUser loginUser) {
        return Result.success(iamAuthService.switchCurrentUserTenant(appCode, loginUser.getUserId(), request));
    }

    @Operation(summary = "查询当前用户部门列表", description = "返回当前登录用户所属的所有部门，包含主部门标记。")
    @GetMapping("/me/departments")
    public Result<List<IamUserDepartmentResponse>> currentUserDepartments(@PathVariable("appCode") String appCode,
                                                                          LoginUser loginUser) {
        return Result.success(iamDepartmentQueryService.listUserDepartments(loginUser.getUserId(), appCode, loginUser));
    }

    @Operation(summary = "切换当前用户主部门", description = "把当前登录用户的主部门切换到已加入的另一个部门。")
    @PutMapping("/me/department")
    public Result<IamDepartmentResponse> switchCurrentUserDepartment(@PathVariable("appCode") String appCode,
                                                                     @RequestBody IamUserDepartmentSwitchRequest request,
                                                                     LoginUser loginUser) {
        return Result.success(iamDepartmentManageService.switchUserDepartment(appCode, loginUser.getUserId(), request, loginUser));
    }
}
