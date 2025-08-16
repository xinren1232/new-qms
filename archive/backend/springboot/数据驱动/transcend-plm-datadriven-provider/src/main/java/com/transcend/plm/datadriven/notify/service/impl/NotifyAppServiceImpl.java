package com.transcend.plm.datadriven.notify.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.common.enums.DefaultParamEnum;
import com.transcend.plm.datadriven.common.pojo.po.TranscendTableNameHandler;
import com.transcend.plm.datadriven.common.tool.CommonConstant;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.common.util.SnowflakeIdWorker;
import com.transcend.plm.datadriven.notify.domain.*;
import com.transcend.plm.datadriven.notify.dto.NotifyConfigDto;
import com.transcend.plm.datadriven.notify.dto.NotifyConfigTriggerDto;
import com.transcend.plm.datadriven.notify.mapstruct.*;
import com.transcend.plm.datadriven.notify.nofifyEnum.NotifyEnum;
import com.transcend.plm.datadriven.notify.service.*;
import com.transcend.plm.datadriven.notify.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通知服务实现类
 *
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Service
public class NotifyAppServiceImpl implements NotifyAppService {
    @Resource
    private NotifyConfigOperateService notifyConfigOperateService;
    @Resource
    private NotifyConfigService notifyConfigService;

    @Resource
    private NotifyConfigTriggerService notifyConfigTriggerService;

    @Resource
    private NotifyTimeRuleService notifyTimeRuleService;

    @Resource
    private NotifyTriggerRuleService notifyTriggerRuleService;


