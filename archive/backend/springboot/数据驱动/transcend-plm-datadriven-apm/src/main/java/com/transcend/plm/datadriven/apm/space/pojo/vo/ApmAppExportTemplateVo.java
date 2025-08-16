package com.transcend.plm.datadriven.apm.space.pojo.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author shu.zhang
 * @version 1.0
 * @className ApmAppExportTemplateVo
 * @description desc
 * @date 2024/5/29 15:33
 */
@Data
public class ApmAppExportTemplateVo {

    /**
     * 业务id
     */
    private String bid;

    /**
     * 空间BID
     */
    private String spaceBid;

    /**
     * 应用BID
     */
    private String spaceAppBid;

    /**
     * 模版名称
     */
    private String templateName;

    /**
     * 字段列表
     */
    private List<String> fields;

    /**
     * 分组列表
     */
    private List<Object> groupData;

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
    private Boolean enableFlag;
}
