package com.transcend.plm.datadriven.api.model.relation.qo;

import com.transcend.plm.datadriven.api.model.qo.ModelFilterQo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 关系路径实体类
 *
 * @author jie.yan
 * @date 2024/3/21
 */
@Data
public class CrossRelationPathQO {

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

    /**
     * 查询条件
     */
    @ApiModelProperty(value = "查询条件")
    private List<ModelFilterQo> queries;
}
