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
import com.hardrockunion.platform.iam.dto.IamDepartmentCreateRequest;
import com.hardrockunion.platform.iam.dto.IamDepartmentResponse;
import com.hardrockunion.platform.iam.dto.IamDepartmentRoleAssignRequest;
import com.hardrockunion.platform.iam.dto.IamDepartmentUpdateRequest;
import com.hardrockunion.platform.iam.dto.IamUserResponse;
import com.hardrockunion.platform.iam.service.IamDepartmentManageService;
import com.hardrockunion.platform.iam.service.IamDepartmentQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "IAM-部门", description = "部门主数据、部门角色绑定与部门成员权限。部门是角色分配的主组织单元。")
@RestController
@RequestMapping("/api/{appCode}/departments")
public class IamDepartmentController {

    private final IamDepartmentQueryService iamDepartmentQueryService;
    private final IamDepartmentManageService iamDepartmentManageService;

    public IamDepartmentController(IamDepartmentQueryService iamDepartmentQueryService,
                                   IamDepartmentManageService iamDepartmentManageService) {
        this.iamDepartmentQueryService = iamDepartmentQueryService;
        this.iamDepartmentManageService = iamDepartmentManageService;
    }

    @Operation(summary = "查询部门列表", description = "返回当前 app 下所有部门。")
    @GetMapping
    public Result<List<IamDepartmentResponse>> list(@Parameter(description = "应用编码，例如 WSGM、PMHUB、PRIMELOAD-MARKETPLACE")
                                                    @PathVariable("appCode") String appCode,
                                                    LoginUser loginUser) {
        return Result.success(iamDepartmentQueryService.listDepartments(appCode, loginUser));
    }

    @Operation(summary = "查询部门详情", description = "返回指定部门的完整信息。")
    @GetMapping("/{departmentId}")
    public Result<IamDepartmentResponse> detail(@Parameter(description = "应用编码，例如 WSGM、PMHUB、PRIMELOAD-MARKETPLACE")
                                                @PathVariable("appCode") String appCode,
                                                @Parameter(description = "部门ID")
                                                @PathVariable("departmentId") Long departmentId,
                                                LoginUser loginUser) {
        return Result.success(iamDepartmentQueryService.getDepartmentDetail(appCode, departmentId, loginUser));
    }

    @Operation(summary = "创建部门", description = "在当前 app 下创建一个部门。")
    @PostMapping
    public Result<IamDepartmentResponse> create(@Parameter(description = "应用编码，例如 WSGM、PMHUB、PRIMELOAD-MARKETPLACE")
                                                @PathVariable("appCode") String appCode,
                                                @RequestBody IamDepartmentCreateRequest request,
                                                LoginUser loginUser) {
        return Result.success(iamDepartmentManageService.createDepartment(appCode, request, loginUser));
    }

    @Operation(summary = "更新部门", description = "更新部门名称、上级、类型、状态、排序。")
    @PutMapping("/{departmentId}")
    public Result<IamDepartmentResponse> update(@Parameter(description = "应用编码，例如 WSGM、PMHUB、PRIMELOAD-MARKETPLACE")
                                                @PathVariable("appCode") String appCode,
                                                @Parameter(description = "部门ID")
                                                @PathVariable("departmentId") Long departmentId,
                                                @RequestBody IamDepartmentUpdateRequest request,
                                                LoginUser loginUser) {
        return Result.success(iamDepartmentManageService.updateDepartment(appCode, departmentId, request, loginUser));
    }

    @Operation(summary = "删除部门", description = "逻辑删除部门。若部门下还有成员，则不允许删除。")
    @DeleteMapping("/{departmentId}")
    public Result<Void> delete(@Parameter(description = "应用编码，例如 WSGM、PMHUB、PRIMELOAD-MARKETPLACE")
                               @PathVariable("appCode") String appCode,
                               @Parameter(description = "部门ID")
                               @PathVariable("departmentId") Long departmentId,
                               LoginUser loginUser) {
        iamDepartmentManageService.deleteDepartment(appCode, departmentId, loginUser);
        return Result.success(null);
    }

    @Operation(summary = "查询部门角色", description = "返回部门当前绑定的角色。")
    @GetMapping("/{departmentId}/roles")
    public Result<List<com.hardrockunion.platform.iam.dto.IamRoleResponse>> listRoles(@Parameter(description = "应用编码，例如 WSGM、PMHUB、PRIMELOAD-MARKETPLACE")
                                                                                      @PathVariable("appCode") String appCode,
                                                                                      @Parameter(description = "部门ID")
                                                                                      @PathVariable("departmentId") Long departmentId,
                                                                                      LoginUser loginUser) {
        return Result.success(iamDepartmentQueryService.listRolesByDepartment(departmentId, appCode));
    }

    @Operation(summary = "查询部门成员", description = "返回部门当前成员列表，包含成员的主部门与角色信息。")
    @GetMapping("/{departmentId}/members")
    public Result<List<IamUserResponse>> listMembers(@Parameter(description = "应用编码，例如 WSGM、PMHUB、PRIMELOAD-MARKETPLACE")
                                                    @PathVariable("appCode") String appCode,
                                                    @Parameter(description = "部门ID")
                                                    @PathVariable("departmentId") Long departmentId,
                                                    LoginUser loginUser) {
        return Result.success(iamDepartmentQueryService.listMembersByDepartment(appCode, departmentId, loginUser));
    }

    @Operation(summary = "更新部门角色", description = "覆盖式更新部门绑定的角色集合。")
    @PutMapping("/{departmentId}/roles")
    public Result<IamDepartmentResponse> assignRoles(@Parameter(description = "应用编码，例如 WSGM、PMHUB、PRIMELOAD-MARKETPLACE")
                                                     @PathVariable("appCode") String appCode,
                                                     @Parameter(description = "部门ID")
                                                     @PathVariable("departmentId") Long departmentId,
                                                     @RequestBody IamDepartmentRoleAssignRequest request,
                                                     LoginUser loginUser) {
        return Result.success(iamDepartmentManageService.assignDepartmentRoles(appCode, departmentId, request, loginUser));
    }
}
