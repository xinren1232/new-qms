package com.transcend.plm.datadriven.apm.integration.message;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.transcend.plm.datadriven.api.model.BaseDataEnum;
import com.transcend.plm.datadriven.api.model.MObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.transcend.plm.datadriven.apm.integration.constant.InstanceOperateConstant.*;
import static com.transcend.plm.datadriven.common.constant.DataBaseConstant.COLUMN_BID;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description  实例操作消息
 * @createTime 2023-12-18 11:36:00
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class InstanceOperateMessage {
    /**
     * 模型类型
     */
    private String bizType;
    /**
     * 操作类型
     */
    private String operateType;
    /**
     * 操作人
     */
    private String operator;
    /**
     * 操作时间
     */
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime operateTime;
    /**
     * 空间Bid
     */
    private String spaceBid;
    /**
     * 空间AppBid
     */
    private String spaceAppBid;
    /**
     * 实例数据
     */
    private MObject mObject;

    /**
     * 错误级别 high.高，middle.中，low.低
     */
    private String errLevel;

    /**
     * 错误信息
     */
    private String errMsg;

    /**
     * 消息记录ID
     */
    private Long messageRecordId;



    public boolean instanceOperateMessageParamCheck() {
        if (Objects.isNull(this.getMObject())) {
            log.error("{} InstanceOperateMessage - mObject be empty！ message:{}", this.bizType, JSON.toJSONString(this));
            return false;
        }
        
        if (BizTypeEnum.SPACE.getCode().equals(this.bizType) && Objects.isNull(this.getMObject().get(DATA_BID))) {
            log.error("{} InstanceOperateMessage - dataBid be blank！ message:{}", this.bizType, JSON.toJSONString(this));
            return false;
        }
        if (!BizTypeEnum.SPACE.getCode().equals(this.bizType) && Objects.isNull(this.getMObject().get(DATA_ID))) {
            log.error("{} InstanceOperateMessage - dataId be blank！ message:{}", this.bizType, JSON.toJSONString(this));
            return false;
        }
        if (BizTypeEnum.ROLE.getCode().equals(this.bizType) && Objects.isNull(this.getMObject().get(PARENT_BID))) {
            log.error("{} InstanceOperateMessage - parentBid be blank！ message:{}", this.bizType, JSON.toJSONString(this));
            return false;
        }
        if (BizTypeEnum.ROLE.getCode().equals(this.bizType) && Objects.isNull(this.getMObject().get(COLUMN_BID))) {
            log.error("{} InstanceOperateMessage - bid be blank！ message:{}", this.bizType, JSON.toJSONString(this));
            return false;
        }
        if (BizTypeEnum.IDENTITY.getCode().equals(this.bizType) && Objects.isNull(this.getMObject().get(GROUP_BID))) {
            log.error("{} InstanceOperateMessage - groupBid be blank！ message:{}", this.bizType, JSON.toJSONString(this));
            return false;
        }
        if (BizTypeEnum.IDENTITY.getCode().equals(this.bizType) && Objects.isNull(this.getMObject().get(JOB_NUMBER))) {
            log.error("{} InstanceOperateMessage - jobNumber be blank！ message:{}", this.bizType, JSON.toJSONString(this));
            return false;
        }
        if (BizTypeEnum.IDENTITY.getCode().equals(this.bizType) && Objects.isNull(this.getMObject().get(BaseDataEnum.BID.getCode()))) {
            log.error("{} InstanceOperateMessage - bid be blank！ message:{}", this.bizType, JSON.toJSONString(this));
            return false;
        }
        return true;
    }

    public void setErrInfo(String errLevel, String errMsg) {
        this.errLevel = errLevel;
        this.errMsg = errMsg;
    }
}
