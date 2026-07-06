package com.hardrockunion.business.project.enums;

import java.util.Arrays;

public enum NexisRealNameStatus {

    VERIFIED("VERIFIED", "已实名", "姓名与身份证号已登记，可参与实名进场"),
    UNVERIFIED("UNVERIFIED", "未实名", "实名信息尚未补齐，暂不能办理实名进场");

    private final String code;
    private final String label;
    private final String description;

    NexisRealNameStatus(String code, String label, String description) {
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

    public static NexisRealNameStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
            .filter(item -> item.code.equalsIgnoreCase(code))
            .findFirst()
            .orElse(null);
    }
}
