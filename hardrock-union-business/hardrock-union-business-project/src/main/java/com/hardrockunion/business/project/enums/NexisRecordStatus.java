package com.hardrockunion.business.project.enums;

import java.util.Arrays;

public enum NexisRecordStatus {

    ENABLED(1, "启用", "当前记录可正常参与业务流程"),
    DISABLED(0, "停用", "当前记录已停用，不再参与业务流程");

    private final Integer code;
    private final String label;
    private final String description;

    NexisRecordStatus(Integer code, String label, String description) {
        this.code = code;
        this.label = label;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public static NexisRecordStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
            .filter(item -> item.code.equals(code))
            .findFirst()
            .orElse(null);
    }
}
