package com.hardrockunion.solution.primeloadmarketplace.facade;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hardrockunion.business.merchant.domain.entity.MerchantOrder;
import com.hardrockunion.business.merchant.domain.entity.MerchantProduct;
import com.hardrockunion.business.merchant.domain.entity.MerchantQuotation;
import com.hardrockunion.business.merchant.mapper.MerchantOrderMapper;
import com.hardrockunion.business.merchant.mapper.MerchantProductMapper;
import com.hardrockunion.business.merchant.mapper.MerchantQuotationMapper;
import com.hardrockunion.business.warehouse.domain.entity.Warehouse;
import com.hardrockunion.business.warehouse.domain.entity.WarehouseStock;
import com.hardrockunion.business.warehouse.mapper.WarehouseMapper;
import com.hardrockunion.business.warehouse.mapper.WarehouseStockMapper;
import com.hardrockunion.platform.tenant.dto.TenantRegistryResponse;
import com.hardrockunion.platform.tenant.service.TenantRegistryService;
import com.hardrockunion.solution.primeloadmarketplace.dashboard.dto.PrimeloadMarketplaceOrderSnapshotResponse;
import com.hardrockunion.solution.primeloadmarketplace.dashboard.dto.PrimeloadMarketplaceProductSnapshotResponse;
import com.hardrockunion.solution.primeloadmarketplace.dashboard.dto.PrimeloadMarketplaceQuotationSnapshotResponse;

@Service
public class PrimeloadMarketplaceDashboardAsyncFacade {

    private final MerchantProductMapper primeloadMarketplaceProductMapper;
    private final MerchantQuotationMapper primeloadMarketplaceQuotationMapper;
    private final MerchantOrderMapper primeloadMarketplaceOrderMapper;
    private final WarehouseMapper warehouseMapper;
    private final WarehouseStockMapper warehouseStockMapper;
    private final TenantRegistryService tenantRegistryService;

    public PrimeloadMarketplaceDashboardAsyncFacade(
        MerchantProductMapper primeloadMarketplaceProductMapper,
        MerchantQuotationMapper primeloadMarketplaceQuotationMapper,
        MerchantOrderMapper primeloadMarketplaceOrderMapper,
        WarehouseMapper warehouseMapper,
        WarehouseStockMapper warehouseStockMapper,
        TenantRegistryService tenantRegistryService
    ) {
        this.primeloadMarketplaceProductMapper = primeloadMarketplaceProductMapper;
        this.primeloadMarketplaceQuotationMapper = primeloadMarketplaceQuotationMapper;
        this.primeloadMarketplaceOrderMapper = primeloadMarketplaceOrderMapper;
        this.warehouseMapper = warehouseMapper;
        this.warehouseStockMapper = warehouseStockMapper;
        this.tenantRegistryService = tenantRegistryService;
    }

    @Async
    public CompletableFuture<TenantRegistryResponse> currentMerchant(Long tenantId) {
        return CompletableFuture.completedFuture(tenantRegistryService.getById(tenantId));
    }

    @Async
    public CompletableFuture<Long> countProducts(Long tenantId) {
        long productCount = primeloadMarketplaceProductMapper.selectCount(new LambdaQueryWrapper<MerchantProduct>()
            .eq(MerchantProduct::getTenantId, tenantId)
            .eq(MerchantProduct::getDeleted, 0));
        return CompletableFuture.completedFuture(productCount);
    }

    @Async
    public CompletableFuture<Long> countActiveProducts(Long tenantId) {
        long activeProductCount = primeloadMarketplaceProductMapper.selectCount(new LambdaQueryWrapper<MerchantProduct>()
            .eq(MerchantProduct::getTenantId, tenantId)
            .eq(MerchantProduct::getDeleted, 0)
            .eq(MerchantProduct::getStatus, 1));
        return CompletableFuture.completedFuture(activeProductCount);
    }

    @Async
    public CompletableFuture<Long> countLowStockProducts(Long tenantId) {
        long lowStockProductCount = primeloadMarketplaceProductMapper.selectCount(new LambdaQueryWrapper<MerchantProduct>()
            .eq(MerchantProduct::getTenantId, tenantId)
            .eq(MerchantProduct::getDeleted, 0)
            .lt(MerchantProduct::getStockQuantity, 100));
        return CompletableFuture.completedFuture(lowStockProductCount);
    }

