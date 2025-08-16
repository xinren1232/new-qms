package com.transcend.plm.datadriven.api.model.relation;

import com.transcend.plm.datadriven.api.model.MObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 源对象与目标对象列表打包参数
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/4/24 17:44
 * @since 1.0
 */
@Data
public class MSourceAndTargetRelation {

    @ApiModelProperty("源对象")
    private MObject sourceData;

    @ApiModelProperty("关系")
    private List<MRelationTargetObject> targetObjects;

}
