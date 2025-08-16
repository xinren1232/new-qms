package com.transcend.plm.configcenter.api.model.objectrelation.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 对象属性表
 * @TableName cfg_object_relation_attr
 */
@Data
public class CfgObjectRelationAttrVo implements Serializable {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 业务id
     */
    private String bid;

    /**
     * cfg_object_relation关联对象的业务ID
     */
    private String relationBid;

    /**
     * 显示名称
     */
    private String displayName;

    /**
     * 内部名称
     */
    private String innerName;

    /**
     * 来源(target:目标对象,relation:关系对象)
     */
    private String sourceModel;

    /**
     * 目标对象bid
     */
    private String sourceModelCode;

    /**
     * 
     */
    private String explain;

    /**
     * 列宽
     */
    private Integer columnWidth;

    /**
     * 是否在列表中显示(具体的关系配置，区分不同源对象)
     */
    private Integer realUseInView;

    /**
     * 关系中排序(具体的关系配置，区分不同源对象)
     */
    private Integer realRelativeSort;

    /**
     * 是否用于默认查询
     */
    private Integer useInQuery;

   /* *
     * 租户ID*/

    private Long tenantId;

    /**
     * 创建人*/

    private String createdBy;

   /* *
     * 创建时间*/

    private LocalDateTime createdTime;

    /**
     * 更新人*/

    private String updatedBy;

    /**
     * 更新时间*/

    private LocalDateTime updatedTime;

    /**
     * 删除标识*/

    private Integer deleteFlag;

    /**
     * 启用标志，0未启用，1启用，2禁用
     */
    private Integer enableFlag;

    private static final long serialVersionUID = 1L;
}