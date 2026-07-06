package com.hardrockunion.solution.primeloadmarketplace.dashboard;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.solution.primeloadmarketplace.dashboard.dto.PrimeloadMarketplaceDashboardOverviewResponse;
import com.hardrockunion.solution.primeloadmarketplace.facade.PrimeloadMarketplaceDashboardFacade;

@RestController
@RequestMapping("/api/primeload-marketplace/dashboard")
public class PrimeloadMarketplaceDashboardController {

    private final PrimeloadMarketplaceDashboardFacade primeloadMarketplaceDashboardFacade;

    public PrimeloadMarketplaceDashboardController(PrimeloadMarketplaceDashboardFacade primeloadMarketplaceDashboardFacade) {
        this.primeloadMarketplaceDashboardFacade = primeloadMarketplaceDashboardFacade;
    }

    @GetMapping("/overview")
    public Result<PrimeloadMarketplaceDashboardOverviewResponse> overview(LoginUser loginUser) {
        return Result.success(primeloadMarketplaceDashboardFacade.overview(loginUser));
    }
}
