package com.transcend.plm.datadriven.apm.permission.pojo.ao;

import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Author Qiu Yuhao
 * @Date 2023/11/15 14:10
 * @Describe
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class AddExpandAo extends RelationBaseParamAo {

    /**
     * 空间bid
     */
    private String spaceBid;

    /**
     * 空间app bid
     */
    private String spaceAppBid;

    private String sourceBid;

    private String sourceDataBid;

    /**
     * 关系ModelCode
     */
    private String relationModelCode;

    /**
     * 需要定制化处理的ModelCode
     */
    private String targetModelCode;

    /**
     * 实例数据
     */
    private MSpaceAppData mSpaceAppData;

    /**
     * 关系实例数据
     */
    private MSpaceAppData relationMObject;

    /**
     * 实例数据列表
     */
    private List<MSpaceAppData> targetMObjects;

    @ApiModelProperty("视图模式编码")
    private String viewModelCode;

    @ApiModelProperty("树形结构类型")
    private String treeType;

    /**
     * 是否是选择，false即为新增，true为选择
     */
    private Boolean selected;

    /**
     * 真实空间应用Bid
     */
    private String realSpaceAppBid;
 }
