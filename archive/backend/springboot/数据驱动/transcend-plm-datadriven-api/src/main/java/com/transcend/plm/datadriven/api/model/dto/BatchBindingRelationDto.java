package com.transcend.plm.datadriven.api.model.dto;

import com.transcend.plm.datadriven.api.model.MRelationObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 批量绑定入参
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/4/24 17:44
 */
@Data
public class BatchBindingRelationDto {

    @ApiModelProperty("关系配置BID")
    private String modelCode;

    @ApiModelProperty("源BID")
    private String sourceBid;

    @ApiModelProperty("源数据BID")
    private String sourceDataBid;

    @ApiModelProperty("关系数据集合")
    private List<MRelationObject> relationDataList;

}
