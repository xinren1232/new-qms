package com.transcend.plm.datadriven.api.model.relation.vo;

import com.transcend.plm.datadriven.api.model.MRelationObject;
import com.transcend.plm.datadriven.api.model.MVersionObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 关系与目标数据并集
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/4/24 17:44
 * @since 1.0
 */
@Data
public class RelationAndTargetVo extends MVersionObject {

    @ApiModelProperty("关系数据")
    private MRelationObject relation;

}
