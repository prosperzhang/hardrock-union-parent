package com.hardrockunion.business.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "NexisWorkerAttendanceCheckInRequest", description = "nexis 工人签到请求")
public class NexisWorkerAttendanceCheckInRequest {

    @Schema(description = "实名进场记录 ID", example = "68280000000000001")
    private Long entryId;

    @Schema(description = "签到备注", example = "早班打卡")
    private String remark;

    public Long getEntryId() {
        return entryId;
    }

    public void setEntryId(Long entryId) {
        this.entryId = entryId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
