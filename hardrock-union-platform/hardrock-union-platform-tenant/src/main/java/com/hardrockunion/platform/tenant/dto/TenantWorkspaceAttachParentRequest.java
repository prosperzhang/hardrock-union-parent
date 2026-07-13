package com.hardrockunion.platform.tenant.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "项目归属调整请求。")
public class TenantWorkspaceAttachParentRequest {

    @Schema(description = "父级租户ID。填写集团或公司租户ID；传空表示取消组织归属，转为独立项目。", example = "75668854082945026")
    private Long parentTenantId;

    public Long getParentTenantId() {
        return parentTenantId;
    }

    public void setParentTenantId(Long parentTenantId) {
        this.parentTenantId = parentTenantId;
    }
}
