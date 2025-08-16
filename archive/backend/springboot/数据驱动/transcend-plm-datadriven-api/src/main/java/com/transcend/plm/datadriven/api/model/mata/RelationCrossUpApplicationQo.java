package com.transcend.plm.datadriven.api.model.mata;

import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transsion.framework.dto.BaseRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 自底向上关系跨层级查询Qo
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/4/24 17:44
 */
@Data
@Accessors(chain = true)
public class RelationCrossUpApplicationQo {


    @ApiModelProperty("当前的源对象实例bid")
    private String currentSourceBid;

    @ApiModelProperty("当前所在的关系对象模型编码")
    private String currentRelationModelCode;

    @ApiModelProperty("目的地的源对象模型编码")
    private String destinationSourceModelCode;

    @ApiModelProperty("跨层级关系对象模型编码,以#分割")
    private String crossRelationModelCodes;

    @ApiModelProperty("标准查询条件")
    private ModelMixQo modelMixQo;

    @ApiModelProperty("标准查询条件")
    private BaseRequest<ModelMixQo> pageQo;

    /**
     * @return {@link RelationCrossUpApplicationQo }
     */
    public static RelationCrossUpApplicationQo of() {
        return new RelationCrossUpApplicationQo();
    }
}
