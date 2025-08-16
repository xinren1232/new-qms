package com.transcend.plm.alm.demandmanagement.event.handler;

import cn.hutool.core.collection.CollUtil;
import com.transcend.plm.alm.demandmanagement.service.RequirementsStatusAutoUpdateService;
import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.common.pool.SimpleThreadPool;
import com.transcend.plm.datadriven.common.wapper.TranscendObjectWrapper;
import com.transcend.plm.datadriven.infrastructure.basedata.event.BaseDataUpdateEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 需求状态自动更新处理器
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/2/27 11:59
 */
@Slf4j
@Component
@AllArgsConstructor
public class RequirementsStatusAutoUpdateHandler {

    private final RequirementsStatusAutoUpdateService autoUpdateService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(BaseDataUpdateEvent event) {
        MBaseData data = event.getData();
        if (data == null) {
            return;
        }
        TranscendObjectWrapper objectWrapper = new TranscendObjectWrapper(data);
        //补充模型编码
        objectWrapper.setModelCode(event.getModelCode());

        List<RequirementsStatusAutoUpdateService.Config> configList = autoUpdateService.getParentUpdateConfig(objectWrapper);
        List<RequirementsStatusAutoUpdateService.Config> selfConfigList = autoUpdateService.getSelfConfig(objectWrapper);

        if (CollUtil.isEmpty(configList) && CollUtil.isEmpty(selfConfigList)) {
            return;
        }

        //异步更新
        CompletableFuture.runAsync(() -> {
            try {
                //上层更新
                autoUpdateService.autoUpdateParent(configList, event.getWrappers());

                //自身更新
                autoUpdateService.autoUpdateSelf(selfConfigList, event.getWrappers());
            } catch (Exception e) {
                log.error("autoUpdate error,event:{}", event, e);
            }
        }, SimpleThreadPool.getInstance());

    }

    /**
     * 前置处理方法
     *
     * @param event 事件
     */
    @EventListener
    public void preHandle(BaseDataUpdateEvent event) {
        if (autoUpdateService.notFocusModelCode(event.getModelCode())) {
            return;
        }
        if (event.getData() == null) {
            return;
        }
        TranscendObjectWrapper objectWrapper = new TranscendObjectWrapper(event.getData());
        autoUpdateService.writePreModifyStatus(event.getModelCode(), objectWrapper, event.getWrappers());
    }


}
