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
 * 对象undoCheckout参数
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/11/24 11:41
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@ApiModel(value = "对象undoCheckout参数")
public class UndoCheckoutParam implements Serializable {

    public static UndoCheckoutParam of() {
        return new UndoCheckoutParam();
    }

    @NotBlank(message = "模型code不能为空")
    @ApiModelProperty(value = "模型code")
    private String modelCode;

    private static final long serialVersionUID = 1L;
}
