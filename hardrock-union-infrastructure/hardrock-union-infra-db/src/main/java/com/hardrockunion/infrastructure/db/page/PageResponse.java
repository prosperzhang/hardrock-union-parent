package com.hardrockunion.infrastructure.db.page;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;

public class PageResponse<T> {

    private long total;
    private long pageNum;
    private long pageSize;
    private List<T> records;

    public static <T> PageResponse<T> from(IPage<T> page) {
        PageResponse<T> response = new PageResponse<>();
        response.setTotal(page.getTotal());
        response.setPageNum(page.getCurrent());
        response.setPageSize(page.getSize());
        response.setRecords(page.getRecords());
        return response;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getPageNum() {
        return pageNum;
    }

    public void setPageNum(long pageNum) {
        this.pageNum = pageNum;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }
}
