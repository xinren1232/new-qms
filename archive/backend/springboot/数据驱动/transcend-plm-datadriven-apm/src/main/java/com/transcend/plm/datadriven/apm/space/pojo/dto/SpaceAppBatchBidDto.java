package com.transcend.plm.datadriven.apm.space.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 对象模型
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/4/24 17:44
 * @since 1.0
 */
@ApiModel("空间应用对象实例模型")
@Data
public class SpaceAppBatchBidDto {

    @ApiModelProperty("空间bid")
    private String spaceBid;

    @ApiModelProperty("空间应用bid")
    private String spaceAppBid;

    @ApiModelProperty("空间应用bid")
    private String bids;

}
