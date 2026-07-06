package com.hardrockunion.business.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PmhubDictionaryOptionResponse", description = "pmhub 字典选项响应")
public class PmhubDictionaryOptionResponse {

    @Schema(description = "字典编码", example = "LABOR_CONTRACTOR")
    private String code;

    @Schema(description = "字典名称", example = "劳务分包单位")
    private String label;

    @Schema(description = "字典说明", example = "负责劳务作业实施的单位")
    private String description;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
