package com.hardrockunion.business.warehouse.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.business.warehouse.dto.WarehouseInventoryRecordResponse;
import com.hardrockunion.business.warehouse.service.WarehouseInventoryRecordService;
import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 商品库存流水入口。
 *
 * <p>接口用于查询仓储域内的商品库存变化记录。
 */
@RestController
@RequestMapping("/api/warehouse/products/{productId}/inventory-records")
@Tag(name = "仓储-库存流水", description = "商品库存变更流水查询")
public class WarehouseInventoryRecordController {

    private final WarehouseInventoryRecordService inventoryRecordService;

    public WarehouseInventoryRecordController(WarehouseInventoryRecordService inventoryRecordService) {
        this.inventoryRecordService = inventoryRecordService;
    }

    @Operation(summary = "商品库存流水", description = "查看某个商品的库存变更记录，包含初始化、手工调整、订单扣减、取消回滚等来源。")
    @GetMapping
    public Result<List<WarehouseInventoryRecordResponse>> list(@Parameter(description = "商品 ID", example = "65998417388650497")
                                                            @PathVariable("productId") Long productId,
                                                            LoginUser loginUser) {
        return Result.success(inventoryRecordService.listByProduct(loginUser.getTenantId(), productId, loginUser));
    }
}
