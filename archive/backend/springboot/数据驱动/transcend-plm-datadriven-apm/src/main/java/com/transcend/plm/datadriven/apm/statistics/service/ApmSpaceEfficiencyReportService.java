package com.transcend.plm.datadriven.apm.statistics.service;

import com.transcend.plm.datadriven.apm.statistics.model.AverageDeliveryChartParam;
import com.transcend.plm.datadriven.apm.statistics.model.BurnDownChartVO;

import java.util.Map;

/**
 * @author jinpeng.bai
 * @version v1.0.0
 * @description 研发能效服务接口
 * @date 2023/10/25 16:37
 **/
public interface ApmSpaceEfficiencyReportService {


    /**
     * 燃尽图
     *
     * @param statisticType 统计类型
     * @param bid           业务id
     * @return BurnDownChartVO
     */
    BurnDownChartVO burnDownChart(String statisticType, String bid);

    /**
     * 需求分布图
     *
     * @param statisticRange 统计维度
     * @param bid            业务id
     * @return Map<String, Object>
     */
    Map<String, Object> demandDistributeChart(String statisticRange, String bid);

    /**
     * 成员需求分布图
     *
     * @param statisticRange 统计维度
     * @param bid            业务id
     * @return Map<String, Object>
     */
    Map<String, Object> memberDemandDistributeChart(String statisticRange, String bid);

    /**
     * 需求平均交付时间图
     *
     * @param statisticRange statisticRange
     * @param bid            bid
     * @param param          param
     * @return Map<String, Object>
     */
    Map<String, Object> demandAverageDeliveryTimeChart(String statisticRange, String bid, AverageDeliveryChartParam param);

    /**
     * 需求平均开发时间图
     *
     * @param bid bid
     * @return Map<String, Object>
     */
    Map<String, Object> demandAverageDevelopTimeChart(String bid);

    /**
     * 团队吞吐量
     *
     * @param bid 项目bid
     * @return Map<String, Object>
     */
    Map<String, Object> teamThroughputChart(String bid);


    /**
     * 产品需求填写剩余工时
     *
     * @param bid           bid
     * @param residualHours residualHours
     * @return Boolean
     */
    Boolean fillDemandResidualWorkingHours(String bid, Integer residualHours);

    /**
     * PI燃尽图
     *
     * @param statisticType statisticType
     * @param bid           bid
     * @return BurnDownChartVO
     */
    BurnDownChartVO burnDownChartPI(String statisticType, String bid);

    /**
     * 迭代任务改状态时构建燃尽图
     *
     * @param bid bid
     * @return Boolean
     */
    Boolean buildBurnoutChart(String bid);
}
