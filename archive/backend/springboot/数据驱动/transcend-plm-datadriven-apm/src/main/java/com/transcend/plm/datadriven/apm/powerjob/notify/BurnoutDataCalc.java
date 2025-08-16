package com.transcend.plm.datadriven.apm.powerjob.notify;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.configcenter.api.feign.DictionaryFeignClient;
import com.transcend.plm.configcenter.api.model.dictionary.qo.CfgDictionaryComplexQo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryItemVo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryVo;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.ObjectEnum;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.api.model.RelationMObject;
import com.transcend.plm.datadriven.apm.space.model.SpaceAppDataEnum;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceService;
import com.transcend.plm.datadriven.apm.space.service.impl.DefaultAppExcelTemplateService;
import com.transcend.plm.datadriven.apm.statistics.repository.po.ApmStatisticRecord;
import com.transcend.plm.datadriven.apm.statistics.repository.service.ApmStatisticRecordService;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.common.util.StringToDoubleUtil;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.TaskResult;
import tech.powerjob.worker.core.processor.sdk.MapReduceProcessor;
import tech.powerjob.worker.log.OmsLogger;

import javax.annotation.Resource;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.transcend.plm.datadriven.apm.constants.ModelCodeProperties.*;
import static com.transcend.plm.datadriven.apm.statistics.constants.EfficiencyReportConstant.BURN_DOWN_STATISTIC_TYPE_COUNT;
import static com.transcend.plm.datadriven.apm.statistics.constants.EfficiencyReportConstant.BURN_DOWN_STATISTIC_TYPE_WORK_HOURS;
import static com.transcend.plm.datadriven.apm.statistics.constants.EfficiencyReportConstant.PlmBurnoutChartConstant.*;
import static com.transcend.plm.datadriven.apm.statistics.constants.EfficiencyReportConstant.RevisionBurnDownConstant.BIZ_TYPE;

/**
 * @author sgx
 * @version V1.0
 * description: 定时计算燃尽图的数据(用一句话描述该文件做什么)
 * date 2024-5-27
 */
@Slf4j
@Component
public class BurnoutDataCalc implements MapReduceProcessor {
    /**
     * 燃尽图的系统代码
      */
    public static final String BURNOUT_DATA_APP = "PLM";

    /**
     * POWERJOB 的任务参数中参数名(必填),默认值为 PLM
     */
    public static final String JOB_PARAMS_APP_NAME = "APP_NAME";

    /**
     * POWERJOB 的任务参数中参数名(非必填)
     */
    public static final String JOB_PARAMS_APP_BID = "APP_BID";

    /**
     * POWERJOB 的任务参数中参数名(非必填)
     */
    public static final String JOB_PARAMS_SPACE_BID = "SPACE_BID";

    @Resource
    private ApmSpaceAppService apmSpaceAppService;

    @Resource
    private ApmSpaceService apmSpaceService;

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Resource
    private ApmStatisticRecordService apmStatisticRecordService;

    @Resource
    private DictionaryFeignClient dictionaryFeignClient;

    @Resource
    private DefaultAppExcelTemplateService defaultAppExcelTemplateService;

    @Override
    public ProcessResult reduce(TaskContext taskContext, List<TaskResult> taskResults) {
        log.info("并行处理器====reduce=====NotifyExecuteRecordConfig reduce");

        // 所有 Task 执行结束后，reduce 将会被执行
        // taskResults 保存了所有子任务的执行结果
        // 用法举例，统计执行结果
        AtomicLong successCnt = new AtomicLong(0);
        AtomicLong failedCnt = new AtomicLong(0);
        taskResults.forEach(tr -> {
            if (tr.isSuccess()) {
                successCnt.incrementAndGet();
            }else {
                //如果有一个失败
                failedCnt.incrementAndGet();
            }
        });
        String resultMsg = String.format("success task num:%d, failed task num::%d", successCnt.get(), failedCnt.get());
        // 该结果将作为任务最终的执行结果
        return new ProcessResult(true, resultMsg);

    }

