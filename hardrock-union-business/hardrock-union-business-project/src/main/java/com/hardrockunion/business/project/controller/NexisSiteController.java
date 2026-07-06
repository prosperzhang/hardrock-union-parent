package com.hardrockunion.business.project.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.business.project.dto.NexisSiteCreateRequest;
import com.hardrockunion.business.project.dto.NexisSiteResponse;
import com.hardrockunion.business.project.service.NexisSiteService;
import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;

@RestController
@RequestMapping("/api/nexis/sites")
public class NexisSiteController {

    private final NexisSiteService siteService;

    public NexisSiteController(NexisSiteService siteService) {
        this.siteService = siteService;
    }

    @GetMapping
    public Result<List<NexisSiteResponse>> list(LoginUser loginUser) {
        return Result.success(siteService.list(loginUser));
    }

    @GetMapping("/{id}")
    public Result<NexisSiteResponse> get(@PathVariable("id") Long id, LoginUser loginUser) {
        return Result.success(siteService.getById(id, loginUser));
    }

    @PostMapping
    public Result<NexisSiteResponse> create(@RequestBody NexisSiteCreateRequest request, LoginUser loginUser) {
        return Result.success(siteService.create(request, loginUser));
    }
}
