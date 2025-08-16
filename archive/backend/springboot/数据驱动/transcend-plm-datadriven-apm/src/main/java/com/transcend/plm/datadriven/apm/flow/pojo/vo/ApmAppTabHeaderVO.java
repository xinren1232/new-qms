package com.transcend.plm.datadriven.apm.flow.pojo.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author unknown
 */
@Data
public class ApmAppTabHeaderVO {
    private Long id;

    /**
     * 业务bid
     */
    private String bizBid;

    /**
     * 配置内容（各个tab自定义）
     */
    private List<Map> configContent;

    private Map<String, Object> viewConfigContent;

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
     * 视图模式code
     */

    private String code;

    /**
     * 租户ID
     */
    private Integer tenantId;
}
