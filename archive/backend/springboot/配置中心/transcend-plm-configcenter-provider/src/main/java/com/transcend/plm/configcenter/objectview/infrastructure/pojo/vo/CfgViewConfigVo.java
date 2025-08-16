package com.transcend.plm.configcenter.objectview.infrastructure.pojo.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;

import lombok.Data;

/**
 * 视图配置表
 * @TableName cfg_view_config
 */
@Data
public class CfgViewConfigVo implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 业务id
     */
    private String bid;

    /**
     * 视图名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 生命周期状态CODE(还有 ALL)
     */
    private String lcStateCode;

    /**
     * 标签集
     */
    private String tags;

    /**
     * 角色类型
     */
    private int roleType;

    /**
     * 优先级
     */
    private int priority;

    /**
     * 启用标志，0未启用，1启用，2禁用
     */
    private Integer enableFlag;

    /**
     * 模型code
     */
    private String modelCode;

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
     * 是否被实例化，使用
     */
    private Boolean bindingFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    List<CfgViewConfigAttrVo> viewAttrList;

}