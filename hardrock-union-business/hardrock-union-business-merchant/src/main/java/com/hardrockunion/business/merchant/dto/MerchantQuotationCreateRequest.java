package com.hardrockunion.business.merchant.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 商城报价单创建请求。
 *
 * <p>当前商户报价面向的是 `pmhub` 侧交易对象，因此这里的目标字段用于描述
 * “卖给哪个租户、哪个项目、哪个工地、哪个联系人”。
 */
@Schema(name = "MerchantQuotationCreateRequest", description = "商城报价单创建请求")
public class MerchantQuotationCreateRequest {

    // 目标应用，当前默认是 PMHUB。
    @Schema(description = "目标应用编码，当前默认对接 PMHUB", example = "PMHUB")
    private String targetAppCode;
    // 目标租户，一般对应 pmhub 里的公司租户。
    @Schema(description = "目标租户 ID，一般对应 pmhub 公司租户", example = "2001")
    private Long targetTenantId;
    // 目标项目名称，现阶段先用文本承接，后面可逐步升级成真实引用。
    @Schema(description = "目标项目名称", example = "浦东新区学校改造项目")
    private String targetProjectName;
    // 目标工地名称。
    @Schema(description = "目标工地名称", example = "1号教学楼工地")
    private String targetSiteName;
    // 目标联系人姓名。
    @Schema(description = "目标联系人姓名", example = "刘工")
    private String targetUserName;
    // 目标联系人手机号。
    @Schema(description = "目标联系人手机号", example = "13900000000")
    private String targetUserPhone;
    // 兼容旧接口字段，内部会逐步收口到 targetUserName。
    @Schema(description = "兼容旧接口的客户姓名字段", example = "刘工")
    private String customerName;
    // 兼容旧接口字段，内部会逐步收口到 targetUserPhone。
    @Schema(description = "兼容旧接口的客户手机号字段", example = "13900000000")
    private String customerPhone;
    // 计划发货仓库 ID。
    @Schema(description = "计划发货仓库 ID", example = "3100001")
    private Long warehouseId;
    // 计划物流公司。
    @Schema(description = "计划物流公司", example = "安能物流")
    private String logisticsCompany;

    // 报价有效期。
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "报价有效期，格式 yyyy-MM-dd HH:mm:ss", example = "2026-03-31 18:00:00")
    private LocalDateTime validUntil;

    // 报价备注。
    @Schema(description = "报价备注", example = "本报价含送货到工地")
    private String remark;
    // 报价明细。
    @ArraySchema(schema = @Schema(implementation = MerchantQuotationItemCreateRequest.class), arraySchema = @Schema(description = "报价明细"))
    private List<MerchantQuotationItemCreateRequest> items;

    public String getTargetAppCode() { return targetAppCode; }
    public void setTargetAppCode(String targetAppCode) { this.targetAppCode = targetAppCode; }
    public Long getTargetTenantId() { return targetTenantId; }
    public void setTargetTenantId(Long targetTenantId) { this.targetTenantId = targetTenantId; }
    public String getTargetProjectName() { return targetProjectName; }
    public void setTargetProjectName(String targetProjectName) { this.targetProjectName = targetProjectName; }
    public String getTargetSiteName() { return targetSiteName; }
    public void setTargetSiteName(String targetSiteName) { this.targetSiteName = targetSiteName; }
    public String getTargetUserName() { return targetUserName; }
    public void setTargetUserName(String targetUserName) { this.targetUserName = targetUserName; }
    public String getTargetUserPhone() { return targetUserPhone; }
    public void setTargetUserPhone(String targetUserPhone) { this.targetUserPhone = targetUserPhone; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getLogisticsCompany() { return logisticsCompany; }
    public void setLogisticsCompany(String logisticsCompany) { this.logisticsCompany = logisticsCompany; }
    public LocalDateTime getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDateTime validUntil) { this.validUntil = validUntil; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<MerchantQuotationItemCreateRequest> getItems() { return items; }
    public void setItems(List<MerchantQuotationItemCreateRequest> items) { this.items = items; }
}
