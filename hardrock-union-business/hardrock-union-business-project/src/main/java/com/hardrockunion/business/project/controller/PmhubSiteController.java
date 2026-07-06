package com.hardrockunion.business.project.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.business.project.dto.PmhubSiteCreateRequest;
import com.hardrockunion.business.project.dto.PmhubSiteResponse;
import com.hardrockunion.business.project.service.PmhubSiteService;
import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;

@RestController
@RequestMapping("/api/pmhub/sites")
public class PmhubSiteController {

    private final PmhubSiteService siteService;

    public PmhubSiteController(PmhubSiteService siteService) {
        this.siteService = siteService;
    }

    @GetMapping
    public Result<List<PmhubSiteResponse>> list(LoginUser loginUser) {
        return Result.success(siteService.list(loginUser));
    }

    @GetMapping("/{id}")
    public Result<PmhubSiteResponse> get(@PathVariable("id") Long id, LoginUser loginUser) {
        return Result.success(siteService.getById(id, loginUser));
    }

    @PostMapping
    public Result<PmhubSiteResponse> create(@RequestBody PmhubSiteCreateRequest request, LoginUser loginUser) {
        return Result.success(siteService.create(request, loginUser));
    }
}
