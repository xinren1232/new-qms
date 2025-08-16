package com.transcend.plm.datadriven.apm.permission.pojo.vo;

import com.transcend.plm.datadriven.apm.space.pojo.dto.FieldConditionParam;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author unknown
 */
@Data
public class ApmAccessVO {
    /**
     * 主键
     */
    private Integer id;

    /**
     * 业务主键
     */
    private String bid;

    /**
     * 编码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 资源
     */
    private String resource;

    /**
     * 类型：菜单，按钮，接口，对象
     */
    private String type;

    /**
     * 创建者
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 租户ID
     */
    private Integer tenantId;

    /**
     * 启用标志
     */
    private Integer enableFlag;

    /**
     * 删除标志
     */
    private Integer deleteFlag;

    /**
     * 可见性配置
     */
    private List<FieldConditionParam> visibleConfig;

    /**
     * 执行操作配置
     */
    private Map<String, String> operationConfig;

    /**
     * 描述
     */
    private String description;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 图标
     */
    private String icon;
}