    /**
     * @param dto
     * @return boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveNotifyConfig(NotifyConfigDto dto) {
        TranscendTableNameHandler.setTenantCode(dto.getTenantCode());
        NotifyConfig notifyConfig = NotifyConfigConverter.INSTANCE.dto2Po(dto);
        if(StringUtils.isEmpty(dto.getBid())){
            notifyConfig.setBid(SnowflakeIdWorker.nextIdStr());
        }
        // 保存通知配置
        if(NotifyEnum.OPERATE_TYPE.getCode().equals(dto.getType())){
            // 操作类型
            NotifyConfigOperate notifyConfigOperate = dto.getNotifyConfigOperate();
            if(notifyConfigOperate != null){
                notifyConfigOperate.setId(null);
                notifyConfigOperate.setBid(SnowflakeIdWorker.nextIdStr());
                notifyConfigOperate.setNotifyConfigBid(notifyConfig.getBid());
                if(StringUtils.isNotEmpty(dto.getBid())){
                    //需要先删除原来的数据
                    List<NotifyTriggerRule> NotifyTriggerRules = notifyTriggerRuleService.list(Wrappers.<NotifyTriggerRule>lambdaQuery().eq(NotifyTriggerRule::getNotifyConfigBid, dto.getBid()).eq(NotifyTriggerRule::getDeleteFlag,false));
                    if(CollectionUtils.isNotEmpty(NotifyTriggerRules)){
                        notifyTriggerRuleService.removeBatchByIds(NotifyTriggerRules.stream().map(NotifyTriggerRule::getId).collect(Collectors.toList()));
                    }
                    NotifyConfigOperate notifyConfigOperate1 = notifyConfigOperateService.getOne(Wrappers.<NotifyConfigOperate>lambdaQuery().eq(NotifyConfigOperate::getNotifyConfigBid, dto.getBid()).eq(NotifyConfigOperate::getDeleteFlag,false));
                    if(notifyConfigOperate1 != null){
                        notifyConfigOperateService.removeById(notifyConfigOperate1.getId());
                    }
                }
                if (dto.getNotifyConfigTriggerDto() != null && CollectionUtils.isNotEmpty(dto.getNotifyConfigTriggerDto().getNotifyTriggerRules())) {
                    List<NotifyTriggerRule> notifyTriggerRules = dto.getNotifyConfigTriggerDto().getNotifyTriggerRules();
                    for(NotifyTriggerRule notifyTriggerRule : notifyTriggerRules){
                        notifyTriggerRule.setId(null);
                        notifyTriggerRule.setBid(SnowflakeIdWorker.nextIdStr());
                        notifyTriggerRule.setNotifyConfigTriggerBid(notifyConfigOperate.getBid());
                        notifyTriggerRule.setNotifyConfigBid(notifyConfig.getBid());
                    }
                    notifyTriggerRuleService.saveBatch(notifyTriggerRules);
                }
                notifyConfigOperateService.save(notifyConfigOperate);
            }
        }else if (NotifyEnum.TRIGGER_TYPE.getCode().equals(dto.getType())){
            // 触发类型
            NotifyConfigTriggerDto notifyConfigTriggerDto = dto.getNotifyConfigTriggerDto();
            if(notifyConfigTriggerDto != null){
                NotifyConfigTrigger notifyConfigTrigger = NotifyConfigTriggerConverter.INSTANCE.dto2Po(notifyConfigTriggerDto);
                notifyConfigTrigger.setBid(SnowflakeIdWorker.nextIdStr());
                notifyConfigTrigger.setNotifyConfigBid(notifyConfig.getBid());
                if(StringUtils.isNotEmpty(dto.getBid())){
                    //需要先删除原来的数据
                    List<NotifyTriggerRule> NotifyTriggerRules = notifyTriggerRuleService.list(Wrappers.<NotifyTriggerRule>lambdaQuery().eq(NotifyTriggerRule::getNotifyConfigBid, dto.getBid()).eq(NotifyTriggerRule::getDeleteFlag,false));
                    if(CollectionUtils.isNotEmpty(NotifyTriggerRules)){
                        notifyTriggerRuleService.removeBatchByIds(NotifyTriggerRules.stream().map(NotifyTriggerRule::getId).collect(Collectors.toList()));
                    }
                    NotifyConfigTrigger notifyConfigTrigger1 = notifyConfigTriggerService.getOne(Wrappers.<NotifyConfigTrigger>lambdaQuery().eq(NotifyConfigTrigger::getNotifyConfigBid, dto.getBid()).eq(NotifyConfigTrigger::getDeleteFlag,false));
                    if(notifyConfigTrigger1 != null){
                        notifyConfigTriggerService.removeById(notifyConfigTrigger1.getId());
                    }
                }
                List<NotifyTriggerRule> notifyTriggerRules = notifyConfigTriggerDto.getNotifyTriggerRules();
                if(CollectionUtils.isNotEmpty(notifyTriggerRules)){
                    for(NotifyTriggerRule notifyTriggerRule : notifyTriggerRules){
                        notifyTriggerRule.setId(null);
                        notifyTriggerRule.setBid(SnowflakeIdWorker.nextIdStr());
                        notifyTriggerRule.setNotifyConfigTriggerBid(notifyConfigTrigger.getBid());
                        notifyTriggerRule.setNotifyConfigBid(notifyConfig.getBid());
                    }
                    notifyTriggerRuleService.saveBatch(notifyTriggerRules);
                }
                notifyConfigTriggerService.save(notifyConfigTrigger);
            }
        }
        NotifyTimeRule notifyTimeRule = dto.getNotifyTimeRule();
        if(notifyTimeRule != null){
            notifyTimeRule.setId(null);
            notifyTimeRule.setBid(SnowflakeIdWorker.nextIdStr());
            notifyTimeRule.setNotifyConfigBid(notifyConfig.getBid());
            if(StringUtils.isNotEmpty(dto.getBid())){
                //需要先删除原来的数据
                NotifyTimeRule notifyTimeRule1 = notifyTimeRuleService.getOne(Wrappers.<NotifyTimeRule>lambdaQuery().eq(NotifyTimeRule::getNotifyConfigBid, dto.getBid()));
                if(notifyTimeRule1 != null){
                    notifyTimeRuleService.removeById(notifyTimeRule1.getId());
                }
            }
            notifyTimeRuleService.save(notifyTimeRule);
        }
        if(StringUtils.isEmpty(dto.getBid())){
            notifyConfig.setId(null);
            notifyConfigService.save(notifyConfig);
        }else{
            LambdaUpdateWrapper<NotifyConfig> updateWrapper = Wrappers.<NotifyConfig>lambdaUpdate().eq(NotifyConfig::getBid, dto.getBid());
            updateWrapper.set(NotifyConfig::getNotifyContent, notifyConfig.getNotifyContent());
            updateWrapper.set(NotifyConfig::getNotifyWay, notifyConfig.getNotifyWay());
            updateWrapper.set(NotifyConfig::getTitle, notifyConfig.getTitle());
            updateWrapper.set(NotifyConfig::getNotifyJobnumbers, JSON.toJSON(notifyConfig.getNotifyJobnumbers()));
            updateWrapper.set(NotifyConfig::getNotifyRoleCodes, JSON.toJSON(notifyConfig.getNotifyRoleCodes()));
            updateWrapper.set(NotifyConfig::getDeleteFlag, notifyConfig.getDeleteFlag());
            updateWrapper.set(NotifyConfig::getEnableFlag, notifyConfig.getEnableFlag());
            updateWrapper.set(StringUtils.isNotEmpty(notifyConfig.getType()),NotifyConfig::getType, notifyConfig.getType());
            updateWrapper.set(StringUtils.isNotEmpty(notifyConfig.getBizBid()),NotifyConfig::getBizBid, notifyConfig.getBizBid());
            updateWrapper.set(StringUtils.isNotEmpty(notifyConfig.getBizType()),NotifyConfig::getBizType, notifyConfig.getBizType());
            notifyConfigService.update(updateWrapper);
        }
        return true;
    }

    /**
     * @param dtos
     * @return boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveNotifyConfigs(List<NotifyConfigDto> dtos) {
        if(CollectionUtils.isEmpty(dtos)){
            return true;
        }
        TranscendTableNameHandler.setTenantCode(dtos.get(0).getTenantCode());
        List<NotifyConfigOperate> notifyConfigOperates = new ArrayList<>();
        List<NotifyTriggerRule> notifyTriggerRules = new ArrayList<>();
        List<NotifyConfigTrigger> notifyConfigTriggers = new ArrayList<>();
        List<NotifyTimeRule> notifyTimeRules = new ArrayList<>();
        List<NotifyConfig> notifyConfigs = new ArrayList<>();
        for(NotifyConfigDto dto : dtos){
            NotifyConfig notifyConfig = NotifyConfigConverter.INSTANCE.dto2Po(dto);
            notifyConfig.setBid(SnowflakeIdWorker.nextIdStr());
            // 保存通知配置
            if(NotifyEnum.OPERATE_TYPE.getCode().equals(dto.getType())){
                // 操作类型
                NotifyConfigOperate notifyConfigOperate = dto.getNotifyConfigOperate();
                notifyConfigOperate.setBid(SnowflakeIdWorker.nextIdStr());
                notifyConfigOperate.setNotifyConfigBid(notifyConfig.getBid());
                if (dto.getNotifyConfigTriggerDto() != null && CollectionUtils.isNotEmpty(dto.getNotifyConfigTriggerDto().getNotifyTriggerRules())) {
                    List<NotifyTriggerRule> notifyTriggerRules1 = dto.getNotifyConfigTriggerDto().getNotifyTriggerRules();
                    for(NotifyTriggerRule notifyTriggerRule : notifyTriggerRules1){
                        notifyTriggerRule.setId(null);
                        notifyTriggerRule.setBid(SnowflakeIdWorker.nextIdStr());
                        notifyTriggerRule.setNotifyConfigTriggerBid(notifyConfigOperate.getBid());
                        notifyTriggerRule.setNotifyConfigBid(notifyConfig.getBid());
                    }
                    notifyTriggerRules.addAll(notifyTriggerRules1);
                }

                notifyConfigOperates.add(notifyConfigOperate);
            }else if (NotifyEnum.TRIGGER_TYPE.getCode().equals(dto.getType())){
                // 触发类型
                NotifyConfigTriggerDto notifyConfigTriggerDto = dto.getNotifyConfigTriggerDto();
                NotifyConfigTrigger notifyConfigTrigger = NotifyConfigTriggerConverter.INSTANCE.dto2Po(notifyConfigTriggerDto);
                notifyConfigTrigger.setBid(SnowflakeIdWorker.nextIdStr());
                notifyConfigTrigger.setNotifyConfigBid(notifyConfig.getBid());
                List<NotifyTriggerRule> notifyTriggerRules1 = notifyConfigTriggerDto.getNotifyTriggerRules();
                if(CollectionUtils.isNotEmpty(notifyTriggerRules1)){
                    for(NotifyTriggerRule notifyTriggerRule : notifyTriggerRules1){
                        notifyTriggerRule.setBid(SnowflakeIdWorker.nextIdStr());
                        notifyTriggerRule.setNotifyConfigTriggerBid(notifyConfigTrigger.getBid());
                        notifyTriggerRule.setNotifyConfigBid(notifyConfig.getBid());
                    }
                }
                notifyTriggerRules.addAll(notifyTriggerRules1);
                notifyConfigTriggers.add(notifyConfigTrigger);
            }
            NotifyTimeRule notifyTimeRule = dto.getNotifyTimeRule();
            notifyTimeRule.setBid(SnowflakeIdWorker.nextIdStr());
            notifyTimeRule.setNotifyConfigBid(notifyConfig.getBid());
            notifyTimeRules.add(notifyTimeRule);
            notifyConfigs.add(notifyConfig);
        }if(CollectionUtils.isNotEmpty(notifyConfigs)){
            notifyConfigService.saveBatch(notifyConfigs);
        }
        if(CollectionUtils.isNotEmpty(notifyTimeRules)){
            notifyTimeRuleService.saveBatch(notifyTimeRules);
        }
        if(CollectionUtils.isNotEmpty(notifyConfigTriggers)){
            notifyConfigTriggerService.saveBatch(notifyConfigTriggers);
        }
        if(CollectionUtils.isNotEmpty(notifyTriggerRules)){
            notifyTriggerRuleService.saveBatch(notifyTriggerRules);
        }
        if(CollectionUtils.isNotEmpty(notifyConfigOperates)){
            notifyConfigOperateService.saveBatch(notifyConfigOperates);
        }
        return true;
    }

    /**
     * 获取操作通知配置
     *
     * @param bizType
     * @param bizBid
     * @param operateType
     * @param tenantCode
     * @return
     */
    @Override
    @Cacheable(value = "NOTIFY_CONFIG", key = "#bizType+'_'+#bizBid+'_'+#operateType+'_'+#tenantCode")
    public NotifyConfigVo getOperateConfig(String bizType, String bizBid, String operateType,String tenantCode){
        TranscendTableNameHandler.setTenantCode(tenantCode);
        List<NotifyConfig> notifyConfigs = notifyConfigService.list(Wrappers.<NotifyConfig>lambdaQuery().eq(NotifyConfig::getBizType,bizType).eq(NotifyConfig::getBizBid,bizBid).eq(NotifyConfig::getType,NotifyEnum.OPERATE_TYPE.getCode()).eq(NotifyConfig::getDeleteFlag,false).eq(NotifyConfig::getEnableFlag,1));
        if(CollectionUtils.isNotEmpty(notifyConfigs)){
            List<String> bids = notifyConfigs.stream().map(NotifyConfig::getBid).collect(Collectors.toList());
            Map<String,NotifyConfig> notifyConfigMap = notifyConfigs.stream().collect(Collectors.toMap(NotifyConfig::getBid, Function.identity()));
            List<NotifyConfigOperate> notifyConfigOperates = notifyConfigOperateService.list(Wrappers.<NotifyConfigOperate>lambdaQuery().in(NotifyConfigOperate::getNotifyConfigBid,bids).eq(NotifyConfigOperate::getOperate,operateType).eq(NotifyConfigOperate::getDeleteFlag,false));
            if(CollectionUtils.isNotEmpty(notifyConfigOperates)){
                NotifyConfigOperate notifyConfigOperate = notifyConfigOperates.get(0);
                NotifyConfig notifyConfig = notifyConfigMap.get(notifyConfigOperate.getNotifyConfigBid());
                NotifyTimeRule notifyTimeRule = notifyTimeRuleService.getOne(Wrappers.<NotifyTimeRule>lambdaQuery().eq(NotifyTimeRule::getNotifyConfigBid,notifyConfig.getBid()));
                NotifyTimeRuleVo notifyTimeRuleVo = NotifyTimeRuleConverter.INSTANCE.po2Vo(notifyTimeRule);
                NotifyConfigOperateVo notifyConfigOperateVo = NotifyConfigOperateConverter.INSTANCE.po2Vo(notifyConfigOperate);
                NotifyConfigVo notifyConfigVo = NotifyConfigConverter.INSTANCE.po2Vo(notifyConfig);
                notifyConfigVo.setNotifyConfigOperateVo(notifyConfigOperateVo);
                notifyConfigVo.setNotifyTimeRuleVo(notifyTimeRuleVo);
                return notifyConfigVo;
            }
        }
        return null;
    }

