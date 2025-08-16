package com.transcend.plm.datadriven.apm.permission.pojo.ao;

import lombok.Data;

import java.util.Date;

/**
 * @Author yanjie
 * @Date 2024/1/3 17:07
 * @Version 1.0
 */
@Data
public class MessageRecordAO {

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 消息类型
     */
    private String type;

    /**
     * 消息状态
     */
    private String state;

    /**
     * 错误级别 high.高，middle.中，low.低
     */
    private String errLevel;

    /**
     * 错误信息
     */
    private String errMsg;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新时间
     */
    private Date updatedTime;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 删除标志
     */
    private Integer deleteFlag;
}
