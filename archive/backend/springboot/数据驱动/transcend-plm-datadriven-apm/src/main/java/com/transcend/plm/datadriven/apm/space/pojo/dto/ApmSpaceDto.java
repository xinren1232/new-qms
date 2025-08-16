package com.transcend.plm.datadriven.apm.space.pojo.dto;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;

import java.util.List;

/**
 * @author unknown
 */
@Data
public class ApmSpaceDto {
    /**
     * 业务id
     */
    private String bid;

    /**
     * 领域id
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
     * 模板业务id
     */
    private String templateName;


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

    /**
     * 删除标志 0未删除 1已删除
     */
    private Integer deleteFlag;

    /**
     * 是否初始化实例本身 ture:是 false:否
     */
    private Boolean initSelfFlag = false;


    private List<ApmSpaceAppDto> apmSpaceAppDtos;


}
