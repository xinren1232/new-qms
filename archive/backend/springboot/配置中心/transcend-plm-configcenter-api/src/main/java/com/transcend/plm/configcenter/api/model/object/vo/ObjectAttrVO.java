package com.transcend.plm.configcenter.api.model.object.vo;

import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.transcend.plm.configcenter.api.model.base.BaseDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * 对象属性DTO
 *
 * @author yikai.lian
 * @version: 1.0
 * @date 2021/02/01 14:53
 */
@Getter
@Setter
@ApiModel(description = "对象属性信息",value = "对象属性VO")
@ToString
public class ObjectAttrVO extends BaseDto{
    /**
     * 关联对象的业务ID
     */
    @ApiModelProperty(value = "关联对象编码",example = "B001")
    private String objBid;

    /**
     * 关联对象的版本
     */
    @ApiModelProperty(value = "关联对象版本",example = "V1.0")
    private String objVersion;

    /**
     * 属性类型
     */
    @ApiModelProperty(value = "属性类型",example = "project")
    private String type;

    /**
     * 显示名称
     */
    @ApiModelProperty(value = "显示名称",example = "名称")
    private String displayName;

    /**
     * 内部名称
     */
    @ApiModelProperty(value = "内部名称",example = "name")
    private String innerName;

    /**
     * 是否必填（默认：0）
     */
    @ApiModelProperty(value = "是否必填",example = "true 是 false否")
    private Boolean required;

    /**
     * 说明
     */
    @ApiModelProperty(value = "描述",example = "这是描述")
    private String description;

    @ApiModelProperty(value = "字段解释", example = "字段解释")
    private String explain;

    /**
     * 属性约束
     */
    @ApiModelProperty(value = "属性约束",example = "[{'name':'名称','is_required':true,type:'select'}]")
    private String constraint;

    /**
     * 默认值
     */
    @ApiModelProperty(value = "默认值",example = "2")
    private String defaultValue;

    /**
     * 是否可见(默认：1 可见)
     */
    @ApiModelProperty(value = "可见",example = "是 true,否 false")
    private Boolean visible;

    /**
     * 布局(前端要用的布局字段) 详细保存格式和前端沟通
     */
    @ApiModelProperty(value = "布局",example = "layout")
    private String layout;

    /**
     * 是否为基础属性(0:否,1:是)
     */
    @ApiModelProperty(value = "是否基础属性",example = "1 是，0 否")
    private Boolean baseAttr;

    @ApiModelProperty(value = "是否为索引(0:否,1:是)，应用到关系框架", example = "0")
    private Boolean index;

    @ApiModelProperty(value = "是否只读",example ="1:是;0:否" )
    private  Boolean readonly;
    /**
     * 是否继承
     */
    @ApiModelProperty(value = "是否继承", example = "false")
    private Boolean inherit;

    @ApiModelProperty(value = "数据类型",example = "string")
    private String dataType;

    public static ObjectAttrVO of() {
        return new ObjectAttrVO();
    }

    @ApiModelProperty(value = "属性排序",example = "1")
    private Integer sort;

    /**继承属性是否自定义*/
    private Boolean custom;


    private String bid;

    /**在关系对象中是否显示*/
    @ApiModelProperty(value = "在关系对象中是否显示",example = "false:不显示 true:显示")
    @JsonProperty(value = "isShowInShip")
    private Boolean show;

    /**编码规则code*/
    @ApiModelProperty(value = "编码规则code",example = "234343")
    private String code;

    @ApiModelProperty(value = "编码规则名称",example = "打发打发")
    private String codeRuleName;

    /**关系中排序*/
    @ApiModelProperty(value = "关系中排序",example = "1")
    @JsonProperty(value = "shipSort")
    private Integer relativeSort;

    @ApiModelProperty(value = "是否在视图中使用",example = "false")
    private Boolean useInView;

    @ApiModelProperty(value = "是否支持远程接口获取",example = "false")
    private Boolean remote;

    @ApiModelProperty(value = "事件方法",example = "[{}]")
    private JSONArray action;

    @ApiModelProperty(value = "是否是关系新增属性")
    private Boolean relationNewAttr;

    @ApiModelProperty(value = "列宽")
    private Integer columnWidth;

    @ApiModelProperty(value = "是否在列表中显示(具体的关系配置，区分不同源对象)")
    private Boolean realUseInView;

    @ApiModelProperty(value = "关系中排序(具体的关系配置，区分不同源对象)")
    private Integer realRelativeSort;

    @ApiModelProperty(value = "是否用于默认查询")
    private Integer useInQuery;

    @ApiModelProperty(value = "来源(target:目标对象,relation:关系对象)")
    private String sourceModel;

    @ApiModelProperty(value = "目标对象bid")
    private String sourceModelCode;

}
