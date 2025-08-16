package com.transcend.plm.datadriven.apm.space.pojo.vo;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author unknown
 */
@Data
public class ApmSpaceVo implements Serializable {
    /**
     * 主键id
     */
    private Long id;
    /**
     * 业务id
     */
    private String bid;
    /**
     * 领域bid
     */
    private String sphereBid;

    /**
     * 绑定的模板bid
     */
    private String templateBid;

    /**
     * 绑定的模板Name
     */
    private String templateName;


    /**
     * 是否是模板（1-是，0-否 ，默认0）
     */
    private Boolean templateFlag;

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
    private JSONArray iconUrl;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 当前默认空间
     */
    private boolean defaultSpace = false;

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
     * 空间对象
     */
    private List<ApmSpaceAppVo> apmSpaceAppVoList;
//
//    /**
//     * 空间对象访问权限
//     */
//    private List<ApmSpaceAppAccessVo> apmSpaceAppAccessDtoList;
}
