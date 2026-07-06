package com.hardrockunion.business.warehouse.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.business.warehouse.dto.WarehouseStockPutRequest;
import com.hardrockunion.business.warehouse.dto.WarehouseStockResponse;
import com.hardrockunion.business.warehouse.service.WarehouseStockService;
import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/{appCode}/warehouses/{warehouseId}/stocks")
@Tag(name = "数字仓库库存", description = "按数字仓库维护产品库存。")
public class WarehouseStockController {

    private final WarehouseStockService warehouseStockService;

    public WarehouseStockController(WarehouseStockService warehouseStockService) {
        this.warehouseStockService = warehouseStockService;
    }

    @Operation(summary = "查询仓库产品库存")
    @GetMapping
    public Result<List<WarehouseStockResponse>> list(@Parameter(description = "应用编码，例如 PMHUB、PRIMELOAD-MARKETPLACE")
                                                     @PathVariable("appCode") String appCode,
                                                     @Parameter(description = "仓库ID")
                                                     @PathVariable("warehouseId") Long warehouseId,
                                                     LoginUser loginUser) {
        return Result.success(warehouseStockService.list(appCode, warehouseId, loginUser));
    }

    @Operation(summary = "放入产品库存", description = "把产品放入当前租户指定数字仓库；重复放入同一产品时累加数量。")
    @PostMapping
    public Result<WarehouseStockResponse> putProduct(@Parameter(description = "应用编码，例如 PMHUB、PRIMELOAD-MARKETPLACE")
                                                     @PathVariable("appCode") String appCode,
                                                     @Parameter(description = "仓库ID")
                                                     @PathVariable("warehouseId") Long warehouseId,
                                                     @RequestBody WarehouseStockPutRequest request,
                                                     LoginUser loginUser) {
        return Result.success(warehouseStockService.putProduct(appCode, warehouseId, request, loginUser));
    }
}
