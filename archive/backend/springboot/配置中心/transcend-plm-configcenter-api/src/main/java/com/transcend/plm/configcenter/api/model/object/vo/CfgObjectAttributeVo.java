package com.transcend.plm.configcenter.api.model.object.vo;

import com.transcend.plm.configcenter.api.model.base.BaseDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 对象属性VO
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/05 19:07
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@ApiModel(value = "对象属性VO")
public class CfgObjectAttributeVo  extends BaseDto {

    public static CfgObjectAttributeVo of() {
        return new CfgObjectAttributeVo();
    }

    @ApiModelProperty(value = "模型code")
    private String modelCode;

    @ApiModelProperty(value = "modelCode+version的唯一id")
    private String modelVersionCode;

    @ApiModelProperty(value = "关联对象的业务ID")
    private String objBid;

    @ApiModelProperty(value = "关联对象的版本")
    private Integer objVersion;

    @ApiModelProperty(value = "数据类型")
    private String dataType;

    @ApiModelProperty(value = "属性类型")
    private String componentType;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "内部名称")
    private String code;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "数据库的字段， 此字段只给实例驱动，不给前端看到 TODO ")
    private String dbKey;

    @ApiModelProperty(value = "是否索引")
    private Boolean index;

    @ApiModelProperty(value = "是否继承")
    private Boolean inherit;

    @ApiModelProperty(value = "是否自定义")
    private Boolean custom;

    @ApiModelProperty(value = "是否只读")
    private Boolean readonly;

    @ApiModelProperty(value = "是否必填（默认：0）")
    private Boolean required;

    @ApiModelProperty(value = "说明")
    private String description;

    @ApiModelProperty(value = "属性约束")
    private String constraint;

    @ApiModelProperty(value = "默认值")
    private String defaultValue;

    @ApiModelProperty(value = "是否可见(默认：1 可见)")
    private Boolean visible;

    @ApiModelProperty(value = "布局(前端要用的布局字段) 详细保存格式和前端沟通")
    private String layout;

    @ApiModelProperty(value = "是否为基础属性(0:否,1:是)")
    private Boolean baseAttr;

    @ApiModelProperty(value = "是否已发布(0:否,1:是)")
    private Boolean published;

    private static final long serialVersionUID = 1L;

}
