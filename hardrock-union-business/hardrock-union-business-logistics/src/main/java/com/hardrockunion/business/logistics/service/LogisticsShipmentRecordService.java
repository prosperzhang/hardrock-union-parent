package com.hardrockunion.business.logistics.service;

import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hardrockunion.business.logistics.domain.entity.LogisticsShipmentRecord;
import com.hardrockunion.business.logistics.dto.LogisticsShipmentRecordResponse;
import com.hardrockunion.business.logistics.mapper.LogisticsShipmentRecordMapper;
import com.hardrockunion.platform.iam.domain.entity.AppRegistry;
import com.hardrockunion.platform.iam.service.AppRegistryQueryService;

/**
 * 供应链发货记录服务。
 *
 * <p>物流发货记录收口在 `supply`，业务模块只负责决定“什么时候发货”。
 */
@Service
public class LogisticsShipmentRecordService {

    private final LogisticsShipmentRecordMapper shipmentRecordMapper;
    private final AppRegistryQueryService appRegistryQueryService;

    public LogisticsShipmentRecordService(LogisticsShipmentRecordMapper shipmentRecordMapper,
                                       AppRegistryQueryService appRegistryQueryService) {
        this.shipmentRecordMapper = shipmentRecordMapper;
        this.appRegistryQueryService = appRegistryQueryService;
    }

    public LogisticsShipmentRecordResponse saveShipment(String businessAppCode,
                                                     Long tenantId,
                                                     String sourceType,
                                                     Long sourceId,
                                                     String sourceNo,
                                                     String logisticsCompany,
                                                     String trackingNo,
                                                     LocalDateTime shippedAt,
                                                     String shippingRemark,
                                                     Long operatorId) {
        LogisticsShipmentRecord existed = shipmentRecordMapper.selectOne(new LambdaQueryWrapper<LogisticsShipmentRecord>()
            .eq(LogisticsShipmentRecord::getTenantId, tenantId)
            .eq(LogisticsShipmentRecord::getSourceType, sourceType)
            .eq(LogisticsShipmentRecord::getSourceId, sourceId)
            .eq(LogisticsShipmentRecord::getDeleted, 0)
            .last("limit 1"));

        LogisticsShipmentRecord record = existed == null ? new LogisticsShipmentRecord() : existed;
        record.setTenantId(tenantId);
        record.setBusinessAppId(resolveAppId(businessAppCode));
        record.setBusinessAppCode(businessAppCode);
        record.setSourceType(sourceType);
        record.setSourceId(sourceId);
        record.setSourceNo(sourceNo);
        record.setLogisticsCompany(logisticsCompany);
        record.setTrackingNo(trackingNo);
        record.setShippedAt(shippedAt);
        record.setShippingRemark(shippingRemark);
        record.setShipmentStatus("SHIPPED");
        record.setInvalidatedAt(null);
        record.setInvalidatedRemark(null);
        record.setCreatedBy(operatorId);
        record.setDeleted(0);
        if (record.getId() == null) {
            shipmentRecordMapper.insert(record);
        } else {
            shipmentRecordMapper.updateById(record);
        }
        return toResponse(record);
    }

    /**
     * 订单取消后，把已有发货记录标记为作废，避免供应链侧还把它当成有效发货单。
     */
    public LogisticsShipmentRecordResponse invalidateBySource(Long tenantId,
                                                           String sourceType,
                                                           Long sourceId,
                                                           String reason,
                                                           Long operatorId) {
        LogisticsShipmentRecord record = shipmentRecordMapper.selectOne(new LambdaQueryWrapper<LogisticsShipmentRecord>()
            .eq(LogisticsShipmentRecord::getTenantId, tenantId)
            .eq(LogisticsShipmentRecord::getSourceType, sourceType)
            .eq(LogisticsShipmentRecord::getSourceId, sourceId)
            .eq(LogisticsShipmentRecord::getDeleted, 0)
            .last("limit 1"));
        if (record == null) {
            return null;
        }
        record.setShipmentStatus("INVALIDATED");
        record.setInvalidatedAt(LocalDateTime.now());
        record.setInvalidatedRemark(StringUtils.defaultIfBlank(StringUtils.trimToNull(reason), "订单取消后作废发货记录"));
        shipmentRecordMapper.updateById(record);
        return toResponse(record);
    }

    public LogisticsShipmentRecordResponse getBySource(Long tenantId, String sourceType, Long sourceId) {
        LogisticsShipmentRecord record = shipmentRecordMapper.selectOne(new LambdaQueryWrapper<LogisticsShipmentRecord>()
            .eq(LogisticsShipmentRecord::getTenantId, tenantId)
            .eq(LogisticsShipmentRecord::getSourceType, sourceType)
            .eq(LogisticsShipmentRecord::getSourceId, sourceId)
            .eq(LogisticsShipmentRecord::getDeleted, 0)
            .last("limit 1"));
        return record == null ? null : toResponse(record);
    }

    private LogisticsShipmentRecordResponse toResponse(LogisticsShipmentRecord record) {
        LogisticsShipmentRecordResponse response = new LogisticsShipmentRecordResponse();
        response.setId(record.getId());
        response.setBusinessAppId(record.getBusinessAppId());
        response.setBusinessAppCode(record.getBusinessAppCode());
        response.setSourceType(record.getSourceType());
        response.setSourceId(record.getSourceId());
        response.setSourceNo(record.getSourceNo());
        response.setLogisticsCompany(record.getLogisticsCompany());
        response.setTrackingNo(record.getTrackingNo());
        response.setShippedAt(record.getShippedAt());
        response.setShippingRemark(record.getShippingRemark());
        response.setShipmentStatus(record.getShipmentStatus());
        response.setInvalidatedAt(record.getInvalidatedAt());
        response.setInvalidatedRemark(record.getInvalidatedRemark());
        return response;
    }

    private Long resolveAppId(String appCode) {
        AppRegistry app = appRegistryQueryService.getEnabledAppByCode(appCode);
        return app.getId();
    }
}
