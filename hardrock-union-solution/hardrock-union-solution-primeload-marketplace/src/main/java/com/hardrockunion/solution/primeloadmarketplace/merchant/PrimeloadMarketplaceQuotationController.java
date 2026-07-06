package com.hardrockunion.solution.primeloadmarketplace.merchant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.business.merchant.dto.MerchantOrderResponse;
import com.hardrockunion.business.merchant.dto.MerchantQuotationCreateRequest;
import com.hardrockunion.business.merchant.dto.MerchantQuotationQueryRequest;
import com.hardrockunion.business.merchant.dto.MerchantQuotationResponse;
import com.hardrockunion.business.merchant.dto.MerchantQuotationStatusUpdateRequest;
import com.hardrockunion.business.merchant.service.MerchantQuotationService;
import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * PRIMELOAD-MARKETPLACE 商户报价单入口。
 *
 * <p>PRIMELOAD-MARKETPLACE 是面向商户使用的应用入口；报价单业务能力仍复用 merchant business。
 */
@RestController
@RequestMapping("/api/primeload-marketplace/quotations")
@Tag(name = "PRIMELOAD-MARKETPLACE 商户报价单", description = "PRIMELOAD-MARKETPLACE 商户报价单管理、状态流转与报价转订单")
public class PrimeloadMarketplaceQuotationController {

    private final MerchantQuotationService merchantQuotationService;

    public PrimeloadMarketplaceQuotationController(MerchantQuotationService merchantQuotationService) {
        this.merchantQuotationService = merchantQuotationService;
    }

    @Operation(summary = "报价单分页列表", description = "支持按关键字、目标项目/工地/联系人、仓库、物流、有效期与是否过期筛选。")
    @GetMapping
    public Result<PageResponse<MerchantQuotationResponse>> list(MerchantQuotationQueryRequest request, LoginUser loginUser) {
        return Result.success(merchantQuotationService.list(request, loginUser));
    }

    @Operation(summary = "报价单详情", description = "查看报价单头信息、目标 NEXIS 交易对象和报价明细。")
    @GetMapping("/{id}")
    public Result<MerchantQuotationResponse> getById(@Parameter(description = "报价单 ID")
                                                     @PathVariable("id") Long id,
                                                     LoginUser loginUser) {
        return Result.success(merchantQuotationService.getById(id, loginUser));
    }

    @Operation(summary = "创建报价单", description = "面向 NEXIS 交易对象创建报价单。")
    @PostMapping
    public Result<MerchantQuotationResponse> create(@RequestBody MerchantQuotationCreateRequest request, LoginUser loginUser) {
        return Result.success(merchantQuotationService.create(request, loginUser));
    }

    @Operation(summary = "更新报价单状态", description = "当前支持 ISSUED 与 CANCELLED 之间的状态流转。")
    @PostMapping("/{id}/status")
    public Result<MerchantQuotationResponse> updateStatus(@Parameter(description = "报价单 ID")
                                                          @PathVariable("id") Long id,
                                                          @RequestBody MerchantQuotationStatusUpdateRequest request,
                                                          LoginUser loginUser) {
        return Result.success(merchantQuotationService.updateStatus(id, request, loginUser));
    }

    @Operation(summary = "报价单转订单", description = "把已签发报价单转换成订单，订单金额沿用报价价格。")
    @PostMapping("/{id}/convert-to-order")
    public Result<MerchantOrderResponse> convertToOrder(@Parameter(description = "报价单 ID")
                                                        @PathVariable("id") Long id,
                                                        LoginUser loginUser) {
        return Result.success(merchantQuotationService.convertToOrder(id, loginUser));
    }
}
