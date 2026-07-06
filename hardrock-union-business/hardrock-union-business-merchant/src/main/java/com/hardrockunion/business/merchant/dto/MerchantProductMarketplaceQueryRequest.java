package com.hardrockunion.business.merchant.dto;

import com.hardrockunion.infrastructure.db.page.PageRequest;

public class MerchantProductMarketplaceQueryRequest extends PageRequest {

    private Long merchantTenantId;
    private String keyword;
    private String categoryCode;

    public Long getMerchantTenantId() { return merchantTenantId; }
    public void setMerchantTenantId(Long merchantTenantId) { this.merchantTenantId = merchantTenantId; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
}
