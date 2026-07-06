package com.hardrockunion.business.warehouse.dto;

public class WarehouseCreateRequest {

    private String warehouseCode;
    private String warehouseName;
    private String warehouseType;
    private Integer defaultFlag;
    private String ownerType;
    private String ownerAppCode;
    private Long ownerTenantId;
    private String contactName;
    private String contactPhone;
    private String warehouseAddress;
    private String provinceCode;
    private String provinceName;
    private String cityCode;
    private String cityName;
    private String districtCode;
    private String districtName;

    public String getWarehouseCode() { return warehouseCode; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public String getWarehouseType() { return warehouseType; }
    public void setWarehouseType(String warehouseType) { this.warehouseType = warehouseType; }
    public Integer getDefaultFlag() { return defaultFlag; }
    public void setDefaultFlag(Integer defaultFlag) { this.defaultFlag = defaultFlag; }
    public String getOwnerType() { return ownerType; }
    public void setOwnerType(String ownerType) { this.ownerType = ownerType; }
    public String getOwnerAppCode() { return ownerAppCode; }
    public void setOwnerAppCode(String ownerAppCode) { this.ownerAppCode = ownerAppCode; }
    public Long getOwnerTenantId() { return ownerTenantId; }
    public void setOwnerTenantId(Long ownerTenantId) { this.ownerTenantId = ownerTenantId; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getWarehouseAddress() { return warehouseAddress; }
    public void setWarehouseAddress(String warehouseAddress) { this.warehouseAddress = warehouseAddress; }
    public String getProvinceCode() { return provinceCode; }
    public void setProvinceCode(String provinceCode) { this.provinceCode = provinceCode; }
    public String getProvinceName() { return provinceName; }
    public void setProvinceName(String provinceName) { this.provinceName = provinceName; }
    public String getCityCode() { return cityCode; }
    public void setCityCode(String cityCode) { this.cityCode = cityCode; }
    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
    public String getDistrictCode() { return districtCode; }
    public void setDistrictCode(String districtCode) { this.districtCode = districtCode; }
    public String getDistrictName() { return districtName; }
    public void setDistrictName(String districtName) { this.districtName = districtName; }
}
