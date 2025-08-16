package com.transcend.plm.datadriven.apm.permission.pojo.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author unknown
 */
@Data
public class ApmRoleAccessVO {

    /**
     * 主键id
     */
    private Integer id;

    /**
     * 角色bid
     */
    private String roleBid;

    /**
     * 资源bid
     */
    private String accessBid;

    /**
     * 域bid
     */
    private String sphereBid;

    /**
     * 对象编码
     */
    private String modelCode;

    /**
     * 对象字段名称
     */
    private String filedName;

    /**
     * 表字段名称
     */
    private String columnName;

    /**
     * 关系
     */
    private String relationship;

    /**
     * 条件比较值
     */
    private String filedValue;

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
}
