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
 * @TableName cfg_life_cycle_template_obj_rel
 */
@TableName(value ="cfg_life_cycle_template_obj_rel")
@Data
public class CfgLifeCycleTemplateObjRelPo extends BasePoEntity implements Serializable {


    /**
     * 生命周期模板id
     */
    private String templateBid;

    /**
     * 生命周期模板版本号
     */
    private String templateVersion;

    /**
     * 生命周期状态编码
     */
    private String lifeCycleCode;

    /**
     * 目标对象bid
     */
    private String targetObjBid;

    /**
     * 目标对象名称
     */
    private String targetObjName;

    /**
     * 目标对象关系，浮动：float，固定：fixed
     */
    private String targetObjRel;

    /**
     * 目标对象model_code
     */
    private String targetModelCode;
    /**
     * 状态（0未启用，1启用，2禁用，默认启用）
     */
    private Integer enableFlag = 1;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}