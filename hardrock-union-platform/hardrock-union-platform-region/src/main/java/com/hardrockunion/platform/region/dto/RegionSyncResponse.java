package com.hardrockunion.platform.region.dto;

public class RegionSyncResponse {

    private int savedCount;

    public RegionSyncResponse() {
    }

    public RegionSyncResponse(int savedCount) {
        this.savedCount = savedCount;
    }

    public int getSavedCount() {
        return savedCount;
    }

    public void setSavedCount(int savedCount) {
        this.savedCount = savedCount;
    }
}
