package com.transcend.plm.alm.openapi.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 系统特性对象
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/27 17:07
 */
@Data
public class AlmSystemFeatureDTO implements Serializable {

    /**
     * 数据bid
     */
    private String bid;

    /**
     * 特性编码
     */
    private String coding;

    /**
     * 特性名称
     */
    private String name;

    /**
     * 特性层级
     * 0-3 分别对应一级特性、二级特性、三级特性、四级特性
     */
    private Integer level;

    /**
     * 特性SE 工号
     * 多个使用英文逗号隔开
     */
    private String featureSe;

    /**
     * 父特性bid
     */
    private String parentBid;

    /**
     * 子层特性
     */
    private List<AlmSystemFeatureDTO> children;

    //2025年6月6日新增字段

    /**
     * 空间Bid
     */
    private String spaceBid;

    /**
     * 空间应用Bid
     */
    private String spaceAppBid;

    /**
     * 特性Owner 工号
     * 多个使用英文逗号隔开
     */
    private String featureOwner;

    /**
     * 归属领域 中文名称
     * 多个使用英文逗号隔开
     */
    private String belongDomain;

    /**
     * 特性描述
     */
    private String description;

}
