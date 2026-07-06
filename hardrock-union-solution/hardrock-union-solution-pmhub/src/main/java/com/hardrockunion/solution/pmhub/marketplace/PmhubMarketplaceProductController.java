package com.hardrockunion.solution.pmhub.marketplace;

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
@RequestMapping("/api/pmhub/marketplace/products")
@Tag(name = "PMHUB-商城商品", description = "PMHUB 项目侧商城商品查询")
public class PmhubMarketplaceProductController {

    private final MerchantProductService merchantProductService;

    public PmhubMarketplaceProductController(MerchantProductService merchantProductService) {
        this.merchantProductService = merchantProductService;
    }

    @Operation(summary = "商城商品分页列表", description = "按当前 PMHUB 项目租户行政区域匹配区县、市、省区域价，未配置时回退商品基础价。")
    @GetMapping
    public Result<PageResponse<MerchantProductResponse>> list(MerchantProductMarketplaceQueryRequest request,
                                                              LoginUser loginUser) {
        return Result.success(merchantProductService.listMarketplace(request, loginUser));
    }
}
