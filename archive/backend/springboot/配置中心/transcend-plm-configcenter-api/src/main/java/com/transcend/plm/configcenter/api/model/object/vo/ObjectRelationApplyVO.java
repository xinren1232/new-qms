package com.transcend.plm.configcenter.api.model.object.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author chaonan.xu
 * @version: 1.0
 * @date 2021/11/19 9:59
 */
@Accessors(chain = true)
@ToString
@Setter
@Getter
public class ObjectRelationApplyVO {

    @ApiModelProperty(value = "源对象的根节点编码", example = "part")
    private String sourceModel;

    @ApiModelProperty(value = "关联对象的根节点编码", example = "part")
    private String targetModel;

    @ApiModelProperty(value = "关联对象编码", example = "123")
    private String targetObjBid;

    @ApiModelProperty(value = "关联对象modelCode", example = "A01")
    private String targetModelCode;

    @ApiModelProperty(value = "关联对象名", example = "研发文档")
    private String targetObjName;

    @ApiModelProperty(value = "关联对象版本", example = "V1.0")
    private String targetObjVersion;

    @ApiModelProperty(value = "关联对象属性")
    private List<ObjectAttrVO> targetObjAttrList;

    @ApiModelProperty(value = "关系对象编码", example = "123")
    private String relationObjBid;

    @ApiModelProperty(value = "关系对象名", example = "部件-部件关系", notes = "和关联对象的区别是：关联对象关系中的目标对象，关系对象是关联关系生成的对象")
    private String relationObjName;

    @ApiModelProperty(value = "关系对象版本", example = "V1.0")
    private String relationObjVersion;

    @ApiModelProperty(value = "关系对象属性")
    private List<ObjectAttrVO> relationObjAttrList;

    @ApiModelProperty(value = "关联行为")
    private String behavior;

    @ApiModelProperty(value = "关联类型")
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

    @ApiModelProperty(value = "排序顺序")
    private Integer sort;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updatedTime;

    @ApiModelProperty(value = "tab名称")
    private String tabName;

    public static ObjectRelationApplyVO of() {
        return new ObjectRelationApplyVO();
    }

}
