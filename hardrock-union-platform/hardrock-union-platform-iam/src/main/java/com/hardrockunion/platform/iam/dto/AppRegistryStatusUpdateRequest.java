package com.hardrockunion.platform.iam.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "App注册表状态更新请求")
public class AppRegistryStatusUpdateRequest {

    @Schema(description = "状态 1启用 0禁用", example = "1")
    private Integer status;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
