package com.transcend.plm.alm.demandmanagement.event.handler;

import com.transcend.plm.alm.demandmanagement.service.RequirementsStatusAutoUpdateService;
import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.common.pool.SimpleThreadPool;
import com.transcend.plm.datadriven.common.wapper.TranscendRelationWrapper;
import com.transcend.plm.datadriven.infrastructure.basedata.event.BaseDataAddEvent;
import com.transcend.plm.datadriven.infrastructure.basedata.event.BaseDataBatchAddEvent;
import com.transcend.plm.datadriven.infrastructure.basedata.event.BaseDataDeleteEvent;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.CompletableFuture;

/**
 * 需求关联状态更新
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/6 10:31
 */
@Log4j2
@Component
@AllArgsConstructor
public class RequirementsRelationStatusAutoUpdateHandler {

    private final RequirementsStatusAutoUpdateService autoUpdateService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(BaseDataAddEvent event) {
        MBaseData data = event.getData();
        if (data == null || autoUpdateService.notFocusRelModelCode(event.getModelCode())) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                TranscendRelationWrapper wrapper = new TranscendRelationWrapper(data);
                wrapper.setModelCode(event.getModelCode());
                autoUpdateService.autoUpdateRelationSource(wrapper);
            } catch (Exception e) {
                log.error("addEvent auto update error,event:{}", event, e);
            }
        }, SimpleThreadPool.getInstance());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(BaseDataBatchAddEvent event) {
        if (autoUpdateService.notFocusRelModelCode(event.getModelCode())) {
            return;
        }
        CompletableFuture.runAsync(() -> event.getDataList().stream().map(TranscendRelationWrapper::new)
                        .forEach(wrapper -> {
                            try {
                                wrapper.setModelCode(event.getModelCode());
                                autoUpdateService.autoUpdateRelationSource(wrapper);
                            } catch (Exception e) {
                                log.error("batchAddEvent auto update error,wrapper:{}", wrapper, e);
                            }
                        })
                , SimpleThreadPool.getInstance());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(BaseDataDeleteEvent event) {
        if (autoUpdateService.notFocusRelModelCode(event.getModelCode())) {
            return;
        }

        CompletableFuture.runAsync(() ->
                        autoUpdateService.autoUpdateRelationSource(event.getModelCode(), event.getWrappers())
                , SimpleThreadPool.getInstance());
    }


}
