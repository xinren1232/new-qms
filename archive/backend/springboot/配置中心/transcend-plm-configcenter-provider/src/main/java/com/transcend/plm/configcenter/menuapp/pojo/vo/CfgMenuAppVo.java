package com.transcend.plm.configcenter.menuapp.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author jie.luo1
 * @Version 1.0
 * @Date 2023-02-22 10:30
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CfgMenuAppVo implements Serializable {
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
     * 描述说明
     */
    @ApiModelProperty("描述说明")
    private String description;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 子角色
     */
    private List<? extends CfgMenuAppVo> children;

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
     * 未启用0，启用1，禁用2
     */
    private Integer enableFlag;

    private static final long serialVersionUID = 1L;
}
