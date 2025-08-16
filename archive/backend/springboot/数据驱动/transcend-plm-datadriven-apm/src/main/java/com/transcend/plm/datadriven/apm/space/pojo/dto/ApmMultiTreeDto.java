package com.transcend.plm.datadriven.apm.space.pojo.dto;

import com.transcend.plm.datadriven.api.model.MObjectTree;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmMultiTreeModelMixQo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author unknown
 */
@Data
public class ApmMultiTreeDto {
    /**
     * 空间bid
     */
    private String spaceBid;

    /**
     * 空间应用业务id
     */
    private String spaceAppBid;

    /**
     * 目标空间应用业务id
     */
    private String targetSpaceAppBid;

    /**
     * 关系对象modelCode
     */
    private String relationModelCode;

    /**
     * tab名称
     */
    private String name;

    /**
     * 过滤条件
     */
    private ApmMultiTreeModelMixQo modelMixQo;

    /**
     * 源实例bid
     */
    private String sourceBid;

    /**
     * 是否过滤未选中的数据
     */
    private boolean filterUnchecked;
    @ApiModelProperty(value = "树")
    List<MObjectTree> tree;
    @ApiModelProperty(value = "分组字段")
    private String groupProperty;
    @ApiModelProperty(value = "分组字段值")
    private String groupPropertyValue;

    private MultiAppConfig multiAppTreeContent;

}
