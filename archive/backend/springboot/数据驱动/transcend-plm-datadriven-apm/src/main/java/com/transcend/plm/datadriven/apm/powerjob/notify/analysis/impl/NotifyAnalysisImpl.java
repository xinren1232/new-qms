package com.transcend.plm.datadriven.apm.powerjob.notify.analysis.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.plm.configcenter.api.feign.LifeCycleFeignClient;
import com.transcend.plm.configcenter.api.model.lifecycle.vo.LifeCycleStateVo;
import com.transcend.plm.datadriven.api.model.BaseDataEnum;
import com.transcend.plm.datadriven.api.model.ObjectEnum;
import com.transcend.plm.datadriven.api.model.QueryFilterConditionEnum;
import com.transcend.plm.datadriven.api.model.qo.ModelFilterQo;
import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transcend.plm.datadriven.apm.enums.InnerRoleEnum;
import com.transcend.plm.datadriven.apm.permission.pojo.bo.ApmUser;
import com.transcend.plm.datadriven.apm.permission.service.impl.ApmRoleIdentityDomainService;
import com.transcend.plm.datadriven.apm.permission.service.impl.PlatformUserWrapper;
import com.transcend.plm.datadriven.apm.powerjob.notify.analysis.service.INotifyMessageService;
import com.transcend.plm.datadriven.apm.powerjob.notify.analysis.service.NotifyAnalysis;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmNotifyExecuteRecord;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmNotifyExecuteRecordHis;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmNotifyExecuteRecordHisService;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmNotifyExecuteRecordService;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.common.spring.PlmContextHolder;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.notify.dto.NotifyConfigDto;
import com.transcend.plm.datadriven.notify.service.impl.NotifyAppServiceImpl;
import com.transcend.plm.datadriven.notify.vo.*;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.transsion.framework.common.date.DateUtil.getDateAddDay;

/**
 * @author quan.cheng
 * @title NotifyAnalysisImpl
 * @date 2024/2/5 9:40
 * @description TODO
 */
@Slf4j
@Service
public class NotifyAnalysisImpl implements NotifyAnalysis {

    @Resource
    private ApmNotifyExecuteRecordService apmNotifyExecuteRecordService;
    @Resource
    private ApmNotifyExecuteRecordHisService apmNotifyExecuteRecordHisService;
    @Resource
    private ApmRoleIdentityDomainService apmRoleIdentityDomainService;

    @Value("${transcend.datadriven.apm.notify.base.url: https://alm.transsion.com//#/share/info/}")
    private String baseUrl;

    @Resource
    private PlatformUserWrapper platformUserWrapper;

    @Resource
    private INotifyMessageService notifyMessageService;

    @Resource
    private LifeCycleFeignClient lifeCycleFeignClient;

    @Resource
    private NotifyAppServiceImpl notifyAppService;

