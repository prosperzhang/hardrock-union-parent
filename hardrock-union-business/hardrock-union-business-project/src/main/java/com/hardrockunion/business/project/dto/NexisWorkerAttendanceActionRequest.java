package com.hardrockunion.business.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "NexisWorkerAttendanceActionRequest", description = "nexis 工人考勤动作请求")
public class NexisWorkerAttendanceActionRequest {

    @Schema(description = "动作备注", example = "晚班收工签退")
    private String remark;

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