    /**
     * @param map
     * @return {@link Map }<{@link String },{@link ObjectAttrValue }>
     */
    private Map<String,ObjectAttrValue> anaylis(Map<String,ObjectAttrValue> map){
        if(CollectionUtils.isNotEmpty(map)){
            for(Map.Entry<String,ObjectAttrValue> entry : map.entrySet()){
                if (entry.getValue() != null){
                    ObjectAttrValue objectAttrValue = JSON.parseObject(JSON.toJSONString(entry.getValue()),ObjectAttrValue.class);
                    entry.setValue(objectAttrValue);
                    if("user".equals(objectAttrValue.getType()) && DefaultParamEnum.LOGIN_USER.getCode().equals(objectAttrValue.getValue())){
                        objectAttrValue.setValue(SsoHelper.getJobNumber());
                    }else if ("date".equals(objectAttrValue.getType()) && DefaultParamEnum.NOW_TIME.getCode().equals(objectAttrValue.getValue())){
                        objectAttrValue.setValue(DateUtil.format(new Date(),"yyyy-MM-dd HH:mm:ss"));
                    }
                }
            }
        }
        return map;
    }

    /**
     * @param dto
     * @return {@link List }<{@link NotifyConfigVo }>
     */
    @Override
    public List<NotifyConfigVo> listNotifyConfig(NotifyConfigDto dto){
        //可以使用缓存 查询空间应用的消息通知为高频操作
        TranscendTableNameHandler.setTenantCode(dto.getTenantCode());
        List<NotifyConfigVo> notifyConfigVos = new ArrayList<>();
        List<NotifyConfig> notifyConfigs = notifyConfigService.list(Wrappers.<NotifyConfig>lambdaQuery().eq(NotifyConfig::getBizType,dto.getBizType())
                .eq(StringUtils.isNotEmpty(dto.getBizBid()),NotifyConfig::getBizBid,dto.getBizBid())
                .eq(StringUtils.isNotEmpty(dto.getType()),NotifyConfig::getType,dto.getType())
                .eq(NotifyConfig::getDeleteFlag,false)
                .eq(dto.getEnableFlagInt() != null,NotifyConfig::getEnableFlag,dto.getEnableFlagInt())
                .eq(StringUtils.isNotEmpty(dto.getBid()),NotifyConfig::getBid,dto.getBid()));
       if(CollectionUtils.isNotEmpty(notifyConfigs)){
           List<String> bids = notifyConfigs.stream().map(NotifyConfig::getBid).collect(Collectors.toList());
           List<NotifyConfigTrigger> notifyConfigTriggers = notifyConfigTriggerService.list(Wrappers.<NotifyConfigTrigger>lambdaQuery().in(NotifyConfigTrigger::getNotifyConfigBid,bids).eq(NotifyConfigTrigger::getDeleteFlag,false));
           List<NotifyTimeRule> notifyTimeRules = notifyTimeRuleService.list(Wrappers.<NotifyTimeRule>lambdaQuery().in(NotifyTimeRule::getNotifyConfigBid,bids).eq(NotifyTimeRule::getDeleteFlag,false));
           List<NotifyConfigOperate> notifyConfigOperates = notifyConfigOperateService.list(Wrappers.<NotifyConfigOperate>lambdaQuery().in(NotifyConfigOperate::getNotifyConfigBid,bids).eq(NotifyConfigOperate::getDeleteFlag,false));
           Map<String, NotifyConfigTriggerVo> triggerVoMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
           Map<String, NotifyTimeRuleVo> timeRuleVoMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
           Map<String, NotifyConfigOperateVo> operaterVoMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
           for(NotifyTimeRule notifyTimeRule : notifyTimeRules){
               NotifyTimeRuleVo notifyTimeRuleVo = NotifyTimeRuleConverter.INSTANCE.po2Vo(notifyTimeRule);
               timeRuleVoMap.put(notifyTimeRule.getNotifyConfigBid(),notifyTimeRuleVo);
           }
           if(CollectionUtils.isNotEmpty(notifyConfigOperates)){
               List<String> operateBids = notifyConfigOperates.stream().map(NotifyConfigOperate::getBid).collect(Collectors.toList());
               List<NotifyTriggerRule> notifyTriggerRules = notifyTriggerRuleService.list(Wrappers.<NotifyTriggerRule>lambdaQuery().in(NotifyTriggerRule::getNotifyConfigTriggerBid,operateBids).eq(NotifyTriggerRule::getDeleteFlag,false));
               Map<String,List<NotifyTriggerRule>> triggerRuleMap = notifyTriggerRules.stream().collect(Collectors.groupingBy(NotifyTriggerRule::getNotifyConfigTriggerBid));
               for(NotifyConfigOperate notifyConfigOperate : notifyConfigOperates){
                   NotifyConfigOperateVo notifyConfigOperateVo = NotifyConfigOperateConverter.INSTANCE.po2Vo(notifyConfigOperate);
                   List<NotifyTriggerRule> notifyTriggerRuleMaps = triggerRuleMap.get(notifyConfigOperateVo.getBid());
                   List<NotifyTriggerRuleVo> notifyTriggerRuleVos = NotifyTriggerRuleConverter.INSTANCE.pos2Vos(notifyTriggerRuleMaps);
                   notifyConfigOperateVo.setNotifyTriggerRuleVos(notifyTriggerRuleVos);
                   operaterVoMap.put(notifyConfigOperate.getNotifyConfigBid(),notifyConfigOperateVo);
               }
           }
           if(CollectionUtils.isNotEmpty(notifyConfigTriggers)){
               List<String> triggerBids = notifyConfigTriggers.stream().map(NotifyConfigTrigger::getBid).collect(Collectors.toList());
               List<NotifyTriggerRule> notifyTriggerRules = notifyTriggerRuleService.list(Wrappers.<NotifyTriggerRule>lambdaQuery().in(NotifyTriggerRule::getNotifyConfigTriggerBid,triggerBids).eq(NotifyTriggerRule::getDeleteFlag,false));
               Map<String,List<NotifyTriggerRule>> triggerRuleMap = notifyTriggerRules.stream().collect(Collectors.groupingBy(NotifyTriggerRule::getNotifyConfigTriggerBid));
               for(NotifyConfigTrigger notifyConfigTrigger : notifyConfigTriggers){
                   NotifyConfigTriggerVo notifyConfigTriggerVo = NotifyConfigTriggerConverter.INSTANCE.po2Vo(notifyConfigTrigger);
                   List<NotifyTriggerRule> notifyTriggerRuleMaps = triggerRuleMap.get(notifyConfigTrigger.getBid());
                   List<NotifyTriggerRuleVo> notifyTriggerRuleVos = NotifyTriggerRuleConverter.INSTANCE.pos2Vos(notifyTriggerRuleMaps);
                   notifyConfigTriggerVo.setNotifyTriggerRuleVos(notifyTriggerRuleVos);
                   triggerVoMap.put(notifyConfigTrigger.getNotifyConfigBid(),notifyConfigTriggerVo);
               }
           }
           for(NotifyConfig notifyConfig : notifyConfigs){
               NotifyConfigVo notifyConfigVo = NotifyConfigConverter.INSTANCE.po2Vo(notifyConfig);
               notifyConfigVo.setNotifyConfigTriggerVo(triggerVoMap.get(notifyConfig.getBid()));
               notifyConfigVo.setNotifyConfigOperateVo(operaterVoMap.get(notifyConfig.getBid()));
               notifyConfigVo.setNotifyTimeRuleVo(timeRuleVoMap.get(notifyConfig.getBid()));
               notifyConfigVos.add(notifyConfigVo);
           }
       }
       return notifyConfigVos;
    }

