package com.hardrockunion.platform.iam.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "权限创建请求。")
public class IamPermissionCreateRequest {

    @Schema(description = "权限编码，必须以当前 app 编码开头", example = "PMHUB_PROJECT_MANAGE")
    private String permissionCode;
    @Schema(description = "权限名称", example = "项目管理")
    private String permissionName;
    @Schema(description = "权限类型，MENU/BUTTON/API/DATA", example = "MENU", allowableValues = {"MENU", "BUTTON", "API", "DATA"})
    private String permissionType;
    @Schema(description = "上级权限ID，0表示顶级", example = "0")
    private Long parentId;
    @Schema(description = "前端路径或资源路径", example = "/api/pmhub/tenants/projects")
    private String permissionPath;
    @Schema(description = "HTTP方法", example = "GET")
    private String httpMethod;
    @Schema(description = "前端组件标识", example = "PmhubProjectPage")
    private String component;
    @Schema(description = "状态，1启用，0停用", example = "1", allowableValues = {"0", "1"})
    private Integer status;
    @Schema(description = "排序号", example = "10")
    private Integer sortNo;

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
}
