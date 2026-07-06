package com.hardrockunion.business.merchant.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 商城订单创建请求。
 *
 * <p>订单沿用 商户到项目 的交易对象模型，不再把“客户”理解成独立本地主数据。
 */
@Schema(name = "MerchantOrderCreateRequest", description = "商城订单创建请求")
public class MerchantOrderCreateRequest {

    // 目标应用，默认按 pmhub 侧对象处理。
    @Schema(description = "目标应用编码，当前默认对接 PMHUB", example = "PMHUB")
    private String targetAppCode;
    // 目标租户。
    @Schema(description = "目标租户 ID，一般对应 pmhub 的公司租户", example = "2001")
    private Long targetTenantId;
    // 目标项目名称。
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
    // 兼容旧接口字段，内部逐步归并到 targetUserName。
    @Schema(description = "兼容旧接口的客户姓名字段", example = "刘工")
    private String customerName;
    // 兼容旧接口字段，内部逐步归并到 targetUserPhone。
    @Schema(description = "兼容旧接口的客户手机号字段", example = "13900000000")
    private String customerPhone;
    // 收货地址。
    @Schema(description = "收货地址", example = "上海市浦东新区川沙路 88 号")
    private String deliveryAddress;
    // 指定发货仓库。
    @Schema(description = "指定发货仓库 ID", example = "3100001")
    private Long warehouseId;
    // 订单备注。
    @Schema(description = "订单备注", example = "今天下午优先配送")
    private String remark;
    // 订单明细。
    @ArraySchema(schema = @Schema(implementation = MerchantOrderItemCreateRequest.class), arraySchema = @Schema(description = "订单明细"))
    private List<MerchantOrderItemCreateRequest> items;

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
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<MerchantOrderItemCreateRequest> getItems() { return items; }
    public void setItems(List<MerchantOrderItemCreateRequest> items) { this.items = items; }
}
