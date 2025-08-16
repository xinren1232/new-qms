package com.transcend.plm.datadriven.apm.integration.consumer;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.transcend.plm.datadriven.api.model.BaseDataEnum;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.apm.constants.RoleConstant;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.MessageRecordAO;
import com.transcend.plm.datadriven.apm.integration.instance.IInstanceOperateService;
import com.transcend.plm.datadriven.apm.integration.instance.InstanceOperateStrategyFactory;
import com.transcend.plm.datadriven.apm.integration.message.InstanceOperateMessage;
import com.transcend.plm.datadriven.apm.integration.message.InstanceOperateTypeEnum;
import com.transcend.plm.datadriven.apm.integration.message.QueueNameConstant;
import com.transcend.plm.datadriven.apm.space.repository.po.MessageRecord;
import com.transcend.plm.datadriven.apm.space.repository.service.MessageRecordService;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transsion.framework.auth.IUser;
import com.transsion.framework.auth.IUserContext;
import com.transsion.framework.auth.UserContextDto;
import com.transsion.framework.auth.dto.UserLoginDto;
import com.transsion.framework.context.holder.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.transcend.plm.datadriven.apm.enums.MessageStateEnum.*;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 实例操作消费者
 * @createTime 2023-12-18 15:10:00
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "spring.rabbitmq.first.enabled", matchIfMissing = true, havingValue = "true")
public class InstanceOperateConsumer {

    @Resource
    MessageRecordService messageRecordService;

    @RabbitListener(queues = QueueNameConstant.INSTANCE_OPERATE_SYNCHRONIZE)
    @RabbitListener(queues = QueueNameConstant.INSTANCE_DEAD_SYNCHRONIZE_FAIL)
    public void process(InstanceOperateMessage message) {
        if (!validateMessage(message)) {
            return;
        }
        formatMessage(message);
        boolean consumptionSuccess = true;
        try {
            setUser(message.getOperator());
            IInstanceOperateService operateService = InstanceOperateStrategyFactory.getOrDefault(message.getBizType());
            if (InstanceOperateTypeEnum.ADD.getCode().equals(message.getOperateType())) {
                consumptionSuccess = operateService.add(message);
            } else if (InstanceOperateTypeEnum.UPDATE.getCode().equals(message.getOperateType())) {
                consumptionSuccess = operateService.update(message);
            } else if (InstanceOperateTypeEnum.DELETE.getCode().equals(message.getOperateType())) {
                consumptionSuccess = operateService.delete(message);
            } else {
                log.error("未知的操作类型, message:{}", message);
            }
        } finally {
            if (Objects.nonNull(message.getMessageRecordId())) {
                log.info("消息重新消费完成, messageRecordId:{}", message.getMessageRecordId());
                MessageRecord messageRecord = messageRecordService.getById(message.getMessageRecordId());
                if (consumptionSuccess) {
                    messageRecord.setState(SUCCESS.getCode());
                } else {
                    messageRecord.setErrLevel(message.getErrLevel());
                    messageRecord.setErrMsg(message.getErrMsg());
                    messageRecord.setContent(JSON.toJSONString(message));
                    messageRecord.setState(FAILURE.getCode());
                }
                messageRecordService.updateById(messageRecord);
            } else {
                if (!consumptionSuccess) {
                    saveMessage(message);
                }
            }
            UserContextHolder.removeUser();
        }
    }

    public void saveMessage(InstanceOperateMessage message) {
        MessageRecordAO ao = new MessageRecordAO();
        ao.setType(message.getBizType() + RoleConstant.SEMICOLON + message.getOperateType());
        ao.setState(FAILURE.getCode());
        ao.setErrLevel(message.getErrLevel());
        ao.setErrMsg(message.getErrMsg());
        ao.setContent(JSON.toJSONString(message));
        ao.setCreatedTime(new Date());
        ao.setUpdatedTime(new Date());
        ao.setCreatedBy(message.getOperator());
        ao.setUpdatedBy(message.getOperator());
        ao.setDeleteFlag(CommonConst.DELETE_FLAG_NOT_DELETED);
        messageRecordService.add(ao);
    }

    /**
     * 进行消息数据检查，业务类型和操作不能为空
     */
    private boolean validateMessage(InstanceOperateMessage message) {
        if (message == null) {
            log.error("InstanceOperateConsumer消费者收到消息为空");
            return false;
        }
        if (message.getBizType() == null) {
            log.error("InstanceOperateConsumer消费者收到消息业务类型为空, message:{}", message);
            return false;
        }
        if (message.getOperateType() == null) {
            log.error("InstanceOperateConsumer消费者收到消息操作类型为空, message:{}", message);
            return false;
        }
        if (!InstanceOperateTypeEnum.contains(message.getOperateType())) {
            log.error("InstanceOperateConsumer消费者收到消息操作类型不合法, message:{}", message);
        }
        return true;
    }

    public static void setUser(String employeeNo) {
        UserLoginDto userLoginDto = new UserLoginDto();
        userLoginDto.setEmployeeNo(employeeNo);
        IUserContext<IUser> UserContextDto = new UserContextDto<>(null, userLoginDto);
        UserContextHolder.setUser(UserContextDto);
    }
    private void formatMessage(InstanceOperateMessage message) {
        if (message.getOperateType() == null) {
            return;
        }
        MObject mObject = message.getMObject();
        if (mObject == null) {
            return;
        }
        mObject.remove(BaseDataEnum.ID.getCode());

        Iterator<Map.Entry<String, Object>> iterator = mObject.entrySet().iterator();
        HashMap<String, Object> camelMap = new HashMap<>(16);
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String && StringUtils.isBlank((String) value)) {
                mObject.replace(key, null);
            }
            if (key.contains("time") && value instanceof String && StringUtils.isNotBlank((String) value)) {
                mObject.replace(key, LocalDateTime.parse((String) value, DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            }
            if (key.contains("time") && value instanceof Long) {
                mObject.replace(key, Instant.ofEpochMilli((Long)value).atZone(ZoneId.systemDefault()).toLocalDateTime());
            }
            if (key.contains("_")) {
                camelMap.put(StrUtil.toCamelCase(key), mObject.get(key));
                iterator.remove();
            }

        }
        mObject.putAll(camelMap);
    }



}
