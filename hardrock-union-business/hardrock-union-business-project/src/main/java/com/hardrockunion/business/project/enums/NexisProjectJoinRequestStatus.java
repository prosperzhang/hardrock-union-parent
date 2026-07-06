package com.hardrockunion.business.project.enums;

public enum NexisProjectJoinRequestStatus {

    PENDING("PENDING", "待审批"),
    APPROVED("APPROVED", "已通过"),
    REJECTED("REJECTED", "已拒绝"),
    CANCELLED("CANCELLED", "已撤销");

    private final String code;
    private final String label;

    NexisProjectJoinRequestStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static NexisProjectJoinRequestStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (NexisProjectJoinRequestStatus value : values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }
        return null;
    }
}
