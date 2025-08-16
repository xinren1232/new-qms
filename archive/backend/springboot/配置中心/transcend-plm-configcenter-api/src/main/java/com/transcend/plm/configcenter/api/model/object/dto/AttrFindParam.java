package com.transcend.plm.configcenter.api.model.object.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 属性查询参数
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/15 14:40
 */
@Getter
@Setter
@ToString
@ApiModel(value = "属性查询参数")
@Accessors(chain = true)
public class AttrFindParam implements Serializable {

    public static AttrFindParam of() {
        return new AttrFindParam();
    }

    @NotBlank(message = "对象bid不能为空")
    @ApiModelProperty(value = "对象bid")
    private String objectBid;

    @NotBlank(message = "属性bid不能为空")
    @ApiModelProperty(value = "属性bid")
    private String bid;

}
