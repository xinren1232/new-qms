package com.transcend.plm.configcenter.api.model.objectrelation.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;


import lombok.Data;

/**
 *
 * @TableName cfg_object_relation
 */

@Data
public class CfgObjectRelationDto implements Serializable {
    /**
     *
     */
    private Long id;

    /**
     * 关系编码
     */
    private String bid;

    /**
     * 关系名称
     */
    private String name;

    /**
     * tab名称
     */
    private String tabName;

    /**
     * 描述
     */
    private String description;

    /**
     * 源对象编码
     */
    private String sourceModelCode;

    /**
     * 关系表自身编码
     */
    private String modelCode;

    /**
     * 关联对象编码
     */
    private String targetModelCode;

    /**
     * 关联行为（固定：关联对象版本固定创建时的版本，浮动：关联对象版本一直用最新的）
     */
    private String behavior;

    /**
     * 关联类型（仅创建：关联对象需要自己创建，仅选取：关联对象需要选择，两者皆可）
     */
    private String type;

    /**
     * 关联项必填（应用时 关联的对象实例是否必需）
     */
    private Byte isRequired;

    /**
     * 允许的最大实例数量
     */
    private Integer maxNumber;

    /**
     * 允许的最小实例数量
     */
    private Integer minNumber;

    /**
     * 应用时是否隐藏tab
     */
    private Byte hideTab;

    /**
     * 标签排序
     */
    private Integer sort;

    /**
     * 检出以浮动方式检出检入
     */
    private Byte floatBehavior;

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
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 删除标识
     */
    private Integer deleteFlag;

    /**
     * 启用标志，0未启用，1启用，2禁用
     */
    private Integer enableFlag;

    /**
     * 目标对象展示方式(table:表格，tree:树形)
     */
    private String showType;

    private static final long serialVersionUID = 1L;

    private List<CfgObjectRelationAttrDto> relationAttr;
}