    @SuppressWarnings("unchecked")
    @Override
    public ProcessResult process(TaskContext taskContext) throws Exception {
        log.info("并行处理器====process=====BurnoutDataCalc process");
        // PowerJob 提供的日志 API，可支持在控制台指定多种日志模式（在线查看 / 本地打印）。最佳实践：全部使用 OmsLogger 打印日志，开发阶段控制台配置为 在线日志方便开发；上线后调整为本地日志，与直接使用 SLF4J 无异
        OmsLogger omsLogger = taskContext.getOmsLogger();
        // 是否为根任务，一般根任务进行任务的分发
        boolean isRootTask = isRootTask();
        // Task 名称，除了 MAP 任务其他 taskName 均由开发者自己创建，某种意义上也可以按参数理解（比如多层 MAP 的情况下，taskName 可以命名为，Map_Level1, Map_Level2，最终按 taskName 判断层级进不同的执行分支）
        String taskName = taskContext.getTaskName();
        // 任务参数，控制台任务配置中直接填写的参数
        String jobParamsStr = taskContext.getJobParams();

        omsLogger.info("[MapReduceDemo] [startExecuteNewTask] jobId:{}, instanceId:{}, taskId:{}, taskName: {}, RetryTimes: {}, isRootTask:{}, jobParams:{}",
                taskContext.getJobId(), taskContext.getInstanceId(), taskContext.getTaskId(), taskName, taskContext.getCurrentRetryTimes(), isRootTask, jobParamsStr );

        final JSONObject params = Optional.ofNullable(jobParamsStr).map(JSONObject::parseObject).orElse(new JSONObject());
        Assert.hasText(params.getString(JOB_PARAMS_APP_NAME), "任务参数APP_NAME不能为空");
        // 只计算PLM的任务
        if (BURNOUT_DATA_APP.equals(params.getString(JOB_PARAMS_APP_NAME))) {
            // 判断是否为根任务
            if (isRootTask) {
                // 根任务，进行任务分发
                omsLogger.info("[MapReduceDemo] [RootTask] start execute root task~");
                //查询有效的空间
                List<String> spaceBids = apmSpaceService.getSpaceBids();
                // 查询出所有迭代的应用BID
                List<ApmSpaceApp> apmSpaceAppList = apmSpaceAppService.list(Wrappers.<ApmSpaceApp>lambdaQuery().eq(ApmSpaceApp::getModelCode, REVISION_MODEL_CODE)
                                .eq(StringUtils.isNotBlank(params.getString(JOB_PARAMS_APP_BID)),ApmSpaceApp::getBid, params.getString(JOB_PARAMS_APP_BID))
                        .eq(StringUtils.isNotBlank(params.getString(JOB_PARAMS_SPACE_BID)),ApmSpaceApp::getSpaceBid, params.getString(JOB_PARAMS_SPACE_BID))
                        .in(ApmSpaceApp::getSpaceBid,spaceBids)
                        .eq(ApmSpaceApp::getDeleteFlag,false).eq(ApmSpaceApp::getEnableFlag,true));
                Assert.notEmpty(apmSpaceAppList, "未查询到迭代应用");
                // 查询关闭状态的生命周期状态的字典
                CfgDictionaryComplexQo qo = new CfgDictionaryComplexQo();
                qo.setCodes(Lists.newArrayList(CLOSE_LIFE_CYCLE_CODE));
                qo.setEnableFlags(Lists.newArrayList(CommonConst.ENABLE_FLAG_ENABLE));
                List<CfgDictionaryVo> cfgDictionaryVos = dictionaryFeignClient.listDictionaryAndItemByCodesAndEnableFlags(qo).getCheckExceptionData();
                Assert.notEmpty(cfgDictionaryVos, "未查询到关闭状态的生命周期状态");
                List<String> colse = cfgDictionaryVos.get(0).getDictionaryItems().stream().filter(
                        cfgDictionaryItemVo ->  cfgDictionaryItemVo.getEnableFlag()!=null&&CommonConst.ENABLE_FLAG_ENABLE.equals(cfgDictionaryItemVo.getEnableFlag())
                        )
                        .map(CfgDictionaryItemVo::getKeyCode).collect(Collectors.toList());
                for (int i = 0; i < colse.size(); i++) {
                    String colseCode = colse.get(i);
                    colseCode = colseCode.substring(0,colseCode.length()-6);
                    colse.set(i,colseCode);
                }
                List<SubTask> subTaskList = new ArrayList<>();
                for (ApmSpaceApp apmSpaceApp : apmSpaceAppList) {
                    SubTask subTask = new SubTask();
                    subTask.setAppBid(apmSpaceApp.getBid());
                    subTask.setApmSpaceApp(apmSpaceApp);
                    subTask.setColseCodeList(colse);
                    subTaskList.add(subTask);
                }
                // 任务分发
                map(subTaskList,"BURNT_OUT_DATA_CALC_TASK");
                return new ProcessResult(true, "result is xxx");
            }

            // 非子任务，可根据 subTask 的类型 或 TaskName 来判断分支
            if (taskContext.getSubTask() instanceof SubTask) {
                log.info("并行处理器====process=====BurnoutDataCalc SubTask");
                omsLogger.info("并行处理器====process=====BurnoutDataCalc SubTask");
                SubTask subTask = (SubTask) taskContext.getSubTask();
                ApmSpaceApp apmSpaceApp = subTask.getApmSpaceApp();
                QueryWrapper queryWrapper = new QueryWrapper();
                queryWrapper.eq(SpaceAppDataEnum.SPACE_APP_BID.getCode(), apmSpaceApp.getBid());
                queryWrapper.and();
                queryWrapper.eq(ObjectEnum.LIFE_CYCLE_CODE.getCode(), UNDERWAY_LIFE_CYCLE_CODE);
                List<QueryWrapper> wrappers = QueryWrapper.buildSqlQo(queryWrapper);
                // 查询所有进行中的迭代数据
                List<MObject> dataList= (List<MObject>) objectModelCrudI.list(apmSpaceApp.getModelCode(), wrappers);
                Assert.notEmpty (dataList, "空间："+apmSpaceApp.getSpaceBid()+",应用："+apmSpaceApp.getName()+",没有进行中的迭代数据");
                dataList.forEach(mObject -> {
                    // 1. 获取迭代关联的需求数据
                    RelationMObject revisionDemand = RelationMObject.builder()
                            .sourceModelCode(REVISION_MODEL_CODE).sourceBid(mObject.getBid()).relationModelCode(REVISION_DEMAND_REL_MODEL_CODE)
                            .targetModelCode(DEMAND_MODEL_CODE).build();
                    List<MObject> demandDataList = objectModelCrudI.listRelationMObjects(revisionDemand);
                    demandDataList = Optional.ofNullable(demandDataList).orElse(Lists.newArrayList());
                    // 2.过滤未关闭的需求数据
                    List<MObject> notCloseDemandList = demandDataList.stream().filter(demandData -> !subTask.getColseCodeList().contains(demandData.getLifeCycleCode())).collect(Collectors.toList());
                    // 3. 计算用户故事的燃尽图的数据
                    calcBurnDownChartByCount(demandDataList,mObject,subTask,notCloseDemandList);
                    //4. 获取迭代关联的工作任务数据
                    RelationMObject revisionTask = RelationMObject.builder()
                            .sourceModelCode(REVISION_MODEL_CODE).sourceBid(mObject.getBid()).relationModelCode(REVISION_TASK_REL_MODEL_CODE)
                            .targetModelCode(TASK_MODEL_CODE).build();
                    List<MObject> taskList = objectModelCrudI.listRelationMObjects(revisionTask);
                    taskList = Optional.ofNullable(taskList).orElse(Lists.newArrayList());
                    // 5. 过滤未关闭的工作任务数据
                    List<MObject> notCloseTaskList = taskList.stream().filter(task -> !subTask.getColseCodeList().contains(task.getLifeCycleCode())).collect(Collectors.toList());
                    // 6. 计算工时的燃尽图的数据
                    calcBurnDownChartByHour(taskList,mObject,subTask,notCloseTaskList);
                });

            }
        }

        return new ProcessResult(true, "result is xxx");
    }

