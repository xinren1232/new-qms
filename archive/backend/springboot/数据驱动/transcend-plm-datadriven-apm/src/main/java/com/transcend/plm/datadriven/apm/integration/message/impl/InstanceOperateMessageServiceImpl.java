package com.transcend.plm.datadriven.apm.integration.message.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.transcend.plm.datadriven.apm.integration.message.IInstanceOperateMessageService;
import com.transcend.plm.datadriven.apm.integration.message.InstanceOperateMessage;
import com.transcend.plm.datadriven.apm.integration.message.QueueNameConstant;
import com.transcend.plm.datadriven.apm.integration.publisher.IPublishService;
import com.transcend.plm.datadriven.apm.space.repository.po.MessageRecord;
import com.transcend.plm.datadriven.apm.space.repository.service.MessageRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.List;
import static com.transcend.plm.datadriven.apm.enums.MessageLevelEnum.MIDDLE;
import static com.transcend.plm.datadriven.apm.enums.MessageStateEnum.FAILURE;
import static com.transcend.plm.datadriven.apm.enums.MessageStateEnum.RETRY;

/**
 * @Author yanjie
 * @Date 2024/1/12 10:18
 * @Version 1.0
 */

@Service
@Slf4j
public class InstanceOperateMessageServiceImpl implements IInstanceOperateMessageService {

    @Resource
    MessageRecordService messageRecordService;

    @Resource
    private IPublishService publishService;


        @Override
    public void failureMessageHandle() {
            log.info("failureMessageHandle：开始处理失败消息！");
            int count = 0;

            do {
                LambdaQueryWrapper<MessageRecord> queryWrapper = Wrappers.<MessageRecord>lambdaQuery()
                        .eq(MessageRecord::getState, FAILURE.getCode()).eq(MessageRecord::getErrLevel, MIDDLE.getCode())
                        .lt(MessageRecord::getRetryTimes,3).last("limit 2000");

                List<MessageRecord> failureMessage = messageRecordService.list(queryWrapper);
                count = failureMessage.size();
                failureMessage.forEach(message -> {
                    message.setState(RETRY.getCode());
                    message.setRetryTimes(message.getRetryTimes() + 1);
                });
                if (CollectionUtils.isEmpty(failureMessage)) {
                    return;
                }
                failureMessage.forEach(message -> {
                    InstanceOperateMessage sendMessage = JSON.parseObject(message.getContent(), InstanceOperateMessage.class);
                    sendMessage.setMessageRecordId(message.getId());
                    publishService.publish(QueueNameConstant.EXCHANGE_INSTANCE_OPERATE, QueueNameConstant.ROUTING_KEY_INSTANCE_OPERATE_SYNCHRONIZE_FAIL, sendMessage);
                });
                messageRecordService.updateBatchById(failureMessage);
            } while (count == 2000);

        }
}
