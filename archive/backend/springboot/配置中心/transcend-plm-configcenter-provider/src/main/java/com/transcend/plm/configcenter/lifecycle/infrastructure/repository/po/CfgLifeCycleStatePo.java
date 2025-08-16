package com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import lombok.Data;

/**
 * 生命周期状态维护表
 * @TableName cfg_life_cycle_state
 */
@TableName(value ="cfg_life_cycle_state")
@Data
public class CfgLifeCycleStatePo extends BasePoEntity implements Serializable {

    /**
     * 编码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 颜色
     */
    private String color;

    /**
     * 所属组编码
     */
    private String groupCode;

    /**
     * 说明
     */
    private String description;

    /**
     * 是否被绑定，0未被绑定，1被绑定
     */
    private Boolean bindingFlag;



    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}