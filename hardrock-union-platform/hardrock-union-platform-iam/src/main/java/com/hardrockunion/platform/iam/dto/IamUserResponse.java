package com.hardrockunion.platform.iam.dto;

import java.util.List;

/**
 * IAM 用户返回模型。
 *
 * <p>当前主要用于 app 级员工管理场景，返回账号的基本信息和角色编码列表。
 */
public class IamUserResponse {

    // 用户主键。
    private Long id;
    // 所属应用，例如 WSGM、PMHUB、PRIMELOAD-MARKETPLACE。
    private String appCode;
    // 所属租户。
    private Long tenantId;
    // 主部门ID。
    private Long departmentId;
    // 主部门编码。
    private String departmentCode;
    // 主部门名称。
    private String departmentName;
    // 主部门简称。
    private String departmentShortName;
    // 登录账号。
    private String username;
    // 昵称。
    private String nickName;
    // 头像地址。
    private String avatarUrl;
    // 用户状态。
    private Integer status;
    // 角色编码列表。
    private List<String> roleCodes;

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

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDepartmentShortName() {
        return departmentShortName;
    }

    public void setDepartmentShortName(String departmentShortName) {
        this.departmentShortName = departmentShortName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    public void setRoleCodes(List<String> roleCodes) {
        this.roleCodes = roleCodes;
    }
}
