package com.transcend.plm.datadriven.notify.vo;

import com.alibaba.fastjson.JSONArray;
import com.transcend.plm.datadriven.notify.domain.ObjectAttrValue;
import lombok.Data;


import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Data
public class NotifyConfigOperateVo {
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
     * 业务驱动类型（OBJECT/FEATURE）
     */
    private String type;

    /**
     * 操作类型,CREATE.创建实例，UPDATE.修改实例，UPDATE_REL.修改关联属性,CREATE_OR_DELETE_REL.添加及删除直接子关系
     */
    private String operate;

    /**
     * 操作额外的配置信息（比如type是对象的场景，还需要记录关系的相关内容）
     */
    private Object operateConfigInfo;
    /**
     * 属性集合
     */

    private List<String> operateAttrs;

    /**
     * 所选属性匹配值集合
     */
    private Map<String, ObjectAttrValue> operateAttrValues;
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

    private List<NotifyTriggerRuleVo> notifyTriggerRuleVos;
}
