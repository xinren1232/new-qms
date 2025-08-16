package com.transcend.plm.datadriven.api.model.relation.add;

import com.transcend.plm.datadriven.api.model.MRelationObject;
import com.transcend.plm.datadriven.api.model.MVersionObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 对象关系
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/4/24 17:44
 */
@Data
public class RelationAdd {

    @ApiModelProperty("关系配置BID")
    private String modelCode;

    @ApiModelProperty("源数据")
    private MVersionObject sourceData;

    @ApiModelProperty("关系数据")
    private MRelationObject relationData;

    @ApiModelProperty("目标数据")
    private MVersionObject targetData;

}
