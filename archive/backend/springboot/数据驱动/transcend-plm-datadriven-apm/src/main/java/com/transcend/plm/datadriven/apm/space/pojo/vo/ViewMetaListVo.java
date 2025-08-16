package com.transcend.plm.datadriven.apm.space.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 视图元数据列表对象
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/6/24 10:58
 */
@Data
public class ViewMetaListVo implements Serializable {

    @ApiModelProperty("视图模式类型")
    private String viewModelType;

    @ApiModelProperty("空间应用bid")
    private String spaceAppBid;

    @ApiModelProperty("空间应用名称")
    private String spaceAppName;

    @ApiModelProperty("对象模型代码")
    private String modelCode;

    @ApiModelProperty("多对象元数据列表")
    private List<ViewMetaVo> viewMetaList;

}
