package com.hardrockunion.solution.primeloadmarketplace.facade;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import com.hardrockunion.business.merchant.service.MerchantAccessGuard;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.tenant.dto.TenantRegistryResponse;
import com.hardrockunion.solution.primeloadmarketplace.dashboard.dto.PrimeloadMarketplaceDashboardOverviewResponse;
import com.hardrockunion.solution.primeloadmarketplace.dashboard.dto.PrimeloadMarketplaceOrderSnapshotResponse;
import com.hardrockunion.solution.primeloadmarketplace.dashboard.dto.PrimeloadMarketplaceProductSnapshotResponse;
import com.hardrockunion.solution.primeloadmarketplace.dashboard.dto.PrimeloadMarketplaceQuotationSnapshotResponse;

@Service
public class PrimeloadMarketplaceDashboardFacade {

    private final MerchantAccessGuard merchantAccessGuard;
    private final PrimeloadMarketplaceDashboardAsyncFacade primeloadMarketplaceDashboardAsyncFacade;

    public PrimeloadMarketplaceDashboardFacade(MerchantAccessGuard merchantAccessGuard, PrimeloadMarketplaceDashboardAsyncFacade primeloadMarketplaceDashboardAsyncFacade) {
        this.merchantAccessGuard = merchantAccessGuard;
        this.primeloadMarketplaceDashboardAsyncFacade = primeloadMarketplaceDashboardAsyncFacade;
    }

    public PrimeloadMarketplaceDashboardOverviewResponse overview(LoginUser loginUser) {
        merchantAccessGuard.ensureLogin(loginUser);

        CompletableFuture<TenantRegistryResponse> merchantFuture = primeloadMarketplaceDashboardAsyncFacade.currentMerchant(loginUser.getTenantId());
        CompletableFuture<Long> productCountFuture = primeloadMarketplaceDashboardAsyncFacade.countProducts(loginUser.getTenantId());
        CompletableFuture<Long> activeProductCountFuture = primeloadMarketplaceDashboardAsyncFacade.countActiveProducts(loginUser.getTenantId());
        CompletableFuture<Long> lowStockProductCountFuture = primeloadMarketplaceDashboardAsyncFacade.countLowStockProducts(loginUser.getTenantId());
        CompletableFuture<Long> quotationCountFuture = primeloadMarketplaceDashboardAsyncFacade.countQuotations(loginUser.getTenantId());
        CompletableFuture<Long> issuedQuotationCountFuture = primeloadMarketplaceDashboardAsyncFacade.countIssuedQuotations(loginUser.getTenantId());
        CompletableFuture<Long> expiredQuotationCountFuture = primeloadMarketplaceDashboardAsyncFacade.countExpiredQuotations(loginUser.getTenantId());
        CompletableFuture<Long> orderCountFuture = primeloadMarketplaceDashboardAsyncFacade.countOrders(loginUser.getTenantId());
        CompletableFuture<Long> pendingShipmentOrderCountFuture = primeloadMarketplaceDashboardAsyncFacade.countPendingShipmentOrders(loginUser.getTenantId());
        CompletableFuture<Long> shippedOrderCountFuture = primeloadMarketplaceDashboardAsyncFacade.countShippedOrders(loginUser.getTenantId());
        CompletableFuture<Long> completedOrderCountFuture = primeloadMarketplaceDashboardAsyncFacade.countCompletedOrders(loginUser.getTenantId());
        CompletableFuture<Long> warehouseCountFuture = primeloadMarketplaceDashboardAsyncFacade.countWarehouses(loginUser.getTenantId());
        CompletableFuture<Long> activeWarehouseCountFuture = primeloadMarketplaceDashboardAsyncFacade.countActiveWarehouses(loginUser.getTenantId());
        CompletableFuture<Long> warehouseStockQuantityFuture = primeloadMarketplaceDashboardAsyncFacade.totalWarehouseStockQuantity(loginUser.getTenantId());
        CompletableFuture<List<PrimeloadMarketplaceProductSnapshotResponse>> latestProductsFuture = primeloadMarketplaceDashboardAsyncFacade.latestProducts(loginUser.getTenantId());
        CompletableFuture<List<PrimeloadMarketplaceQuotationSnapshotResponse>> latestQuotationsFuture = primeloadMarketplaceDashboardAsyncFacade.latestQuotations(loginUser.getTenantId());
        CompletableFuture<List<PrimeloadMarketplaceOrderSnapshotResponse>> latestOrdersFuture = primeloadMarketplaceDashboardAsyncFacade.latestOrders(loginUser.getTenantId());

        TenantRegistryResponse merchant = merchantFuture.join();

        PrimeloadMarketplaceDashboardOverviewResponse response = new PrimeloadMarketplaceDashboardOverviewResponse();
        response.setTenantId(loginUser.getTenantId());
        response.setMerchantName(merchant == null ? null : merchant.getTenantName());
        response.setProductCount(productCountFuture.join());
        response.setActiveProductCount(activeProductCountFuture.join());
        response.setLowStockProductCount(lowStockProductCountFuture.join());
        response.setQuotationCount(quotationCountFuture.join());
        response.setIssuedQuotationCount(issuedQuotationCountFuture.join());
        response.setExpiredQuotationCount(expiredQuotationCountFuture.join());
        response.setOrderCount(orderCountFuture.join());
        response.setPendingShipmentOrderCount(pendingShipmentOrderCountFuture.join());
        response.setShippedOrderCount(shippedOrderCountFuture.join());
        response.setCompletedOrderCount(completedOrderCountFuture.join());
        response.setWarehouseCount(warehouseCountFuture.join());
        response.setActiveWarehouseCount(activeWarehouseCountFuture.join());
        response.setWarehouseStockQuantity(warehouseStockQuantityFuture.join());
        response.setLatestProducts(latestProductsFuture.join());
        response.setLatestQuotations(latestQuotationsFuture.join());
        response.setLatestOrders(latestOrdersFuture.join());
        return response;
    }
}
