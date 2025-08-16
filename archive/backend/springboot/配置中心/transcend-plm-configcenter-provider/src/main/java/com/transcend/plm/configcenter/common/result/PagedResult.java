//package com.transcend.plm.configcenter.common.result;
//
//import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
//
//import java.io.Serializable;
//import java.util.List;
//
///**
// * PagedResult 分页查询结果对象接口
// * 方法中可以使用接口，接收不同实现，以支持多种实现方式
// * @author taixi.yu
// * @version: 1.0
// * @date 2020/12/08 19:13
// */
//@JsonDeserialize(as=PagedResultDeserialize.class)
//public interface PagedResult<T> extends Serializable {
//
//    public long getCurrent();
//
//    public PagedResult<T> setCurrent(long current);
//
//    public long getTotal();
//
//    public PagedResult<T> setTotal(long total);
//
//    public long getSize();
//
//    public PagedResult<T> setSize(long size);
//
//    public long getPages();
//
//    public PagedResult<T> setPages(long sages);
//
//    public List<T> getData();
//
//    public PagedResult<T> setData(List<T> data);
//
//}
