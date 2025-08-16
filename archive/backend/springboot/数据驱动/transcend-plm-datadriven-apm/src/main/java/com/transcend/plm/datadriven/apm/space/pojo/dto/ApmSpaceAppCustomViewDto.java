package com.transcend.plm.datadriven.apm.space.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author unknown
 */
@Data
public class ApmSpaceAppCustomViewDto implements Serializable {

    /**
     * 主键id
     */
    private Integer id;

    /**
     * 业务id
     */
    private String bid;

    /**
     * 空间应用bid
     */
    private String spaceAppBid;
    /**
     * 编码
     */
    private String category;

    /**
     * 角色BID列表
     */
    private List<String> roleBids;

    /**
     * 名称
     */
    private String name;

    /**
     * 配置内容
     */
    private Map<String, Object> configContent;

    /**
     * 排序
     */
    private Integer sort;
    /**
     * 权限类型（1-指定团队人员，2-所有空间团队成员）
     */
    private Byte permissionType;

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

}