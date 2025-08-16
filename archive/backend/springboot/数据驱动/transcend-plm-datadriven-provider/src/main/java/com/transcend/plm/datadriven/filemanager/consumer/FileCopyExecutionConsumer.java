package com.transcend.plm.datadriven.filemanager.consumer;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.rabbitmq.client.Channel;
import com.transcend.plm.datadriven.filemanager.mapstruct.FileCopyExecutionRecordConverter;
import com.transcend.plm.datadriven.filemanager.pojo.bo.FileCopyExecutionBo;
import com.transcend.plm.datadriven.filemanager.pojo.po.FileCopyExecutionRecordPo;
import com.transcend.plm.datadriven.filemanager.service.FileCopyExecutionRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author bin.yin
 * @date 2024/04/30 13:46
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "spring.rabbitmq.first.enabled", matchIfMissing = true, havingValue = "true")
public class FileCopyExecutionConsumer {
    @Resource
    private FileCopyExecutionRecordService poService;

    /**
     * @param fileCopyExecutionBo
     * @param channel
     * @param deliveryTag
     */
    @RabbitListener(queues = {"/file/copy/execution"}, containerFactory = "firstFactory")
    public void userDemandListener(FileCopyExecutionBo fileCopyExecutionBo, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("file copy execution consumer start, deliveryTag = {}, bo = {}", deliveryTag, JSON.toJSONString(fileCopyExecutionBo));
        String syncType = fileCopyExecutionBo.getSyncType();
        FileCopyExecutionRecordPo po = FileCopyExecutionRecordConverter.INSTANCE.bo2Po(fileCopyExecutionBo);
        if (StringUtils.isBlank(syncType)) {
            return;
        }
        switch (syncType) {
            case "add":
                poService.save(po);
                break;
            case "edit":
                po.setBid(null);
                poService.update(po, Wrappers.<FileCopyExecutionRecordPo>lambdaUpdate().eq(FileCopyExecutionRecordPo::getBid, fileCopyExecutionBo.getBid()));
                break;
            default:
                log.error("file copy execution consumer syncType error");
                break;
        }
    }
}
