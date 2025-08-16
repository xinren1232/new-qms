package com.transcend.plm.configcenter.api.model.object.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author jinpeng.bai
 * @date 2022-09-14
 * 对象属性表
 */
@Data
public class ObjectRelationAttrVO implements Serializable {
    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 业务id
     */
    private String bid;

    /**
     * 关联对象的业务ID
     */
    private String relationBid;

    /**
     * 内部名称
     */
    private String innerName;

    /**
     * 字段解释
     */
    private String explain;

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
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新·时间
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
     * 应用ID
     */
    private String appId;

    /**
     * 公司ID
     */
    private Integer companyId;

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

}