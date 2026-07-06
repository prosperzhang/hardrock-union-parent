package com.hardrockunion.solution.primeloadmarketplace.merchant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.business.merchant.dto.MerchantCategoryCreateRequest;
import com.hardrockunion.business.merchant.dto.MerchantCategoryQueryRequest;
import com.hardrockunion.business.merchant.dto.MerchantCategoryResponse;
import com.hardrockunion.business.merchant.dto.MerchantCategoryUpdateRequest;
import com.hardrockunion.business.merchant.service.MerchantCategoryService;
import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/primeload-marketplace/product-categories")
@Tag(name = "PRIMELOAD-MARKETPLACE-商品分类", description = "当前 PRIMELOAD-MARKETPLACE 商户的商品分类。")
public class PrimeloadMarketplaceProductCategoryController {

    private final MerchantCategoryService merchantCategoryService;

    public PrimeloadMarketplaceProductCategoryController(MerchantCategoryService merchantCategoryService) {
        this.merchantCategoryService = merchantCategoryService;
    }

    @Operation(summary = "商品分类分页列表", description = "查询当前 PRIMELOAD-MARKETPLACE 商户租户下的商品分类。")
    @GetMapping
    public Result<PageResponse<MerchantCategoryResponse>> list(MerchantCategoryQueryRequest request, LoginUser loginUser) {
        return Result.success(merchantCategoryService.list(request, loginUser));
    }

    @Operation(summary = "创建商品分类", description = "在当前 PRIMELOAD-MARKETPLACE 商户租户下创建商品分类。")
    @PostMapping
    public Result<MerchantCategoryResponse> create(@RequestBody MerchantCategoryCreateRequest request, LoginUser loginUser) {
        return Result.success(merchantCategoryService.create(request, loginUser));
    }

    @Operation(summary = "更新商品分类", description = "更新当前 PRIMELOAD-MARKETPLACE 商户租户下商品分类的名称、排序和状态。")
    @PutMapping("/{id}")
    public Result<MerchantCategoryResponse> update(@PathVariable("id") Long id,
                                                   @RequestBody MerchantCategoryUpdateRequest request,
                                                   LoginUser loginUser) {
        return Result.success(merchantCategoryService.update(id, request, loginUser));
    }

    @Operation(summary = "删除商品分类", description = "逻辑删除当前 PRIMELOAD-MARKETPLACE 商户租户下的商品分类。若仍有子分类或商品占用，则不允许删除。")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable("id") Long id, LoginUser loginUser) {
        merchantCategoryService.remove(id, loginUser);
        return Result.success();
    }
}
