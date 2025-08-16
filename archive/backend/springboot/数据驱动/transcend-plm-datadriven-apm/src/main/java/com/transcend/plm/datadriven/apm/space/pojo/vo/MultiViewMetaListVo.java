package com.transcend.plm.datadriven.apm.space.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 多对象元数据列表对象
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/4/23 11:55
 */
@Data
public class MultiViewMetaListVo {

    @ApiModelProperty("视图模式类型")
    private String viewModelType;
    @ApiModelProperty("对象模型代码列表")
    private List<String> modelCodeList;
    @ApiModelProperty("对象应用Bid列表")
    private List<String> spaceAppBidList;
    @ApiModelProperty("多对象元数据列表")
    private List<ViewMetaVo> viewMetaList;

}
