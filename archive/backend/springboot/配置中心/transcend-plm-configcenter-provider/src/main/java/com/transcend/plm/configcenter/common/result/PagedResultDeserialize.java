//package com.transcend.plm.configcenter.common.result;
//
//import com.alibaba.fastjson.JSON;
//
//import java.io.Serializable;
//import java.util.List;
//
///**
// * PagedResultDeserialize
// *
// * @author shuangzhi.zeng
// * @version: 1.0
// * @date 2021/04/09 18:39
// */
//public class PagedResultDeserialize<T> implements PagedResult<T>, Serializable {
//    private long current;
//    private long total;
//    private long size;
//    private long pages;
//    private List<T> data;
//
//    @Override
//    public PagedResult<T> setCurrent(long current) {
//
//        this.current = current;
//        return this;
//    }
//    @Override
//    public PagedResult<T> setTotal(long total) {
//        this.total = total;
//        return this;
//    }
//    @Override
//    public PagedResult<T> setSize(long size) {
//        this.size = size;
//        return this;
//    }
//    @Override
//    public PagedResult<T> setData(List<T> data) {
//        this.data = data;
//        return this;
//    }
//
//    @Override
//    public PagedResult<T> setPages(long pages) {
//        this.pages = pages;
//        return this;
//    }
//
//    public PagedResult<T> calPages() {
//        if (getSize() == 0) {
//            this.pages = 0L;
//            return this;
//        }
//        long tmpPages = getTotal() / getSize();
//        if (getTotal() % getSize() != 0) {
//            tmpPages++;
//        }
//        this.pages = tmpPages;
//        return this;
//    }
//    @Override
//    public long getTotal() {
//        return total;
//    }
//    @Override
//    public long getPages() {
//        return pages;
//    }
//
//    @Override
//    public List<T> getData() {
//        return data;
//    }
//    @Override
//    public long getCurrent() {
//        return current;
//    }
//    @Override
//    public long getSize() {
//        return size;
//    }
//
//    @Override
//    public String toString() {
//        return JSON.toJSONString(this);
//    }
//
//}
