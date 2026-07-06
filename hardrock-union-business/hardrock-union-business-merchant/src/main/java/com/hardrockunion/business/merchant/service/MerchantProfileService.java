package com.hardrockunion.business.merchant.service;

import org.springframework.stereotype.Service;

import com.hardrockunion.business.merchant.dto.MerchantProfileResponse;
import com.hardrockunion.business.merchant.dto.MerchantProfileUpdateRequest;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.tenant.dto.TenantRegistryResponse;
import com.hardrockunion.platform.tenant.service.TenantRegistryService;

@Service
public class MerchantProfileService {

    private static final String APP_CODE = "PRIMELOAD-MARKETPLACE";

    private final MerchantAccessGuard merchantAccessGuard;
    private final TenantRegistryService tenantRegistryService;

    public MerchantProfileService(MerchantAccessGuard merchantAccessGuard,
                                  TenantRegistryService tenantRegistryService) {
        this.merchantAccessGuard = merchantAccessGuard;
        this.tenantRegistryService = tenantRegistryService;
    }

    public MerchantProfileResponse current(LoginUser loginUser) {
        TenantRegistryResponse merchant = merchantAccessGuard.ensureMerchantLogin(loginUser);
        return toResponse(merchant);
    }

    public MerchantProfileResponse update(MerchantProfileUpdateRequest request, LoginUser loginUser) {
        merchantAccessGuard.ensureMerchantLogin(loginUser);
        TenantRegistryResponse merchant = tenantRegistryService.updateBasicInfo(
            APP_CODE,
            loginUser.getTenantId(),
            request == null ? null : request.getMerchantName(),
            request == null ? null : request.getBusinessAddress(),
            null,
            null,
            null,
            null,
            null,
            null,
            request == null ? null : request.getManagerName(),
            request == null ? null : request.getManagerPhone()
        );
        return toResponse(merchant);
    }

    private MerchantProfileResponse toResponse(TenantRegistryResponse merchant) {
        MerchantProfileResponse response = new MerchantProfileResponse();
        response.setTenantId(merchant.getId());
        response.setMerchantCode(merchant.getTenantCode());
        response.setMerchantName(merchant.getTenantName());
        response.setMerchantType(merchant.getTenantType());
        response.setBusinessAddress(merchant.getProjectAddress());
        response.setManagerName(merchant.getManagerName());
        response.setManagerPhone(merchant.getManagerPhone());
        response.setStatus(merchant.getStatus());
        return response;
    }
}
