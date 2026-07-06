package com.hardrockunion.business.project.enums;

public enum PmhubProjectMemberStatus {

    ACTIVE("ACTIVE", "已加入"),
    REMOVED("REMOVED", "已移出");

    private final String code;
    private final String label;

    PmhubProjectMemberStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static PmhubProjectMemberStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (PmhubProjectMemberStatus value : values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }
        return null;
    }
}
