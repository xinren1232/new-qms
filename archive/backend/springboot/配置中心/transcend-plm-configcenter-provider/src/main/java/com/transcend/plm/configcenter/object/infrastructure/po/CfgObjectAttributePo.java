package com.transcend.plm.configcenter.object.infrastructure.po;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.annotation.TableName;
import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 对象-属性PO
 *
 * @author yikai.lian
 * @version: 1.0
 * @date 2021/01/27 20:10
 */
@Setter
@Getter
@ToString
@Accessors(chain = true)
@TableName("cfg_object_attribute")
public class CfgObjectAttributePo extends BasePoEntity {

    public static CfgObjectAttributePo of() {
        return new CfgObjectAttributePo();
    }

    @ApiModelProperty(value = "模型code")
    private String modelCode;

    @ApiModelProperty(value = "modelCode+version的唯一id")
    private String modelVersionCode;

    @ApiModelProperty(value = "关联对象的业务ID")
    private String objBid;

    @ApiModelProperty(value = "关联对象的版本")
    private Integer objVersion;

    @ApiModelProperty(value = "属性类型")
    private String componentType;

    @ApiModelProperty(value = "数据类型")
    private String dataType;


    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "属性编码")
    private String code;

    @ApiModelProperty(value = "字段名称")
    private String name;

    @ApiModelProperty(value = "数据库的字段")
    private String dbKey;

    @ApiModelProperty(value = "是否自定义")
    private Boolean custom;

    @ApiModelProperty(value = "是否只读")
    private Boolean readonly;

    @ApiModelProperty(value = "是否索引")
    private Boolean index;

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

    @ApiModelProperty(value = "是否发布(0:否,1:是)")
    private Boolean published;

    private static final long serialVersionUID = 1L;

}
