package com.transcend.plm.configcenter.object.infrastructure.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @author chaonan.xu
 * @version: 1.0
 * @date 2021/8/30 15:18
 */
@Accessors(chain = true)
@ToString
@Setter
@Getter
@TableName("cfg_object_relation")
public class ObjectRelationPO extends BasePoEntity{

    @TableField("name")
    @ApiModelProperty(value = "关系名称", example = "文档任务关系")
    private String name;

    @TableField("tab_name")
    @ApiModelProperty(value = "tab名称", example = "文档关系")
    private String tabName;

    @TableField("description")
    @ApiModelProperty(value = "描述", example = "这是测试描述")
    private String description;

    @TableField("source_model_code")
    @ApiModelProperty(value = "源对象编码", example = "123")
    private String sourceModelCode;

    @TableField("target_model_code")
    @ApiModelProperty(value = "关联对象编码", example = "123")
    private String targetModelCode;

    @ApiModelProperty(value = "关系表model_code")
    @TableField("model_code")
    private String modelCode;


    @TableField("behavior")
    @ApiModelProperty(value = "关联行为", example = "固定：关联对象版本固定创建时的版本，浮动：关联对象版本一直用最新的")
    private String behavior;

    /*@TableField("app_code")
    @ApiModelProperty(value = "应用编码", example = "某个应用的项目的编码")
    private String appCode;*/

    /*@TableField("app_flag")
    @ApiModelProperty(value = "是否为应用", example = "确定是否为一个项目")
    private Boolean appFlag;*/

    @TableField("type")
    @ApiModelProperty(value = "关联类型", example = "仅创建：关联对象需要自己创建，仅选取：关联对象需要选择，两者皆可")
    private String type;

    @TableField("is_required")
    @ApiModelProperty(value = "关联项必填", example = "应用时 关联的对象实例是否必需")
    private Boolean required;

    @TableField("max_number")
    @ApiModelProperty(value = "允许的实例最大数量")
    private Integer maxNumber;

    @TableField("min_number")
    @ApiModelProperty(value = "允许的实例最小数量")
    private Integer minNumber;

    @TableField("hide_tab")
    @ApiModelProperty(value = "应用时隐藏tab", example = "true")
    private Boolean hideTab;

    @TableField("sort")
    @ApiModelProperty(value = "标签排序")
    private Integer sort;

    @TableField("float_behavior")
    @ApiModelProperty(value = "检出以浮动方式检出检入，只有在固定方式时可以配置", example = "true")
    private Boolean floatBehavior;
}
