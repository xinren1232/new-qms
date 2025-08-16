package com.transcend.plm.datadriven.api.model;

import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Data
@Builder(toBuilder = true)
public class RelationMObject {
    @ApiModelProperty("目标实例数据")
    private List<MObject> targetMObjects;
    @ApiModelProperty("目标实例dataBids，用于删除")
    private List<String> targetDataBids;
    @ApiModelProperty("源对象模型编码")
    private String sourceModelCode;

    @ApiModelProperty("目标模型编码")
    private String targetModelCode;

    @ApiModelProperty("源数据bid")
    private String sourceBid;

    @ApiModelProperty("源数据bid集合")
    private List<String> sourceBids;

    @ApiModelProperty("源数据dataBid")
    private String sourceDataBid;

    @ApiModelProperty("关系模型编码")
    private String relationModelCode;
    @ApiModelProperty("查询条件")
    private ModelMixQo modelMixQo;

    @ApiModelProperty("源关系模型编码")
    private String sourceRelationModelCode;

    @ApiModelProperty("目标关系模型编码")
    private String targetRelationModelCode;

    private List<String> targetBids;

    @ApiModelProperty("关系实例数据")
    private MObject relationMObject;

    /**
     * 反向查询
     */
    private Boolean inverseQuery;


    /**
     * 获取关系查询的目标数据模型编码
     *
     * @return 目标数据模型编码
     */
    public String getTargetModelCode() {
        return Boolean.TRUE.equals(inverseQuery) ? this.sourceModelCode : this.targetModelCode;
    }

    /**
     * 获取关系查询的源数据模型编码
     *
     * @return 源数据模型编码
     */
    public String getSourceModelCode() {
        return Boolean.TRUE.equals(inverseQuery) ? this.targetModelCode : this.sourceModelCode;
    }

    /**
     * 获取关系查询的目标数据Bid字段名
     *
     * @return 目标数据Bid字段名
     */
    public String targetBidFieldName() {
        return Boolean.TRUE.equals(inverseQuery) ?
                RelationEnum.SOURCE_BID.getCode() : RelationEnum.TARGET_BID.getCode();
    }

    /**
     * 获取关系查询的源数据Bid字段名
     *
     * @return 源数据Bid字段名
     */
    public String sourceBidFieldName() {
        return Boolean.TRUE.equals(inverseQuery) ?
                RelationEnum.TARGET_BID.getCode() : RelationEnum.SOURCE_BID.getCode();
    }

}
