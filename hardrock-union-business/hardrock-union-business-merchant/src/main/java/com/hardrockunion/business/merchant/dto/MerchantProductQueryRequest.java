package com.hardrockunion.business.merchant.dto;

import com.hardrockunion.infrastructure.db.page.PageRequest;

/**
 * 商品列表查询条件。
 */
public class MerchantProductQueryRequest extends PageRequest {

    // 商品关键词，当前匹配商品名和 SKU。
    private String keyword;
    private String categoryCode;
    private Integer status;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
