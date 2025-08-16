package com.transcend.plm.datadriven.notify.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.transcend.plm.datadriven.notify.domain.NotifyTriggerRule;
import lombok.Data;

import java.util.List;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Data
public class NotifyConfigTriggerDto {
    /**
     * 业务id
     */
    private String bid;

    /**
     * 通知主表业务id
     */
    private String notifyConfigBid;

    /**
     * 业务驱动类型（OBJECT/特性化等）
     */
    private String type;

    /**
     * 操作额外的配置信息（比如type是对象的场景，还需要记录关系的相关内容）
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler.class)
    private Object triggerConfigInfo;

    private List<NotifyTriggerRule> notifyTriggerRules;
}
