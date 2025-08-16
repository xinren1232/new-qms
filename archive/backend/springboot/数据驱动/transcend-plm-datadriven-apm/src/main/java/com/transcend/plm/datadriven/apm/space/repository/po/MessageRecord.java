package com.transcend.plm.datadriven.apm.space.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 消息记录表
 *
 * @author unknown
 * @TableName message_record
 */

@TableName(value = "message_record")
@Data
public class MessageRecord {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

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
    @TableLogic
    private Integer deleteFlag;

    /**
     * 重试次数
     */
    private Integer retryTimes;
}
