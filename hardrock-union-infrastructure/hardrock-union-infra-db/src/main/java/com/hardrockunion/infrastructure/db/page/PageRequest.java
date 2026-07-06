package com.hardrockunion.infrastructure.db.page;

public class PageRequest {

    private long pageNum = 1;
    private long pageSize = 10;

    public long getPageNum() {
        return pageNum;
    }

    public void setPageNum(long pageNum) {
        this.pageNum = Math.max(pageNum, 1);
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = Math.max(pageSize, 1);
    }
}
