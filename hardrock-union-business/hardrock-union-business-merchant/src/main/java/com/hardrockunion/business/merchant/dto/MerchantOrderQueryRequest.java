package com.hardrockunion.business.merchant.dto;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.hardrockunion.infrastructure.db.page.PageRequest;

/**
 * 订单列表查询条件。
 *
 * <p>既支持订单自身字段筛选，也支持按 `pmhub` 交易目标筛单。
 */
public class MerchantOrderQueryRequest extends PageRequest {

    // 订单状态，例如 CREATED、ACCEPTED、SHIPPED、RECEIVED、COMPLETED。
    private String orderStatus;
    // 订单关键词，当前匹配订单号。
    private String orderKeyword;
    // 兼容旧查询习惯，当前会匹配 customerName 和 customerPhone。
    private String customerKeyword;
    // 目标租户，一般对应 pmhub 里的公司租户。
    private Long targetTenantId;
    // 目标项目名称，当前按模糊匹配处理。
    private String targetProjectName;
    // 目标工地名称，当前按模糊匹配处理。
    private String targetSiteName;
    // 目标联系人姓名，当前按模糊匹配处理。
    private String targetUserName;
    // 预留发货仓库 ID，当前仅作为简化履约字段筛选。
    private Long warehouseId;
    // 预留发货仓库名称，当前按模糊匹配处理。
    private String warehouseName;
    // 物流公司，当前按模糊匹配处理。
    private String logisticsCompany;
    // 运单号，当前按模糊匹配处理。
    private String trackingNo;

    // 创建时间起点。
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdFrom;

    // 创建时间终点。
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTo;

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderKeyword() {
        return orderKeyword;
    }

    public void setOrderKeyword(String orderKeyword) {
        this.orderKeyword = orderKeyword;
    }

    public String getCustomerKeyword() {
        return customerKeyword;
    }

    public void setCustomerKeyword(String customerKeyword) {
        this.customerKeyword = customerKeyword;
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

    public String getTrackingNo() {
        return trackingNo;
    }

    public void setTrackingNo(String trackingNo) {
        this.trackingNo = trackingNo;
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
