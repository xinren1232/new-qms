package com.transcend.plm.configcenter.api.model.object.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Map;

/**
 * 新增对象参数
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/11/24 11:42
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@ApiModel(value = "新增对象参数")
public class ObjectAddParam implements Serializable {

    public static ObjectAddParam of() {
        return new ObjectAddParam();
    }

    @ApiModelProperty(value = "业务ID(编码)")
    private String bid;

    @NotBlank(message = "对象名称不能为空")
    @ApiModelProperty(value = "对象名称")
    private String name;

    @ApiModelProperty(value = "父的modelCode")
    private String parentCode;

    @ApiModelProperty(value = "根模型")
    private String baseModel;

    @ApiModelProperty(value = "排序号")
    private Integer sort;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "多语言字典")
    private Map<String, String> langDict;

    @ApiModelProperty(value = "类型")
    private String type;

    private Integer enableFlag;

    private static final long serialVersionUID = 1L;

}
