package com.hardrockunion.platform.tenant.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "提交加入项目租户申请请求。")
public class TenantJoinRequestCreateRequest {

    @Schema(description = "目标项目租户ID，和 tenantKeyword 二选一", example = "75668854082945026")
    private Long tenantId;
    @Schema(description = "目标项目租户搜索关键字，可匹配租户名称、编码、负责人等，和 tenantId 二选一", example = "张栋俊测试项目A")
    private String tenantKeyword;
    @Schema(description = "申请说明", example = "我是工程部施工员，请审批加入项目。")
    private String applyMessage;

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantKeyword() {
        return tenantKeyword;
    }

    public void setTenantKeyword(String tenantKeyword) {
        this.tenantKeyword = tenantKeyword;
    }

    public String getApplyMessage() {
        return applyMessage;
    }

    public void setApplyMessage(String applyMessage) {
        this.applyMessage = applyMessage;
    }
}
