package com.transcend.plm.configcenter.api.model.object.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author yinbin
 * @version:
 * @date 2023/01/07 11:46
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@ApiModel(value = "对象历史getOne参数")
public class ObjectHistoryGetOneParam {
    public static ObjectHistoryGetOneParam of() {
        return new ObjectHistoryGetOneParam();
    }

    @NotBlank(message = "模型code不能为空")
    @ApiModelProperty(value = "模型code")
    private String modelCode;

    @NotNull(message = "版本不能为空")
    @ApiModelProperty(value = "版本")
    private Integer version;

    private static final long serialVersionUID = 1L;
}
