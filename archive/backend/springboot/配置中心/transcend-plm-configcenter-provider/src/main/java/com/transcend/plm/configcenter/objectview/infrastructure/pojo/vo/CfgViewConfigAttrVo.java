package com.transcend.plm.configcenter.objectview.infrastructure.pojo.vo;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;

import lombok.Data;

/**
 * 视图配置属性表
 * @TableName cfg_view_config_attr
 */
@Data
public class CfgViewConfigAttrVo implements Serializable {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 业务id
     */
    private String bid;

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
    private String key;

    /**
     * 是否自定义
     */
    private Integer isCustom;

    /**
     * 是否来自父类继承属性
     */
    private Integer isInherit;

    /**
     * 是否自读
     */
    private Integer isReadonly;

    /**
     * 是否必填（默认：0）
     */
    private Integer isRequired;

    /**
     * 说明
     */
    private String description;

    /**
     * 属性约束
     */
    private String constraint;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 是否可见(默认：1 可见)
     */
    private Integer isVisible;

    /**
     * 布局(前端要用的布局字段) 详细保存格式和前端沟通
     */
    private String layout;

    /**
     * 是否为基础属性(0:否,1:是)
     */
    private Integer isBaseAttr;

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
    private Integer deleteFlag;

    /**
     * 事件
     */
    private String action;

    /**
     * 编码bid
     */
    private String code;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}