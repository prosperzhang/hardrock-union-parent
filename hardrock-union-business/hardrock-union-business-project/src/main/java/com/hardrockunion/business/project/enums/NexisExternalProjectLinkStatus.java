package com.hardrockunion.business.project.enums;

public enum NexisExternalProjectLinkStatus {
    PENDING("待上级单位审核"),
    LINKED("已关联"),
    REJECTED("已拒绝"),
    CANCELLED("已撤回"),
    UNLINKED("已解除");

    private final String label;

    NexisExternalProjectLinkStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
