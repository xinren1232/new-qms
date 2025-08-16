package com.transcend.plm.configcenter.object.infrastructure.po;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import lombok.Data;

/**
 * 对象生命周期表
 * @TableName cfg_object_life_cycle
 */
@TableName(value ="cfg_object_life_cycle")
@Data
public class CfgObjectLifeCycle extends BasePoEntity implements Serializable {

    /**
     * 生命周期状态ID
     */
    private String lcTemplBid;

    /**
     * 生命周期状态版本（备份使用，不会使用在实例上）
     */
    private String lcTemplVersion;

    /**
     * 初始状态
     */
    private String initState;

    /**
     * 说明
     */
    private String description;

    /**
     * 模型code
     */
    private String modelCode;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}