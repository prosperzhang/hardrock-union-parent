package com.hardrockunion.platform.region.client;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TencentGeocoderResponse {

    private Integer status;

    private String message;

    private GeocoderResult result;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public GeocoderResult getResult() {
        return result;
    }

    public void setResult(GeocoderResult result) {
        this.result = result;
    }

    public static class GeocoderResult {

        private String address;

        @JsonProperty("standard_address")
        private String standardAddress;

        @JsonProperty("formatted_addresses")
        private FormattedAddresses formattedAddresses;

        @JsonProperty("address_component")
        private AddressComponent addressComponent;

        @JsonProperty("address_reference")
        private AddressReference addressReference;

        private List<Poi> pois;

        @JsonProperty("ad_info")
        private AddressInfo adInfo;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getStandardAddress() {
            return standardAddress;
        }

        public void setStandardAddress(String standardAddress) {
            this.standardAddress = standardAddress;
        }

        public FormattedAddresses getFormattedAddresses() {
            return formattedAddresses;
        }

        public void setFormattedAddresses(FormattedAddresses formattedAddresses) {
            this.formattedAddresses = formattedAddresses;
        }

        public AddressComponent getAddressComponent() {
            return addressComponent;
        }

        public void setAddressComponent(AddressComponent addressComponent) {
            this.addressComponent = addressComponent;
        }

        public AddressReference getAddressReference() {
            return addressReference;
        }

        public void setAddressReference(AddressReference addressReference) {
            this.addressReference = addressReference;
        }

        public List<Poi> getPois() {
            return pois;
        }

        public void setPois(List<Poi> pois) {
            this.pois = pois;
        }

        public AddressInfo getAdInfo() {
            return adInfo;
        }

        public void setAdInfo(AddressInfo adInfo) {
            this.adInfo = adInfo;
        }
    }

    public static class AddressComponent {

        private String street;

        @JsonProperty("street_number")
        private String streetNumber;

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getStreetNumber() {
            return streetNumber;
        }

        public void setStreetNumber(String streetNumber) {
            this.streetNumber = streetNumber;
        }
    }

    public static class AddressReference {

        private ReferenceItem town;

        private ReferenceItem street;

        @JsonProperty("street_number")
        private ReferenceItem streetNumber;

        @JsonProperty("landmark_l1")
        private ReferenceItem landmarkL1;

        @JsonProperty("landmark_l2")
        private ReferenceItem landmarkL2;

        public ReferenceItem getTown() {
            return town;
        }

        public void setTown(ReferenceItem town) {
            this.town = town;
        }

        public ReferenceItem getStreet() {
            return street;
        }

        public void setStreet(ReferenceItem street) {
            this.street = street;
        }

        public ReferenceItem getStreetNumber() {
            return streetNumber;
        }

        public void setStreetNumber(ReferenceItem streetNumber) {
            this.streetNumber = streetNumber;
        }

        public ReferenceItem getLandmarkL1() {
            return landmarkL1;
        }

        public void setLandmarkL1(ReferenceItem landmarkL1) {
            this.landmarkL1 = landmarkL1;
        }

        public ReferenceItem getLandmarkL2() {
            return landmarkL2;
        }

        public void setLandmarkL2(ReferenceItem landmarkL2) {
            this.landmarkL2 = landmarkL2;
        }
    }

    public static class ReferenceItem {

        private String title;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    public static class FormattedAddresses {

        private String recommend;

        private String rough;

        @JsonProperty("standard_address")
        private String standardAddress;

        public String getRecommend() {
            return recommend;
        }

        public void setRecommend(String recommend) {
            this.recommend = recommend;
        }

        public String getRough() {
            return rough;
        }

        public void setRough(String rough) {
            this.rough = rough;
        }

        public String getStandardAddress() {
            return standardAddress;
        }

        public void setStandardAddress(String standardAddress) {
            this.standardAddress = standardAddress;
        }
    }

    public static class Poi {

        private String title;

        private String address;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

    public static class AddressInfo {

        private String adcode;

        private String province;

        private String city;

        private String district;

        public String getAdcode() {
            return adcode;
        }

        public void setAdcode(String adcode) {
            this.adcode = adcode;
        }

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getDistrict() {
            return district;
        }

        public void setDistrict(String district) {
            this.district = district;
        }
    }
}
