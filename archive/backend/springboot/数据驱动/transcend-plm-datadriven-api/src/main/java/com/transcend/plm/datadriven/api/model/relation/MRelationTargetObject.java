package com.transcend.plm.datadriven.api.model.relation;

import com.transcend.plm.datadriven.api.model.MRelationObject;
import com.transcend.plm.datadriven.api.model.MObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 关系+目标对象
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/4/24 17:44
 * @since 1.0
 */
@Data
public class MRelationTargetObject extends MRelationObject {

    @ApiModelProperty("目标数据")
    private MObject targetData;


}
