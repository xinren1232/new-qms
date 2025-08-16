package com.transcend.plm.configcenter.api.model.object.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author chaonan.xu
 * @version: 1.0
 * @date 2021/8/30 15:18
 */
@Accessors(chain = true)
@ToString
@Setter
@Getter
public class ObjectRelationDTO implements Serializable {

    @ApiModelProperty(value = "关系编码", example = "12345678")
    private String bid;

    @ApiModelProperty(value = "关系名称", example = "文档任务关系")
    private String name;

    @ApiModelProperty(value = "tab名称", example = "文档关系")
    private String tabName;

    @ApiModelProperty(value = "状态", example = "off:未启用 enable:启用 disable:禁用")
    private Integer enableFlag;

    @ApiModelProperty(value = "描述", example = "这是测试描述")
    private String description;

    @ApiModelProperty(value = "源对象编码", example = "123")
    private String sourceModelCode;

    @ApiModelProperty(value = "目标对象编码", example = "123")
    private String targetModelCode;

    @ApiModelProperty(value = "关联行为", example = "固定：关联对象版本固定创建时的版本，浮动：关联对象版本一直用最新的")
    private String behavior;

    /*@ApiModelProperty(value = "应用编码", example = "某个应用的项目的编码")
    private String appCode;

    @ApiModelProperty(value = "是否为应用", example = "确定是否为一个项目")
    private Boolean appFlag;*/

    @ApiModelProperty(value = "关联类型", example = "仅创建：关联对象需要自己创建，仅选取：关联对象需要选择，两者皆可")
    private String type;

    @ApiModelProperty(value = "关联项必填", example = "应用时 关联的对象实例是否必需")
    private Boolean required;

    @ApiModelProperty(value = "允许的实例最大数量")
    private Integer maxNumber;

    @ApiModelProperty(value = "允许的实例最小数量")
    private Integer minNumber;

    @ApiModelProperty(value = "应用时隐藏tab", example = "true")
    private Boolean hideTab;

    @ApiModelProperty(value = "检出以浮动方式检出检入，只有在固定方式时可以配置", example = "true")
    private Boolean floatBehavior;

    @ApiModelProperty(value = "关系列表展示属性配置")
    private List<ObjectRelationAttrDTO> relationAttr;
}
