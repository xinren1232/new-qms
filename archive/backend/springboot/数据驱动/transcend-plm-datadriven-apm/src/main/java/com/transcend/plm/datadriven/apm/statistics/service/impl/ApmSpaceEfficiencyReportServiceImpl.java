package com.transcend.plm.datadriven.apm.statistics.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.AtomicDouble;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.configcenter.api.feign.DictionaryFeignClient;
import com.transcend.plm.configcenter.api.model.dictionary.qo.CfgDictionaryComplexQo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryItemVo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryVo;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.apm.powerjob.notify.BurnoutDataCalc;
import com.transcend.plm.datadriven.apm.space.model.SpaceAppDataEnum;
import com.transcend.plm.datadriven.apm.statistics.constants.EfficiencyReportConstant;
import com.transcend.plm.datadriven.apm.statistics.model.AverageDeliveryChartParam;
import com.transcend.plm.datadriven.apm.statistics.model.BurnDownChartVO;
import com.transcend.plm.datadriven.apm.statistics.repository.mapper.ApmSpaceEfficiencyReportMapper;
import com.transcend.plm.datadriven.apm.statistics.repository.po.ApmStatisticRecord;
import com.transcend.plm.datadriven.apm.statistics.repository.service.ApmStatisticRecordService;
import com.transcend.plm.datadriven.apm.statistics.service.ApmSpaceEfficiencyReportService;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.common.tool.CommonConstant;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transsion.framework.uac.model.dto.UserDTO;
import com.transsion.framework.uac.service.IUacUserService;
import org.apache.commons.lang.time.DateUtils;
import org.assertj.core.util.Lists;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.transcend.plm.datadriven.apm.constants.ModelCodeProperties.*;
import static com.transcend.plm.datadriven.apm.statistics.constants.EfficiencyReportConstant.*;
import static com.transcend.plm.datadriven.apm.statistics.constants.EfficiencyReportConstant.DemandDistributionChartConstant.*;
import static com.transcend.plm.datadriven.apm.statistics.constants.EfficiencyReportConstant.MemberDemandDistributionChartConstant.*;
import static com.transcend.plm.datadriven.apm.statistics.constants.EfficiencyReportConstant.PlmBurnoutChartConstant.CLOSE_LIFE_CYCLE_CODE;
import static com.transcend.plm.datadriven.apm.statistics.constants.EfficiencyReportConstant.RevisionBurnDownConstant.*;

/**
 * @author jinpeng.bai
 * @version v1.0.0
 * @description 研发能效服务接口
 * @date 2023/10/25 16:37
 **/
@Service
public class ApmSpaceEfficiencyReportServiceImpl implements ApmSpaceEfficiencyReportService {

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;
    @Resource
    private ApmSpaceEfficiencyReportMapper apmSpaceEfficiencyReportMapper;
    @Resource
    private ApmStatisticRecordService apmStatisticRecordService;
    @Resource
    private IUacUserService uacUserService;

    @Resource
    private DictionaryFeignClient dictionaryFeignClient;

    @Resource
    private BurnoutDataCalc burnoutDataCalc;

    @Override
    public BurnDownChartVO burnDownChart(String statisticType, String bid) {
        BurnDownChartVO burnDownChartVO = BurnDownChartVO.builder().build();
        RelationMObject relationMObject = RelationMObject.builder()
                .sourceModelCode(REVISION_MODEL_CODE).sourceBid(bid).relationModelCode(REVISION_DEMAND_REL_MODEL_CODE)
                .targetModelCode(DEMAND_MODEL_CODE).build();
        List<MObject> dataList = objectModelCrudI.listRelationMObjects(relationMObject);
        if(CollectionUtils.isEmpty(dataList)){
            return burnDownChartVO;
        }
        burnDownChartVO.setStatisticsDate(initRevisionDate(bid,false));
        if(BURN_DOWN_STATISTIC_TYPE_WORK_HOURS.equals(statisticType)){
            initBurnDownWorkHoursChats(burnDownChartVO, dataList);
        }
        if(EfficiencyReportConstant.BURN_DOWN_STATISTIC_TYPE_COUNT.equals(statisticType)){
            initBurnDownCountChats(burnDownChartVO, dataList);
        }
        return burnDownChartVO;
    }

    @Override
    public Map<String,Object> demandDistributeChart(String statisticRange, String bid) {
        Map<String,Object> chartData = Maps.newHashMap();
        List<MObject> chartDataList = Lists.newArrayList();
        //按照项目得维度查询关联的用户需求和任务数据
        if(STATISTIC_RANGE_PROJECT.equals(statisticRange)){
            chartDataList.addAll(querySourceTargetList(PROJECT_MODEL_CODE, bid, PROJECT_DEMAND_REL_MODEL_CODE, DEMAND_MODEL_CODE));
            chartDataList.addAll(querySourceTargetList(PROJECT_MODEL_CODE, bid, PROJECT_TASK_REL_MODEL_CODE, TASK_MODEL_CODE));
        }
        //按照迭代得维度查询关联的用户需求和任务数据
        if(STATISTIC_RANGE_REVISION.equals(statisticRange)) {
            chartDataList.addAll(querySourceTargetList(REVISION_MODEL_CODE, bid, REVISION_DEMAND_REL_MODEL_CODE, DEMAND_MODEL_CODE));
            chartDataList.addAll(querySourceTargetList(REVISION_MODEL_CODE, bid, REVISION_TASK_REL_MODEL_CODE, TASK_MODEL_CODE));
        }
        //定义属性结构横坐标返回结构
        LinkedList<String> xAxis = new LinkedList<>();
        HashMap<String,LinkedList<Integer>> sortTitleMap = new HashMap<>(16);
        if(CollectionUtils.isNotEmpty(chartDataList)){
            //按照横坐标分组
            Map<String,List<MObject>> demandTypeMap = chartDataList.stream().collect(Collectors.groupingBy(e->MODEL_TYPE_MAP.getOrDefault(e.getModelCode(),"未知")));
            //分组数据
            buildStatusMap(demandTypeMap, xAxis, sortTitleMap);
        }
        chartData.put(RETURN_TITLE,xAxis);
        chartData.put("data",sortTitleMap);
        return chartData;
    }

