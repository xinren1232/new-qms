package com.transcend.plm.configcenter.api.model.lifecycle.dto;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class CfgLifeCycleTemplateDto implements Serializable {

    private Long id;

    /**
     * 生命周期模板名称
     */
    private String name;

    /**
     * 当前版本
     */
    private String currentVersion;

    /**
     * 状态（启用标志，0未启用，1启用，2禁用）
     */
    private Integer enableFlag;

    /**
     * 业务id
     */
    private String bid;

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
    private Integer deleteFlag;

    /**
     * 说明
     */
    private String description;

    @ApiModelProperty(value = "版本列表")
    private List<String> versionList;

    @ApiModelProperty(value = "生命周期节点列表")
    private List<CfgLifeCycleTemplateNodeDto> nodeList;

    @ApiModelProperty(value = "生命周期转换线列表")
    private List<CfgLifeCycleTemplateTransitionLineDto> lineList;

    //前端页面布局信息
    private List<JSONObject> layouts;

}
