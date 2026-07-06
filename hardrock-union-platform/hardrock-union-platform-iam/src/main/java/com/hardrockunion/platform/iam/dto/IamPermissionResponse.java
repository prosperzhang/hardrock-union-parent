package com.hardrockunion.platform.iam.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "权限响应。")
public class IamPermissionResponse {

    @Schema(description = "权限ID", example = "72000000000000001")
    private Long id;
    @Schema(description = "应用编码", example = "NEXIS")
    private String appCode;
    @Schema(description = "权限编码", example = "NEXIS_PROJECT_MANAGE")
    private String permissionCode;
    @Schema(description = "权限名称", example = "项目管理")
    private String permissionName;
    @Schema(description = "权限类型", example = "MENU")
    private String permissionType;
    @Schema(description = "上级权限ID", example = "0")
    private Long parentId;
    @Schema(description = "前端路径或资源路径", example = "/api/nexis/tenants/projects")
    private String permissionPath;
    @Schema(description = "HTTP方法", example = "GET")
    private String httpMethod;
    @Schema(description = "前端组件标识", example = "NexisProjectPage")
    private String component;
    @Schema(description = "状态，1启用，0停用", example = "1")
    private Integer status;
    @Schema(description = "排序号", example = "10")
    private Integer sortNo;
    @Schema(description = "子权限列表")
    private List<IamPermissionResponse> children;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public String getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(String permissionType) {
        this.permissionType = permissionType;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getPermissionPath() {
        return permissionPath;
    }

    public void setPermissionPath(String permissionPath) {
        this.permissionPath = permissionPath;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
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

    public List<IamPermissionResponse> getChildren() {
        return children;
    }

    public void setChildren(List<IamPermissionResponse> children) {
        this.children = children;
    }
}
