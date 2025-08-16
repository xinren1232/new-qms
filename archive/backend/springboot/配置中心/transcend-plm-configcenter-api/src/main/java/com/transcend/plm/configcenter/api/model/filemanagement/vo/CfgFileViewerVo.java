package com.transcend.plm.configcenter.api.model.filemanagement.vo;

import lombok.Data;

import java.util.Date;
@Data
public class CfgFileViewerVo implements java.io.Serializable {
    private Integer id;

    /**
     * 业务id
     */
    private String bid;

    /**
     * 文件查看器名称
     */
    private String name;

    /**
     * URL
     */
    private String url;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否是默认库，0不是，1是
     */
    private Boolean defaultFlag;

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
     * 删除标识
     */
    private Boolean deleteFlag;

    /**
     * 启用标志，0未启用，1启用，2禁用
     */
    private Integer enableFlag;
}
