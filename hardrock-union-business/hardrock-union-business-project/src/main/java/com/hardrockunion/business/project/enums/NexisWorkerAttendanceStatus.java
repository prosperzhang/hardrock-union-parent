package com.hardrockunion.business.project.enums;

import java.util.Arrays;

public enum NexisWorkerAttendanceStatus {

    CHECKED_IN("CHECKED_IN", "已签到", "工人当天已完成签到，尚未签退"),
    CHECKED_OUT("CHECKED_OUT", "已签退", "工人当天已完成签到和签退");

    private final String code;
    private final String label;
    private final String description;

    NexisWorkerAttendanceStatus(String code, String label, String description) {
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

    public static NexisWorkerAttendanceStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
            .filter(item -> item.code.equalsIgnoreCase(code))
            .findFirst()
            .orElse(null);
    }
}
