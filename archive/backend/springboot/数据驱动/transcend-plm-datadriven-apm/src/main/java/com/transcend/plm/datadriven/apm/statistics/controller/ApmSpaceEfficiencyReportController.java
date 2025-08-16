package com.transcend.plm.datadriven.apm.statistics.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.datadriven.apm.statistics.model.AverageDeliveryChartParam;
import com.transcend.plm.datadriven.apm.statistics.model.BurnDownChartVO;
import com.transcend.plm.datadriven.apm.statistics.service.ApmSpaceEfficiencyReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author jinpeng.bai
 * @version v1.0.0
 * @description 研发能效报表控制器
 * @date 2023/10/25 15:55
 **/
@RestController
@Api(value = "Apm SpaceEfficiencyReportController", tags = "空间-研发能效报表-控制器")
@RequestMapping("/apm/space/efficiency/report")
public class ApmSpaceEfficiencyReportController {

    @Resource
    private ApmSpaceEfficiencyReportService apmSpaceEfficiencyReportService;

    @ApiOperation("燃尽图")
    @GetMapping("/burnDownChart/{statisticType}/{bid}")
    public TranscendApiResponse<BurnDownChartVO> burnDownChart(@PathVariable("statisticType") String statisticType, @PathVariable("bid") String bid) {
        return TranscendApiResponse.success(apmSpaceEfficiencyReportService.burnDownChart(statisticType, bid));
    }

    @ApiOperation("PI燃尽图")
    @GetMapping("/burnDownChart/{statisticType}/{bid}/PI")
    public TranscendApiResponse<BurnDownChartVO> burnDownChartPI(@PathVariable("statisticType") String statisticType, @PathVariable("bid") String bid) {
        return TranscendApiResponse.success(apmSpaceEfficiencyReportService.burnDownChartPI(statisticType, bid));
    }

    @ApiOperation("工作项分布")
    @GetMapping("/demandDistributeChart/{statisticRange}/{bid}")
    public TranscendApiResponse<Map<String,Object>> demandDistributeChart(@PathVariable("statisticRange") String statisticRange, @PathVariable("bid") String bid) {
        return TranscendApiResponse.success(apmSpaceEfficiencyReportService.demandDistributeChart(statisticRange, bid));
    }

    @ApiOperation("人员工作项分布")
    @GetMapping("/memberDemandDistributeChart/{statisticType}/{bid}")
    public TranscendApiResponse<Map<String,Object>> memberDemandDistributeChart(@PathVariable("statisticType") String statisticType, @PathVariable("bid") String bid) {
        return TranscendApiResponse.success(apmSpaceEfficiencyReportService.memberDemandDistributeChart(statisticType, bid));
    }


    @ApiOperation("需求平均交付周期")
    @PostMapping("/demandAverageDeliveryTimeChart/{statisticType}/{bid}")
    public TranscendApiResponse<Map<String,Object>> demandAverageDeliveryTimeChart(@PathVariable("statisticType") String statisticType,
                                                                                   @PathVariable("bid") String bid,
                                                                                   @RequestBody AverageDeliveryChartParam param) {
        return TranscendApiResponse.success(apmSpaceEfficiencyReportService.demandAverageDeliveryTimeChart(statisticType, bid, param));
    }

    @ApiOperation("需求平均开发周期")
    @GetMapping("/demandAverageDevelopTimeChart/{bid}")
    public TranscendApiResponse<Map<String,Object>> demandAverageDevelopTimeChart(@PathVariable("bid") String bid) {
        return TranscendApiResponse.success(apmSpaceEfficiencyReportService.demandAverageDevelopTimeChart(bid));
    }
    @ApiOperation("团队吞吐量")
    @GetMapping("/teamThroughputChart/{bid}")
    public TranscendApiResponse<Map<String,Object>> teamThroughputChart(@PathVariable("bid") String bid) {
        return TranscendApiResponse.success(apmSpaceEfficiencyReportService.teamThroughputChart(bid));
    }


    @ApiOperation("填写需求剩余工时")
    @PostMapping("/fillDemandResidualWorkingHours/{bid}/{residualHours}")
    public TranscendApiResponse<Boolean> fillDemandResidualWorkingHours(@PathVariable("bid") String bid, @PathVariable("residualHours") Integer residualHours) {
        return TranscendApiResponse.success(apmSpaceEfficiencyReportService.fillDemandResidualWorkingHours(bid, residualHours));
    }

    @ApiOperation("迭代任务改状态时构建燃尽图")
    @PostMapping("/buildBurnoutChart/{bid}/PI")
    public TranscendApiResponse<Boolean> buildBurnoutChar(@PathVariable("bid") String bid) {
        return TranscendApiResponse.success(apmSpaceEfficiencyReportService.buildBurnoutChart(bid));
    }

}