    @Override
    public Map<String,Object> memberDemandDistributeChart(String statisticRange, String bid) {
        Map<String,Object> memberDistributeChart = Maps.newHashMap();
        List<MObject> chartDataList = Lists.newArrayList();
        //按照项目得维度查询关联的用户需求和任务数据
        if(STATISTIC_RANGE_PROJECT.equals(statisticRange)){
            chartDataList.addAll(querySourceTargetList(PROJECT_MODEL_CODE, bid, PROJECT_DEMAND_REL_MODEL_CODE, DEMAND_MODEL_CODE));
            chartDataList.addAll(querySourceTargetList(PROJECT_MODEL_CODE, bid, PROJECT_TASK_REL_MODEL_CODE, TASK_MODEL_CODE));
        }
        //按照迭代得维度查询关联的用户需求和任务数据
        if(STATISTIC_RANGE_REVISION.equals(statisticRange)) {
            chartDataList.addAll(querySourceTargetList(REVISION_MODEL_CODE, bid, REVISION_DEMAND_REL_MODEL_CODE, DEMAND_MODEL_CODE));
            chartDataList.addAll(querySourceTargetList(REVISION_MODEL_CODE, bid, REVISION_TASK_REL_MODEL_CODE, TASK_MODEL_CODE));
        }
        if(CollectionUtils.isNotEmpty(chartDataList)){
            //转换生命周期编码
            chartDataList.stream().forEach(e->e.put(LIFE_CODE_NAME,Optional.ofNullable(EfficiencyReportConstant.REPORT_LIFE_CODE_NAME.get(e.getLifeCycleCode())).orElse("未开始")));
            memberDistributeChart.put("person",personDistributeChartBuild(chartDataList));
            memberDistributeChart.put("modelPerson",modelPersonDistributeChartBuild(chartDataList));
        }

        return memberDistributeChart;
    }

    @Override
    public Map<String,Object> demandAverageDeliveryTimeChart(String statisticRange, String bid, AverageDeliveryChartParam param) {
        Map<String,Object> memberDistributeChart = Maps.newHashMap();
        List<MObject> chartDataList = Lists.newArrayList();
        List<Map<String,Object>> averageDeliveryTimeList = Lists.newArrayList();
        List<String> revisionDateRange = Lists.newArrayList();
        //按照项目得维度查询关联的用户需求和任务数据
        if(STATISTIC_RANGE_DEMAND_MANAGER.equals(statisticRange)){
            if(StringUtils.isBlank(param.getSpaceBid())){
                throw new PlmBizException("空间标识不能为空");
            }
            if(param.getEndDate()!=null && param.getStartDate()!=null){
                revisionDateRange = getDatesBetween(param.getStartDate(),param.getEndDate());
            }else {
                Date now = new Date();
                Date dataStart = DateUtils.addDays(now,-14);
                revisionDateRange = getDatesBetween(dataStart,now);
            }
            //统计用户需求三条线，史诗，特性，用户故事
            chartDataList.addAll(objectModelCrudI.list( DEMAND_MODEL_CODE,buildQueryWrapperSpaceBid(param.getSpaceBid(),DEMAND_STORY_MODEL_CODE)));
            chartDataList.forEach(e->{
                if(DEMAND_STORY_MODEL_CODE.equals(e.getModelCode())){
                    e.put(CALCULATE_START_TIME,e.get(SCHEDULE_START_TIME));
                }else {
                    e.put(CALCULATE_START_TIME,e.get(DEVELOPMENT_START_TIME));
                }
            });
            averageDeliveryTimeList = calculateAverageDeliveryTime(revisionDateRange,chartDataList, ImmutableList.of(TASK_MODEL_CODE,DEMAND_STORY_MODEL_CODE));
        }
        //按照迭代得维度查询关联的用户需求和任务数据
        if(STATISTIC_RANGE_REVISION.equals(statisticRange)) {
            revisionDateRange = initRevisionDate(bid,true);
            //统计用户需求和任务数据2条线
            chartDataList.addAll(querySourceTargetList(REVISION_MODEL_CODE, bid, REVISION_DEMAND_REL_MODEL_CODE, DEMAND_MODEL_CODE));
            chartDataList.forEach(e->e.put(CALCULATE_START_TIME,e.get(SCHEDULE_START_TIME)));
            List<MObject> taskList =  querySourceTargetList(REVISION_MODEL_CODE, bid, REVISION_TASK_REL_MODEL_CODE, TASK_MODEL_CODE);
            taskList.forEach(e->{
                e.put(ACTUAL_COMPLETE_TIME,e.get(END_TIME));
                e.put(CALCULATE_START_TIME,e.get(START_TIME));
            });
            chartDataList.addAll(taskList);
            averageDeliveryTimeList = calculateAverageDeliveryTime(revisionDateRange,chartDataList, ImmutableList.of(TASK_MODEL_CODE,DEMAND_STORY_MODEL_CODE));
        }

        memberDistributeChart.put(RETURN_TITLE,revisionDateRange);
        memberDistributeChart.put("data",averageDeliveryTimeList);
        return memberDistributeChart;
    }

