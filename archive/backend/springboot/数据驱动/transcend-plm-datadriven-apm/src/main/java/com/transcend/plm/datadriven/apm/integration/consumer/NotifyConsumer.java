package com.transcend.plm.datadriven.apm.integration.consumer;

import com.alibaba.fastjson.JSON;
import com.transcend.plm.datadriven.apm.aspect.notify.OperateBusiService;
import com.transcend.plm.datadriven.apm.integration.message.QueueNameConstant;
import com.transcend.plm.datadriven.apm.powerjob.notify.analysis.service.INotifyMessageService;
import com.transcend.plm.datadriven.apm.powerjob.notify.analysis.service.NotifyAnalysis;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmNotifyExecuteRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 实例操作消费者
 * @createTime 2023-12-18 15:10:00
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "spring.rabbitmq.notify.enabled", matchIfMissing = true, havingValue = "true")
public class NotifyConsumer {

    @Resource
    private INotifyMessageService notifyMessageService;

    @Resource
    private NotifyAnalysis notifyAnalysis;

    @RabbitListener(queues = QueueNameConstant.QUEUE_DELAY)
    public void process(ApmNotifyExecuteRecord apmNotifyExecuteRecord) {
        if (!validateMessage(apmNotifyExecuteRecord)) {
            return;
        }
        try {
            log.info("接收到消息:{}", JSON.toJSONString(apmNotifyExecuteRecord));
            if(notifyAnalysis.sendBeforeCheck(apmNotifyExecuteRecord)){
                notifyMessageService.pushMsg(apmNotifyExecuteRecord);
            }else {
                log.info("消息发送前检查失败，消息不发送,消息内容:{}", JSON.toJSONString(apmNotifyExecuteRecord));
            }
        }catch (Exception e){
            log.error("消息处理失败",e);
        }

    }


    /**
     * 进行消息数据检查，业务类型和操作不能为空
     */
    private boolean validateMessage(ApmNotifyExecuteRecord apmNotifyExecuteRecord) {
        if (apmNotifyExecuteRecord == null) {
            log.error("InstanceOperateConsumer消费者收到消息为空");
            return false;
        }

        return true;
    }

}
