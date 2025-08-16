package com.transcend.plm.configcenter.api.model.lifecycle.dto;

import com.transcend.plm.configcenter.api.model.base.BaseDto;
import lombok.Data;

/**
 * 
 * @TableName cfg_life_cycle_template_obj_rel
 */
@Data
public class CfgLifeCycleTemplateObjRelDto extends BaseDto {


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

    private static final long serialVersionUID = 1L;

}