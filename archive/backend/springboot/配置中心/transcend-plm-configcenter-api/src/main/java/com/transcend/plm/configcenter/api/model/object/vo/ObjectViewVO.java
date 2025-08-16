package com.transcend.plm.configcenter.api.model.object.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 对象视图VO
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/12 11:52
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@ApiModel(value = "对象视图VO")
public class ObjectViewVO implements Serializable {

    public static ObjectViewVO of() {
        return new ObjectViewVO();
    }


    @ApiModelProperty(value = "视图名称", example = "视图名称")
    private String name;

    @ApiModelProperty(value = "描述", example = "描述")
    private String description;

    @ApiModelProperty(value = "角色编码", example = "ALL")
    private String roleBid;

    @ApiModelProperty(value = "关联对象bid", example = "00000002")
    private String relativeObjBid;

    @ApiModelProperty(value = "模型code")
    private String modelCode;

    @ApiModelProperty(value = "优先级", example = "1")
    private Integer priority;

    @ApiModelProperty(value = "标签", example = "template")
    private List<String> tags;

    private static final long serialVersionUID = 1L;

}
