package com.transcend.plm.configcenter.api.model.object.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 删除对象权限参数
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/12 16:50
 */
@Getter
@Setter
@ToString
@ApiModel(value = "删除对象权限参数")
public class ObjectAuthDeleteParam implements Serializable {

    @ApiModelProperty(value = "权限业务BID(关联权限的bid)", example = "234232423")
    @NotBlank(message = "权限BID不能为空！")
    private String bid;

    private static final long serialVersionUID = 1L;

}
