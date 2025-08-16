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
 * @TableName cfg_life_cycle_template_version
 */
@TableName(value ="cfg_life_cycle_template_version")
@Data
public class CfgLifeCycleTemplateVersionPo extends BasePoEntity implements Serializable {


    /**
     * 生命周期模板id
     */
    private String templateBid;

    /**
     * 生命周期模板名称
     */
    private String name;

    /**
     * 说明
     */
    private String description;

    /**
     * 版本号
     */
    private String version;

    /**
     * 状态（0不可用，1可用）
     */
    private String stateCode;
    /**
     * 状态（0未启用，1启用，2禁用，默认启用）
     */
    private Integer enableFlag = 1;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}