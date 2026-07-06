package com.hardrockunion.business.project.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.business.project.dto.PmhubSiteWorkScopeCreateRequest;
import com.hardrockunion.business.project.dto.PmhubSiteWorkScopeQueryRequest;
import com.hardrockunion.business.project.dto.PmhubSiteWorkScopeResponse;
import com.hardrockunion.business.project.service.PmhubSiteWorkScopeService;
import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/pmhub/site-work-scopes")
@Tag(name = "PMHUB-施工范围", description = "PMHub 标段/工地下的责任范围管理，用于表达 A-001 到 A-5000 这类承包范围")
public class PmhubSiteWorkScopeController {

    private final PmhubSiteWorkScopeService siteWorkScopeService;

    public PmhubSiteWorkScopeController(PmhubSiteWorkScopeService siteWorkScopeService) {
        this.siteWorkScopeService = siteWorkScopeService;
    }

    @Operation(summary = "施工范围分页列表", description = "支持按项目、标段、参建单位、范围类型和关键词分页查看施工范围。")
    @GetMapping
    public Result<PageResponse<PmhubSiteWorkScopeResponse>> list(PmhubSiteWorkScopeQueryRequest request,
                                                                LoginUser loginUser) {
        return Result.success(siteWorkScopeService.list(request, loginUser));
    }

    @Operation(summary = "施工范围详情", description = "查看单条施工范围，确认项目、标段、参建单位和起止范围编码。")
    @GetMapping("/{id}")
    public Result<PmhubSiteWorkScopeResponse> get(@Parameter(description = "施工范围 ID", example = "68262140034686979")
                                                 @PathVariable("id") Long id,
                                                 LoginUser loginUser) {
        return Result.success(siteWorkScopeService.getById(id, loginUser));
    }

    @Operation(summary = "创建施工范围", description = "为某个参建单位在某个标段下登记责任范围，例如 A-001 到 A-5000。")
    @PostMapping
    public Result<PmhubSiteWorkScopeResponse> create(@RequestBody PmhubSiteWorkScopeCreateRequest request,
                                                    LoginUser loginUser) {
        return Result.success(siteWorkScopeService.create(request, loginUser));
    }
}
