package com.hardrockunion.business.project.enums;

import java.util.Arrays;

public enum NexisSiteWorkScopeType {

    CODE_RANGE("CODE_RANGE", "编码区间", "适合 A-001 到 A-5000 这类编号范围"),
    MILESTONE_RANGE("MILESTONE_RANGE", "里程区间", "适合 KM0+000 到 KM1+800 这类里程范围"),
    AREA_RANGE("AREA_RANGE", "区域范围", "适合东区、西区、作业面等空间范围"),
    PIPE_SEGMENT("PIPE_SEGMENT", "管段范围", "适合按管段或线路区间管理责任范围"),
    BUILDING_FLOOR_RANGE("BUILDING_FLOOR_RANGE", "楼层区间", "适合按楼栋楼层范围管理责任范围");

    private final String code;
    private final String label;
    private final String description;

    NexisSiteWorkScopeType(String code, String label, String description) {
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

    public static NexisSiteWorkScopeType fromCode(String code) {
        return Arrays.stream(values())
            .filter(item -> item.code.equalsIgnoreCase(code))
            .findFirst()
            .orElse(null);
    }
}
