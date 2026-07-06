package com.hardrockunion.business.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "NexisWorkerEntryActionRequest", description = "nexis 工人实名进场动作请求")
public class NexisWorkerEntryActionRequest {

    @Schema(description = "动作备注", example = "门禁已核验通过")
    private String remark;

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