    @Async
    public CompletableFuture<List<PrimeloadMarketplaceProductSnapshotResponse>> latestProducts(Long tenantId) {
        List<PrimeloadMarketplaceProductSnapshotResponse> latestProducts = primeloadMarketplaceProductMapper.selectList(new LambdaQueryWrapper<MerchantProduct>()
                .eq(MerchantProduct::getTenantId, tenantId)
                .eq(MerchantProduct::getDeleted, 0)
                .orderByDesc(MerchantProduct::getId)
                .last("limit 3"))
            .stream()
            .map(product -> {
                PrimeloadMarketplaceProductSnapshotResponse response = new PrimeloadMarketplaceProductSnapshotResponse();
                response.setId(product.getId());
                response.setProductName(product.getProductName());
                response.setSkuCode(product.getSkuCode());
                response.setSalePrice(product.getSalePrice());
                response.setStockQuantity(product.getStockQuantity());
                return response;
            })
            .toList();
        return CompletableFuture.completedFuture(latestProducts);
    }

    @Async
    public CompletableFuture<Long> countQuotations(Long tenantId) {
        long quotationCount = primeloadMarketplaceQuotationMapper.selectCount(new LambdaQueryWrapper<MerchantQuotation>()
            .eq(MerchantQuotation::getTenantId, tenantId)
            .eq(MerchantQuotation::getDeleted, 0));
        return CompletableFuture.completedFuture(quotationCount);
    }

    @Async
    public CompletableFuture<Long> countIssuedQuotations(Long tenantId) {
        long issuedQuotationCount = primeloadMarketplaceQuotationMapper.selectCount(new LambdaQueryWrapper<MerchantQuotation>()
            .eq(MerchantQuotation::getTenantId, tenantId)
            .eq(MerchantQuotation::getDeleted, 0)
            .eq(MerchantQuotation::getQuotationStatus, "ISSUED"));
        return CompletableFuture.completedFuture(issuedQuotationCount);
    }

    @Async
    public CompletableFuture<Long> countExpiredQuotations(Long tenantId) {
        long expiredQuotationCount = primeloadMarketplaceQuotationMapper.selectCount(new LambdaQueryWrapper<MerchantQuotation>()
            .eq(MerchantQuotation::getTenantId, tenantId)
            .eq(MerchantQuotation::getDeleted, 0)
            .eq(MerchantQuotation::getQuotationStatus, "ISSUED")
            .isNotNull(MerchantQuotation::getValidUntil)
            .lt(MerchantQuotation::getValidUntil, java.time.LocalDateTime.now()));
        return CompletableFuture.completedFuture(expiredQuotationCount);
    }

    @Async
    public CompletableFuture<List<PrimeloadMarketplaceQuotationSnapshotResponse>> latestQuotations(Long tenantId) {
        List<PrimeloadMarketplaceQuotationSnapshotResponse> latestQuotations = primeloadMarketplaceQuotationMapper.selectList(new LambdaQueryWrapper<MerchantQuotation>()
                .eq(MerchantQuotation::getTenantId, tenantId)
                .eq(MerchantQuotation::getDeleted, 0)
                .orderByDesc(MerchantQuotation::getId)
                .last("limit 3"))
            .stream()
            .map(quotation -> {
                PrimeloadMarketplaceQuotationSnapshotResponse response = new PrimeloadMarketplaceQuotationSnapshotResponse();
                response.setId(quotation.getId());
                response.setQuotationNo(quotation.getQuotationNo());
                response.setQuotationStatus(quotation.getQuotationStatus());
                response.setTotalAmount(quotation.getTotalAmount());
                response.setValidUntil(quotation.getValidUntil());
                response.setTargetProjectName(quotation.getTargetProjectName());
                response.setTargetUserName(quotation.getTargetUserName());
                return response;
            })
            .toList();
        return CompletableFuture.completedFuture(latestQuotations);
    }

    @Async
    public CompletableFuture<Long> countOrders(Long tenantId) {
        long orderCount = primeloadMarketplaceOrderMapper.selectCount(new LambdaQueryWrapper<MerchantOrder>()
            .eq(MerchantOrder::getTenantId, tenantId)
            .eq(MerchantOrder::getDeleted, 0));
        return CompletableFuture.completedFuture(orderCount);
    }