    /**
     * @return {@link NotifyExecuteTimeVo }
     */
    private NotifyExecuteTimeVo getNotifyExecuteTimeNow(){
        NotifyExecuteTimeVo notifyExecuteTimeVo = new NotifyExecuteTimeVo();
        notifyExecuteTimeVo.setExecuteTime(new Date());
        notifyExecuteTimeVo.setExecuteTimeStr(DateUtil.formatDateTime(new Date()));
        notifyExecuteTimeVo.setIsNow(true);
        return notifyExecuteTimeVo;
    }

    /**
     * @param hour
     * @param minute
     * @param date
     * @return {@link Date }
     */
    private Date getExecuteDateByHourAndMinute(Integer hour,Integer minute,Date date){
        if(date == null){
            date = new Date();
        }
        String hourStr = "00";
        if(hour != null){
            hourStr = hour < 10 ? "0" + hour : hour + "";
        }
        String minuteStr = "00";
        if(minute != null){
            minuteStr = minute < 10 ? "0" + minute : minute + "";
        }
        //组装时间
        String executeTimeStr = DateUtil.formatDate(date) + " " + hourStr + ":" + minuteStr + ":00";
        Date executeTime = DateUtil.parseDateTime(executeTimeStr);
        return executeTime;
    }

    /**
     * 解析获取通知时间
     *
     * @param notifyTimeRuleVo
     * @param insData
     * @return
     */
    @Override
    public NotifyExecuteTimeVo getNotifyExecuteTime(NotifyTimeRuleVo notifyTimeRuleVo, Map<String,Object> insData){
        NotifyExecuteTimeVo notifyExecuteTimeVo = new NotifyExecuteTimeVo();
        if(NotifyEnum.TIME_TYPE_NOW.getCode().equals(notifyTimeRuleVo.getTimeType())){
            //当前时间通知
            return getNotifyExecuteTimeNow();
        }
        //指定发送时间
        if(NotifyEnum.TIME_TYPE_SPECIFY_TIME.getCode().equals(notifyTimeRuleVo.getTimeType())){
            notifyExecuteTimeVo.setExecuteTime(notifyTimeRuleVo.getSpecifyTime());
            notifyExecuteTimeVo.setExecuteTimeStr(DateUtil.formatDateTime(notifyTimeRuleVo.getSpecifyTime()));
            notifyExecuteTimeVo.setIsNow(false);
            if(notifyTimeRuleVo.getSpecifyTime().before(new Date())){
                notifyExecuteTimeVo.setIsNow(true);
            }
            notifyExecuteTimeVo.setExecuteNum(1);
            return notifyExecuteTimeVo;
        }
        //取业务数据时间
        if(NotifyEnum.TIME_TYPE_BUSINESS.getCode().equals(notifyTimeRuleVo.getTimeType())){
            if(insData == null || insData.get(notifyTimeRuleVo.getBusinessAttr()) == null){
                return null;
            }
            Date businessTime =Date.from(((LocalDateTime) insData.get(notifyTimeRuleVo.getBusinessAttr())).atZone(ZoneId.systemDefault()).toInstant());
            notifyExecuteTimeVo.setExecuteTime(businessTime);
            notifyExecuteTimeVo.setExecuteTimeStr(DateUtil.formatDateTime(businessTime));
            notifyExecuteTimeVo.setIsNow(false);
            if(businessTime.before(new Date())){
                notifyExecuteTimeVo.setIsNow(true);
            }
            return notifyExecuteTimeVo;
        }
        if(NotifyEnum.TIME_TYPE_DAY.getCode().equals(notifyTimeRuleVo.getTimeType())){
            //按天
            Date executeTime = getExecuteDateByHourAndMinute(notifyTimeRuleVo.getHour(),notifyTimeRuleVo.getMinute(),null);
            if(executeTime.before(new Date())){
                //如果当前时间已经超过了执行时间，则执行时间为明天
                executeTime = DateUtil.offsetDay(executeTime, 1);
            }
            notifyExecuteTimeVo.setExecuteTime(executeTime);
            notifyExecuteTimeVo.setExecuteTimeStr(DateUtil.formatDateTime(executeTime));
            notifyExecuteTimeVo.setIsNow(false);
            return notifyExecuteTimeVo;
        }
        if(NotifyEnum.TIME_TYPE_WEEK.getCode().equals(notifyTimeRuleVo.getTimeType())){
            Date executeTime = getExecuteDateByHourAndMinute(notifyTimeRuleVo.getHour(),notifyTimeRuleVo.getMinute(),null);
            Week week = DateUtil.thisDayOfWeekEnum();
            int thisWeekDay = week.getValue() - 1;
            if(thisWeekDay == 0){
                thisWeekDay = 7;
            }
            //和参数比
            if(thisWeekDay < notifyTimeRuleVo.getWeek()){
                executeTime = DateUtil.offsetDay(executeTime, thisWeekDay - notifyTimeRuleVo.getWeek());
            }else if (thisWeekDay > notifyTimeRuleVo.getWeek()){
                executeTime = DateUtil.offsetDay(executeTime, 7);
            }
            notifyExecuteTimeVo.setExecuteTime(executeTime);
            notifyExecuteTimeVo.setExecuteTimeStr(DateUtil.formatDateTime(executeTime));
            notifyExecuteTimeVo.setIsNow(false);
            return notifyExecuteTimeVo;
        }
        if(NotifyEnum.TIME_TYPE_HOUR.getCode().equals(notifyTimeRuleVo.getTimeType())){
            //按小时
            int hour = DateUtil.thisHour(true);
            Date executeTime = getExecuteDateByHourAndMinute(hour,notifyTimeRuleVo.getMinute(),null);
            if(executeTime.before(new Date())){
                executeTime = DateUtil.offsetHour(executeTime, 1);
            }
            notifyExecuteTimeVo.setExecuteTime(executeTime);
            notifyExecuteTimeVo.setExecuteTimeStr(DateUtil.formatDateTime(executeTime));
            notifyExecuteTimeVo.setIsNow(false);
            return notifyExecuteTimeVo;
        }
        if(NotifyEnum.TIME_TYPE_MONTH.getCode().equals(notifyTimeRuleVo.getTimeType())){
             //按月
            int year = DateUtil.thisYear();
            int month = DateUtil.thisMonth() + 1;
            String monthStr = month < 10 ? "0" + month : month + "";
            String dayStr = notifyTimeRuleVo.getDay() < 10 ? "0" + notifyTimeRuleVo.getDay() : notifyTimeRuleVo.getDay() + "";
            Date executeTime = DateUtil.parse(year + "-" + monthStr + "-" + dayStr + " " + "00" + ":" + "00" + ":00");
            executeTime = getExecuteDateByHourAndMinute(notifyTimeRuleVo.getHour(),notifyTimeRuleVo.getMinute(),executeTime);
            if(executeTime.before(new Date())){
                executeTime = DateUtil.offsetMonth(executeTime, 1);
            }
            notifyExecuteTimeVo.setExecuteTime(executeTime);
            notifyExecuteTimeVo.setExecuteTimeStr(DateUtil.formatDateTime(executeTime));
            notifyExecuteTimeVo.setIsNow(false);
            return notifyExecuteTimeVo;
        }
        if(NotifyEnum.TIME_TYPE_INTERVAL.getCode().equals(notifyTimeRuleVo.getTimeType())){
           //按间隔
           if(notifyTimeRuleVo.getDay() != null){
               //按天间隔
               Date executeTime = getExecuteDateByHourAndMinute(notifyTimeRuleVo.getHour(),notifyTimeRuleVo.getMinute(),null);
               executeTime = DateUtil.offsetDay(executeTime, notifyTimeRuleVo.getDay());
               notifyExecuteTimeVo.setExecuteTime(executeTime);
               notifyExecuteTimeVo.setExecuteTimeStr(DateUtil.formatDateTime(executeTime));
               notifyExecuteTimeVo.setIsNow(false);
               return notifyExecuteTimeVo;
           }
            if(notifyTimeRuleVo.getHour() != null){
                //按小时间隔
                int year = DateUtil.thisYear();
                int month = DateUtil.thisMonth() + 1;
                int hour = DateUtil.thisHour(true);
                String monthStr = month < 10 ? "0" + month : month + "";
                String dayStr = notifyTimeRuleVo.getDay() < 10 ? "0" + notifyTimeRuleVo.getDay() : notifyTimeRuleVo.getDay() + "";
                String hourStr = hour < 10 ? "0" + hour : hour + "";
                String minuteStr = notifyTimeRuleVo.getMinute() < 10 ? "0" + notifyTimeRuleVo.getMinute() : notifyTimeRuleVo.getMinute() + "";
                Date executeTime = DateUtil.parse(year + "-" + monthStr + "-" + dayStr + " " + hourStr + ":" + minuteStr + ":00");
                executeTime = DateUtil.offsetHour(executeTime, notifyTimeRuleVo.getHour());
                notifyExecuteTimeVo.setExecuteTime(executeTime);
                notifyExecuteTimeVo.setExecuteTimeStr(DateUtil.formatDateTime(executeTime));
                notifyExecuteTimeVo.setIsNow(false);
                return notifyExecuteTimeVo;
            }
            if(notifyTimeRuleVo.getMinute() != null){
                //按分钟间隔
                Date executeTime = new Date();
                executeTime = DateUtil.offsetMinute(executeTime, notifyTimeRuleVo.getMinute());
                notifyExecuteTimeVo.setExecuteTime(executeTime);
                notifyExecuteTimeVo.setExecuteTimeStr(DateUtil.formatDateTime(executeTime));
                notifyExecuteTimeVo.setIsNow(false);
                return notifyExecuteTimeVo;
            }
        }
        return null;
    }
}