    @Resource
    private IApmSpaceAppDataDrivenService apmSpaceAppDataDrivenService;
    @Override
    public List<ApmNotifyExecuteRecord> analysis(List<NotifyConfigVo> notifyConfigVos, List<MSpaceAppData> mSpaceAppDataList,boolean checkInstanceBid) {
        if (notifyConfigVos == null || notifyConfigVos.isEmpty() || CollectionUtils.isEmpty(mSpaceAppDataList)) {
            return null;
        }
        Map<String,String> instanceBidMap = new HashMap<>(16);
        if(checkInstanceBid){
            List<String> instanceBids = mSpaceAppDataList.stream().map(MSpaceAppData::getBid).collect(Collectors.toList());
            List<ApmNotifyExecuteRecord> apmNotifyExecuteRecords = apmNotifyExecuteRecordService.listByInstanceBids(instanceBids,mSpaceAppDataList.get(0).getSpaceAppBid());
            if(CollectionUtils.isNotEmpty(apmNotifyExecuteRecords)){
                for(ApmNotifyExecuteRecord apmNotifyExecuteRecord : apmNotifyExecuteRecords){
                    instanceBidMap.put(apmNotifyExecuteRecord.getInstanceBid(),apmNotifyExecuteRecord.getNotifyContent());
                }
            }
        }
        // 根据规则生成通知记录
        // 如果是操作事件，消息通知应该是立即发送
        List<ApmNotifyExecuteRecord> apmNotifyExecuteRecordArrayList = new ArrayList<>();
        Map<String, String> lifeCycleMap = new HashMap<>(16);
        for (NotifyConfigVo notifyConfigVo : notifyConfigVos) {
            LocalDate currentLD = LocalDate.now();
            // 获取当前日期的结束时间（23:59:59.999999999）
            LocalDateTime endOfDayLD = currentLD.atTime(LocalTime.MAX);
            // LocalDateTime转Date
            Date endOfDayD = java.sql.Timestamp.valueOf(endOfDayLD);
            for (MSpaceAppData mSpaceAppData : mSpaceAppDataList) {
                NotifyTimeRuleVo notifyTimeRuleVo = notifyConfigVo.getNotifyTimeRuleVo();

                ApmNotifyExecuteRecord apmNotifyExecuteRecord = new ApmNotifyExecuteRecord();
                apmNotifyExecuteRecord.setBid(SnowflakeIdWorker.nextIdStr());
                apmNotifyExecuteRecord.setNotifyConfigBid(notifyConfigVo.getBid());
                //拼装url
                String url = baseUrl+mSpaceAppData.getSpaceBid()+"/"+mSpaceAppData.getSpaceAppBid()+"/"+mSpaceAppData.getBid()+"?viewMode=tableView";
                apmNotifyExecuteRecord.setUrl(url);
                //记录查询条件
                if(mSpaceAppData.get("WRAPPERS_STR_RECORD") != null){
                    apmNotifyExecuteRecord.setNofifyResultMsg(mSpaceAppData.get("WRAPPERS_STR_RECORD")+"");
                }

                //解析时间
                NotifyExecuteTimeVo notifyExecuteTimeVo = notifyAppService.getNotifyExecuteTime(notifyTimeRuleVo,mSpaceAppData);
                log.info("notifyExecuteTimeVo analysis result:{}",notifyExecuteTimeVo);
                //如果定时任务执行次数是一次需要判断是否已经执行过
                if(notifyExecuteTimeVo.getExecuteNum() != null && notifyExecuteTimeVo.getExecuteNum() == 1){
                    List<ApmNotifyExecuteRecordHis> recordHis = apmNotifyExecuteRecordHisService.list(Wrappers.<ApmNotifyExecuteRecordHis>lambdaQuery()
                            .eq(ApmNotifyExecuteRecordHis::getNotifyConfigBid, notifyConfigVo.getBid())
                            .eq(ApmNotifyExecuteRecordHis::getInstanceBid, mSpaceAppData.getBid())
                            .eq(ApmNotifyExecuteRecordHis::getSpaceAppBid, mSpaceAppData.getSpaceAppBid()));
                    if(CollectionUtils.isNotEmpty(recordHis)){
                        continue;
                    }
                }
                //设置过期时间 需要解析规则
                if(notifyExecuteTimeVo != null){
                    apmNotifyExecuteRecord.setNofifyNow(notifyExecuteTimeVo.getIsNow());
                    apmNotifyExecuteRecord.setNofifyTime(notifyExecuteTimeVo.getExecuteTime());
                }
                // 存储角色还是用户ID 待定
                // 根据角色ID查询用户
                List<String> codes = notifyConfigVo.getNotifyRoleCodes();
                List<String> jobNumbers = apmRoleIdentityDomainService.listNotifyJobNumbers(codes, mSpaceAppData.getBid(),mSpaceAppData.getSpaceAppBid(),mSpaceAppData.getSpaceBid());
                if(jobNumbers == null){
                    jobNumbers = new ArrayList<>();
                }
                if(codes.contains(InnerRoleEnum.CREATER.getCode()) && StringUtils.isNotBlank(mSpaceAppData.getCreatedBy())){
                    jobNumbers.add(mSpaceAppData.getCreatedBy());
                }
                if(codes.contains(InnerRoleEnum.PERSON_RESPONSIBLE.getCode())){
                    Object personResponsible = mSpaceAppData.get("personResponsible");
                    if (ObjectUtil.isNotNull(personResponsible)) {
                        if (personResponsible instanceof List) {
                            List<String> pers = (List) personResponsible;
                            jobNumbers.addAll(pers);
                        } else if (personResponsible instanceof String) {
                            jobNumbers.add((String) personResponsible);
                        }
                    }
                }

                if(codes.contains(InnerRoleEnum.TECHNICAL_DIRECTOR.getCode())){
                    Object personResponsible = mSpaceAppData.get(ObjectEnum.PERSON_RESPONSIBLE.getCode());
                    if (ObjectUtil.isNotNull(personResponsible)) {
                        if (personResponsible instanceof List) {
                            List<String> pers = (List) personResponsible;
                            jobNumbers.addAll(pers);
                        } else if (personResponsible instanceof String) {
                            jobNumbers.add((String) personResponsible);
                        }
                    }
                }

                if(codes.contains(InnerRoleEnum.UX_AGENT.getCode())){
                    Object personResponsible = mSpaceAppData.get(ObjectEnum.UX_SCORE.getCode());
                    if (ObjectUtil.isNotNull(personResponsible)) {
                        if (personResponsible instanceof List) {
                            List<String> pers = (List) personResponsible;
                            jobNumbers.addAll(pers);
                        } else if (personResponsible instanceof String) {
                            jobNumbers.add((String) personResponsible);
                        }
                    }
                }

                apmNotifyExecuteRecord.setNotifyJobnumbers(jobNumbers);

                apmNotifyExecuteRecord.setNotifyWay(notifyConfigVo.getNotifyWay());
                // 创建类型为实时发送
                String notifyContent = notifyConfigVo.getNotifyContent();
                if(org.apache.commons.lang3.StringUtils.isNotEmpty(notifyContent) && notifyContent.contains("{") && notifyContent.contains("}")){
                    for(Map.Entry<String, Object> entry : mSpaceAppData.entrySet()){
                        if(notifyContent.contains("{"+entry.getKey()+"}")){
                            if(ObjectEnum.PERSON_RESPONSIBLE.getCode().equals(entry.getKey())){
                               //责任人需要取人员名称
                                ApmUser apmUser = platformUserWrapper.getUserBOByEmpNO(entry.getValue()+"");
                                if(apmUser != null){
                                    notifyContent = notifyContent.replaceAll("\\{"+entry.getKey()+"}",apmUser.getName()+"("+apmUser.getEmpNo()+")");
                                }else{
                                    notifyContent = notifyContent.replaceAll("\\{"+entry.getKey()+"}",entry.getValue()+"");
                                }
                            }else if (ObjectEnum.LIFE_CYCLE_CODE.getCode().equals(entry.getKey())){
                               //生命周期需要取生命周期名称
                                String lifeCycleName = getLifeCycleName(entry.getValue()+"",lifeCycleMap);
                                notifyContent = notifyContent.replaceAll("\\{"+entry.getKey()+"}",lifeCycleName);
                            } else{
                                notifyContent = notifyContent.replaceAll("\\{"+entry.getKey()+"}",entry.getValue()+"");
                            }
                        }
                    }
                }
                if (apmNotifyExecuteRecord.getNofifyTime().after(endOfDayD)) {
                    apmNotifyExecuteRecord.setNofifyResult(0);
                }else{
                    //需要待执行 发送
                    apmNotifyExecuteRecord.setNofifyResult(3);
                }
                apmNotifyExecuteRecord.setNotifyContent(notifyContent);
                if(checkInstanceBid && instanceBidMap.get(mSpaceAppData.getBid()) != null){
                    if(notifyContent.equals(instanceBidMap.get(mSpaceAppData.getBid()))){
                        continue;
                    }
                }
                apmNotifyExecuteRecord.setNotifyTitle(notifyConfigVo.getTitle());
                apmNotifyExecuteRecord.setInstanceBid(mSpaceAppData.getBid());
                apmNotifyExecuteRecord.setSpaceAppBid(mSpaceAppData.getSpaceAppBid());
                apmNotifyExecuteRecord.setModelCode(mSpaceAppData.getModelCode());
                apmNotifyExecuteRecord.setCreatedTime(new Date());
                if(apmNotifyExecuteRecord.getNofifyNow() || apmNotifyExecuteRecord.getNofifyTime().before(new Date())){
                    if(sendBeforeCheck(apmNotifyExecuteRecord)){
                        notifyMessageService.pushMsg(apmNotifyExecuteRecord);
                    }else {
                        log.info("消息发送前检查失败，消息不发送,消息内容:{}", JSON.toJSONString(apmNotifyExecuteRecord));
                    }
                    //立即发送z
                }else{
                    apmNotifyExecuteRecordArrayList.add(apmNotifyExecuteRecord);
                }
            }
        }
        if (apmNotifyExecuteRecordArrayList.isEmpty()) {
            return null;
        }
        // 保存通知记录
        boolean b = apmNotifyExecuteRecordService.saveBatch(apmNotifyExecuteRecordArrayList);
        return apmNotifyExecuteRecordArrayList;
    }