  /**
   * 根据对象类型分组计算平均时间
   * @Description calculateAverageDeliveryTime
   * @Author jinpeng.bai
   * @Date 2023/10/30 14:54
   * @Param [revisionDateRange, chartDataList, modelCodes] 统计x轴时间范围，统计数据，统计对象类型
   * @Return java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
   * @Since version 1.0
   */

  private List<Map<String,Object>> calculateAverageDeliveryTime(List<String> revisionDateRange,List<MObject> chartDataList, List<String> modelCodes) {
        Map<String, List<MObject>> modelTypeMap = chartDataList.stream().filter(e -> modelCodes.contains(e.getModelCode()) && e.get(ACTUAL_COMPLETE_TIME)!=null).collect(Collectors.groupingBy(MObject::getModelCode));
        List<Map<String,Object>> chartData = Lists.newArrayList();
        modelTypeMap.forEach((k,v)->{
            Map<String,Object> modelDetailMap = Maps.newHashMap();
            modelDetailMap.put("name",MODEL_TYPE_MAP.get(k));
            LinkedList<BigDecimal> everyDeliveryTime = new LinkedList<>();
            revisionDateRange.forEach(e->{
                LocalDateTime actualDate = LocalDateTime.of(LocalDate.parse(e), LocalTime.MAX);
                List<BigDecimal> publishList = v.stream().filter(mObject -> actualDate.isAfter((LocalDateTime) mObject.get(ACTUAL_COMPLETE_TIME))).map(mObject -> BigDecimal.valueOf(((LocalDateTime) mObject.get(CALCULATE_START_TIME)).until((LocalDateTime) mObject.get(ACTUAL_COMPLETE_TIME), ChronoUnit.HOURS))).collect(Collectors.toList());
                everyDeliveryTime.add(CollectionUtils.isEmpty(publishList)?BigDecimal.ZERO:publishList.stream().reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(publishList.size()),0,BigDecimal.ROUND_HALF_UP));
            });
            modelDetailMap.put("data",everyDeliveryTime);
            chartData.add(modelDetailMap);
        });
        return chartData;
    }

    @Override
    public Map<String,Object> demandAverageDevelopTimeChart(String bid) {
        Map<String,Object> memberDistributeChart = Maps.newHashMap();
        List<MObject> chartDataList = Lists.newArrayList();
        List<String> revisionDateRange = initRevisionDate(bid,true);
        //统计用户需求和任务数据2条线
        chartDataList.addAll(querySourceTargetList(REVISION_MODEL_CODE, bid, REVISION_DEMAND_REL_MODEL_CODE, DEMAND_MODEL_CODE));
        chartDataList.forEach(e->{
            e.put(CALCULATE_START_TIME,e.get(DEVELOPMENT_START_TIME));
            e.put(ACTUAL_COMPLETE_TIME,e.get(TEST_START_TIME));
        });
        List<MObject> taskList =  querySourceTargetList(REVISION_MODEL_CODE, bid, REVISION_TASK_REL_MODEL_CODE, TASK_MODEL_CODE);
        taskList.forEach(e->{
            e.put(ACTUAL_COMPLETE_TIME,e.get(END_TIME));
            e.put(CALCULATE_START_TIME,e.get(START_TIME));
        });
        chartDataList.addAll(taskList);
        List<Map<String,Object>>  averageDeliveryTimeList = calculateAverageDeliveryTime(revisionDateRange,chartDataList, ImmutableList.of(TASK_MODEL_CODE,DEMAND_STORY_MODEL_CODE));
        memberDistributeChart.put(RETURN_TITLE,revisionDateRange);
        memberDistributeChart.put("data",averageDeliveryTimeList);
        return memberDistributeChart;
    }

    @Override
    public Map<String,Object> teamThroughputChart(String bid) {
      //查询项目下的迭代
        List<MObject> revisionGroupDemand =  apmSpaceEfficiencyReportMapper.queryTeamThroughputChartByProjectBid(bid);
        Map<String,Object> chartData = Maps.newHashMap();
        if(CollectionUtils.isNotEmpty(revisionGroupDemand)){
            Map<String,List<MObject>> revisionGroupDemandMap = revisionGroupDemand.stream().collect(
                    Collectors.groupingBy(e->e.get("revisionName").toString()));
            LinkedList<Integer> sumCountList = new LinkedList<>();
            LinkedList<Integer> completedCountList = new LinkedList<>();
            LinkedList<Integer> completeRateList = new LinkedList<>();
            LinkedList<String> statisticList = new LinkedList<>();
            revisionGroupDemandMap.forEach((k,v)->{
                sumCountList.add(v.size());
                BigDecimal completedCount = new BigDecimal(v.stream().filter(e->e.get(ACTUAL_COMPLETE_TIME)!=null).count());
                completedCountList.add(completedCount.intValue());
                completeRateList.add(new BigDecimal(v.size()).divide(completedCount,0,BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)).intValue());
                statisticList.add(k);
            });
            chartData.put(RETURN_TITLE,statisticList);
            chartData.put("data",ImmutableMap.of("总数量",sumCountList,"实际完成数量",completedCountList,"比例",completeRateList));
        }
        return chartData;
    }

    @Override
    public Boolean fillDemandResidualWorkingHours(String bid, Integer residualHours) {
        List<ApmStatisticRecord> hourRecordList = apmStatisticRecordService.list(Wrappers.lambdaQuery(ApmStatisticRecord.class)
                .eq(ApmStatisticRecord::getBizBid, bid).eq(ApmStatisticRecord::getStatisticType, BURN_DOWN_STATISTIC_TYPE_WORK_HOURS)
                .eq(ApmStatisticRecord::getStatisticBiz, BIZ_TYPE).eq(ApmStatisticRecord::getStatisticDate, LocalDate.now()));
        if(CollectionUtils.isEmpty(hourRecordList)){
            ApmStatisticRecord apmStatisticRecord = ApmStatisticRecord.builder()
                    .actualValue(residualHours.toString()).bizBid(bid)
                    .bid(SnowflakeIdWorker.nextIdStr()).createdTime(new Date()).updatedTime(new Date()).statisticDate(LocalDate.now())
                    .createdBy(SsoHelper.getJobNumber()).updatedBy(SsoHelper.getJobNumber())
                    .statisticBiz(BIZ_TYPE).statisticType(BURN_DOWN_STATISTIC_TYPE_WORK_HOURS).build();
            return apmStatisticRecordService.save(apmStatisticRecord);
        }else {
            ApmStatisticRecord apmStatisticRecord = hourRecordList.get(0);
            apmStatisticRecord.setActualValue(residualHours.toString());
            return apmStatisticRecordService.updateById(apmStatisticRecord);
        }
    }

    @Override
    public BurnDownChartVO burnDownChartPI(String statisticType, String bid) {
        BurnDownChartVO burnDownChartVO = BurnDownChartVO.builder().build();
        initBurnDownChartPI(burnDownChartVO, bid,statisticType);
        return burnDownChartVO;
    }

    /**
     * 迭代任务改状态时构建燃尽图
     * @param bid
     * @return
     */
    @Override
    public Boolean buildBurnoutChart(String bid) {
        // 查询关闭状态的生命周期状态的字典
        CfgDictionaryComplexQo qo = new CfgDictionaryComplexQo();
        qo.setCodes(com.google.common.collect.Lists.newArrayList(CLOSE_LIFE_CYCLE_CODE));
        qo.setEnableFlags(com.google.common.collect.Lists.newArrayList(CommonConst.ENABLE_FLAG_ENABLE));
        List<CfgDictionaryVo> cfgDictionaryVos = dictionaryFeignClient.listDictionaryAndItemByCodesAndEnableFlags(qo).getCheckExceptionData();
        Assert.notEmpty(cfgDictionaryVos, "未查询到关闭状态的生命周期状态");
        List<String> colse = cfgDictionaryVos.get(0).getDictionaryItems().stream()
                .filter(
                        cfgDictionaryItemVo ->  cfgDictionaryItemVo.getEnableFlag()!=null&&CommonConst.ENABLE_FLAG_ENABLE.equals(cfgDictionaryItemVo.getEnableFlag())
                )
                .map(CfgDictionaryItemVo::getKeyCode).collect(Collectors.toList());
        for (int i = 0; i < colse.size(); i++) {
            String colseCode = colse.get(i);
            colseCode = colseCode.substring(0,colseCode.length()-6);
            colse.set(i,colseCode);
        }
        BurnoutDataCalc.SubTask subTask = new BurnoutDataCalc.SubTask();
        subTask.setColseCodeList(colse);
        MObject mObject = objectModelCrudI.getByBid(REVISION_MODEL_CODE,bid);
        Assert.notNull(mObject,"当前对象实例数据不存在");
        // 1. 获取迭代关联的需求数据
        RelationMObject revisionDemand = RelationMObject.builder()
                .sourceModelCode(REVISION_MODEL_CODE).sourceBid(bid).relationModelCode(REVISION_DEMAND_REL_MODEL_CODE)
                .targetModelCode(DEMAND_MODEL_CODE).build();
        List<MObject> demandDataList = objectModelCrudI.listRelationMObjects(revisionDemand);
        demandDataList = Optional.ofNullable(demandDataList).orElse(com.google.common.collect.Lists.newArrayList());
        // 2.过滤未关闭的需求数据
        List<MObject> notCloseDemandList = demandDataList.stream().filter(demandData -> !subTask.getColseCodeList().contains(demandData.getLifeCycleCode())).collect(Collectors.toList());
        // 3. 计算用户故事的燃尽图的数据
        burnoutDataCalc.calcBurnDownChartByCount(demandDataList,mObject,subTask, notCloseDemandList);
        //4. 获取迭代关联的工作任务数据
        RelationMObject revisionTask = RelationMObject.builder()
                .sourceModelCode(REVISION_MODEL_CODE).sourceBid(bid).relationModelCode(REVISION_TASK_REL_MODEL_CODE)
                .targetModelCode(TASK_MODEL_CODE).build();
        List<MObject> taskList = objectModelCrudI.listRelationMObjects(revisionTask);
        taskList = Optional.ofNullable(taskList).orElse(com.google.common.collect.Lists.newArrayList());
        // 5. 过滤未关闭的工作任务数据
        List<MObject> notCloseTaskList = taskList.stream().filter(task -> !subTask.getColseCodeList().contains(task.getLifeCycleCode())).collect(Collectors.toList());
        // 6. 计算工时的燃尽图的数据
        burnoutDataCalc.calcBurnDownChartByHour(taskList,mObject,subTask, notCloseTaskList);
        return true;
    }

    /**
     * 初始化PI燃尽图
     * @param burnDownChartVO
     * @param bid
     * @param statisticType
     */
    private void initBurnDownChartPI(BurnDownChartVO burnDownChartVO, String bid, String statisticType) {
        // 标准
        Map<String,Object> idealLineMap = Maps.newHashMap();
        // 实际
        Map<String,Object> actualLineMap = Maps.newHashMap();
        // 数据
        List<ApmStatisticRecord> hourRecordList = apmStatisticRecordService.list(Wrappers.lambdaQuery(ApmStatisticRecord.class)
                .eq(ApmStatisticRecord::getBizBid, bid).eq(ApmStatisticRecord::getStatisticType, statisticType)
                .eq(ApmStatisticRecord::getStatisticBiz, BIZ_TYPE));
        if(CollectionUtils.isEmpty(hourRecordList)){
            return;
        }
        List<String> dateList = hourRecordList.stream().map( e -> e.getStatisticDate().format(DateTimeFormatter.ISO_LOCAL_DATE)).collect(Collectors.toList());
        // 设置统计日期线
        burnDownChartVO.setStatisticsDate(dateList);
        List<String> idealLine = hourRecordList.stream().map(e -> e.getIdealValue()).collect(Collectors.toList());
        List<String> actualLine = hourRecordList.stream().map(e -> e.getActualValue()).collect(Collectors.toList());
        if (BURN_DOWN_STATISTIC_TYPE_WORK_HOURS.equals(statisticType)) {
            // 工时统计
            idealLineMap.put("name","计划工时");
            actualLineMap.put("name","实际工时");
        }else if (BURN_DOWN_STATISTIC_TYPE_COUNT.equals(statisticType)) {
            // 用户故事数统计
            idealLineMap.put("name","计划用户故事数");
            actualLineMap.put("name","实际用户故事数");
        }
        idealLineMap.put("data",idealLine);
        actualLineMap.put("data",actualLine);
        burnDownChartVO.setSeries(Arrays.asList(idealLineMap,actualLineMap));
    }

    /**
     * @Description 初始化按照工时卫东统计的燃尽图
     * @Author jinpeng.bai
     * @Date 2023/10/25 17:34
     * @Param [burnDownChartVO, bid]
     * @Return void
     * @Since version 1.0
     */
    private List<String> initRevisionDate(String bid, boolean todayEnd){
        MObject object = objectModelCrudI.getByBid(REVISION_MODEL_CODE,bid);
        if(object == null){
            throw new PlmBizException("当前对象实例数据不存在");
        }
        Date startDate =DateUtil.parseDate(object.get(EfficiencyReportConstant.RevisionBurnDownConstant.START_TIME).toString());
        Date endDate =DateUtil.parseDate(object.get(EfficiencyReportConstant.RevisionBurnDownConstant.END_TIME).toString());
        if(todayEnd){
            endDate = new Date();
        }
        return getDatesBetween(startDate, DateUtil.parseDate(DateUtil.formatDate(endDate) + " 00:00:00"));
    }

    /**
     * @Description 获取2个时间段之间的所有日期(天)
     * @Author jinpeng.bai
     * @Date 2023/10/26 9:19
     * @Param [startDate, endDate]
     * @Return java.util.List<java.lang.String>
     * @Since version 1.0
     */

    public List<String> getDatesBetween(Date startDate, Date endDate) {
        List<String> dateList = Lists.newArrayList();
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        // 结束日期需要加1，以包含结束日期
        endCal.add(Calendar.DAY_OF_MONTH, 1);

        while (startCal.before(endCal)) {
            dateList.add(DateUtil.formatDate(startCal.getTime()));
            startCal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return dateList;
    }

    /**
     * @Description 初始化按照工时卫东统计的燃尽图
     * @Author jinpeng.bai
     * @Date 2023/10/25 17:34
     * @Param [burnDownChartVO, dataList]
     * @Return void
     * @Since version 1.0
     */
    private void initBurnDownWorkHoursChats(BurnDownChartVO burnDownChartVO, List<MObject> dataList){
        BigDecimal demandSum = new BigDecimal(dataList.stream().mapToInt(e -> Integer.parseInt(e.get(PLAN_WORKING_HOURS).toString())).sum());
        AtomicReference<BigDecimal> haveDoneIdealCount = new AtomicReference<>(new BigDecimal("0"));
        BigDecimal everyDayHours = BigDecimal.ZERO;
        if(burnDownChartVO.getStatisticsDate().size()<=1){
            everyDayHours =  demandSum;
        }else {
            everyDayHours =  demandSum.divide(new BigDecimal(burnDownChartVO.getStatisticsDate().size()-1),2,BigDecimal.ROUND_HALF_UP);
        }
        List<BigDecimal> idealLine = Lists.newArrayList();
        List<BigDecimal> actualLine = Lists.newArrayList();
        List<String> bids = dataList.stream().map(MBaseData::getBid).collect(Collectors.toList());
        Map<String,Double> everyDemand = dataList.stream().collect(Collectors.toMap(e->e.getBid(),e->Double.parseDouble(e.get(PLAN_WORKING_HOURS).toString())));
        List<ApmStatisticRecord> hourRecordList = apmStatisticRecordService.list(Wrappers.lambdaQuery(ApmStatisticRecord.class)
                .in(ApmStatisticRecord::getBizBid, bids).eq(ApmStatisticRecord::getStatisticType, BURN_DOWN_STATISTIC_TYPE_WORK_HOURS)
                .eq(ApmStatisticRecord::getStatisticBiz, BIZ_TYPE));
        Map<String,String> haveConsumeHoursMap = hourRecordList.stream().collect(
                Collectors.toMap(e->(e.getBizBid()+e.getStatisticDate().format(DateTimeFormatter.ISO_LOCAL_DATE)),ApmStatisticRecord::getActualValue));
        Map<String,Integer> demandResidualHoursMap = dataList.stream().collect(Collectors.toMap(e->e.getBid(),e->Integer.parseInt(e.get(PLAN_WORKING_HOURS).toString())));
            burnDownChartVO.getStatisticsDate().forEach(date->{
                AtomicDouble everyDayConsumeHours = new AtomicDouble(0);
                        dataList.forEach(e-> {
                            int residualHours = demandResidualHoursMap.get(e.getBid());
                            if(haveConsumeHoursMap.get(e.getBid() + date) != null){
                                demandResidualHoursMap.put(e.getBid(),Integer.parseInt(haveConsumeHoursMap.get(e.getBid() + date)));
                            }
                            everyDayConsumeHours.getAndAdd(Optional.ofNullable(haveConsumeHoursMap.get(e.getBid() + date)).map(Integer::parseInt).orElse(residualHours));
                        });
                everyDemand.put(date,everyDayConsumeHours.get());
            });
        BigDecimal finalEveryDayHours = everyDayHours;
        burnDownChartVO.getStatisticsDate().forEach(date -> {
            if(idealLine.size() ==0){
                idealLine.add(demandSum);
            }else if(idealLine.size() == burnDownChartVO.getStatisticsDate().size()-1){
                idealLine.add(BigDecimal.ZERO);
            }else {
                haveDoneIdealCount.getAndSet(haveDoneIdealCount.get().add(finalEveryDayHours));
                idealLine.add(demandSum.subtract(haveDoneIdealCount.get()));
            }
            if(DateUtil.compare(DateUtil.parseDate(date),DateUtil.parseDate(DateUtil.today())) <= 0){
                actualLine.add(new BigDecimal(everyDemand.get(date)));
            }
        });
        Map<String,Object> idealLineMap = Maps.newHashMap();
        Map<String,Object> actualLineMap = Maps.newHashMap();
        idealLineMap.put("name","计划工时");
        idealLineMap.put("data",idealLine);
        actualLineMap.put("name","实际工时");
        actualLineMap.put("data",actualLine);
        burnDownChartVO.setSeries(Arrays.asList(idealLineMap,actualLineMap));
    }

    /**
     * @Description 构建需求个数燃尽图，以用户计划完成时间和实际完成时间来做统计
     * @Author jinpeng.bai
     * @Date 2023/10/25 17:34
     * @Param [burnDownChartVO, dataList]
     * @Return void
     * @Since version 1.0
     */
    private void initBurnDownCountChats(BurnDownChartVO burnDownChartVO, List<MObject> dataList){
        AtomicReference<BigDecimal> haveDoneIdealCount = new AtomicReference<>(new BigDecimal("0"));
        AtomicReference<BigDecimal> haveDoneActualCount = new AtomicReference<>(new BigDecimal("0"));
        BigDecimal demandSum = new BigDecimal(dataList.size());
        List<BigDecimal> idealLine = Lists.newArrayList();
        List<BigDecimal> actualLine = Lists.newArrayList();
        BigDecimal everyDayHours = BigDecimal.ZERO;
        if(burnDownChartVO.getStatisticsDate().size()<=1){
            everyDayHours = demandSum;
        }else {
            everyDayHours =  demandSum.divide(new BigDecimal(burnDownChartVO.getStatisticsDate().size()-1),3,BigDecimal.ROUND_HALF_UP);
        }
        Map<String,Integer> actualLineDateSumMap = dataList.stream().filter(
                mObject -> mObject.get(EfficiencyReportConstant.RevisionBurnDownConstant.ACTUAL_COMPLETE_TIME)!=null &&
                StringUtils.isNotEmpty(mObject.get(EfficiencyReportConstant.RevisionBurnDownConstant.ACTUAL_COMPLETE_TIME).toString())).collect(
                Collectors.groupingBy(e->dataFormatStr(e.get(EfficiencyReportConstant.RevisionBurnDownConstant.ACTUAL_COMPLETE_TIME)),Collectors.summingInt(e->1)));
        BigDecimal finalEveryDayHours = everyDayHours;
        burnDownChartVO.getStatisticsDate().forEach(date -> {
            if(idealLine.size() ==0){
                idealLine.add(demandSum);
            }else if(idealLine.size() == burnDownChartVO.getStatisticsDate().size()-1){
                idealLine.add(BigDecimal.ZERO);
            }else {
                haveDoneIdealCount.getAndSet(haveDoneIdealCount.get().add(finalEveryDayHours));
                idealLine.add(demandSum.subtract(haveDoneIdealCount.get()));
            }
            if(DateUtil.compare(DateUtil.parseDate(date),DateUtil.parseDate(DateUtil.today())) <= 0){
                haveDoneActualCount.getAndSet(actualLineDateSumMap.get(date) == null ? haveDoneActualCount.get().add(BigDecimal.ZERO) : haveDoneActualCount.get().add(new BigDecimal(actualLineDateSumMap.get(date))));
                actualLine.add(demandSum.subtract(haveDoneActualCount.get()));
            }
        });
        Map<String,Object> idealLineMap = Maps.newHashMap();
        Map<String,Object> actualLineMap = Maps.newHashMap();
        idealLineMap.put("name","计划用户故事数");
        idealLineMap.put("data",idealLine);
        actualLineMap.put("name","实际用户故事数");
        actualLineMap.put("data",actualLine);
        burnDownChartVO.setSeries(Arrays.asList(idealLineMap,actualLineMap));
    }

    /**
     * @Description 格式化时间
     * @Author jinpeng.bai
     * @Date 2023/10/27 15:22
     * @Param [date]
     * @Return java.lang.String
     * @Since version 1.0
     */

    private String dataFormatStr(Object date){
        if(date == null){
            return "";
        }
        if(date instanceof LocalDateTime){
           return  ((LocalDateTime) date).format(DateTimeFormatter.ISO_DATE);
        }
        if (date instanceof Date){
            return DateUtil.formatDate((Date) date);
        }
        return date.toString();
    }

    private List<MObject> querySourceTargetList(String sourceModelCode, String bid,String relationModelCode, String targetModelCode){
        RelationMObject relationMObject = RelationMObject.builder().sourceModelCode(sourceModelCode).sourceBid(bid).relationModelCode(relationModelCode).targetModelCode(targetModelCode).build();
        List resList = objectModelCrudI.listRelationMObjects(relationMObject);
        if(resList == null){
            resList = Lists.newArrayList();
        }
        return resList;
    }

    /**
     * @Description 责任人分布图
     * @Author jinpeng.bai
     * @Date 2023/10/28 16:00
     * @Param [chartDataList]
     * @Return java.util.Map<java.lang.String, java.lang.Object>
     * @Since version 1.0
     */

    private Map<String,Object> personDistributeChartBuild(List<MObject> chartDataList){
        //按照横坐标分组
        Map<String,List<MObject>> demandTypeMap = chartDataList.stream().filter(e->e.get(PERSON_RESPONSIBLE)!=null && StringUtils.isNotEmpty(e.get(PERSON_RESPONSIBLE).toString())).
                collect(Collectors.groupingBy(mObject -> mObject.get(PERSON_RESPONSIBLE).toString()));
        List<String> userNoList = Lists.newArrayList();
        Map<String,List<String>> userMap = Maps.newHashMap();
        demandTypeMap.forEach((key,value)->{
            List<String> userTempList = new ArrayList<>();
            if(key.startsWith(CommonConstant.OPEN_BRACKET) && key.endsWith(CommonConstant.OPEN_RIGHT_BRACKET)){
                userTempList = JSON.parseArray(key,String.class);
                userNoList.addAll(userTempList);
            }else{
                userTempList.add(key);
                userNoList.add(key);
            }
            userMap.put(key,userTempList);
        });
        Set<String> userNoSet = Sets.newHashSet(userNoList);
        Map<String,String> userNameMap = getUserInfo(userNoSet);
        userMap.forEach((key,value)->{
            if(key.startsWith(CommonConstant.OPEN_BRACKET) && key.endsWith(CommonConstant.OPEN_RIGHT_BRACKET)){
                StringBuffer name = new StringBuffer();
                value.forEach(e->{
                    name.append(userNameMap.getOrDefault(e,e)).append(" ");
                });
                userNameMap.put(key,name.toString());
            }
        });
        Map<String,List<MObject>> convertMap = Maps.newHashMap();
        demandTypeMap.forEach((key,value)->{
            convertMap.put(userNameMap.getOrDefault(key,key),value);
                });
        //分组数据按照横坐标排序
        return getStringObjectMap(convertMap);
    }

    @NotNull
    private Map<String, Object> getStringObjectMap(Map<String, List<MObject>> demandTypeMap) {
        LinkedList<String> xAxis = new LinkedList<>();
        HashMap<String,LinkedList<Integer>> sortTitleMap = new HashMap<>(16);
        buildStatusMap(demandTypeMap, xAxis, sortTitleMap);
        return ImmutableMap.of(RETURN_TITLE,xAxis,"data",sortTitleMap);
    }

    /**
     * @Description 模块责任人分布图
     * @Author jinpeng.bai
     * @Date 2023/10/28 16:00
     * @Param [chartDataList]
     * @Return java.util.Map<java.lang.String, java.lang.Object>
     * @Since version 1.0
     */

    private Map<String,Object> modelPersonDistributeChartBuild(List<MObject> chartDataList){
        //按照横坐标分组
        Map<String,List<MObject>> modelGroupTypeMap = chartDataList.stream().filter(e->e.get(PERSON_IN_CHARGE_OF_TESTING)!=null && StringUtils.isNotEmpty(e.get(PERSON_IN_CHARGE_OF_TESTING).toString())).
                collect(Collectors.groupingBy(mObject -> mObject.get(PERSON_IN_CHARGE_OF_TESTING).toString()));
        buildModulePersonList(chartDataList, modelGroupTypeMap, DEVELOPMENT_RESPONSIBLE);
        buildModulePersonList(chartDataList, modelGroupTypeMap, PRODUCT_OWNER);
        Map<String,String> userNameMap = getUserInfo(modelGroupTypeMap.keySet());
        Map<String,List<MObject>> convertMap = Maps.newHashMap();
        modelGroupTypeMap.forEach((key,value)->{
            convertMap.put(userNameMap.getOrDefault(key,key),value);
        });
        //分组数据按照横坐标排序
        return getStringObjectMap(convertMap);
    }

    /**
     * @Description 构建模块负责人和开发负责人的数据
     * @Author jinpeng.bai
     * @Date 2023/10/28 15:30
     * @Param [chartDataList, modelGroupTypeMap, productOwner]
     * @Return void
     * @Since version 1.0
     */

    private void buildModulePersonList(List<MObject> chartDataList, Map<String, List<MObject>> modelGroupTypeMap, String productOwner) {
        chartDataList.stream().filter(e->e.get(productOwner)!=null && StringUtils.isNotEmpty(e.get(productOwner).toString())).
                collect(Collectors.groupingBy(mObject -> mObject.get(productOwner).toString())).forEach((k, v)->{
                    List<MObject> list = modelGroupTypeMap.getOrDefault(k, Lists.newArrayList());
                    list.addAll(v);
                    modelGroupTypeMap.put(k, list);
                });
    }

    /**
     * @Description 构建状态数据
     * @Author jinpeng.bai
     * @Date 2023/10/28 15:30
     * @Param [modelGroupTypeMap, xAxis, sortTitleMap]
     * @Return void
     * @Since version 1.0
     */

    private void buildStatusMap(Map<String, List<MObject>> modelGroupTypeMap, LinkedList<String> xAxis, HashMap<String, LinkedList<Integer>> sortTitleMap) {
        modelGroupTypeMap.forEach((k,v)->{
            if(CollectionUtils.isNotEmpty(v)){
                v.stream().forEach(e->e.put(LIFE_CODE_NAME,Optional.ofNullable(EfficiencyReportConstant.REPORT_LIFE_CODE_NAME.get(e.getLifeCycleCode())).orElse("未开始")));
            }
            //按照生命周期分组
            Map<String,Integer> lifeMap = v.stream().collect(Collectors.groupingBy(e->e.get(LIFE_CODE_NAME).toString(),Collectors.summingInt(e->1)));
            LinkedList<Integer> unStartList = sortTitleMap.getOrDefault(LIFE_UN_START,new LinkedList<>());
            LinkedList<Integer> processingList = sortTitleMap.getOrDefault(LIFE_PROCESSING,new LinkedList<>());
            LinkedList<Integer> completedList = sortTitleMap.getOrDefault(LIFE_COMPLETED,new LinkedList<>());
            unStartList.add(lifeMap.getOrDefault(LIFE_UN_START,0));
            processingList.add(lifeMap.getOrDefault(LIFE_PROCESSING,0));
            completedList.add(lifeMap.getOrDefault(LIFE_COMPLETED,0));
            sortTitleMap.put(LIFE_UN_START,unStartList);
            sortTitleMap.put(LIFE_PROCESSING,processingList);
            sortTitleMap.put(LIFE_COMPLETED,completedList);
            xAxis.add(k);
        });
    }

    /**
     * @Description 构建targetBid查询条件
     * @Author jinpeng.bai
     * @Date 2023/10/11 10:11
     * @Param [targetBid]
     * @Return com.transcend.plm.datadriven.api.model.QueryWrapper
     * @Since version 1.0
     */
    private List<QueryWrapper> buildQueryWrapperSpaceBid(String spaceBid,String modeCode){
        List<QueryWrapper> queryWrappers = com.google.common.collect.Lists.newArrayList();
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty(SpaceAppDataEnum.SPACE_BID.getCode());
        queryWrapper.setCondition("=");
        queryWrapper.setValue(spaceBid);
        QueryWrapper modelCodeWrapper = new QueryWrapper(Boolean.TRUE);
        modelCodeWrapper.setProperty(ObjectEnum.MODEL_CODE.getCode());
        modelCodeWrapper.setCondition("=");
        modelCodeWrapper.setSqlRelation(" and ");
        modelCodeWrapper.setValue(modeCode);
        queryWrappers.add(queryWrapper);
        return queryWrappers;
    }

    /**
     * @Description 根据工号过去用户信息
     * @Author jinpeng.bai
     * @Date 2023/11/1 10:35
     * @Param [empNos]
     * @Return java.util.Map<java.lang.String, java.lang.String>
     * @Since version 1.0
     */
    private Map<String,String> getUserInfo(Set<String> empNos){
        if(CollectionUtils.isEmpty(empNos)){
            return Maps.newHashMap();
        }
        List<String> userEmpNos = Lists.newArrayList(empNos);
        List<UserDTO> userDTOList = Lists.newArrayList();
        if(userEmpNos.size() >=100){
            // 指定分割的大小
            int chunkSize = 100;
            // 计算需要分割的次数
            int numChunks = (int) Math.ceil((double) userEmpNos.size() / chunkSize);
            for (int i = 0; i < numChunks; i++) {
                // 计算起始索引和结束索引
                int startIndex = i * chunkSize;
                int endIndex = Math.min((i + 1) * chunkSize, userEmpNos.size());
                // 使用subList()方法截取子List
                List<String> subList = userEmpNos.subList(startIndex, endIndex);
                // 将子List添加到结果List中
                userDTOList.addAll(uacUserService.batchQueryByEmpNos(subList).getData());
            }
        }else {
                userDTOList = uacUserService.batchQueryByEmpNos(userEmpNos).getData();
        }
        return userDTOList.stream().collect(Collectors.toMap(UserDTO::getEmployeeNo,UserDTO::getRealName));
    }
}
