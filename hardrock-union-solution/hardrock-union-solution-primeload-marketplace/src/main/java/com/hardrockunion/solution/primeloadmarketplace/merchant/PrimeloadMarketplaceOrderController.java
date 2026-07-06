package com.hardrockunion.solution.primeloadmarketplace.merchant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.business.merchant.dto.MerchantOrderCreateRequest;
import com.hardrockunion.business.merchant.dto.MerchantOrderQueryRequest;
import com.hardrockunion.business.merchant.dto.MerchantOrderResponse;
import com.hardrockunion.business.merchant.dto.MerchantOrderShipRequest;
import com.hardrockunion.business.merchant.dto.MerchantOrderStatusUpdateRequest;
import com.hardrockunion.business.merchant.service.MerchantOrderService;
import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * PRIMELOAD-MARKETPLACE 商户订单入口。
 *
 * <p>这里提供 PRIMELOAD-MARKETPLACE 应用语义下的订单接口，业务规则统一收口在 merchant business。
 */
@RestController
@RequestMapping("/api/primeload-marketplace/orders")
@Tag(name = "PRIMELOAD-MARKETPLACE 商户订单", description = "PRIMELOAD-MARKETPLACE 商户订单管理、状态流转与简化履约")
public class PrimeloadMarketplaceOrderController {

    private final MerchantOrderService merchantOrderService;

    public PrimeloadMarketplaceOrderController(MerchantOrderService merchantOrderService) {
        this.merchantOrderService = merchantOrderService;
    }

    @Operation(summary = "订单分页列表", description = "支持按订单号、状态、目标项目/工地/联系人和履约信息筛选。")
    @GetMapping
    public Result<PageResponse<MerchantOrderResponse>> list(MerchantOrderQueryRequest request, LoginUser loginUser) {
        return Result.success(merchantOrderService.list(request, loginUser));
    }

    @Operation(summary = "订单详情", description = "查看订单头信息、目标 PMHUB 交易对象、履约信息和订单明细。")
    @GetMapping("/{id}")
    public Result<MerchantOrderResponse> getById(@Parameter(description = "订单 ID")
                                                 @PathVariable("id") Long id,
                                                 LoginUser loginUser) {
        return Result.success(merchantOrderService.getById(id, loginUser));
    }

    @Operation(summary = "创建订单", description = "创建 PRIMELOAD-MARKETPLACE 商户订单，绑定 PMHUB 项目/工地/联系人。")
    @PostMapping
    public Result<MerchantOrderResponse> create(@RequestBody MerchantOrderCreateRequest request, LoginUser loginUser) {
        return Result.success(merchantOrderService.create(request, loginUser));
    }

    @Operation(summary = "更新订单状态", description = "支持接单、收货、完成、取消和售后等商户主链路状态流转。")
    @PostMapping("/{id}/status")
    public Result<MerchantOrderResponse> updateStatus(@Parameter(description = "订单 ID")
                                                      @PathVariable("id") Long id,
                                                      @RequestBody MerchantOrderStatusUpdateRequest request,
                                                      LoginUser loginUser) {
        return Result.success(merchantOrderService.updateStatus(id, request, loginUser));
    }

    @Operation(summary = "订单发货", description = "录入物流公司、运单号、发货时间与发货备注，并推进到 SHIPPED。")
    @PostMapping("/{id}/ship")
    public Result<MerchantOrderResponse> ship(@Parameter(description = "订单 ID")
                                              @PathVariable("id") Long id,
                                              @RequestBody MerchantOrderShipRequest request,
                                              LoginUser loginUser) {
        return Result.success(merchantOrderService.ship(id, request, loginUser));
    }
}
