package com.transcend.plm.datadriven.apm.space.pojo.dto;
import lombok.Data;

import java.util.Date;

/**
 * @Author yanjie
 * @Date 2023/12/22 14:30
 * @Version 1.0
 */

@Data
public class ApmRoleDto {

    private String bid;

    /**
     * 父级bid
     */
    private String pbid;

    /**
     * 编码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 层级路径
     */
    private String path;

    /**
     * 域id
     */
    private String sphereBid;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 创建者
     */
    private String createdBy;

    /**
     * 更新时间
     */
    private Date updatedTime;

    /**
     * 更新人
     */
    private String updatedBy;

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
     * 删除标志
     */
    private String foreignBid;
}
