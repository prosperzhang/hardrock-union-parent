package com.hardrockunion.business.project.enums;

import java.util.Arrays;

public enum PmhubWorkerEntryStatus {

    REGISTERED("REGISTERED", "已登记", "实名进场资料已登记，待现场确认进场"),
    ENTERED("ENTERED", "已进场", "工人已完成实名进场并在场"),
    EXITED("EXITED", "已退场", "工人已从当前项目现场退场");

    private final String code;
    private final String label;
    private final String description;

    PmhubWorkerEntryStatus(String code, String label, String description) {
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

    public static PmhubWorkerEntryStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
            .filter(item -> item.code.equalsIgnoreCase(code))
            .findFirst()
            .orElse(null);
    }
}
