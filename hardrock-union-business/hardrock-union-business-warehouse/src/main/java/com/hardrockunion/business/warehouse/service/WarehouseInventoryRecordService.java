package com.hardrockunion.business.warehouse.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hardrockunion.business.warehouse.domain.entity.WarehouseInventoryRecord;
import com.hardrockunion.business.warehouse.dto.WarehouseInventoryRecordResponse;
import com.hardrockunion.business.warehouse.mapper.WarehouseInventoryRecordMapper;
import com.hardrockunion.framework.security.model.LoginUser;

/**
 * 仓储库存流水服务。
 *
 * <p>库存变化记录收口在 `warehouse`，上层业务只负责触发业务动作。
 */
@Service
public class WarehouseInventoryRecordService {

    private final WarehouseInventoryRecordMapper inventoryRecordMapper;

    public WarehouseInventoryRecordService(WarehouseInventoryRecordMapper inventoryRecordMapper) {
        this.inventoryRecordMapper = inventoryRecordMapper;
    }

    public List<WarehouseInventoryRecordResponse> listByProduct(Long tenantId, Long productId, LoginUser loginUser) {
        return inventoryRecordMapper.selectList(new LambdaQueryWrapper<WarehouseInventoryRecord>()
                .eq(WarehouseInventoryRecord::getTenantId, tenantId)
                .eq(WarehouseInventoryRecord::getProductId, productId)
                .eq(WarehouseInventoryRecord::getDeleted, 0)
                .orderByDesc(WarehouseInventoryRecord::getId))
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public void record(Long tenantId,
                       Long warehouseId,
                       String warehouseName,
                       Long productId,
                       String productName,
                       String skuCode,
                       String changeType,
                       Integer changeQuantity,
                       Integer beforeQuantity,
                       Integer afterQuantity,
                       String sourceType,
                       Long sourceId,
                       String sourceNo,
                       String remark,
                       Long operatorId) {
        WarehouseInventoryRecord record = new WarehouseInventoryRecord();
        record.setTenantId(tenantId);
        record.setWarehouseId(warehouseId);
        record.setWarehouseName(warehouseName);
        record.setProductId(productId);
        record.setProductName(productName);
        record.setSkuCode(skuCode);
        record.setChangeType(changeType);
        record.setChangeQuantity(changeQuantity);
        record.setBeforeQuantity(beforeQuantity);
        record.setAfterQuantity(afterQuantity);
        record.setSourceType(sourceType);
        record.setSourceId(sourceId);
        record.setSourceNo(sourceNo);
        record.setRemark(remark);
        record.setCreatedBy(operatorId);
        record.setDeleted(0);
        inventoryRecordMapper.insert(record);
    }

    private WarehouseInventoryRecordResponse toResponse(WarehouseInventoryRecord record) {
        WarehouseInventoryRecordResponse response = new WarehouseInventoryRecordResponse();
        response.setId(record.getId());
        response.setWarehouseId(record.getWarehouseId());
        response.setWarehouseName(record.getWarehouseName());
        response.setProductId(record.getProductId());
        response.setProductName(record.getProductName());
        response.setSkuCode(record.getSkuCode());
        response.setChangeType(record.getChangeType());
        response.setChangeQuantity(record.getChangeQuantity());
        response.setBeforeQuantity(record.getBeforeQuantity());
        response.setAfterQuantity(record.getAfterQuantity());
        response.setSourceType(record.getSourceType());
        response.setSourceId(record.getSourceId());
        response.setSourceNo(record.getSourceNo());
        response.setRemark(record.getRemark());
        response.setCreatedAt(record.getCreatedAt());
        return response;
    }
}
