package com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName cfg_life_cycle_template_transition_line
 */
@TableName(value ="cfg_life_cycle_template_transition_line")
@Data
public class CfgLifeCycleTemplateTransitionLinePo extends BasePoEntity implements Serializable {



    /**
     * 生命周期模板id
     */
    private String templateBid;

    /**
     * 说明
     */
    private String description;

    /**
     * 版本号
     */
    private String templateVersion;

    /**
     * 角色bid
     */
    private String roleBid;

    /**
     * 开始节点
     */
    private String source;

    /**
     * 结束节点
     */
    private String target;

    /**
     * 存储如坐标等信息
     */
    private String layout;

    /**
     * 
     */
    private String beforeMethod;

    /**
     * 
     */
    private String afterMethod;

    /**
     * 状态（0未启用，1启用，2禁用，默认启用）
     */
    private Integer enableFlag = 1;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}