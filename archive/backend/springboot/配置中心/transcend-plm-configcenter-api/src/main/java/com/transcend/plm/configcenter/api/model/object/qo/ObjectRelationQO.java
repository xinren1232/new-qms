package com.transcend.plm.configcenter.api.model.object.qo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author chaonan.xu
 * @version: 1.0
 * @date 2021/8/30 15:18
 */
@ToString
@Setter
@Getter
@Accessors(chain = true)
public class ObjectRelationQO {
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

    @ApiModelProperty(value = "关联类型", example = "仅创建：关联对象需要自己创建，仅选取：关联对象需要选择，两者皆可")
    private String type;

    @ApiModelProperty(value = "关联项必填", example = "应用时 关联的对象实例是否必需")
    private Boolean required;

    @ApiModelProperty(value = "编码列表，便于批量查询")
    private List<String> bids;

    @ApiModelProperty(value = "源对象类型")
    private String sourceModel;

    @ApiModelProperty(value = "当前生命周期(提升用，目标生命周期)")
    private String lifeCode;

    @ApiModelProperty(value = "当前生命周期范围，如果为空默认查询启用状态,ALL，查询全部")
    private String scope;

    @ApiModelProperty(value = "旧生命周期编码")
    private String oldLifeCode;

    @ApiModelProperty(value = "生命周期模板ID")
    private String templateId;

    @ApiModelProperty(value = "生命周期模板版本")
    private String templateVersion;


    public static ObjectRelationQO of() {
        return new ObjectRelationQO();
    }
}
