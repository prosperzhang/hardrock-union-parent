package com.hardrockunion.business.project.enums;

import java.util.Arrays;

public enum PmhubParticipantCompanyType {

    GENERAL_CONTRACTOR("GENERAL_CONTRACTOR", "总承包单位", "负责项目总承包管理的单位"),
    SPECIALTY_CONTRACTOR("SPECIALTY_CONTRACTOR", "专业分包单位", "负责专业工程分包实施的单位"),
    LABOR_CONTRACTOR("LABOR_CONTRACTOR", "劳务分包单位", "负责劳务作业实施的单位"),
    SUPPLIER("SUPPLIER", "供应商", "负责材料或设备供应的单位"),
    OWNER("OWNER", "建设单位", "项目业主或建设单位"),
    SUPERVISOR("SUPERVISOR", "监理单位", "负责工程监理的单位");

    private final String code;
    private final String label;
    private final String description;

    PmhubParticipantCompanyType(String code, String label, String description) {
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

    public static PmhubParticipantCompanyType fromCode(String code) {
        return Arrays.stream(values())
            .filter(item -> item.code.equalsIgnoreCase(code))
            .findFirst()
            .orElse(null);
    }
}
