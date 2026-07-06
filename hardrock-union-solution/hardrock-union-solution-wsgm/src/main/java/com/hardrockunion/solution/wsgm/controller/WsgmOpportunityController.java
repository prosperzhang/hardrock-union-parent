package com.hardrockunion.solution.wsgm.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.solution.wsgm.dto.WsgmOpportunityCreateRequest;
import com.hardrockunion.solution.wsgm.dto.WsgmOpportunityResponse;
import com.hardrockunion.solution.wsgm.service.WsgmOpportunityService;

@RestController
@RequestMapping("/api/wsgm/customers/{customerId}/opportunities")
public class WsgmOpportunityController {

    private final WsgmOpportunityService opportunityService;

    public WsgmOpportunityController(WsgmOpportunityService opportunityService) {
        this.opportunityService = opportunityService;
    }

    @GetMapping
    public Result<List<WsgmOpportunityResponse>> list(
        @PathVariable("customerId") Long customerId,
        LoginUser loginUser
    ) {
        return Result.success(opportunityService.list(customerId, loginUser));
    }

    @PostMapping
    public Result<WsgmOpportunityResponse> create(
        @PathVariable("customerId") Long customerId,
        @RequestBody WsgmOpportunityCreateRequest request,
        LoginUser loginUser
    ) {
        return Result.success(opportunityService.create(customerId, request, loginUser));
    }
}
