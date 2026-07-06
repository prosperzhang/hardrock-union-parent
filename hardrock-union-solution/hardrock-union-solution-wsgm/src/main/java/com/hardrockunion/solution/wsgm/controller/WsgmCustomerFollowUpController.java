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
import com.hardrockunion.solution.wsgm.dto.WsgmCustomerFollowUpCreateRequest;
import com.hardrockunion.solution.wsgm.dto.WsgmCustomerFollowUpResponse;
import com.hardrockunion.solution.wsgm.service.WsgmCustomerFollowUpService;

@RestController
@RequestMapping("/api/wsgm/customers/{customerId}/follow-ups")
public class WsgmCustomerFollowUpController {

    private final WsgmCustomerFollowUpService followUpService;

    public WsgmCustomerFollowUpController(WsgmCustomerFollowUpService followUpService) {
        this.followUpService = followUpService;
    }

    @GetMapping
    public Result<List<WsgmCustomerFollowUpResponse>> list(
        @PathVariable("customerId") Long customerId,
        LoginUser loginUser
    ) {
        return Result.success(followUpService.list(customerId, loginUser));
    }

    @PostMapping
    public Result<WsgmCustomerFollowUpResponse> create(
        @PathVariable("customerId") Long customerId,
        @RequestBody WsgmCustomerFollowUpCreateRequest request,
        LoginUser loginUser
    ) {
        return Result.success(followUpService.create(customerId, request, loginUser));
    }
}
