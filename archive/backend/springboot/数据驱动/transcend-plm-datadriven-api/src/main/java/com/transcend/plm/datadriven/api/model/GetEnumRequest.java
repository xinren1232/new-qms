package com.transcend.plm.datadriven.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author wwx
 */
@Data
@ApiModel
public class GetEnumRequest implements Serializable {
    /**
     *枚举类型
     */
    @ApiModelProperty("枚举类型")
    private String enumKey;
}
