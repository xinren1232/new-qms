package com.transcend.plm.configcenter.api.model.object.dto;

import com.transcend.plm.configcenter.api.model.base.BaseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author jinpeng.bai
 * @date 2022-09-14
 * 对象属性表
 * @TableName object_relation_attr
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectRelationAttrDTO extends BaseDto implements Serializable {

    /**
     * 关系bid
     */
    private String relationBid;

    /**
     * 字段解释
     */
    private String explain;

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
     * 是否删除(1：是，0：否)
     */
    private Boolean isDelete;

    /**
     * 列宽
     */
    private Integer columnWidth;

    /**
     * 是否在列表中显示(具体的关系配置，区分不同源对象)
     */
    private Boolean realUseInView;

    /**
     * 关系中排序(具体的关系配置，区分不同源对象)
     */
    private Integer realRelativeSort;

    /**
     * 是否用于默认查询
     */
    private Integer useInQuery;


    private static final long serialVersionUID = 1L;

}