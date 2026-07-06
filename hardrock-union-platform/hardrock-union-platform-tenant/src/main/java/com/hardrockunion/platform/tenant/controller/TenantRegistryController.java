package com.hardrockunion.platform.tenant.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.platform.tenant.dto.TenantRegistryResponse;
import com.hardrockunion.platform.tenant.service.TenantRegistryService;

@RestController
public class TenantRegistryController {

    private final TenantRegistryService tenantRegistryService;

    public TenantRegistryController(TenantRegistryService tenantRegistryService) {
        this.tenantRegistryService = tenantRegistryService;
    }

    @GetMapping("/api/tenant-registry")
    public Result<List<TenantRegistryResponse>> list(@RequestParam(value = "appCode", required = false) String appCode) {
        return Result.success(tenantRegistryService.listEnabled(appCode));
    }

    @GetMapping("/api/tenant-registry/{tenantId}")
    public Result<TenantRegistryResponse> get(@PathVariable("tenantId") Long tenantId) {
        return Result.success(tenantRegistryService.getById(tenantId));
    }

    @GetMapping("/api/{appCode}/tenant-registry")
    public Result<List<TenantRegistryResponse>> listByApp(@PathVariable("appCode") String appCode) {
        return Result.success(tenantRegistryService.listEnabledByApp(appCode));
    }

    @GetMapping("/api/{appCode}/tenant-registry/{tenantId}")
    public Result<TenantRegistryResponse> getByApp(@PathVariable("appCode") String appCode, @PathVariable("tenantId") Long tenantId) {
        return Result.success(tenantRegistryService.getByAppAndId(appCode, tenantId));
    }
}
