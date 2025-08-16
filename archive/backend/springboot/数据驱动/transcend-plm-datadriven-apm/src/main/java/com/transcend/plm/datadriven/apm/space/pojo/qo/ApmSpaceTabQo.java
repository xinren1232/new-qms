package com.transcend.plm.datadriven.apm.space.pojo.qo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author jinpeng.bai
 * @version v1.0.0
 * @description 应用配置关系tab查询
 * @date 2024/06/28 11:26
 **/
@Accessors(chain = true)
@Getter
@Setter
public class ApmSpaceTabQo implements Serializable {

    @ApiModelProperty(value = "实例Bid")
    private String instanceBid;

    @ApiModelProperty(value = "空间bid")
    private String spaceBid;

    @ApiModelProperty(value = "应用bid")
    private String spaceAppBid;

    private String modelCode;
}
