package com.hardrockunion.platform.iam.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "App注册表更新请求")
public class AppRegistryUpdateRequest {

    @Schema(description = "应用名称", example = "项目协同")
    private String appName;

    @Schema(description = "应用类型", example = "SAAS_PLATFORM")
    private String appType;

    @Schema(description = "首页路径", example = "/pmhub")
    private String homePath;

    @Schema(description = "登录路径", example = "/pmhub/login")
    private String loginPath;

    @Schema(description = "图标", example = "https://example.com/icon.png")
    private String icon;

    @Schema(description = "排序号", example = "20")
    private Integer sortNo;

    @Schema(description = "状态 1启用 0停用", example = "1")
    private Integer status;

    @Schema(description = "描述", example = "施工项目协同管理平台")
    private String description;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getHomePath() {
        return homePath;
    }

    public void setHomePath(String homePath) {
        this.homePath = homePath;
    }

    public String getLoginPath() {
        return loginPath;
    }

    public void setLoginPath(String loginPath) {
        this.loginPath = loginPath;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