    /**
     * 计算工时的燃尽图的数据
     *
     * @param demandDataList   需求数据
     * @param mObject          迭代数据
     * @param subTask          子任务数据
     * @param notCloseTaskList  未关闭的工作任务数据
     */
    public void calcBurnDownChartByHour(List<MObject> demandDataList, MObject mObject, SubTask subTask, List<MObject> notCloseTaskList) {
        try {
            // 查询迭代燃尽图字段的字典
            Map<String, String> attrMap = defaultAppExcelTemplateService.getDictCodeAndEnNameMap(VISION_DEMAND_BURNOUT_ATTR);
            Assert.hasText(attrMap.get(ACTUAL_START_DATE), "迭代任务燃尽图字段的字典中实际开始不能为空");
            Assert.hasText(attrMap.get(PLAN_END_DATE), "迭代任务燃尽图字段的字典中计划结束不能为空");
            Assert.hasText(attrMap.get(PLAN_START_DATE), "迭代任务燃尽图字段的字典中计划开始不能为空");
            Assert.hasText(attrMap.get(ACTUAL_END_DATE), "迭代任务燃尽图字段的字典中实际结束不能为空");
            // 没有实际开始时间的数据不计算
            if (mObject.get(attrMap.get(ACTUAL_START_DATE)) == null || mObject.get(attrMap.get(PLAN_END_DATE)) == null) {
                log.info("迭代：{},主键：{},实际开始日期：{},计划结束日期：{}", mObject.getName(), mObject.getBid(), mObject.get(attrMap.get(ACTUAL_START_DATE)), mObject.get(attrMap.get(PLAN_END_DATE)));
                return;
            }
            // 横向坐标-开始时间：迭代的【实际开始日期】
            // 横向坐标-结束时间：迭代的【计划结束时间】
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            // 计算迭代的开始时间
            String actualStartDateStr = mObject.get(attrMap.get(ACTUAL_START_DATE)).toString();
            // 计算迭代的结束时间
            String planEndDateStr = mObject.get(attrMap.get(PLAN_END_DATE)).toString();
            LocalDate actualStartDate = LocalDate.parse(actualStartDateStr, formatter);
            LocalDate planEndDate = LocalDate.parse(planEndDateStr, formatter);
            List<LocalDate> dates = getDatesBetween(actualStartDate, planEndDate);
            // 默认升序排列
            dates = dates.stream().sorted().collect(Collectors.toList());
            // 标准值统计所有的任务
            AtomicReference<Double> idealVal = new AtomicReference<>(0D);
            // 实际值统计未关闭的任务
            AtomicReference<Double> actualVal = new AtomicReference<>(0D);
            demandDataList.stream()
                    .filter(d -> d.get(attrMap.get(ESTIMATED_WORK_HOUR)) != null)
                    .forEach(
                            task ->
                                    idealVal.set(idealVal.get() + StringToDoubleUtil.stringToDouble(task.get(attrMap.get(ESTIMATED_WORK_HOUR)).toString()))
                            );
            // 计算工时的标准线
            calcCountStandardLine(dates, mObject, attrMap, idealVal.get(), subTask,BURN_DOWN_STATISTIC_TYPE_WORK_HOURS);
            notCloseTaskList.stream()
                    .filter(d -> d.get(attrMap.get(ESTIMATED_WORK_HOUR)) != null)
                    .forEach(
                            task ->
                                    actualVal.set(actualVal.get() + StringToDoubleUtil.stringToDouble(task.get(attrMap.get(ESTIMATED_WORK_HOUR)).toString()))
                    );
            // 判断是否有实际结束时间
            String actualEndDateStr = mObject.get(attrMap.get(ACTUAL_END_DATE))!=null?mObject.get(attrMap.get(ACTUAL_END_DATE)).toString():null;
            // 当天日期超过计划日期，没有实际结束时间
            LocalDate local = LocalDate.now();
            if (StringUtils.isBlank(actualEndDateStr) && local.isAfter(planEndDate)) {
                actualEndDateStr = local.atStartOfDay().format(formatter);
            }
            if (StringUtils.isNotBlank(actualEndDateStr)) {
                planEndDate = LocalDate.parse(actualEndDateStr, formatter);
                dates = getDatesBetween(actualStartDate, planEndDate);
                dates = dates.stream().sorted().collect(Collectors.toList());
            }
            // 计算工时的实际完成线
            calcCountActualLine(dates, mObject, attrMap, actualVal.get(), subTask,BURN_DOWN_STATISTIC_TYPE_WORK_HOURS);
        } catch (Exception e) {
            log.error("计算工时燃尽图失败", e);
        }

    }

