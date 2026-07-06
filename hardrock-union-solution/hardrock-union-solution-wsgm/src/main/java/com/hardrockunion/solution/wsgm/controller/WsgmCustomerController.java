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
import com.hardrockunion.solution.wsgm.dto.WsgmCustomerCreateRequest;
import com.hardrockunion.solution.wsgm.dto.WsgmCustomerResponse;
import com.hardrockunion.solution.wsgm.service.WsgmCustomerService;

@RestController
@RequestMapping("/api/wsgm/customers")
public class WsgmCustomerController {

    private final WsgmCustomerService customerService;

    public WsgmCustomerController(WsgmCustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public Result<List<WsgmCustomerResponse>> list(LoginUser loginUser) {
        return Result.success(customerService.list(loginUser));
    }

    @GetMapping("/{id}")
    public Result<WsgmCustomerResponse> get(@PathVariable("id") Long id, LoginUser loginUser) {
        return Result.success(customerService.getById(id, loginUser));
    }

    @PostMapping
    public Result<WsgmCustomerResponse> create(@RequestBody WsgmCustomerCreateRequest request, LoginUser loginUser) {
        return Result.success(customerService.create(request, loginUser));
    }
}
