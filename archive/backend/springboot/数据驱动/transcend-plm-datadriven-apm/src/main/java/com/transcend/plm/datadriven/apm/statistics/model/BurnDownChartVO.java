package com.transcend.plm.datadriven.apm.statistics.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author jinpeng.bai
 * @version v1.0.0
 * @description 燃尽图返回参数
 * @date 2023/10/25 16:09
 **/
@Data
@Builder
public class BurnDownChartVO {
    @ApiModelProperty(value = "统计类型", example = "")
    private List<String> statisticsDate;

    @ApiModelProperty(value = "统计数据", example = "")
    List<Map<String,Object>> series;
}
