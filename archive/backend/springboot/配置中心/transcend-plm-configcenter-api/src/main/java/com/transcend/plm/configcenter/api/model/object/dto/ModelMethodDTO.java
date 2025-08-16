package com.transcend.plm.configcenter.api.model.object.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author pingzhang.chen
 * @version: 1.0
 * @date 2021/11/18 09:48
 */
@Accessors(chain = true)
@Getter
@Setter
@ToString
@ApiModel(value = "方法管理")
public class ModelMethodDTO implements Serializable{
    /**
     * 主键ID
     */
    @ApiModelProperty(value = "主键ID")
    private Integer id;

    /**
     * 业务ID(编码)
     */
    @ApiModelProperty(value = "业务ID")
    private String bid;

    /**
     * 使用状态，禁用disable，启用enable，未启用off(默认off)
     */
    @ApiModelProperty(value = "使用状态，禁用disable，启用enable，未启用off(默认off)")
    private Integer enableFlag;

    /**
     * 动作方法 (add,get)
     */
    @ApiModelProperty(value = "动作方法 (add,get)")
    private String actionMethod;

    /**
     * 方法类型（1 前置 2 后置）
     */
    @ApiModelProperty(value = "方法类型（1 前置 2 后置）")
    private Integer methodType;

    /**
     * 全限定类名
     */
    @ApiModelProperty(value = "全限定类名")
    private String referenceClassName;

    /**
     * 执行的方法名称
     */
    @ApiModelProperty(value = "执行的方法名称")
    private String executeMethodName;

    /**
     * 方法名称
     */
    @ApiModelProperty(value = "方法名称")
    private String name;

    /**
     * 方法说明
     */
    @ApiModelProperty(value = "方法说明")
    private String description;

    /**
     * 方法代码
     */
    @ApiModelProperty(value = "方法代码",example = "方法代码")
    private String methodCode;

    /**
     * 是否异步
     */
    @ApiModelProperty(value = "是否异步")
    private Boolean sync;


    private static final long serialVersionUID = 1L;
}