    /**
     * 计算用户故事的燃尽图的数据
     *
     * @param demandDataList     用户故事的数据
     * @param mObject            迭代数据
     * @param subTask            子任务数据
     * @param notCloseDemandList 未关闭的用户故事
     */
    public void calcBurnDownChartByCount(List<MObject> demandDataList, MObject mObject, SubTask subTask, List<MObject> notCloseDemandList) {
        try {
            // 查询迭代燃尽图字段的字典
            Map<String, String> attrMap = defaultAppExcelTemplateService.getDictCodeAndEnNameMap(VISION_DEMAND_BURNOUT_ATTR);
            Assert.hasText(attrMap.get(ACTUAL_START_DATE), "迭代需求燃尽图字段的字典中实际开始不能为空");
            Assert.hasText(attrMap.get(PLAN_END_DATE), "迭代需求燃尽图字段的字典中计划结束不能为空");
            Assert.hasText(attrMap.get(PLAN_START_DATE), "迭代需求燃尽图字段的字典中计划开始不能为空");
            Assert.hasText(attrMap.get(ACTUAL_END_DATE), "迭代需求燃尽图字段的字典中实际结束不能为空");
            // 没有实际开始时间的数据不计算
            if (mObject.get(attrMap.get(ACTUAL_START_DATE)) == null || mObject.get(attrMap.get(PLAN_END_DATE)) == null) {
                log.info("迭代：{},主键：{},实际开始日期：{},计划结束日期：{}", mObject.getName(), mObject.getBid(), mObject.get(attrMap.get(ACTUAL_START_DATE)), mObject.get(attrMap.get(PLAN_END_DATE)));
                return;
            }
            // 横向坐标-开始时间：迭代的【实际开始日期】
            // 横向坐标-结束时间：迭代的【计划结束时间】
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            // 计算迭代的开始时间
            String actualStartDateStr = mObject.get(attrMap.get(ACTUAL_START_DATE)).toString();
            // 计算迭代的结束时间
            String planEndDateStr = mObject.get(attrMap.get(PLAN_END_DATE)).toString();
            LocalDate actualStartDate = LocalDate.parse(actualStartDateStr, formatter);
            LocalDate planEndDate = LocalDate.parse(planEndDateStr, formatter);
            List<LocalDate> dates = getDatesBetween(actualStartDate, planEndDate);
            // 默认升序排列
            dates = dates.stream().sorted().collect(Collectors.toList());
            // 标准值统计所有的需求
            double idealVal = demandDataList.size();
            // 实际值统计未关闭的需求
            double actualVal = notCloseDemandList.size();
            // 计算用户故事的标准线
            calcCountStandardLine(dates, mObject, attrMap, idealVal, subTask,BURN_DOWN_STATISTIC_TYPE_COUNT);
            // 判断是否有实际结束时间
            String actualEndDateStr = mObject.get(attrMap.get(ACTUAL_END_DATE))!=null?mObject.get(attrMap.get(ACTUAL_END_DATE)).toString():null;
            // 当天日期超过计划日期，没有实际结束时间
            LocalDate local = LocalDate.now();
            if (StringUtils.isBlank(actualEndDateStr) && local.isAfter(planEndDate)) {
                actualEndDateStr = local.atStartOfDay().format(formatter);
            }
            if (StringUtils.isNotBlank(actualEndDateStr)) {
                planEndDate = LocalDate.parse(actualEndDateStr, formatter);
                dates = getDatesBetween(actualStartDate, planEndDate);
                dates = dates.stream().sorted().collect(Collectors.toList());
            }
            // 计算用户故事的实际完成线
            calcCountActualLine(dates, mObject, attrMap, actualVal, subTask,BURN_DOWN_STATISTIC_TYPE_COUNT);
        } catch (Exception e) {
            log.error("计算用户故事的燃尽图失败", e );
        }
    }

