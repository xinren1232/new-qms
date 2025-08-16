package com.transcend.plm.configcenter.api.model.objectrelation.qo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author jie.yan
 * @date 2024/3/21
 * @description
 */

@Data
public class ObjectPathChainQO {

    /**
     *  方位 up、down
     */
    @ApiModelProperty(value = "方位 up、down")
    private String direction;

    /**
     * 模型code
     */
    @ApiModelProperty(value = "模型code")
    private String modelCode;
}
