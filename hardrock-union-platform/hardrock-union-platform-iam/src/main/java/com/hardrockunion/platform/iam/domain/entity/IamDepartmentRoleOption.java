package com.hardrockunion.platform.iam.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hardrockunion.infrastructure.db.entity.BaseEntity;

@TableName("iam_department_role_option")
public class IamDepartmentRoleOption extends BaseEntity {

    private Long appId;

    private String appCode;

    private Long departmentId;

    private Long roleId;

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}