    /**
     * 计算用户故事的实际完成线
     * @param dates 日期列表
     * @param mObject 迭代数据
     * @param attrMap 属性
     * @param maxDemand 实际数据值
     * @param subTask 子任务数据
     */

    public void calcCountActualLine(List<LocalDate> dates, MObject mObject, Map<String, String> attrMap, double maxDemand, SubTask subTask,String statisType) {
        LocalDate local = LocalDate.now();
        dates.forEach(date -> {
            // 判断当前日期和计算的日期是否相等
            if (date.isBefore(local) || date.isAfter(local)){
                return;
            }
            List<ApmStatisticRecord> recordList = apmStatisticRecordService.list(Wrappers.lambdaQuery(ApmStatisticRecord.class)
                    .eq(ApmStatisticRecord::getBizBid, mObject.getBid()).eq(ApmStatisticRecord::getStatisticType, statisType)
                    .eq(ApmStatisticRecord::getStatisticBiz, BIZ_TYPE).eq(ApmStatisticRecord::getStatisticDate, date));
            if (CollectionUtils.isNotEmpty(recordList)) {
                ApmStatisticRecord apmStatisticRecord = recordList.get(0);
                apmStatisticRecord.setActualValue(String.valueOf(maxDemand));
                apmStatisticRecord.setUpdatedTime(new Date());
                apmStatisticRecord.setUpdatedBy(SsoHelper.getJobNumber());
                apmStatisticRecordService.updateById(apmStatisticRecord);
            }else {
                ApmStatisticRecord apmStatisticRecord = ApmStatisticRecord.builder()
                        .actualValue(String.valueOf(maxDemand)).bizBid(mObject.getBid())
                        .bid(SnowflakeIdWorker.nextIdStr()).createdTime(new Date()).updatedTime(new Date()).statisticDate(date)
                        .createdBy(SsoHelper.getJobNumber()).updatedBy(SsoHelper.getJobNumber())
                        .statisticBiz(BIZ_TYPE).statisticType(statisType).build();
                apmStatisticRecordService.save(apmStatisticRecord);
            }
        });
    }

