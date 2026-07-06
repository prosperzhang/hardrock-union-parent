package com.hardrockunion.business.project.enums;

public enum NexisProjectMemberStatus {

    ACTIVE("ACTIVE", "已加入"),
    REMOVED("REMOVED", "已移出");

    private final String code;
    private final String label;

    NexisProjectMemberStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static NexisProjectMemberStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (NexisProjectMemberStatus value : values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }
        return null;
    }
}
