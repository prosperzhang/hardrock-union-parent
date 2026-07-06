package com.hardrockunion.platform.iam.dto;

/**
 * 用户切换主部门请求。
 */
public class IamUserDepartmentSwitchRequest {

    private Long departmentId;

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
}
