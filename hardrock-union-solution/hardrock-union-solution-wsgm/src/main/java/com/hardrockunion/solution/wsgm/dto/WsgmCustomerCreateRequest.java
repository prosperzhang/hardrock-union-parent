package com.hardrockunion.solution.wsgm.dto;

public class WsgmCustomerCreateRequest {

    private String customerName;
    private String contactName;
    private String contactPhone;
    private String levelCode;
    private String sourceCode;
    private String remark;

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getLevelCode() { return levelCode; }
    public void setLevelCode(String levelCode) { this.levelCode = levelCode; }
    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