    /**
     * 计算用户故事的标准线
     * @param dates     日期列表
     * @param mObject   迭代数据
     * @param attrMap   属性
     * @param maxDemand 实际数据值
     * @param subTask  子任务数据
     */
    private void calcCountStandardLine(List<LocalDate> dates, MObject mObject, Map<String, String> attrMap, double maxDemand, SubTask subTask, String statisType) {
        LocalDate local = LocalDate.now();
        // 获取最小日期
        LocalDate minDate = dates.get(0);
        // 获取最大日期
        LocalDate maxDate = dates.get(dates.size() - 1);
        List<ApmStatisticRecord> minRecordList = apmStatisticRecordService.list(Wrappers.lambdaQuery(ApmStatisticRecord.class)
                .eq(ApmStatisticRecord::getBizBid, mObject.getBid()).eq(ApmStatisticRecord::getStatisticType, statisType)
                .eq(ApmStatisticRecord::getStatisticBiz, BIZ_TYPE).eq(ApmStatisticRecord::getStatisticDate, minDate));
        List<ApmStatisticRecord> maxRecordList = apmStatisticRecordService.list(Wrappers.lambdaQuery(ApmStatisticRecord.class)
                .eq(ApmStatisticRecord::getBizBid, mObject.getBid()).eq(ApmStatisticRecord::getStatisticType, statisType)
                .eq(ApmStatisticRecord::getStatisticBiz, BIZ_TYPE).eq(ApmStatisticRecord::getStatisticDate, maxDate));
        if (minRecordList.isEmpty() && maxRecordList.isEmpty()) {
            // 无数据
            //TODO 排除周末暂时注释
//            int days = dates.size() -1 - calculateWeekendDays(minDate, maxDate);
            int days = dates.size() -1 ;
            double avg = BigDecimal.valueOf(maxDemand/days).setScale(2, RoundingMode.HALF_UP).doubleValue();
            for (int i = 0; i < dates.size(); i++) {
                LocalDate date = dates.get(i);
                if (i ==  dates.size()-1) {
                    maxDemand = 0;
                }
                ApmStatisticRecord apmStatisticRecord = ApmStatisticRecord.builder()
                        .idealValue(String.valueOf(maxDemand)).bizBid(mObject.getBid())
                        .bid(SnowflakeIdWorker.nextIdStr()).createdTime(new Date()).updatedTime(new Date()).statisticDate(date)
                        .createdBy(SsoHelper.getJobNumber()).updatedBy(SsoHelper.getJobNumber())
                        .statisticBiz(BIZ_TYPE).statisticType(statisType).build();
                apmStatisticRecordService.save(apmStatisticRecord);
                //TODO 排除周末暂时注释
                /*// 如果是周末
                if (date.getDayOfWeek().equals(DayOfWeek.FRIDAY) || date.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
                    continue;
                }else {
                    // 如果是工作日
                    maxDemand = BigDecimal.valueOf(maxDemand - avg).setScale(2, RoundingMode.HALF_UP).doubleValue();
                }*/
                maxDemand = BigDecimal.valueOf(maxDemand - avg).setScale(2, RoundingMode.HALF_UP).doubleValue();
            }
        }else {
            // 最小日期和当前时间是同一天
            if (local.equals(minDate)) {
                //TODO 排除周末暂时注释
//                int days = dates.size() -1 - calculateWeekendDays(minDate, maxDate);
                int days = dates.size() -1 ;
                double avg = BigDecimal.valueOf(maxDemand/days).setScale(2, RoundingMode.HALF_UP).doubleValue();
                for (int i = 0; i < dates.size(); i++) {
                    LocalDate date = dates.get(i);
                    if (i ==  dates.size()-1) {
                        maxDemand = 0;
                    }
                    List<ApmStatisticRecord> hourRecordList = apmStatisticRecordService.list(Wrappers.lambdaQuery(ApmStatisticRecord.class)
                            .eq(ApmStatisticRecord::getBizBid, mObject.getBid()).eq(ApmStatisticRecord::getStatisticType, statisType)
                            .eq(ApmStatisticRecord::getStatisticBiz, BIZ_TYPE).eq(ApmStatisticRecord::getStatisticDate, date));
                    if (CollectionUtils.isNotEmpty(hourRecordList)) {
                        ApmStatisticRecord apmStatisticRecord = hourRecordList.get(0);
                        apmStatisticRecord.setIdealValue(String.valueOf(maxDemand));
                        apmStatisticRecord.setUpdatedTime(new Date());
                        apmStatisticRecord.setUpdatedBy(SsoHelper.getJobNumber());
                        apmStatisticRecordService.updateById(apmStatisticRecord);
                    }else {
                        ApmStatisticRecord apmStatisticRecord = ApmStatisticRecord.builder()
                                .idealValue(String.valueOf(maxDemand)).bizBid(mObject.getBid())
                                .bid(SnowflakeIdWorker.nextIdStr()).createdTime(new Date()).updatedTime(new Date()).statisticDate(date)
                                .createdBy(SsoHelper.getJobNumber()).updatedBy(SsoHelper.getJobNumber())
                                .statisticBiz(BIZ_TYPE).statisticType(statisType).build();
                        apmStatisticRecordService.save(apmStatisticRecord);
                    }
                    //TODO 排除周末暂时注释
                    /*// 如果是周末
                    if (date.getDayOfWeek().equals(DayOfWeek.FRIDAY) || date.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
                        continue;
                    }else {
                        // 如果是工作日
                        maxDemand = BigDecimal.valueOf(maxDemand - avg).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    }*/
                    maxDemand = BigDecimal.valueOf(maxDemand - avg).setScale(2, RoundingMode.HALF_UP).doubleValue();
                }
            }
        }

    }


