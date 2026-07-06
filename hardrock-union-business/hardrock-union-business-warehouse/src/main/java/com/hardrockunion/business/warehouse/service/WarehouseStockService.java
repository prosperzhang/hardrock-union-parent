package com.hardrockunion.business.warehouse.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hardrockunion.business.warehouse.domain.entity.Warehouse;
import com.hardrockunion.business.warehouse.domain.entity.WarehouseStock;
import com.hardrockunion.business.warehouse.dto.WarehouseStockPutRequest;
import com.hardrockunion.business.warehouse.dto.WarehouseStockResponse;
import com.hardrockunion.business.warehouse.mapper.WarehouseMapper;
import com.hardrockunion.business.warehouse.mapper.WarehouseStockMapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;

@Service
public class WarehouseStockService {

    private final WarehouseMapper warehouseMapper;
    private final WarehouseStockMapper warehouseStockMapper;

    public WarehouseStockService(WarehouseMapper warehouseMapper,
                                 WarehouseStockMapper warehouseStockMapper) {
        this.warehouseMapper = warehouseMapper;
        this.warehouseStockMapper = warehouseStockMapper;
    }

    public List<WarehouseStockResponse> list(String appCode, Long warehouseId, LoginUser loginUser) {
        Warehouse warehouse = loadWarehouse(appCode, warehouseId, loginUser);
        return warehouseStockMapper.selectList(new LambdaQueryWrapper<WarehouseStock>()
                .eq(WarehouseStock::getAppCode, warehouse.getAppCode())
                .eq(WarehouseStock::getTenantId, warehouse.getTenantId())
                .eq(WarehouseStock::getWarehouseId, warehouse.getId())
                .eq(WarehouseStock::getDeleted, 0)
                .orderByDesc(WarehouseStock::getId))
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public WarehouseStockResponse putProduct(String appCode,
                                             Long warehouseId,
                                             WarehouseStockPutRequest request,
                                             LoginUser loginUser) {
        Warehouse warehouse = loadWarehouse(appCode, warehouseId, loginUser);
        if (request == null || request.getProductId() == null || StringUtils.isBlank(request.getProductName())
            || request.getQuantity() == null) {
            throw new BusinessException("productId、productName、quantity 不能为空");
        }
        if (request.getQuantity() <= 0) {
            throw new BusinessException("quantity 必须大于 0");
        }

        WarehouseStock stock = warehouseStockMapper.selectOne(new LambdaQueryWrapper<WarehouseStock>()
            .eq(WarehouseStock::getAppCode, warehouse.getAppCode())
            .eq(WarehouseStock::getTenantId, warehouse.getTenantId())
            .eq(WarehouseStock::getWarehouseId, warehouse.getId())
            .eq(WarehouseStock::getProductId, request.getProductId())
            .eq(WarehouseStock::getDeleted, 0)
            .last("limit 1"));
        if (stock == null) {
            stock = new WarehouseStock();
            stock.setAppCode(warehouse.getAppCode());
            stock.setTenantId(warehouse.getTenantId());
            stock.setWarehouseId(warehouse.getId());
            stock.setProductId(request.getProductId());
            stock.setStockQuantity(0);
            stock.setDeleted(0);
            stock.setCreatedBy(loginUser == null ? null : loginUser.getUserId());
        }
        stock.setProductName(StringUtils.trim(request.getProductName()));
        stock.setSkuCode(StringUtils.trimToNull(request.getSkuCode()));
        stock.setUnit(StringUtils.defaultIfBlank(StringUtils.trimToNull(request.getUnit()), "件"));
        stock.setStockQuantity((stock.getStockQuantity() == null ? 0 : stock.getStockQuantity()) + request.getQuantity());
        if (stock.getId() == null) {
            warehouseStockMapper.insert(stock);
        } else {
            warehouseStockMapper.updateById(stock);
        }
        return toResponse(stock);
    }

    @Transactional(rollbackFor = Exception.class)
    public WarehouseStockResponse applyChange(String appCode,
                                              Long tenantId,
                                              Long warehouseId,
                                              Long productId,
                                              String productName,
                                              String skuCode,
                                              String unit,
                                              String adjustType,
                                              Integer quantity,
                                              Long operatorId) {
        if (tenantId == null || warehouseId == null || productId == null || quantity == null) {
            throw new BusinessException("tenantId、warehouseId、productId、quantity 不能为空");
        }
        if (quantity <= 0) {
            throw new BusinessException("quantity 必须大于 0");
        }
        String normalizedAppCode = StringUtils.upperCase(StringUtils.trimToEmpty(appCode));
        Warehouse warehouse = warehouseMapper.selectOne(new LambdaQueryWrapper<Warehouse>()
            .eq(Warehouse::getAppCode, normalizedAppCode)
            .eq(Warehouse::getTenantId, tenantId)
            .eq(Warehouse::getId, warehouseId)
            .eq(Warehouse::getDeleted, 0)
            .last("limit 1"));
        if (warehouse == null) {
            throw new BusinessException("仓库不存在");
        }

        WarehouseStock stock = warehouseStockMapper.selectOne(new LambdaQueryWrapper<WarehouseStock>()
            .eq(WarehouseStock::getAppCode, normalizedAppCode)
            .eq(WarehouseStock::getTenantId, tenantId)
            .eq(WarehouseStock::getWarehouseId, warehouseId)
            .eq(WarehouseStock::getProductId, productId)
            .eq(WarehouseStock::getDeleted, 0)
            .last("limit 1"));
        if (stock == null) {
            stock = new WarehouseStock();
            stock.setAppCode(normalizedAppCode);
            stock.setTenantId(tenantId);
            stock.setWarehouseId(warehouseId);
            stock.setProductId(productId);
            stock.setStockQuantity(0);
            stock.setDeleted(0);
            stock.setCreatedBy(operatorId);
        }

        int beforeQuantity = stock.getStockQuantity() == null ? 0 : stock.getStockQuantity();
        int afterQuantity = calculateAfterQuantity(beforeQuantity, adjustType, quantity);
        stock.setProductName(StringUtils.defaultIfBlank(StringUtils.trimToNull(productName), stock.getProductName()));
        stock.setSkuCode(StringUtils.defaultIfBlank(StringUtils.trimToNull(skuCode), stock.getSkuCode()));
        stock.setUnit(StringUtils.defaultIfBlank(StringUtils.trimToNull(unit), StringUtils.defaultIfBlank(stock.getUnit(), "件")));
        stock.setStockQuantity(afterQuantity);
        if (stock.getId() == null) {
            warehouseStockMapper.insert(stock);
        } else {
            warehouseStockMapper.updateById(stock);
        }
        return toResponse(stock);
    }

    private int calculateAfterQuantity(int beforeQuantity, String adjustType, int quantity) {
        String normalizedAdjustType = StringUtils.upperCase(StringUtils.trimToEmpty(adjustType));
        return switch (normalizedAdjustType) {
            case "IN" -> beforeQuantity + quantity;
            case "OUT" -> {
                if (beforeQuantity < quantity) {
                    throw new BusinessException("仓库库存不足");
                }
                yield beforeQuantity - quantity;
            }
            default -> throw new BusinessException("adjustType 只支持 IN 或 OUT");
        };
    }

    private Warehouse loadWarehouse(String appCode, Long warehouseId, LoginUser loginUser) {
        if (warehouseId == null || loginUser == null || loginUser.getTenantId() == null) {
            throw new BusinessException("warehouseId 或登录租户不能为空");
        }
        String normalizedAppCode = StringUtils.upperCase(StringUtils.trimToEmpty(appCode));
        Warehouse warehouse = warehouseMapper.selectOne(new LambdaQueryWrapper<Warehouse>()
            .eq(Warehouse::getAppCode, normalizedAppCode)
            .eq(Warehouse::getTenantId, loginUser.getTenantId())
            .eq(Warehouse::getId, warehouseId)
            .eq(Warehouse::getDeleted, 0)
            .last("limit 1"));
        if (warehouse == null) {
            throw new BusinessException("仓库不存在");
        }
        return warehouse;
    }

    private WarehouseStockResponse toResponse(WarehouseStock stock) {
        WarehouseStockResponse response = new WarehouseStockResponse();
        response.setId(stock.getId());
        response.setAppCode(stock.getAppCode());
        response.setTenantId(stock.getTenantId());
        response.setWarehouseId(stock.getWarehouseId());
        response.setProductId(stock.getProductId());
        response.setProductName(stock.getProductName());
        response.setSkuCode(stock.getSkuCode());
        response.setUnit(stock.getUnit());
        response.setStockQuantity(stock.getStockQuantity());
        return response;
    }
}
