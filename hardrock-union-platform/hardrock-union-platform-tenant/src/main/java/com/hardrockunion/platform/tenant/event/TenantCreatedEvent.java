package com.hardrockunion.platform.tenant.event;

public record TenantCreatedEvent(
    String appCode,
    String tenantType,
    Long tenantId,
    String tenantName,
    String tenantSource,
    String projectAddress,
    String provinceCode,
    String provinceName,
    String cityCode,
    String cityName,
    String districtCode,
    String districtName,
    String managerName,
    String managerPhone,
    String createdByAppCode,
    Long createdBy
) {
}
