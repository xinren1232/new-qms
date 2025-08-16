package com.transcend.plm.datadriven.apm.aspect.notify;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.transcend.plm.datadriven.api.model.QueryFilterConditionEnum;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.api.model.qo.ModelFilterQo;
import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transcend.plm.datadriven.apm.integration.message.QueueNameConstant;
import com.transcend.plm.datadriven.apm.integration.publisher.IPublishService;
import com.transcend.plm.datadriven.apm.powerjob.notify.analysis.service.NotifyAnalysis;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmNotifyExecuteRecord;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.common.tool.QueryConveterTool;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.notify.domain.ObjectAttrValue;
import com.transcend.plm.datadriven.notify.dto.NotifyConfigDto;
import com.transcend.plm.datadriven.notify.service.NotifyAppService;
import com.transcend.plm.datadriven.notify.vo.NotifyConfigOperateVo;
import com.transcend.plm.datadriven.notify.vo.NotifyConfigTriggerVo;
import com.transcend.plm.datadriven.notify.vo.NotifyConfigVo;
import com.transcend.plm.datadriven.notify.vo.NotifyTriggerRuleVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author unknown
 */
@Slf4j
@Service
public class OperateBusiService {
    @Resource
    private NotifyAppService notifyAppService;
    @Resource
    private NotifyAnalysis notifyAnalysis;
    @Resource
    private IApmSpaceAppDataDrivenService apmSpaceAppDataDrivenService;
    @Resource
    private IPublishService rabbitPublishService;

    public List<NotifyConfigVo> getTriggerConfigs(){
        NotifyConfigDto notifyConfigDto =NotifyConfigDto.builder().
                tenantCode("apm").bizType("APP").type("2").enableFlagInt(1).build();
        List<NotifyConfigVo> notifyConfigVos = notifyAppService.listNotifyConfig(notifyConfigDto);
        return notifyConfigVos;
    }