    @Async
    public CompletableFuture<Long> countPendingShipmentOrders(Long tenantId) {
        long pendingShipmentOrderCount = primeloadMarketplaceOrderMapper.selectCount(new LambdaQueryWrapper<MerchantOrder>()
            .eq(MerchantOrder::getTenantId, tenantId)
            .eq(MerchantOrder::getDeleted, 0)
            .in(MerchantOrder::getOrderStatus, List.of("ACCEPTED", "CONFIRMED")));
        return CompletableFuture.completedFuture(pendingShipmentOrderCount);
    }

    @Async
    public CompletableFuture<Long> countShippedOrders(Long tenantId) {
        long shippedOrderCount = primeloadMarketplaceOrderMapper.selectCount(new LambdaQueryWrapper<MerchantOrder>()
            .eq(MerchantOrder::getTenantId, tenantId)
            .eq(MerchantOrder::getDeleted, 0)
            .eq(MerchantOrder::getOrderStatus, "SHIPPED"));
        return CompletableFuture.completedFuture(shippedOrderCount);
    }

    @Async
    public CompletableFuture<Long> countCompletedOrders(Long tenantId) {
        long completedOrderCount = primeloadMarketplaceOrderMapper.selectCount(new LambdaQueryWrapper<MerchantOrder>()
            .eq(MerchantOrder::getTenantId, tenantId)
            .eq(MerchantOrder::getDeleted, 0)
            .eq(MerchantOrder::getOrderStatus, "COMPLETED"));
        return CompletableFuture.completedFuture(completedOrderCount);
    }

    @Async
    public CompletableFuture<List<PrimeloadMarketplaceOrderSnapshotResponse>> latestOrders(Long tenantId) {
        List<PrimeloadMarketplaceOrderSnapshotResponse> latestOrders = primeloadMarketplaceOrderMapper.selectList(new LambdaQueryWrapper<MerchantOrder>()
                .eq(MerchantOrder::getTenantId, tenantId)
                .eq(MerchantOrder::getDeleted, 0)
                .orderByDesc(MerchantOrder::getId)
                .last("limit 3"))
            .stream()
            .map(order -> {
                PrimeloadMarketplaceOrderSnapshotResponse response = new PrimeloadMarketplaceOrderSnapshotResponse();
                response.setId(order.getId());
                response.setOrderNo(order.getOrderNo());
                response.setOrderStatus(order.getOrderStatus());
                response.setTotalAmount(order.getTotalAmount());
                response.setTargetProjectName(order.getTargetProjectName());
                response.setTargetUserName(order.getTargetUserName());
                return response;
            })
            .toList();
        return CompletableFuture.completedFuture(latestOrders);
    }

    @Async
    public CompletableFuture<Long> countWarehouses(Long tenantId) {
        long warehouseCount = warehouseMapper.selectCount(new LambdaQueryWrapper<Warehouse>()
            .eq(Warehouse::getAppCode, "PRIMELOAD-MARKETPLACE")
            .eq(Warehouse::getTenantId, tenantId)
            .eq(Warehouse::getDeleted, 0));
        return CompletableFuture.completedFuture(warehouseCount);
    }

    @Async
    public CompletableFuture<Long> countActiveWarehouses(Long tenantId) {
        long activeWarehouseCount = warehouseMapper.selectCount(new LambdaQueryWrapper<Warehouse>()
            .eq(Warehouse::getAppCode, "PRIMELOAD-MARKETPLACE")
            .eq(Warehouse::getTenantId, tenantId)
            .eq(Warehouse::getDeleted, 0)
            .eq(Warehouse::getStatus, 1));
        return CompletableFuture.completedFuture(activeWarehouseCount);
    }

    @Async
    public CompletableFuture<Long> totalWarehouseStockQuantity(Long tenantId) {
        long totalWarehouseStockQuantity = warehouseStockMapper.selectList(new LambdaQueryWrapper<WarehouseStock>()
                .eq(WarehouseStock::getAppCode, "PRIMELOAD-MARKETPLACE")
                .eq(WarehouseStock::getTenantId, tenantId)
                .eq(WarehouseStock::getDeleted, 0))
            .stream()
            .map(WarehouseStock::getStockQuantity)
            .filter(quantity -> quantity != null)
            .mapToLong(Integer::longValue)
            .sum();
        return CompletableFuture.completedFuture(totalWarehouseStockQuantity);
    }
}
