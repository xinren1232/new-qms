package com.transcend.plm.configcenter.menuapp.pojo.qo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author jie.luo1
 * @Version 1.0
 * @Date 2023-02-22 15:43
 **/
@Data
public class CfgMenuAppQo {


    /**
     * 编码
     */
    @ApiModelProperty("编码")
    private String code;

    /**
     * 名称
     */
    @ApiModelProperty("名称")
    private String name;

    /**
     * 描述说明
     */
    private String description;

    /**
     * 使用状态，禁用disable，启用enable，未启用off(默认off)
     */
    private Integer enableFlag;

    /**
     * 类型编码（object-对象，view-视图，url-外链）
     */
    @ApiModelProperty("类型编码（object-对象，view-视图，url-外链）")
    private String typeCode;

    /**
     * 类型值
     */
    @ApiModelProperty("类型值")
    private String typeValue;

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
}
