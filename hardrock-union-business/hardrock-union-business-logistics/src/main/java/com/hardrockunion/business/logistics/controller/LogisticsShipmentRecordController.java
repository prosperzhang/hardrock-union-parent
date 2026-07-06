package com.hardrockunion.business.logistics.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.business.logistics.dto.LogisticsShipmentRecordResponse;
import com.hardrockunion.business.logistics.service.LogisticsShipmentRecordService;
import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 发货记录入口。
 */
@RestController
@RequestMapping("/api/logistics/orders/{orderId}/shipment-record")
@Tag(name = "物流-发货记录", description = "订单发货与作废记录查询")
public class LogisticsShipmentRecordController {

    private final LogisticsShipmentRecordService shipmentRecordService;

    public LogisticsShipmentRecordController(LogisticsShipmentRecordService shipmentRecordService) {
        this.shipmentRecordService = shipmentRecordService;
    }

    @Operation(summary = "订单发货记录", description = "查看订单对应的发货记录；如订单取消，记录会标记为 INVALIDATED。")
    @GetMapping
    public Result<LogisticsShipmentRecordResponse> get(@Parameter(description = "订单 ID", example = "66047732201627648")
                                                    @PathVariable("orderId") Long orderId,
                                                    LoginUser loginUser) {
        return Result.success(shipmentRecordService.getBySource(loginUser.getTenantId(), "ORDER", orderId));
    }
}
