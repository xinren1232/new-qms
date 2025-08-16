package com.transcend.plm.alm.demandmanagement.event.handler;

import com.transcend.plm.alm.demandmanagement.service.AlmDataDeliverQueueService;
import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.common.pool.SimpleThreadPool;
import com.transcend.plm.datadriven.infrastructure.basedata.event.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 数据投递事件处理器
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/7 17:32
 */
@Component
@AllArgsConstructor
public class AlmDataDeliverQueueEventHandler {

    private final AlmDataDeliverQueueService almDataDeliverQueueService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(BaseDataAddEvent event) {
        //异步更新
        CompletableFuture.runAsync(() -> almDataDeliverQueueService.deliver(event.getData())
                , SimpleThreadPool.getInstance());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(BaseDataBatchAddEvent event) {
        CompletableFuture.runAsync(() -> {
            List<MBaseData> dataList = event.getDataList().stream()
                    .map(data -> (MBaseData) data).collect(Collectors.toList());
            almDataDeliverQueueService.deliver(dataList);
        }, SimpleThreadPool.getInstance());

    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(BaseDataBatchUpdateEvent event) {
        CompletableFuture.runAsync(() -> event.getUpdateList().forEach(update ->
                        almDataDeliverQueueService.deliverByCondition(event.getModelCode(), update.getWrappers())),
                SimpleThreadPool.getInstance());

    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(BaseDataUpdateEvent event) {
        CompletableFuture.runAsync(() ->
                        almDataDeliverQueueService.deliverByCondition(event.getModelCode(), event.getWrappers()),
                SimpleThreadPool.getInstance());

    }


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(BaseDataDeleteEvent event) {
        CompletableFuture.runAsync(() ->
                        almDataDeliverQueueService.deliverByCondition(event.getModelCode(), event.getWrappers()),
                SimpleThreadPool.getInstance());
    }
}
