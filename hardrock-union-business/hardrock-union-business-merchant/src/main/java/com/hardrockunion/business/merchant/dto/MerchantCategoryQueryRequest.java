package com.hardrockunion.business.merchant.dto;

import com.hardrockunion.infrastructure.db.page.PageRequest;

/**
 * 商品分类列表查询条件。
 */
public class MerchantCategoryQueryRequest extends PageRequest {

    // 分类关键词，当前匹配分类编码和分类名称。
    private String keyword;
    private Integer status;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
