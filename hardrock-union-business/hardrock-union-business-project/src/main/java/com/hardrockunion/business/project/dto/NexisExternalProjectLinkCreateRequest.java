package com.hardrockunion.business.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "外部上级项目关联申请")
public class NexisExternalProjectLinkCreateRequest {

    @Schema(description = "准备认领外部单位引用的正式公司租户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long targetOrganizationTenantId;
    @Schema(description = "本地项目中代表外部上级单位的参建单位ID。审核通过后自动绑定正式租户")
    private Long externalParticipantCompanyId;

    public Long getTargetOrganizationTenantId() { return targetOrganizationTenantId; }
    public void setTargetOrganizationTenantId(Long targetOrganizationTenantId) { this.targetOrganizationTenantId = targetOrganizationTenantId; }
    public Long getExternalParticipantCompanyId() { return externalParticipantCompanyId; }
    public void setExternalParticipantCompanyId(Long externalParticipantCompanyId) { this.externalParticipantCompanyId = externalParticipantCompanyId; }
}
