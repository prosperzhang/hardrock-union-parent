package com.hardrockunion.platform.iam.controller;

import java.util.List;

import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.iam.dto.IamDepartmentResponse;
import com.hardrockunion.platform.iam.dto.IamUserDepartmentResponse;
import com.hardrockunion.platform.iam.dto.IamUserDepartmentAssignRequest;
import com.hardrockunion.platform.iam.dto.IamUserDepartmentSwitchRequest;
import com.hardrockunion.platform.iam.service.IamDepartmentManageService;
import com.hardrockunion.platform.iam.service.IamDepartmentQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "IAM-用户部门", description = "用户与部门的绑定关系。部门是用户权限的主组织单元。")
@RestController
@RequestMapping("/api/{appCode}/users")
public class IamUserDepartmentController {

    private final IamDepartmentQueryService iamDepartmentQueryService;
    private final IamDepartmentManageService iamDepartmentManageService;

    public IamUserDepartmentController(IamDepartmentQueryService iamDepartmentQueryService,
                                       IamDepartmentManageService iamDepartmentManageService) {
        this.iamDepartmentQueryService = iamDepartmentQueryService;
        this.iamDepartmentManageService = iamDepartmentManageService;
    }

    @Operation(summary = "查询用户主部门", description = "返回指定用户当前绑定的主部门。")
    @GetMapping("/{userId}/department")
    public Result<IamDepartmentResponse> getUserDepartment(@Parameter(description = "应用编码，例如 WSGM、NEXIS、PRIMELOAD-MARKETPLACE")
                                                            @PathVariable("appCode") String appCode,
                                                            @Parameter(description = "用户ID")
                                                            @PathVariable("userId") Long userId,
                                                            LoginUser loginUser) {
        return Result.success(toResponse(iamDepartmentQueryService.getPrimaryDepartmentByUser(userId, appCode, loginUser.getTenantId())));
    }

    @Operation(summary = "绑定用户主部门", description = "把指定用户绑定到一个部门，并设为主部门。")
    @PutMapping("/{userId}/department")
    public Result<IamDepartmentResponse> assignUserDepartment(@Parameter(description = "应用编码，例如 WSGM、NEXIS、PRIMELOAD-MARKETPLACE")
                                                              @PathVariable("appCode") String appCode,
                                                              @Parameter(description = "用户ID")
                                                              @PathVariable("userId") Long userId,
                                                              @RequestBody IamUserDepartmentAssignRequest request,
                                                              LoginUser loginUser) {
        return Result.success(iamDepartmentManageService.assignUserDepartment(appCode, userId, request, loginUser));
    }

    @Operation(summary = "查询用户部门列表", description = "返回指定用户当前所属的所有部门，包含主部门标记。")
    @GetMapping("/{userId}/departments")
    public Result<List<IamUserDepartmentResponse>> listUserDepartments(@Parameter(description = "应用编码，例如 WSGM、NEXIS、PRIMELOAD-MARKETPLACE")
                                                                       @PathVariable("appCode") String appCode,
                                                                       @Parameter(description = "用户ID")
                                                                       @PathVariable("userId") Long userId,
                                                                       LoginUser loginUser) {
        return Result.success(iamDepartmentQueryService.listUserDepartments(userId, appCode, loginUser));
    }

    @Operation(summary = "切换用户主部门", description = "把指定用户的主部门切换到已加入的另一个部门。")
    @PutMapping("/{userId}/department/switch")
    public Result<IamDepartmentResponse> switchUserDepartment(@Parameter(description = "应用编码，例如 WSGM、NEXIS、PRIMELOAD-MARKETPLACE")
                                                              @PathVariable("appCode") String appCode,
                                                              @Parameter(description = "用户ID")
                                                              @PathVariable("userId") Long userId,
                                                              @RequestBody IamUserDepartmentSwitchRequest request,
                                                              LoginUser loginUser) {
        return Result.success(iamDepartmentManageService.switchUserDepartment(appCode, userId, request, loginUser));
    }

    private IamDepartmentResponse toResponse(com.hardrockunion.platform.iam.domain.entity.IamDepartment department) {
        if (department == null) {
            return null;
        }
        IamDepartmentResponse response = new IamDepartmentResponse();
        response.setId(department.getId());
        response.setAppCode(department.getAppCode());
        response.setDeptCode(department.getDeptCode());
        response.setDeptName(department.getDeptName());
        response.setParentId(department.getParentId());
        response.setDeptType(department.getDeptType());
        response.setStatus(department.getStatus());
        response.setSortNo(department.getSortNo());
        return response;
    }
}
