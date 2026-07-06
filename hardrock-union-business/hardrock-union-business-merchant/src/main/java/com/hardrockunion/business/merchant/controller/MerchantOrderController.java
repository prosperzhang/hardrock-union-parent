package com.hardrockunion.business.merchant.controller;

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
 * 商城订单入口。
 *
 * <p>这里负责 商户订单相关的查询、创建和状态流转接口。
 */
@RestController
@RequestMapping("/api/merchant/orders")
@Tag(name = "商户订单", description = "订单管理、状态流转与简化履约")
public class MerchantOrderController {

    private final MerchantOrderService merchantOrderService;

    public MerchantOrderController(MerchantOrderService merchantOrderService) {
        this.merchantOrderService = merchantOrderService;
    }

    /**
     * 订单列表，支持状态、时间和 `nexis` 交易目标筛选。
     */
    @Operation(summary = "订单分页列表", description = "支持按订单号、状态、简化履约信息、目标项目/工地/联系人和时间范围分页筛选。")
    @GetMapping
    public Result<PageResponse<MerchantOrderResponse>> list(MerchantOrderQueryRequest request, LoginUser loginUser) {
        return Result.success(merchantOrderService.list(request, loginUser));
    }

    /**
     * 订单详情。
     */
    @Operation(summary = "订单详情", description = "查看订单头信息、nexis 交易对象、简化履约信息和订单明细。")
    @GetMapping("/{id}")
    public Result<MerchantOrderResponse> getById(@Parameter(description = "订单 ID", example = "66050472395350018")
                                             @PathVariable("id") Long id,
                                             LoginUser loginUser) {
        return Result.success(merchantOrderService.getById(id, loginUser));
    }

    /**
     * 创建订单。
     */
    @Operation(summary = "创建订单", description = "创建商城订单，绑定 nexis 项目/工地/联系人。")
    @PostMapping
    public Result<MerchantOrderResponse> create(@RequestBody MerchantOrderCreateRequest request, LoginUser loginUser) {
        return Result.success(merchantOrderService.create(request, loginUser));
    }

    /**
     * 推进订单状态流转。
     */
    @Operation(summary = "更新订单状态", description = "支持创建、接单、收货、完成、取消和售后等 merchant 主链路状态流转。")
    @PostMapping("/{id}/status")
    public Result<MerchantOrderResponse> updateStatus(@Parameter(description = "订单 ID", example = "66050472395350018")
                                                  @PathVariable("id") Long id,
                                                  @RequestBody MerchantOrderStatusUpdateRequest request,
                                                  LoginUser loginUser) {
        return Result.success(merchantOrderService.updateStatus(id, request, loginUser));
    }

    /**
     * 录入发货信息并把订单推进到已发货。
     */
    @Operation(summary = "订单发货", description = "录入物流公司、运单号、发货时间与发货备注，并把订单推进到 SHIPPED。")
    @PostMapping("/{id}/ship")
    public Result<MerchantOrderResponse> ship(@Parameter(description = "订单 ID", example = "66050472395350018")
                                          @PathVariable("id") Long id,
                                          @RequestBody MerchantOrderShipRequest request,
                                          LoginUser loginUser) {
        return Result.success(merchantOrderService.ship(id, request, loginUser));
    }
}
