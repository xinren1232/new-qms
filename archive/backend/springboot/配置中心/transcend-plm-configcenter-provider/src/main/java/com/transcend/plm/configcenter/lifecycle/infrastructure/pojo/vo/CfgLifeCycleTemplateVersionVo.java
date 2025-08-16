package com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.vo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;


import lombok.Data;

@Data
public class CfgLifeCycleTemplateVersionVo implements Serializable {

    private Long id;

    /**
     * 生命周期模板id
     */
    private String templateBid;

    private String name;

    /**
     * 说明
     */
    private String description;

    /**
     * 版本号
     */
    private String version;

    /**
     * 状态（0不可用，1可用）
     */
    private String stateCode;
    /**
     * 状态（0未启用，1启用，2禁用，默认启用）
     */
    private Integer enableFlag = 1;

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
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 删除标识
     */
    private Integer deleteFlag;

    private static final long serialVersionUID = 1L;

    //前端页面布局信息
    private List<JSONObject> layouts;

}