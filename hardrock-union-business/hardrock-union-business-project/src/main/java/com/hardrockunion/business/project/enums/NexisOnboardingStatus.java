package com.hardrockunion.business.project.enums;

public enum NexisOnboardingStatus {

    NEED_CREATE_OR_JOIN("NEED_CREATE_OR_JOIN", "需要创建项目或加入项目"),
    WAITING_APPROVAL("WAITING_APPROVAL", "等待项目管理员审批"),
    WAITING_ROLE_ASSIGNMENT("WAITING_ROLE_ASSIGNMENT", "等待项目管理员分配角色"),
    READY("READY", "已具备进入首页条件");

    private final String code;
    private final String label;

    NexisOnboardingStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}
