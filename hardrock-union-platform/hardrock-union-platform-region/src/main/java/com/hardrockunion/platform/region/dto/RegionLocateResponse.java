package com.hardrockunion.platform.region.dto;

public class RegionLocateResponse {

    private String address;

    private RegionResponse province;

    private RegionResponse city;

    private RegionResponse district;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public RegionResponse getProvince() {
        return province;
    }

    public void setProvince(RegionResponse province) {
        this.province = province;
    }

    public RegionResponse getCity() {
        return city;
    }

    public void setCity(RegionResponse city) {
        this.city = city;
    }

    public RegionResponse getDistrict() {
        return district;
    }

    public void setDistrict(RegionResponse district) {
        this.district = district;
    }
}