    @Override
    public Boolean sendBeforeCheck(ApmNotifyExecuteRecord apmNotifyExecuteRecord) {
        List<NotifyConfigVo> notifyConfigVos = notifyAppService.listNotifyConfig(
                NotifyConfigDto.builder().bid(apmNotifyExecuteRecord.getNotifyConfigBid()).tenantCode("apm").bizType("APP").build());
        if (CollectionUtils.isEmpty(notifyConfigVos)) {
            return true;
        }
        NotifyConfigVo notifyConfigVo = notifyConfigVos.get(0);
        if (!notifyConfigVo.getSendCheck()) {
            return true;
        }
        String nullStr = "null";
        if (StringUtils.isNotEmpty(notifyConfigVo.getExecuteClass()) && !nullStr.equalsIgnoreCase(notifyConfigVo.getExecuteClass())) {
            try {
                Class<?> clazz = Class.forName(notifyConfigVo.getExecuteClass().split("#")[0]);
                Method method = clazz.getMethod(notifyConfigVo.getExecuteClass().split("#")[1],ApmNotifyExecuteRecord.class);
                return (Boolean) method.invoke(PlmContextHolder.getBean(clazz),apmNotifyExecuteRecord);
            } catch (Exception e) {
                log.error("找不到类", e);
                return false;
            }
        } else {
            AtomicBoolean check = new AtomicBoolean(true);
            NotifyConfigTriggerVo notifyConfigTriggerVo = notifyConfigVo.getNotifyConfigTriggerVo();
            NotifyConfigOperateVo notifyConfigOperateVo = notifyConfigVo.getNotifyConfigOperateVo();
            if (notifyConfigTriggerVo == null && notifyConfigOperateVo == null) {
                return check.get();
            } else if (notifyConfigTriggerVo != null) {
                List<NotifyTriggerRuleVo> notifyTriggerRuleVos = notifyConfigTriggerVo.getNotifyTriggerRuleVos();
                notifyTriggerRuleVos = notifyTriggerRuleVos.stream().filter(e -> e.getRuleType() ==2).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(notifyTriggerRuleVos)) {
                    //组装查询条件
                    ModelMixQo modelMixQo = new ModelMixQo();
                    List<ModelFilterQo> modelFilterQos = getModelFilterQos(notifyTriggerRuleVos, getConditionMap());
                    modelMixQo.setAnyMatch(false);
                    modelMixQo.setQueries(modelFilterQos);
                    //查询实例
                    List<MSpaceAppData> mObjects = apmSpaceAppDataDrivenService.listByModelMixQo(notifyConfigVo.getBizBid(), modelMixQo);
                    if (CollectionUtils.isEmpty(mObjects)) {
                        check.set(false);
                    } else {
                        check.set(mObjects.stream().anyMatch(e -> StringUtils.isNotEmpty(apmNotifyExecuteRecord.getInstanceBid()) && apmNotifyExecuteRecord.getInstanceBid().equals(e.get(BaseDataEnum.BID.getCode()))));
                    }
                }
            } else {
                List<NotifyTriggerRuleVo> notifyTriggerRuleVos = notifyConfigOperateVo.getNotifyTriggerRuleVos();
                notifyTriggerRuleVos = notifyTriggerRuleVos.stream().filter(e -> e.getRuleType() ==2).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(notifyTriggerRuleVos)) {
                    //组装查询条件
                    ModelMixQo modelMixQo = new ModelMixQo();
                    List<ModelFilterQo> modelFilterQos = getModelFilterQos(notifyTriggerRuleVos, getConditionMap());
                    modelMixQo.setAnyMatch(false);
                    modelMixQo.setQueries(modelFilterQos);
                    //查询实例
                    List<MSpaceAppData> mObjects = apmSpaceAppDataDrivenService.listByModelMixQo(notifyConfigVo.getBizBid(), modelMixQo);
                    if (CollectionUtils.isEmpty(mObjects)) {
                        check.set(false);
                    }else {
                        check.set(mObjects.stream().anyMatch(e -> StringUtils.isNotEmpty(apmNotifyExecuteRecord.getInstanceBid()) && apmNotifyExecuteRecord.getInstanceBid().equals(e.get(BaseDataEnum.BID.getCode()))));
                    }
                }
            }
            return check.get();
        }
    }
    private String getLifeCycleName(String lifeCycleCode,Map<String,String> lifeCycleMap){
        if(lifeCycleMap.containsKey(lifeCycleCode)){
            return lifeCycleMap.get(lifeCycleCode);
        }
        List<String> lifeCycleList = new ArrayList<>();
        lifeCycleList.add(lifeCycleCode);
        try {
            List<LifeCycleStateVo> lifeCycleStateVos = lifeCycleFeignClient.queryByCodes(lifeCycleList).getCheckExceptionData();
            if(CollectionUtils.isNotEmpty(lifeCycleStateVos)){
                return lifeCycleStateVos.get(0).getName();
            }
        }catch (Exception e){
           log.error("获取生命周期名称失败",e);
        }
        return lifeCycleCode;
    }
    private List<ModelFilterQo> getModelFilterQos(List<NotifyTriggerRuleVo> notifyTriggerRuleVos,Map<String,String> conditionMap){
        List<ModelFilterQo> modelFilterQos = new ArrayList<>();
        for(NotifyTriggerRuleVo notifyTriggerRuleVo : notifyTriggerRuleVos){
            ModelFilterQo modelFilterQo = new ModelFilterQo();
            if("date".equals(notifyTriggerRuleVo.getRelationValueType()) && "nowTime".equals(notifyTriggerRuleVo.getRelationValue())){
                SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String nowTime = sd.format(new Date());
                if(org.apache.commons.lang3.StringUtils.isNotEmpty(notifyTriggerRuleVo.getComparisonValue())){
                    int compareValue = Integer.parseInt(notifyTriggerRuleVo.getComparisonValue());
                    nowTime = getDateAddDay(nowTime,compareValue,"yyyy-MM-dd HH:mm:ss");
                }
                modelFilterQo.setValue(nowTime);
            }else{
                modelFilterQo.setValue(notifyTriggerRuleVo.getRelationValue());
            }
            modelFilterQo.setProperty(notifyTriggerRuleVo.getAttrCode());
            modelFilterQo.setCondition(conditionMap.get(notifyTriggerRuleVo.getRelationship()));
            modelFilterQos.add(modelFilterQo);
        }
        return modelFilterQos;
    }
    public Map<String,String> getConditionMap(){
        //组装查询条件
        //比较条件：contain.包含，notContain.不包含，equal.等于，notEqual.不等于，null.为空，notNull.不为空，gt.大于，lt.小于，notGt.小于等于，notLt.大于等于',
        Map<String,String> conditionMap = new HashMap<>(16);
        conditionMap.put("contain", QueryFilterConditionEnum.LIKE.getFilter());
        conditionMap.put("notContain",QueryFilterConditionEnum.NOT_LIKE.getFilter());
        conditionMap.put("equal",QueryFilterConditionEnum.EQ.getFilter());
        conditionMap.put("notEqual",QueryFilterConditionEnum.NE.getFilter());
        conditionMap.put("null",QueryFilterConditionEnum.IS_NULL.getFilter());
        conditionMap.put("notNull",QueryFilterConditionEnum.IS_NOT_NULL.getFilter());
        conditionMap.put("gt",QueryFilterConditionEnum.GT.getFilter());
        conditionMap.put("lt",QueryFilterConditionEnum.LT.getFilter());
        conditionMap.put("notGt",QueryFilterConditionEnum.LE.getFilter());
        conditionMap.put("notLt",QueryFilterConditionEnum.GE.getFilter());
        return conditionMap;
    }


}