    @Async
    public void executetTriggerConfig(NotifyConfigVo notifyConfigVo, Map<String,String> conditionMap){
        NotifyConfigTriggerVo notifyConfigTriggerVo = notifyConfigVo.getNotifyConfigTriggerVo();
        if(notifyConfigTriggerVo == null){
            log.info("notifyConfigTriggerVo is null,notifyConfigVo.bid:{}",notifyConfigVo.getBid());
            return;
        }
        //根据多线程新增的数据和规则,发送消息通知
        List<NotifyTriggerRuleVo> notifyTriggerRuleVos = notifyConfigTriggerVo.getNotifyTriggerRuleVos();
        notifyTriggerRuleVos = notifyTriggerRuleVos.stream().filter(e -> e.getRuleType() != null && e.getRuleType() == 1).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(notifyTriggerRuleVos)){
            //组装查询条件
            ModelMixQo modelMixQo = new ModelMixQo();
            List<ModelFilterQo> modelFilterQos = getModelFilterQos(notifyTriggerRuleVos,conditionMap);
            modelMixQo.setAnyMatch(false);
            modelMixQo.setQueries(modelFilterQos);
            //查询实例
            //记录查询条件日志
            List<QueryWrapper> wrappers = QueryConveterTool.convert(modelMixQo).getQueries();
            QueryWrapper qo = new QueryWrapper();
            qo.eq(TranscendModelBaseFields.DELETE_FLAG, 0).and().eq(TranscendModelBaseFields.SPACE_APP_BID, notifyConfigVo.getBizBid());
            wrappers = QueryConveterTool.appendMoreQueriesAndCondition(wrappers,QueryWrapper.buildSqlQo(qo));
            String wrappersStr = JSON.toJSONString(wrappers);
            log.info("trigger rule for query ins wrapper,spaceAppBid:{},查询条件:{}",notifyConfigVo.getBizBid(),JSON.toJSONString(modelMixQo));
            List<MSpaceAppData> mObjects = apmSpaceAppDataDrivenService.listByModelMixQo(notifyConfigVo.getBizBid(),modelMixQo);
            if (CollectionUtils.isNotEmpty(mObjects)){
                for(MSpaceAppData mSpaceAppData : mObjects){
                    mSpaceAppData.put("WRAPPERS_STR_RECORD",wrappersStr);
                }
                log.info("time analysis ins list:{}",JSON.toJSONString(mObjects));
                //发送通知
                List<ApmNotifyExecuteRecord> analysis = notifyAnalysis.analysis(Collections.singletonList(notifyConfigVo), mObjects,true);
                log.info("notify execute list:{}",JSON.toJSONString(analysis));
                if (analysis != null && !analysis.isEmpty()) {
                    for (ApmNotifyExecuteRecord apmNotifyExecuteRecord : analysis) {
                        // 判断如果过期时间小于今天,则添加到时间轮中
                        if (apmNotifyExecuteRecord.getNofifyResult() != 3) {
                            log.info("新增实例通知时间大于今天,不添加到时间轮中");
                            continue;
                        }
                        //放入延迟消除队列
                        rabbitPublishService.publishWithDelay(QueueNameConstant.EXCHANGE_INSTANCE_DELAY,QueueNameConstant.ROUTING_KEY_DELAY,apmNotifyExecuteRecord,apmNotifyExecuteRecord.getNofifyTime().getTime()-System.currentTimeMillis());
                    }
                }
            }
        }
    }

    private String getDateAddDay(String date, int days, String pattern) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(df.parse(date));
            cal.set(6, cal.get(6) + days);
            return df.format(cal.getTime());
        } catch (ParseException var5) {
            throw new RuntimeException(var5);
        }
    }

    private List<ModelFilterQo> getModelFilterQos(List<NotifyTriggerRuleVo> notifyTriggerRuleVos,Map<String,String> conditionMap){
        List<ModelFilterQo> modelFilterQos = new ArrayList<>();
        for(NotifyTriggerRuleVo notifyTriggerRuleVo : notifyTriggerRuleVos){
            ModelFilterQo modelFilterQo = new ModelFilterQo();
            if("date".equals(notifyTriggerRuleVo.getRelationValueType()) && "nowTime".equals(notifyTriggerRuleVo.getRelationValue())){
                SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String nowTime = sd.format(new Date());
              if(StringUtils.isNotEmpty(notifyTriggerRuleVo.getComparisonValue())){
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
        conditionMap.put("contain",QueryFilterConditionEnum.LIKE.getFilter());
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

    @Async
    public void executeNotify(String spaceAppBid, MSpaceAppData result){
        NotifyConfigDto notifyConfigDto = getNotifyConfigDto(spaceAppBid);
        List<NotifyConfigVo> notifyConfigVos = notifyAppService.listNotifyConfig(notifyConfigDto);
        if (notifyConfigVos == null || notifyConfigVos.isEmpty()) {
            log.info("未配置通知信息");
            return;
        }
        //从消息通知配置中过滤出操作实例新增的通知配置
        List<NotifyConfigVo> addNotifyConfigVos = notifyConfigVos.stream().filter(
                notifyConfigVo -> "1".equals(notifyConfigVo.getType())
                        && notifyConfigVo.getNotifyConfigOperateVo() != null
                        && "CREATE".equals(notifyConfigVo.getNotifyConfigOperateVo().getOperate())
        ).collect(Collectors.toList());
        if (addNotifyConfigVos.isEmpty()) {
            log.info("未配置新增实例通知信息");
            return;
        }
        //根据多线程新增的数据和规则,发送消息通知

            log.info("新增实例{}", JSONObject.toJSONString(result));
            for(NotifyConfigVo notifyConfigVo : addNotifyConfigVos){
                //处理通知内容
                String notifyContent = notifyConfigVo.getNotifyContent();
                if(StringUtils.isNotEmpty(notifyContent)){
                }else{
                    notifyContent = "新增实例:"+result.get(TranscendModelBaseFields.NAME);
                }
                notifyConfigVo.setNotifyContent(notifyContent);
            }
            List<ApmNotifyExecuteRecord> analysis = notifyAnalysis.analysis(addNotifyConfigVos, Collections.singletonList((MSpaceAppData) result),false);
            if (analysis != null && !analysis.isEmpty()) {
                for (ApmNotifyExecuteRecord apmNotifyExecuteRecord : analysis) {
                    // 判断如果过期时间小于今天,则添加到时间轮中
                    if (apmNotifyExecuteRecord.getNofifyResult() != 3) {
                        log.info("新增实例通知时间大于今天,不添加到时间轮中");
                        continue;
                    }
                    //放入延迟消除队列
                    rabbitPublishService.publishWithDelay(QueueNameConstant.EXCHANGE_INSTANCE_DELAY,QueueNameConstant.ROUTING_KEY_DELAY,apmNotifyExecuteRecord,apmNotifyExecuteRecord.getNofifyTime().getTime()-System.currentTimeMillis());
                }
            }
    }

    private NotifyConfigDto getNotifyConfigDto(String spaceAppBid) {
        NotifyConfigDto notifyConfigDto =NotifyConfigDto.builder().
        tenantCode("apm").bizBid(spaceAppBid).bizType("APP").type("1").enableFlagInt(1).build();
        return notifyConfigDto;
    }

    @Async
    public void executeNotifyUpdate(String spaceAppBid,String instanceBid, MSpaceAppData result){
        NotifyConfigDto notifyConfigDto = getNotifyConfigDto(spaceAppBid);
        List<NotifyConfigVo> notifyConfigVos = notifyAppService.listNotifyConfig(notifyConfigDto);
        if (notifyConfigVos == null || notifyConfigVos.isEmpty()) {
            log.info("未配置通知信息");
            return;
        }
        //从消息通知配置中过滤出操作实例新增的通知配置
        List<NotifyConfigVo> addNotifyConfigVos = notifyConfigVos.stream().filter(
                notifyConfigVo -> "1".equals(notifyConfigVo.getType())
                        && notifyConfigVo.getNotifyConfigOperateVo() != null
                        && "UPDATE".equals(notifyConfigVo.getNotifyConfigOperateVo().getOperate())
        ).collect(Collectors.toList());
        if (addNotifyConfigVos.isEmpty()) {
            log.info("未配置新增实例通知信息");
            return;
        }
        //先过滤数据
        for(int i = addNotifyConfigVos.size() - 1; i >= 0; i--){
            boolean isUpdateNotify = false;
            NotifyConfigVo notifyConfigVo = addNotifyConfigVos.get(i);
            NotifyConfigOperateVo notifyConfigOperateVo = notifyConfigVo.getNotifyConfigOperateVo();
            if (notifyConfigOperateVo != null) {
                List<String> operateAttrs = notifyConfigOperateVo.getOperateAttrs();
                Map<String, ObjectAttrValue> ObjectAttrValueMap = notifyConfigOperateVo.getOperateAttrValues();
                if(ObjectAttrValueMap == null){
                    ObjectAttrValueMap = new HashMap<>(16);
                }
                if (CollectionUtils.isNotEmpty(operateAttrs) && result != null) {
                    for (String attr : operateAttrs) {
                        if (result.containsKey(attr)) {
                            ObjectAttrValue objectAttrValue = ObjectAttrValueMap.get(attr);
                            if(objectAttrValue == null){
                                isUpdateNotify = true;
                            }else{
                                //判断匹配的值是否对
                                if(objectAttrValue.getValue().equals(result.get(attr)+"")){
                                    isUpdateNotify = true;
                                }
                            }
                        }
                    }
                }
            }
            if(!isUpdateNotify){
                addNotifyConfigVos.remove(i);
            }
        }
        log.info("修改实例{}", JSONObject.toJSONString(result));
        for(NotifyConfigVo notifyConfigVo : addNotifyConfigVos){
            NotifyConfigOperateVo notifyConfigOperateVo = notifyConfigVo.getNotifyConfigOperateVo();
            if(notifyConfigOperateVo == null){
                continue;
            }
            //匹配到有需要通知的字段
                MSpaceAppData mSpaceAppData = apmSpaceAppDataDrivenService.get(spaceAppBid, instanceBid,false);
                if(mSpaceAppData == null){
                    log.error("实例{}不存在", instanceBid);
                }else {
                    mSpaceAppData.putAll(result);
                    //处理通知内容
                    String notifyContent = notifyConfigVo.getNotifyContent();
                    if(StringUtils.isNotEmpty(notifyContent)){

                    }else{
                        notifyContent = "实例:"+mSpaceAppData.get(TranscendModelBaseFields.NAME)+"发生变更";
                    }
                    notifyConfigVo.setNotifyContent(notifyContent);
                    List<ApmNotifyExecuteRecord> analysis = notifyAnalysis.analysis(addNotifyConfigVos, Collections.singletonList(mSpaceAppData),false);
                    if (analysis != null && !analysis.isEmpty()) {
                        for (ApmNotifyExecuteRecord apmNotifyExecuteRecord : analysis) {
                            // 判断如果过期时间小于今天,则添加到时间轮中
                            if (apmNotifyExecuteRecord.getNofifyResult() != 3) {
                                log.info("新增实例通知时间大于今天,不添加到时间轮中");
                                continue;
                            }
                            //放入延迟消除队列
                            rabbitPublishService.publishWithDelay(QueueNameConstant.EXCHANGE_INSTANCE_DELAY,QueueNameConstant.ROUTING_KEY_DELAY,apmNotifyExecuteRecord,apmNotifyExecuteRecord.getNofifyTime().getTime()-System.currentTimeMillis());
                        }
                    }
                }

        }
    }

}
