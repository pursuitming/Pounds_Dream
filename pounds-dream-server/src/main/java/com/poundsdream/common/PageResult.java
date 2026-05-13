package com.poundsdream.common;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {

    private List<T> list;
    private long total;
    private long pageNum;
    private long pageSize;

    public PageResult(List<T> list, long total, long pageNum, long pageSize) {
        this.list = list;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }
}
