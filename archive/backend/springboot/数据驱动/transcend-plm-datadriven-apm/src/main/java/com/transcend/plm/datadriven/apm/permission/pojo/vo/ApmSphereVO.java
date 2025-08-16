package com.transcend.plm.datadriven.apm.permission.pojo.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 空间领域VO
 * @createTime 2023-09-25 16:09:00
 */
@Data
public class ApmSphereVO {
    /**
     * 主键id
     */
    private Integer id;

    /**
     * 业务id
     */
    private String bid;

    /**
     * 父级bid
     */
    private String pbid;

    /**
     * 名称
     */
    private String name;

    /**
     * 业务id
     */
    private String bizBid;

    /**
     * 类型 :space空间，object对象，instance对象下实例
     */
    private String type;

    /**
     * 创建人
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
}
