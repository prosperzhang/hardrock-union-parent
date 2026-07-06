package com.hardrockunion.business.merchant.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.business.merchant.dto.MerchantProductCreateRequest;
import com.hardrockunion.business.merchant.dto.MerchantProductQueryRequest;
import com.hardrockunion.business.merchant.dto.MerchantProductRegionPriceResponse;
import com.hardrockunion.business.merchant.dto.MerchantProductRegionPriceUpsertRequest;
import com.hardrockunion.business.merchant.dto.MerchantProductResponse;
import com.hardrockunion.business.merchant.dto.MerchantProductStockAdjustRequest;
import com.hardrockunion.business.merchant.dto.MerchantProductUpdateRequest;
import com.hardrockunion.business.merchant.service.MerchantProductService;
import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/merchant/products")
@Tag(name = "商户商品", description = "商城商品管理与库存手工调整")
public class MerchantProductController {

    private final MerchantProductService productService;

    public MerchantProductController(MerchantProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "商品分页列表", description = "支持按关键字分页查询当前商户的商品列表。")
    @GetMapping
    public Result<PageResponse<MerchantProductResponse>> list(MerchantProductQueryRequest request, LoginUser loginUser) {
        return Result.success(productService.list(request, loginUser));
    }

    @Operation(summary = "商品详情", description = "查看单个商品的基础信息、价格与当前总库存。")
    @GetMapping("/{id}")
    public Result<MerchantProductResponse> get(@Parameter(description = "商品 ID", example = "65998417388650497")
                                           @PathVariable("id") Long id,
                                           LoginUser loginUser) {
        return Result.success(productService.getById(id, loginUser));
    }

    @Operation(summary = "创建商品", description = "在当前商户租户下创建一个商品。")
    @PostMapping
    public Result<MerchantProductResponse> create(@RequestBody MerchantProductCreateRequest request, LoginUser loginUser) {
        return Result.success(productService.create(request, loginUser));
    }

    @Operation(summary = "更新商品", description = "更新当前商户租户下商品的基础资料和主图 URL。")
    @PutMapping("/{id}")
    public Result<MerchantProductResponse> update(@Parameter(description = "商品 ID", example = "65998417388650497")
                                                  @PathVariable("id") Long id,
                                                  @RequestBody MerchantProductUpdateRequest request,
                                                  LoginUser loginUser) {
        return Result.success(productService.update(id, request, loginUser));
    }

    /**
     * 手工调整商品库存。
     */
    @Operation(summary = "手工调整商品总库存", description = "支持手工入库或手工出库，同时会记录库存流水。")
    @PostMapping("/{id}/stock-adjust")
    public Result<MerchantProductResponse> adjustStock(@Parameter(description = "商品 ID", example = "65998417388650497")
                                                   @PathVariable("id") Long id,
                                                   @RequestBody MerchantProductStockAdjustRequest request,
                                                   LoginUser loginUser) {
        return Result.success(productService.adjustStock(id, request, loginUser));
    }

    @Operation(summary = "商品区域价列表", description = "查看当前商品按省、市、区县配置的区域价格。")
    @GetMapping("/{id}/region-prices")
    public Result<List<MerchantProductRegionPriceResponse>> listRegionPrices(
        @Parameter(description = "商品 ID", example = "65998417388650497") @PathVariable("id") Long id,
        LoginUser loginUser) {
        return Result.success(productService.listRegionPrices(id, loginUser));
    }

    @Operation(summary = "保存商品区域价", description = "按区域级别和行政区编码新增或更新商品区域价。")
    @PostMapping("/{id}/region-prices")
    public Result<MerchantProductRegionPriceResponse> upsertRegionPrice(
        @Parameter(description = "商品 ID", example = "65998417388650497") @PathVariable("id") Long id,
        @RequestBody MerchantProductRegionPriceUpsertRequest request,
        LoginUser loginUser) {
        return Result.success(productService.upsertRegionPrice(id, request, loginUser));
    }

    @Operation(summary = "删除商品区域价", description = "删除当前商品的某条区域价格。")
    @DeleteMapping("/{id}/region-prices/{priceId}")
    public Result<Void> removeRegionPrice(
        @Parameter(description = "商品 ID", example = "65998417388650497") @PathVariable("id") Long id,
        @Parameter(description = "区域价 ID", example = "65998417388650498") @PathVariable("priceId") Long priceId,
        LoginUser loginUser) {
        productService.removeRegionPrice(id, priceId, loginUser);
        return Result.success();
    }

    @Operation(summary = "删除商品", description = "逻辑删除当前商户租户下的商品。")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@Parameter(description = "商品 ID", example = "65998417388650497")
                               @PathVariable("id") Long id,
                               LoginUser loginUser) {
        productService.remove(id, loginUser);
        return Result.success();
    }
}
