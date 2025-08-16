package com.transcend.plm.alm.demandmanagement.service;

import com.transcend.plm.datadriven.common.strategy.MultipleStrategy;
import com.transcend.plm.datadriven.common.wapper.TranscendObjectWrapper;

/**
 * 数据投递特殊处理转换器
 * 策略范围建议为任意对象的某个字段
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/11 08:47
 */
public interface AlmDataDeliverSpecialConverter extends MultipleStrategy<TranscendObjectWrapper> {


    /**
     * 转换操作
     * 注意此次是引用内部修改，不改变源对象引用
     *
     * @param data 数据包装处理器
     */
    void convert(TranscendObjectWrapper data);
}


