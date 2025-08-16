package com.transcend.plm.datadriven.infrastructure.basedata.event;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 底层数据事务发布器
 * <p>
 * 为防止底层调用频率太高，调用事件发布器进行发布需先指定modelCode
 * 例如：ApplicationEventPublisher.registerModelCodes("A01","A02")
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/2/27 17:08
 */
@Service
@AllArgsConstructor
public class BaseDataEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;


    private Set<String> allowModelCodeList;

    @Value("#{'${transcend.plm.base.event.allow-model-code-list:}'.split(',')}")
    public void setAllowModelCodeList(List<String> modelCodeList) {
        if (modelCodeList == null || modelCodeList.isEmpty()) {
            this.allowModelCodeList = Collections.emptySet();
            return;
        }
        this.allowModelCodeList = new HashSet<>(modelCodeList);
    }

    /**
     * 发布事件
     *
     * @param event 事件
     */
    public void publishEvent(AbstractBaseDataEvent event) {
        if (event == null || !allowModelCodeList.contains(event.getModelCode())) {
            return;
        }
        applicationEventPublisher.publishEvent(event);
    }


}
