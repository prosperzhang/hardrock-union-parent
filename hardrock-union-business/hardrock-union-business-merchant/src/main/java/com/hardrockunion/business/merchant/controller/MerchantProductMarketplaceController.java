package com.hardrockunion.business.merchant.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.business.merchant.dto.MerchantProductMarketplaceQueryRequest;
import com.hardrockunion.business.merchant.dto.MerchantProductResponse;
import com.hardrockunion.business.merchant.service.MerchantProductService;
import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/marketplace/products")
@Tag(name = "商城商品", description = "面向项目租户的商城商品查询")
public class MerchantProductMarketplaceController {

    private final MerchantProductService productService;

    public MerchantProductMarketplaceController(MerchantProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "商城商品分页列表", description = "按当前登录租户行政区域匹配区县、市、省区域价，未配置时回退商品基础价。")
    @GetMapping
    public Result<PageResponse<MerchantProductResponse>> list(MerchantProductMarketplaceQueryRequest request,
                                                              LoginUser loginUser) {
        return Result.success(productService.listMarketplace(request, loginUser));
    }
}
