package com.hardrockunion.business.merchant.dto;

/**
 * 商品库存手工调整请求。
 */
public class MerchantProductStockAdjustRequest {

    // 调整类型，目前支持 IN 和 OUT。
    private String adjustType;
    // 调整数量，必须大于 0。
    private Integer quantity;
    // 调整备注。
    private String remark;

    public String getAdjustType() { return adjustType; }
    public void setAdjustType(String adjustType) { this.adjustType = adjustType; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
