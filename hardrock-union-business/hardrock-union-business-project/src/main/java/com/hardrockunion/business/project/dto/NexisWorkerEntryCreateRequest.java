package com.hardrockunion.business.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "NexisWorkerEntryCreateRequest", description = "nexis 工人实名进场登记请求")
public class NexisWorkerEntryCreateRequest {

    @Schema(description = "工人 ID", example = "68270000000000002")
    private Long workerId;

    @Schema(description = "实名进场备注", example = "首次办理进场实名登记")
    private String remark;

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
