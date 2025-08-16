package com.transcend.plm.datadriven.apm.powerjob.notify;

import com.alibaba.fastjson.JSON;
import com.transcend.plm.datadriven.apm.integration.message.QueueNameConstant;
import com.transcend.plm.datadriven.apm.integration.publisher.IPublishService;
import com.transcend.plm.datadriven.apm.powerjob.notify.analysis.service.INotifyMessageService;
import com.transcend.plm.datadriven.apm.powerjob.notify.analysis.service.NotifyAnalysis;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmNotifyExecuteRecord;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmNotifyExecuteRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.TaskResult;
import tech.powerjob.worker.core.processor.sdk.MapReduceProcessor;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 发送通知执行器
 *
 * @author quan.cheng
 * @title NotifyExecuteRecordConfig
 * @date 2024/1/24 14:42
 * @description 发送通知执行器
 */
@Component
@Slf4j
public class NotifyExecuteRecordConfig implements MapReduceProcessor {

    @Resource
    private ApmNotifyExecuteRecordService apmNotifyExecuteRecordService;

    /**
     * 时间轮定时器
     */
    @Resource
    private IPublishService rabbitPublishService;

    @Resource
    private INotifyMessageService notifyMessageService;

    @Resource
    private NotifyAnalysis notifyAnalysis;

    /**
     * 定时执行将需要发送的通知添加到时间轮中执行
     * 1.获取需要发送的通知
     * 2.将需要发送的通知进行分组
     * 3.将分组后的通知分发到子任务中执行
     *
     * @param context 任务上下文，可通过 jobParams 和 instanceParams 分别获取控制台参数和OpenAPI传递的任务实例参数
     * @return 任务执行结果，可通过 TaskResult.success/failure/running 方法构造
     * @throws Exception 任务执行异常
     */
    @Override
    public ProcessResult process(TaskContext context) throws Exception {
        log.info("并行处理器====process=====NotifyExecuteRecordConfig process");
        // 判断是否为根任务
        if (isRootTask()) {
            // 定时任务开始时间
            long startTime = System.currentTimeMillis();
            List<ApmNotifyExecuteRecord> apmNotifyExecuteRecords = apmNotifyExecuteRecordService.selectAllExecuteRecord();
            if (apmNotifyExecuteRecords == null || apmNotifyExecuteRecords.isEmpty()) {
                log.info("没有需要发送的通知");
            } else {
                log.info("需要发送的通知数量为：{}", apmNotifyExecuteRecords.size());
                for (ApmNotifyExecuteRecord apmNotifyExecuteRecord : apmNotifyExecuteRecords) {
                    //讲状态改为待执行
                    apmNotifyExecuteRecord.setNofifyResult(3);
                    Boolean notifyNow = apmNotifyExecuteRecord.getNofifyNow();
                    if (notifyNow) {
                        //需要立即发送消息的任务
                        apmNotifyExecuteRecord.setNofifyNow(true);
                        continue;
                    }
                    // 非立即发送的任务，根据发送时间减去时间轮的起始时间，
                    long notifyTime = apmNotifyExecuteRecord.getNofifyTime().getTime();
                    long delay = notifyTime - startTime;
                    if (delay < 0) {
                        // 如果延迟时间小于0，说明已经过了发送时间，需要立即发送
                        apmNotifyExecuteRecord.setNofifyNow(true);
                        continue;
                    }
                    apmNotifyExecuteRecord.setNofifyTime(apmNotifyExecuteRecord.getNofifyTime());
                }
                apmNotifyExecuteRecordService.updateBatchById(apmNotifyExecuteRecords);
            }

            // 根据数据的空间ID进行分组
            if (apmNotifyExecuteRecords == null || apmNotifyExecuteRecords.isEmpty()) {
                log.info("没有需要发送的通知");
                return new ProcessResult(true, "没有需要发送的通知");
            }
            for(ApmNotifyExecuteRecord apmNotifyExecuteRecord : apmNotifyExecuteRecords){
                if(apmNotifyExecuteRecord.getNofifyNow() || apmNotifyExecuteRecord.getNofifyTime().before(new Date())){
                    if(notifyAnalysis.sendBeforeCheck(apmNotifyExecuteRecord)){
                        notifyMessageService.pushMsg(apmNotifyExecuteRecord);
                    }else {
                        log.info("消息发送前检查失败，消息不发送,消息内容:{}", JSON.toJSONString(apmNotifyExecuteRecord));
                    }
                }else{
                    //放入延迟消除队列
                    rabbitPublishService.publishWithDelay(QueueNameConstant.EXCHANGE_INSTANCE_DELAY,QueueNameConstant.ROUTING_KEY_DELAY,apmNotifyExecuteRecord,apmNotifyExecuteRecord.getNofifyTime().getTime()-System.currentTimeMillis());
                }
            }

            // 执行子任务，注：子任务人可以 map 产生新的子任务，可以构建任意级的 MapReduce 处理器
            return new ProcessResult(true, "result is xxx");
        }

        return new ProcessResult(true, "result is xxx");
    }

    @Override
    public ProcessResult reduce(TaskContext taskContext, List<TaskResult> taskResults) {
        log.info("并行处理器====reduce=====NotifyExecuteRecordConfig reduce");

        // 所有 Task 执行结束后，reduce 将会被执行
        // taskResults 保存了所有子任务的执行结果
        // 用法举例，统计执行结果
        AtomicLong successCnt = new AtomicLong(0);
        taskResults.forEach(tr -> {
            if (tr.isSuccess()) {
                successCnt.incrementAndGet();
            }
        });
        // 该结果将作为任务最终的执行结果
        return new ProcessResult(true, "success task num:" + successCnt.get());
    }

    // 自定义的子任务

    static class SubTask {
        private String spaceId;
        //根据空间ID 进行分片的子任务数据
        private String apmNotifyExecuteRecords;

        public String getSpaceId() {
            return spaceId;
        }

        public void setSpaceId(String spaceId) {
            this.spaceId = spaceId;
        }

        public String getApmNotifyExecuteRecords() {
            return apmNotifyExecuteRecords;
        }

        public void setApmNotifyExecuteRecords(String apmNotifyExecuteRecords) {
            this.apmNotifyExecuteRecords = apmNotifyExecuteRecords;
        }
    }

}
