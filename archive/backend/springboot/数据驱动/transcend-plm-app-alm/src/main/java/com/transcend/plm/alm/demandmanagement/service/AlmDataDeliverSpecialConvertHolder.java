package com.transcend.plm.alm.demandmanagement.service;

import com.transcend.plm.datadriven.common.strategy.MultipleStrategyContext;
import com.transcend.plm.datadriven.common.wapper.TranscendObjectWrapper;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 数据投递特殊转换器持有者
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/11 09:04
 */
@Log4j2
@Service
public class AlmDataDeliverSpecialConvertHolder
        extends MultipleStrategyContext<TranscendObjectWrapper, AlmDataDeliverSpecialConverter> {

    public AlmDataDeliverSpecialConvertHolder(@NonNull List<AlmDataDeliverSpecialConverter> strategyList) {
        super(strategyList);
    }

    /**
     * 转换器
     *
     * @param data 转换前数据
     */
    public void convert(TranscendObjectWrapper data) {
        Optional.of(data).map(this::supportServices)
                .ifPresent(converters -> converters.forEach(converter -> {
                    try {
                        converter.convert(data);
                    } catch (Exception e) {
                        log.error("数据转换器执行失败", e);
                    }
                }));
    }

}
