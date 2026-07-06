package com.hardrockunion.business.warehouse.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.business.warehouse.dto.WarehouseCreateRequest;
import com.hardrockunion.business.warehouse.dto.WarehouseResponse;
import com.hardrockunion.business.warehouse.service.WarehouseRegistryService;
import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/{appCode}/warehouses")
@Tag(name = "数字仓库", description = "项目仓、商家仓等多业务方仓库。")
public class WarehouseRegistryController {

    private final WarehouseRegistryService warehouseRegistryService;

    public WarehouseRegistryController(WarehouseRegistryService warehouseRegistryService) {
        this.warehouseRegistryService = warehouseRegistryService;
    }

    @Operation(summary = "查询当前租户数字仓库", description = "返回当前租户下的项目仓或商家仓。")
    @GetMapping
    public Result<List<WarehouseResponse>> list(@Parameter(description = "应用编码，例如 NEXIS、PRIMELOAD-MARKETPLACE")
                                                @PathVariable("appCode") String appCode,
                                                LoginUser loginUser) {
        return Result.success(warehouseRegistryService.listCurrentTenantWarehouses(appCode, loginUser));
    }

    @Operation(summary = "创建数字仓库", description = "用于 WSGM 综合仓、云仓、商家仓、项目仓等仓库注册。")
    @PostMapping
    public Result<WarehouseResponse> create(@Parameter(description = "应用编码，例如 WSGM、NEXIS、PRIMELOAD-MARKETPLACE")
                                            @PathVariable("appCode") String appCode,
                                            @RequestBody WarehouseCreateRequest request,
                                            LoginUser loginUser) {
        return Result.success(warehouseRegistryService.createWarehouse(appCode, request, loginUser));
    }
}
