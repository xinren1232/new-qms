//package com.transcend.plm.datadriven.common.dao;
//
//import com.baomidou.mybatisplus.core.mapper.BaseMapper;
//import com.baomidou.mybatisplus.core.toolkit.Wrappers;
//import com.baomidou.mybatisplus.extension.service.IService;
//import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import org.springframework.util.Assert;
//
///**
// * TODO 描述
// *
// * @author jie.luo <jie.luo1@transsion.com>
// * @version V1.0.0
// * @date 2023/10/30 11:37
// * @since 1.0
// */
//public class IPlmDaoService<M extends BaseMapper<T>, T> extends ServiceImpl {
//    /**
//     * 根据bid进行更新
//     *
//     * @param po
//     * @return
//     */
//    @Override
//    public T updateByBid(T po) {
//        this.update(po, Wrappers.<T>lambdaUpdate()
//                .eq(CfgObjectViewRulePo::getBid, po.getBid())
//        );
//        return po;
//    }
//}
