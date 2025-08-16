package com.transcend.plm.datadriven.apm.permission.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.transcend.plm.datadriven.apm.space.pojo.dto.FieldConditionParam;
import com.transcend.plm.datadriven.apm.tools.FieldConditionListTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author unknown
 * @TableName apm_access
 */
@TableName(value ="apm_access", autoResultMap = true)
@Data
public class ApmAccess implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 业务主键
     */
    @TableField(value = "bid")
    private String bid;

    /**
     * 编码
     */
    @TableField(value = "code")
    private String code;

    /**
     * 名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 资源
     */
    @TableField(value = "resource")
    private String resource;

    /**
     * 类型：菜单，按钮，接口，对象
     */
    @TableField(value = "type")
    private String type;

    /**
     * 创建者
     */
    @TableField(value = "created_by")
    private String createdBy;

    /**
     * 创建时间
     */
    @TableField(value = "created_time")
    private Date createdTime;

    /**
     * 租户ID
     */
    @TableField(value = "tenant_id")
    private Integer tenantId;

    /**
     * 启用标志
     */
    @TableField(value = "enable_flag")
    private Integer enableFlag;

    /**
     * 删除标志
     */
    @TableField(value = "delete_flag")
    @TableLogic
    private Integer deleteFlag;

    /**
     * 可见性配置
     */
    @TableField(value = "visible_config", typeHandler = FieldConditionListTypeHandler.class)
    private List<FieldConditionParam> visibleConfig;

    /**
     * 执行操作配置
     */
    @TableField(value = "operation_config", typeHandler = JacksonTypeHandler.class)
    private Map<String, String> operationConfig;

    /**
     * 视图
     */
    @TableField(value = "view_config", typeHandler = JacksonTypeHandler.class)
    private Map<String, String> viewConfig;

    /**
     * 方法数据
     */
    @TableField(value = "method_config", typeHandler = JacksonTypeHandler.class)
    private Map<String, String> methodConfig;

    /**
     * 描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 排序
     */
    @TableField(value = "sort")
    private Integer sort;

    /**
     * 图标
     */
    @TableField(value = "icon")
    private String icon;

    /**
     * 按钮展示位置
     */
    @TableField(value = "show_location")
    private String showLocation;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}