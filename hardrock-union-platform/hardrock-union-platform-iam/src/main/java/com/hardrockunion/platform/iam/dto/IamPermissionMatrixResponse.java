package com.hardrockunion.platform.iam.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "权限矩阵响应。用于前端按部门、角色配置菜单权限。")
public class IamPermissionMatrixResponse {

    @Schema(description = "应用编码", example = "PMHUB")
    private String appCode;

    @Schema(description = "可分配菜单权限树")
    private List<IamPermissionResponse> permissionTree;

    @Schema(description = "部门角色权限树")
    private List<DepartmentNode> departments;

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public List<IamPermissionResponse> getPermissionTree() {
        return permissionTree;
    }

    public void setPermissionTree(List<IamPermissionResponse> permissionTree) {
        this.permissionTree = permissionTree;
    }

    public List<DepartmentNode> getDepartments() {
        return departments;
    }

    public void setDepartments(List<DepartmentNode> departments) {
        this.departments = departments;
    }

    @Schema(description = "部门节点。")
    public static class DepartmentNode {

        private Long departmentId;
        private String deptCode;
        private String deptName;
        private Long parentId;
        private String deptType;
        private Integer status;
        private Integer sortNo;
        private List<RolePermissionNode> roles;
        private List<DepartmentNode> children;

        public Long getDepartmentId() {
            return departmentId;
        }

        public void setDepartmentId(Long departmentId) {
            this.departmentId = departmentId;
        }

        public String getDeptCode() {
            return deptCode;
        }

        public void setDeptCode(String deptCode) {
            this.deptCode = deptCode;
        }

        public String getDeptName() {
            return deptName;
        }

        public void setDeptName(String deptName) {
            this.deptName = deptName;
        }

        public Long getParentId() {
            return parentId;
        }

        public void setParentId(Long parentId) {
            this.parentId = parentId;
        }

        public String getDeptType() {
            return deptType;
        }

        public void setDeptType(String deptType) {
            this.deptType = deptType;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public Integer getSortNo() {
            return sortNo;
        }

        public void setSortNo(Integer sortNo) {
            this.sortNo = sortNo;
        }

        public List<RolePermissionNode> getRoles() {
            return roles;
        }

        public void setRoles(List<RolePermissionNode> roles) {
            this.roles = roles;
        }

        public List<DepartmentNode> getChildren() {
            return children;
        }

        public void setChildren(List<DepartmentNode> children) {
            this.children = children;
        }
    }

    @Schema(description = "角色权限节点。")
    public static class RolePermissionNode {

        private Long roleId;
        private String roleCode;
        private String roleName;
        private Integer status;
        private Integer assignable;
        private Integer adminRole;
        private List<String> permissionCodes;
        private List<Long> permissionIds;

        public Long getRoleId() {
            return roleId;
        }

        public void setRoleId(Long roleId) {
            this.roleId = roleId;
        }

        public String getRoleCode() {
            return roleCode;
        }

        public void setRoleCode(String roleCode) {
            this.roleCode = roleCode;
        }

        public String getRoleName() {
            return roleName;
        }

        public void setRoleName(String roleName) {
            this.roleName = roleName;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public Integer getAssignable() {
            return assignable;
        }

        public void setAssignable(Integer assignable) {
            this.assignable = assignable;
        }

        public Integer getAdminRole() {
            return adminRole;
        }

        public void setAdminRole(Integer adminRole) {
            this.adminRole = adminRole;
        }

        public List<String> getPermissionCodes() {
            return permissionCodes;
        }

        public void setPermissionCodes(List<String> permissionCodes) {
            this.permissionCodes = permissionCodes;
        }

        public List<Long> getPermissionIds() {
            return permissionIds;
        }

        public void setPermissionIds(List<Long> permissionIds) {
            this.permissionIds = permissionIds;
        }
    }
}
