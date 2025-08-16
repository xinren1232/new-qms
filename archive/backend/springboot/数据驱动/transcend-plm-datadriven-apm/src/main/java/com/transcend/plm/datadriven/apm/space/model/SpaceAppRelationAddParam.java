package com.transcend.plm.datadriven.apm.space.model;

import com.transcend.plm.datadriven.api.model.MBaseData;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;

/**
 * 空间应用关系添加参数
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/10/13 16:58
 * @since 1.0
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@Builder(toBuilder = true)
public class SpaceAppRelationAddParam extends MBaseData {

    @ApiModelProperty("空间bid")
    private String spaceBid;

    @ApiModelProperty("空间应用bid")
    private String spaceAppBid;

    @ApiModelProperty("关系实例数据")
    private MSpaceAppData relationMObject;

    @ApiModelProperty("目标实例数据")
    private List<MSpaceAppData> targetMObjects;
    @ApiModelProperty("源对象模型编码")
    private String sourceModelCode;

    @ApiModelProperty("目标模型编码")
    private String targetModelCode;

    @ApiModelProperty("源数据bid")
    private String sourceBid;

    @ApiModelProperty("源数据dataBid")
    private String sourceDataBid;

    @ApiModelProperty("关系模型编码")
    private String relationModelCode;

    @ApiModelProperty("视图模式编码")
    private String viewModelCode;

    @ApiModelProperty("树形结构类型")
    private String treeType;

    @ApiModelProperty("源数据空间应用bid")
    private String sourceSpaceAppBid;

    /**
     * 是否是选择，false即为新增，true为选择
     */
    private Boolean selected;
}
