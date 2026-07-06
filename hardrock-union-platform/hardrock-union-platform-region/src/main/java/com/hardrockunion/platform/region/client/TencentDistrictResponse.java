package com.hardrockunion.platform.region.client;

import java.math.BigDecimal;
import java.util.List;

public class TencentDistrictResponse {

    private Integer status;

    private String message;

    private List<List<DistrictItem>> result;

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

    public List<List<DistrictItem>> getResult() {
        return result;
    }

    public void setResult(List<List<DistrictItem>> result) {
        this.result = result;
    }

    public static class DistrictItem {

        private String id;

        private String name;

        private String fullname;

        private Location location;

        private List<Integer> cidx;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFullname() {
            return fullname;
        }

        public void setFullname(String fullname) {
            this.fullname = fullname;
        }

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        public List<Integer> getCidx() {
            return cidx;
        }

        public void setCidx(List<Integer> cidx) {
            this.cidx = cidx;
        }
    }

    public static class Location {

        private BigDecimal lat;

        private BigDecimal lng;

        public BigDecimal getLat() {
            return lat;
        }

        public void setLat(BigDecimal lat) {
            this.lat = lat;
        }

        public BigDecimal getLng() {
            return lng;
        }

        public void setLng(BigDecimal lng) {
            this.lng = lng;
        }
    }
}
