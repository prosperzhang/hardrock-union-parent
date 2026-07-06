package com.hardrockunion.business.merchant.dto;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.hardrockunion.infrastructure.db.page.PageRequest;

/**
 * 报价单列表查询条件。
 *
 * <p>重点支持按 `pmhub` 交易目标字段筛选报价单。
 */
public class MerchantQuotationQueryRequest extends PageRequest {

    // 报价单状态，例如 ISSUED、CONVERTED。
    private String quotationStatus;
    // 报价单关键词，当前匹配报价单号、联系人和联系电话。
    private String quotationKeyword;
    // 目标租户，一般对应 pmhub 里的公司租户。
    private Long targetTenantId;
    // 目标项目名称，当前按模糊匹配处理。
    private String targetProjectName;
    // 目标工地名称，当前按模糊匹配处理。
    private String targetSiteName;
    // 目标联系人姓名，当前按模糊匹配处理。
    private String targetUserName;
    // 计划发货仓库 ID。
    private Long warehouseId;
    // 计划发货仓库名称，当前按模糊匹配处理。
    private String warehouseName;
    // 计划物流公司，当前按模糊匹配处理。
    private String logisticsCompany;

    // 报价有效期起点。
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validUntilFrom;

    // 报价有效期终点。
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validUntilTo;

    // 是否只看已过期报价单。
    private Boolean expired;

    // 创建时间起点。
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdFrom;

    // 创建时间终点。
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTo;

    public String getQuotationStatus() {
        return quotationStatus;
    }

    public void setQuotationStatus(String quotationStatus) {
        this.quotationStatus = quotationStatus;
    }

    public String getQuotationKeyword() {
        return quotationKeyword;
    }

    public void setQuotationKeyword(String quotationKeyword) {
        this.quotationKeyword = quotationKeyword;
    }

    public Long getTargetTenantId() {
        return targetTenantId;
    }

    public void setTargetTenantId(Long targetTenantId) {
        this.targetTenantId = targetTenantId;
    }

    public String getTargetProjectName() {
        return targetProjectName;
    }

    public void setTargetProjectName(String targetProjectName) {
        this.targetProjectName = targetProjectName;
    }

    public String getTargetSiteName() {
        return targetSiteName;
    }

    public void setTargetSiteName(String targetSiteName) {
        this.targetSiteName = targetSiteName;
    }

    public String getTargetUserName() {
        return targetUserName;
    }

    public void setTargetUserName(String targetUserName) {
        this.targetUserName = targetUserName;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public String getLogisticsCompany() {
        return logisticsCompany;
    }

    public void setLogisticsCompany(String logisticsCompany) {
        this.logisticsCompany = logisticsCompany;
    }

    public LocalDateTime getValidUntilFrom() {
        return validUntilFrom;
    }

    public void setValidUntilFrom(LocalDateTime validUntilFrom) {
        this.validUntilFrom = validUntilFrom;
    }

    public LocalDateTime getValidUntilTo() {
        return validUntilTo;
    }

    public void setValidUntilTo(LocalDateTime validUntilTo) {
        this.validUntilTo = validUntilTo;
    }

    public Boolean getExpired() {
        return expired;
    }

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    public LocalDateTime getCreatedFrom() {
        return createdFrom;
    }

    public void setCreatedFrom(LocalDateTime createdFrom) {
        this.createdFrom = createdFrom;
    }

    public LocalDateTime getCreatedTo() {
        return createdTo;
    }

    public void setCreatedTo(LocalDateTime createdTo) {
        this.createdTo = createdTo;
    }
}
