package com.transcend.plm.datadriven.notify.vo;


import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Data
public class NotifyConfigTriggerVo {
    private Integer id;

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
    private Object triggerConfigInfo;

    /**
     * 启用标志，0未启用，1启用，2禁用
     */
    private Boolean enableFlag;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    private Date updatedTime;

    /**
     * 删除标识
     */
    private Boolean deleteFlag;

    /**
     * 说明
     */
    private String description;

    private List<NotifyTriggerRuleVo> notifyTriggerRuleVos;
}
