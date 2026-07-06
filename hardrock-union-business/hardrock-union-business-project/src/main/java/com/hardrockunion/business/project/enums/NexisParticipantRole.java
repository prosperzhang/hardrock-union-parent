package com.hardrockunion.business.project.enums;

import java.util.Arrays;

public enum NexisParticipantRole {

    GENERAL_CONTRACTOR("GENERAL_CONTRACTOR", "总承包", "在项目或标段层面承担总承包角色"),
    SPECIALTY_CONTRACTOR("SPECIALTY_CONTRACTOR", "专业分包", "在项目或标段层面承担专业分包角色"),
    LABOR_CONTRACTOR("LABOR_CONTRACTOR", "劳务分包", "在项目或标段层面承担劳务分包角色"),
    SUPPLIER("SUPPLIER", "供应商", "在项目或标段层面承担材料设备供应角色");

    private final String code;
    private final String label;
    private final String description;

    NexisParticipantRole(String code, String label, String description) {
        this.code = code;
        this.label = label;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public static NexisParticipantRole fromCode(String code) {
        return Arrays.stream(values())
            .filter(item -> item.code.equalsIgnoreCase(code))
            .findFirst()
            .orElse(null);
    }
}
