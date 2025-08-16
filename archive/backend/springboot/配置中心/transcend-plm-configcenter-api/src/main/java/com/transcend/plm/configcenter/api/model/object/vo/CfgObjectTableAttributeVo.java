package com.transcend.plm.configcenter.api.model.object.vo;

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
@ApiModel(value = "对象表发布属性VO")
public class CfgObjectTableAttributeVo extends CfgObjectAttributeVo {

    public static CfgObjectTableAttributeVo of() {
        return new CfgObjectTableAttributeVo();
    }

    @ApiModelProperty(value = "是否发布")
    private Boolean published;


    private static final long serialVersionUID = 1L;

}
