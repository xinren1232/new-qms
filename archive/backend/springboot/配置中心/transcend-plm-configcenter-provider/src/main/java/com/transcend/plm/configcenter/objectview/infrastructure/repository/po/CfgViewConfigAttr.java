package com.transcend.plm.configcenter.objectview.infrastructure.repository.po;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import lombok.Data;

/**
 * 视图配置属性表
 * @TableName cfg_view_config_attr
 */
@TableName(value ="cfg_view_config_attr")
@Data
public class CfgViewConfigAttr extends BasePoEntity implements Serializable {
    /**
     * 主键ID
     */
    /*@TableId(type = IdType.AUTO)
    private Integer id;*/

    /**
     * 业务id
     */
    /*private String bid;*/

    /**
     * 关联视图的业务ID
     */
    private String viewBid;

    /**
     * 模型code
     */
    private String modelCode;

    /**
     * 属性类型
     */
    private String componentType;

    /**
     * 数据类型
     */
    private String dataType;


    /**
     * 排序
     */
    private Integer sort;

    /**
     * 显示名称
     */
    private String displayName;

    /**
     * 内部名称
     */
    private String innerName;

    /**
     * 关联的key
     */
    @TableField(value = "`key`")
    private String key;

    /**
     * 是否自定义
     */
    private Boolean isCustom;

    /**
     * 是否来自父类继承属性
     */
    private Boolean isInherit;

    /**
     * 是否自读
     */
    private Boolean isReadonly;

    /**
     * 是否必填（默认：0）
     */
    private Boolean isRequired;

    /**
     * 说明
     */
    private String description;

    /**
     * 属性约束
     */
    @TableField(value = "`constraint`")
    private String constraint;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 是否可见(默认：1 可见)
     */
    private Boolean isVisible;

    /**
     * 布局(前端要用的布局字段) 详细保存格式和前端沟通
     */
    private String layout;

    /**
     * 是否为基础属性(0:否,1:是)
     */
    private Boolean isBaseAttr;

    /**
     * 租户ID
     */
    /*private Long tenantId;

    *//**
     * 创建人
     *//*
    private String createdBy;

    *//**
     * 创建时间
     *//*
    private Date createdTime;

    *//**
     * 更新人
     *//*
    private String updatedBy;

    *//**
     * 更新时间
     *//*
    private Date updatedTime;

    *//**
     * 删除标识
     *//*
    private Integer deleteFlag;*/

    /**
     * 事件
     */
    @TableField(value = "`action`")
    private String action;

    /**
     * 编码bid
     */
    private String code;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}