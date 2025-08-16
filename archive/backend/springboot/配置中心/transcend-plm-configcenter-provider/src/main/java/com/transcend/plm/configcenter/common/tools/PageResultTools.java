//package com.transcend.plm.configcenter.common.tools;
//
//import com.baomidou.mybatisplus.core.metadata.IPage;
//import com.github.pagehelper.Page;
//import com.github.pagehelper.PageInfo;
//import com.google.common.collect.Lists;
//import com.transcend.framework.core.model.api.page.PagedResult;
//import com.transcend.plm.configcenter.common.result.PagedResultDeserialize;
//
//import java.io.Serializable;
//import java.util.List;
//
///**
// * 分页返回结果封装
// *
// * @author jingfang.Luo
// * @version: 1.0
// * @date 2021/09/27 10:02
// */
//public class PageResultTools {
//    public static <T extends Serializable> PagedResult<T> create(Page<T> page, List<T> data) {
//        PagedResult<T> result = new PagedResultDeserialize<>();
//        result.setData(data);
//        result.setCurrent(page.getPageNum());
//        result.setTotal(page.getTotal());
//        result.setSize(page.getPageSize());
//        result.setPages(page.getPages());
//        return result;
//    }
//
//    public static <T> PagedResult<T> createEmpty() {
//        PagedResult<T> result = new PagedResultDeserialize<>();
//        result.setData(Lists.newArrayList());
//        result.setCurrent(1);
//        result.setTotal(0);
//        result.setSize(0);
//        result.setPages(0);
//        return result;
//    }
//
//    public static <T> PagedResult<T> create(PageInfo<T> pageInfo) {
//        PagedResult<T> pageResult = new PagedResultDeserialize<>();
//        pageResult.setCurrent(pageInfo.getPageNum())
//                .setPages(pageInfo.getPages())
//                .setTotal(pageInfo.getTotal())
//                .setSize(pageInfo.getSize())
//                .setData(pageInfo.getList());
//        return pageResult;
//    }
//
//    public static <T extends Serializable> PagedResult<T> create(IPage page,List<T> data) {
//        PagedResult<T> result = new PagedResultDeserialize<>();
//        result.setData(data);
//        result.setCurrent(page.getCurrent());
//        result.setTotal(page.getTotal());
//        result.setSize(page.getSize());
//        result.setPages(page.getPages());
//        return result;
//    }
//}