    /**
     * Description 获取2个时间段之间的所有日期(天)
     * Author jinpeng.bai
     * Date 2023/10/26 9:19
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return java.util.List<LocalDate>
     * @since version 1.0
     */
    private List<LocalDate> getDatesBetween(LocalDate startDate, LocalDate  endDate) {
        List<LocalDate> dates = new ArrayList<>();
        while (!startDate.isAfter(endDate)) {
            dates.add(startDate);
            startDate = startDate.plusDays(1);
        }
        return dates;
    }

    /**
     * 计算两个日期之间的周末天数
     * @param start
     * @param end
     * @return
     */
    private int calculateWeekendDays(LocalDate start, LocalDate end) {
        int weekendCount = 0;
        LocalDate date = start;

        while (!date.isAfter(end)) {
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                weekendCount++;
            }
            date = date.plusDays(1);
        }

        return weekendCount;
    }

    /**
     * 自定义的子任务，按自己的业务需求定义即可
     * 注意：代表子任务参数的类：一定要有无参构造方法！一定要有无参构造方法！一定要有无参构造方法！
     * 最好把 GET / SET 方法也加上，减少序列化问题的概率
     */
    @Data
    @AllArgsConstructor
    @Getter
    public static class SubTask implements Serializable {

        /**
         * 再次强调，一定要有无参构造方法
         */
        public SubTask() {
        }

        private String appBid;

        private ApmSpaceApp apmSpaceApp;

        private List<String> colseCodeList;

        public void setAppBid(String appBid) {
            this.appBid = appBid;
        }

        public void setApmSpaceApp(ApmSpaceApp apmSpaceApp) {
            this.apmSpaceApp = apmSpaceApp;
        }

        public void setColseCodeList(List<String> colseCodeList) {
            this.colseCodeList = colseCodeList;
        }
    }
}
