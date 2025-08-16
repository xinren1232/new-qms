package com.transcend.plm.configcenter.api.model.attribute.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:30
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("标准属性")
public class CfgAttributeDto implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 业务id
     */
    private String bid;

    /**
     * 编码
     */
    @ApiModelProperty(value = "编码", example = "country", required = true)
    private String code;

    /**
     * 名称
     */
    @ApiModelProperty(value = "名称", example = "国家", required = true)
    private String name;

    /**
     * 对象模型code
     */
    @ApiModelProperty(value = "对象模型code", example = "A28", required = false)
    private String modelCode;

    /**
     * 描述说明
     */
    @ApiModelProperty(value = "描述说明", example = "xxx", required = false)
    private String description;

    /**
     * 使用状态，禁用2，启用1，未启用0
     */
    @ApiModelProperty(value = "使用状态，禁用2，启用1，未启用0", example = "0", required = false)
    private int enableFlag;

    /**
     * 分组
     */
    @ApiModelProperty(value = "分组", example = "kk", required = false)
    private String groupName;

    /**
     * 数据类型
     */
    @ApiModelProperty(value = "数据类型", example = "int", required = true)
    private String dataType;

    /**
     * 是否为空
     */
    @ApiModelProperty(value = "是否为空", example = "false", required = false)
    private Boolean nullAble;

    /**
     * 长度
     */
    @ApiModelProperty(value = "长度", example = "1", required = false)
    private Integer length;

    /**
     * 国际化语言字典
     */
    @ApiModelProperty(value = "国际化语言字典", example = "{}", required = false)
    private Map<String, String> langDict;

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

    private static final long serialVersionUID = 1L;
}
