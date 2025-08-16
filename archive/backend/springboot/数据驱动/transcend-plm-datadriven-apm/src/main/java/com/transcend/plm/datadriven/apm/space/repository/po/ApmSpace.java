package com.transcend.plm.datadriven.apm.space.repository.po;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.annotation.*;
import com.transcend.plm.datadriven.apm.tools.CustomJSONArrayTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 空间表
 * @author unknown
 * @TableName apm_space
 */
@TableName(value ="apm_space")
@Data
public class ApmSpace implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 业务id
     */
    private String bid;


    /**
     * 空间bid
     */
    private String spaceBid;

    /**
     * 空间应用bid
     */
    private String spaceAppBid;

    /**
     * 领域bid
     */
    private String sphereBid;

    /**
     * 名称
     */
    private String name;

    /**
     * 说明
     */
    private String description;

    /**
     * 图标url
     */
    @TableField(typeHandler = CustomJSONArrayTypeHandler.class)
    private JSONArray iconUrl;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 是否是模板，0不是，1是
     */
    private Boolean templateFlag;

    /**
     * 模板业务id
     */
    private String templateBid;

    /**
     * 模板业务Name
     */
    private String templateName;

    /**
     * 删除标志
     */
    @TableLogic
    private Integer deleteFlag;

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
     * 租户ID
     */
    private Integer tenantId;

    /**
     * 外部唯一标识
     */
    private String foreignBid;
    /**
     * 来源bid
     */
    private String originBid;
    /**
     * 来源modelCode
     */
    private String originModelCode;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}