package com.transcend.plm.datadriven.api.model.relation.add;

import com.transcend.plm.datadriven.api.model.MVersionObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 关系与目标数据并集
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/4/24 17:44
 */
@Data
public class RelationAndTargetAdd {

    @ApiModelProperty("关系数据")
    private MVersionObject relationData;

    @ApiModelProperty("目标数据")
    private MVersionObject targetData;

}
