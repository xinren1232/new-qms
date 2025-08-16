package com.transcend.plm.configcenter.menuapp.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author jie.luo1
 * @Version 1.0
 * @Date 2023-02-22 10:30
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CfgMenuAppDto implements Serializable {

    /**
     * 业务id
     */
    @ApiModelProperty("业务id")
    private String bid;

    /**
     * 父编码
     */
    @ApiModelProperty("父编码")
    private String parentBid;
    
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
     * 图标
     */
    @ApiModelProperty("图标")
    private String icon;

    /**
     * 排序
     */
    private Integer sort;


    /**
     * 描述说明
     */
    @ApiModelProperty("描述说明")
    private String description;

    /**
     * 未启用0，启用1，禁用2
     */
    private Integer enableFlag;

    private static final long serialVersionUID = 1L;
}
