package com.hardrockunion.platform.region.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.platform.region.dto.RegionLocateResponse;
import com.hardrockunion.platform.region.dto.RegionResponse;
import com.hardrockunion.platform.region.dto.RegionSyncResponse;
import com.hardrockunion.platform.region.service.SysRegionService;
import com.hardrockunion.platform.region.service.TencentRegionLocationService;
import com.hardrockunion.platform.region.service.TencentRegionSyncService;

@RestController
public class SysRegionController {

    private final SysRegionService sysRegionService;

    private final TencentRegionSyncService tencentRegionSyncService;

    private final TencentRegionLocationService tencentRegionLocationService;

    public SysRegionController(SysRegionService sysRegionService,
                               TencentRegionSyncService tencentRegionSyncService,
                               TencentRegionLocationService tencentRegionLocationService) {
        this.sysRegionService = sysRegionService;
        this.tencentRegionSyncService = tencentRegionSyncService;
        this.tencentRegionLocationService = tencentRegionLocationService;
    }

    @GetMapping("/api/platform/regions")
    public Result<List<RegionResponse>> listChildren(@RequestParam(value = "parentCode", required = false) String parentCode) {
        return Result.success(sysRegionService.listChildren(parentCode));
    }

    @GetMapping("/api/platform/regions/locate")
    public Result<RegionLocateResponse> locate(@RequestParam("latitude") BigDecimal latitude,
                                                @RequestParam("longitude") BigDecimal longitude) {
        return Result.success(tencentRegionLocationService.locate(latitude, longitude));
    }

    @GetMapping("/api/platform/regions/{code:\\d+}")
    public Result<RegionResponse> getByCode(@PathVariable("code") String code) {
        return Result.success(sysRegionService.getByCode(code));
    }

    @PostMapping("/api/platform/regions/sync/tencent")
    public Result<RegionSyncResponse> syncTencentRegions() {
        return Result.success(tencentRegionSyncService.syncTencentRegions());
    }
}
