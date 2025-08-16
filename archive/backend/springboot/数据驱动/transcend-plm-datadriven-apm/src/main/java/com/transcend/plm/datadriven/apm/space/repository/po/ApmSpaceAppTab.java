package com.transcend.plm.datadriven.apm.space.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.transcend.plm.datadriven.api.model.qo.ModelFilterQo;
import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transcend.plm.datadriven.apm.space.pojo.dto.MultiAppConfig;
import com.transcend.plm.datadriven.apm.space.pojo.dto.RelationActionPermission;
import com.transcend.plm.datadriven.apm.tools.RelationActionPermissionListTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 空间应用tab表
 * @author unknown
 * @TableName apm_space_app_tab
 */
@TableName(value ="apm_space_app_tab",autoResultMap = true)
@Data
public class ApmSpaceAppTab implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 业务id
     */
    private String bid;

    /**
     * 空间应用业务id
     */
    private String spaceAppBid;

    /**
     * 目标空间应用业务id
     */
    private String targetSpaceAppBid;

    /**
     * 关系对象modelCode
     */
    private String relationModelCode;

    /**
     * tab名称
     */
    private String name;

    /**
     * tab名称
     */
    private String tabName;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler.class)
    private List<Map> multiTreeContent;

    /**
     * 配置内容（各个tab自定义）
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler.class)
    private ModelMixQo configContent;

    /**
     * 显示 视图模式
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler.class)
    private List<String> showViewModels;

    /**
     * 是否展示条件配置（各个tab自定义）
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler.class)
    private List<ModelFilterQo> showConditionContent;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler.class)
    private MultiAppConfig multiAppTreeContent;

    @TableField(typeHandler = RelationActionPermissionListTypeHandler.class)
    private List<RelationActionPermission> relationActionPermissionList;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 删除标志
     */
    private Boolean deleteFlag;

    /**
     * 启用标志，0未启用，1启用，2禁用
     */
    private Boolean enableFlag;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新时间
     */
    private Date updatedTime;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 租户ID
     */
    private Integer tenantId;

    /**
     * 前端组件组件类型（关系-RELATION）
     */
    private String component;

    /**
     * 
     */
    private String code;

    /**
     * 
     */
    private String viewModelCode;

    private String source;

    /**
     * 是否每次切换都从新加载数据
     */
    private Boolean isRefresh;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}