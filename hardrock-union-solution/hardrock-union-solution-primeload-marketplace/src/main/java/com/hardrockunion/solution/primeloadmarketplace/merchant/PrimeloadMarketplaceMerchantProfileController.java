package com.hardrockunion.solution.primeloadmarketplace.merchant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.business.merchant.dto.MerchantProfileResponse;
import com.hardrockunion.business.merchant.dto.MerchantProfileUpdateRequest;
import com.hardrockunion.business.merchant.service.MerchantProfileService;
import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/primeload-marketplace/merchant/profile")
@Tag(name = "PRIMELOAD-MARKETPLACE-商户资料", description = "当前 PRIMELOAD-MARKETPLACE 商户资料。商户身份来自 tenant_registry。")
public class PrimeloadMarketplaceMerchantProfileController {

    private final MerchantProfileService merchantProfileService;

    public PrimeloadMarketplaceMerchantProfileController(MerchantProfileService merchantProfileService) {
        this.merchantProfileService = merchantProfileService;
    }

    @Operation(summary = "查询当前商户资料", description = "返回当前 PRIMELOAD-MARKETPLACE 登录上下文对应的商户租户资料。")
    @GetMapping
    public Result<MerchantProfileResponse> current(LoginUser loginUser) {
        return Result.success(merchantProfileService.current(loginUser));
    }

    @Operation(summary = "更新当前商户资料", description = "更新当前商户租户的名称、经营地址和负责人信息。")
    @PutMapping
    public Result<MerchantProfileResponse> update(@RequestBody MerchantProfileUpdateRequest request, LoginUser loginUser) {
        return Result.success(merchantProfileService.update(request, loginUser));
    }
}
