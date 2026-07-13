package com.hardrockunion.business.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "NexisParticipantCompanyBindRequest", description = "nexis 参建单位绑定真实租户请求")
public class NexisParticipantCompanyBindRequest {

    @Schema(description = "要绑定的平台租户 ID。必须是同应用下的 COMPANY", example = "2001")
    private Long bindTenantId;

    public Long getBindTenantId() {
        return bindTenantId;
    }

    public void setBindTenantId(Long bindTenantId) {
        this.bindTenantId = bindTenantId;
    }
}
