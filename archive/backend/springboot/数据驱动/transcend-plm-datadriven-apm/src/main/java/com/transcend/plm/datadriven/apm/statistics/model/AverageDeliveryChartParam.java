package com.transcend.plm.datadriven.apm.statistics.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author jinpeng.bai
 * @version v1.0.0
 * @description 团队需求平均交付周期入参
 * @date 2023/10/30 16:07
 **/
@Data
public class AverageDeliveryChartParam {
    @ApiModelProperty(value = "开始时间")
    private Date startDate;
    @ApiModelProperty(value = "结束时间")
    private Date endDate;
    @ApiModelProperty(value = "空间bid")
    private String spaceBid;
}
