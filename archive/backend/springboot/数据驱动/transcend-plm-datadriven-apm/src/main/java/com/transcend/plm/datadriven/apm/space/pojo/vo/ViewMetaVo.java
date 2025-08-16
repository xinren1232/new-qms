package com.transcend.plm.datadriven.apm.space.pojo.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.transcend.plm.configcenter.api.model.dictionary.dto.CfgOptionItemDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 视图元数据对象
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/6/24 10:58
 */
@Data
public class ViewMetaVo implements Serializable {

    /**
     * 字段编码
     */
    @ApiModelProperty(value = "字段编码")
    private String name;
    /**
     * 显示名
     */
    @ApiModelProperty(value = "显示名")
    private String label;
    /**
     * 类型
     */
    @ApiModelProperty(value = "类型")
    private String type;
    /**
     * 默认值
     */
    @ApiModelProperty(value = "默认值")
    private Object defaultValue;
    /**
     * 字典类型
     */
    @ApiModelProperty(value = "字典类型")
    private List<String> remoteDictTypes;
    /**
     * 自定义下拉选项
     */
    @ApiModelProperty(value = "自定义下拉选项")
    private List<CfgOptionItemDto> optionItems;
    /**
     * 是否多选
     */
    @ApiModelProperty(value = "是否多选")
    private Boolean multiple;
    /**
     * 是否隐藏
     */
    @ApiModelProperty(value = "是否隐藏")
    private Boolean hidden;
    /**
     * 是否禁用
     */
    @ApiModelProperty(value = "是否禁用")
    private Boolean disabled;
    /**
     * 是否只读
     */
    @ApiModelProperty(value = "是否只读")
    private Boolean readonly;
    /**
     * 是否必填
     */
    @ApiModelProperty(value = "是否必填")
    private Boolean required;
    /**
     * 是否是密码模式
     */
    @ApiModelProperty(value = "是否是密码模式")
    private Boolean showPassword;
    /**
     * 源对象modelCode
     */
    @ApiModelProperty(value = "源对象modelCode")
    private String sourceModelCode;
    /**
     * 目标对象modelCode
     */
    @ApiModelProperty(value = "目标对象modelCode")
    private String targetModelCode;
    /**
     * 关系对象modelCode
     */
    @ApiModelProperty(value = "关系对象modelCode")
    private String relationModelCode;
    /**
     * 实例选择应用Bid
     */
    @ApiModelProperty(value = "实例选择应用Bid")
    private String instanceSelectAppBid;

    /**
     * 全量参数信息
     */
    @ApiModelProperty(value = "全量参数信息")
    private Object properties;


    /**
     * 排序字段
     */
    @JsonIgnore
    private int order;

}
