package com.transcend.plm.datadriven.apm.permission.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.esotericsoftware.minlog.Log;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.common.util.StringUtil;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.api.model.BaseDataEnum;
import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.api.model.ObjectEnum;
import com.transcend.plm.datadriven.api.model.config.TableAttributeDefinition;
import com.transcend.plm.datadriven.api.model.config.TableDefinition;
import com.transcend.plm.datadriven.apm.constants.RoleConstant;
import com.transcend.plm.datadriven.apm.enums.InnerRoleEnum;
import com.transcend.plm.datadriven.apm.enums.TypeEnum;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceRoleUser;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowInstanceRoleUserService;
import com.transcend.plm.datadriven.apm.permission.enums.ConditionOperatorConstant;
import com.transcend.plm.datadriven.apm.permission.enums.OperatorEnum;
import com.transcend.plm.datadriven.apm.permission.pojo.dto.AppPermissionDto;
import com.transcend.plm.datadriven.apm.permission.pojo.dto.PermissionOperationItemDto;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.*;
import com.transcend.plm.datadriven.apm.permission.repository.entity.*;
import com.transcend.plm.datadriven.apm.permission.repository.mapper.*;
import com.transcend.plm.datadriven.apm.permission.repository.service.*;
import com.transcend.plm.datadriven.apm.permission.service.*;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.apm.space.service.impl.DefaultAppExcelTemplateService;
import com.transcend.plm.datadriven.apm.tools.UniversalComparator;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transcend.plm.datadriven.common.dynamic.fields.DynamicFieldConverter;
import com.transcend.plm.datadriven.common.tool.ObjectTools;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transsion.framework.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.compress.utils.Sets;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.transcend.plm.datadriven.apm.constants.DictionaryConstant.BUTTON_CODE_GROUP;
import static com.transcend.plm.datadriven.apm.permission.enums.OperatorEnum.OPERATOR_ORDER_LIST;

/**
 * @author unknown
 */
@Service
@Slf4j
public class PermissionConfigService implements IPermissionConfigService {

    @Resource
    private PermissionPlmAddRuleItemService permissionPlmAddRuleItemService;
    @Resource
    private PermissionPlmListRuleItemService permissionPlmListRuleItemService;
    @Resource
    private PermissionPlmOperationRuleItemService permissionPlmOperationRuleItemService;

    @Resource
    private PermissionPlmRuleService permissionPlmRuleService;
    @Resource
    private PermissionPlmRuleConditionService permissionPlmRuleConditionService;

    @Resource
    private ApmSpaceAppService apmSpaceAppService;
    @Resource
    private PermissionPlmAddRuleItemMapper permissionPlmAddRuleItemMapper;
    @Resource
    private PermissionPlmListRuleItemMapper permissionPlmListRuleItemMapper;
    @Resource
    private PermissionPlmOperationRuleItemMapper permissionPlmOperationRuleItemMapper;
    @Resource
    private PermissionPlmRuleConditionMapper permissionPlmRuleConditionMapper;
    @Resource
    private PermissionPlmRuleMapper permissionPlmRuleMapper;
    @Resource
    private IPermissionInstanceService permissionInstanceService;
    @Resource
    private IPermissionRuleService permissionRuleService;
    @Resource
    private IApmSpaceAppDataDrivenService apmSpaceAppDataDrivenService;

    @Resource
    private IPermissionOperationService permissionOperationService;

    @Resource
    private IApmRoleDomainService apmRoleDomainService;

    @Resource
    private ApmSphereService apmSphereService;

    @Resource
    private DefaultAppExcelTemplateService defaultAppExcelTemplateService;

    @Resource
    private ApmRoleService apmRoleService;

    @Resource
    private ApmFlowInstanceRoleUserService apmFlowInstanceRoleUserService;

    @Resource
    private DynamicFieldConverter dynamicFieldConverter;

    @Resource
    private IPermissionCheckService permissionCheckService;


    /**
     * 获取条件权限配置信息
     * @param spaceAppBid
     * @return
     */
    @Override
    @Cacheable(value = "AppConditionPermission", key = "#spaceAppBid")
    public List<AppConditionPermissionVo> listAppConditionPermission(String spaceAppBid){
        List<AppConditionPermissionVo> appConditionPermissionVos = new ArrayList<>();
        List<PermissionPlmRule> permissionPlmRules = permissionPlmRuleService.list(new LambdaQueryWrapper<PermissionPlmRule>().likeRight(PermissionPlmRule::getBid,"LC:"+spaceAppBid+":"));
        if(CollectionUtils.isNotEmpty(permissionPlmRules)){
           List<String> permissionPlmRuleBids = permissionPlmRules.stream().map(PermissionPlmRule::getBid).collect(Collectors.toList());
            //查询ITEM表
            List<PermissionPlmAddRuleItem> addRuleItems = permissionPlmAddRuleItemService.list(new LambdaQueryWrapper<PermissionPlmAddRuleItem>().in(PermissionPlmAddRuleItem::getPermissionBid, permissionPlmRuleBids));
            List<PermissionPlmListRuleItem> listRuleItems = permissionPlmListRuleItemService.list(new LambdaQueryWrapper<PermissionPlmListRuleItem>().in(PermissionPlmListRuleItem::getPermissionBid, permissionPlmRuleBids));
            List<PermissionPlmOperationRuleItem> operationRuleItems = permissionPlmOperationRuleItemService.list(new LambdaQueryWrapper<PermissionPlmOperationRuleItem>().in(PermissionPlmOperationRuleItem::getPermissionBid, permissionPlmRuleBids));
            List<PermissionPlmRuleCondition> ruleConditions =permissionPlmRuleConditionMapper.selectByPermissionBidList(permissionPlmRuleBids);
            //List<PermissionPlmRuleCondition> ruleConditions = permissionPlmRuleConditionService.list(new LambdaQueryWrapper<PermissionPlmRuleCondition>().in(PermissionPlmRuleCondition::getPermissionBid, permissionPlmRuleBids));
            Map<String,List<PermissionPlmAddRuleItem>> addRuleItemMap = new HashMap<>(16);
            if(CollectionUtils.isNotEmpty(addRuleItems)){
                addRuleItemMap.putAll(addRuleItems.stream().collect(Collectors.groupingBy(PermissionPlmAddRuleItem::getPermissionBid)));
            }
            Map<String,List<PermissionPlmListRuleItem>> listRuleItemMap = new HashMap<>(16);
            if(CollectionUtils.isNotEmpty(listRuleItems)){
                listRuleItemMap.putAll(listRuleItems.stream().collect(Collectors.groupingBy(PermissionPlmListRuleItem::getPermissionBid)));
            }
            Map<String,List<PermissionPlmOperationRuleItem>> operationRuleItemMap = new HashMap<>(16);
            if(CollectionUtils.isNotEmpty(operationRuleItems)){
                operationRuleItemMap.putAll(operationRuleItems.stream().collect(Collectors.groupingBy(PermissionPlmOperationRuleItem::getPermissionBid)));
            }
            //获取应用扩展按钮
            List<PermissionPlmOperationMapVO> permissionPlmOperationMapVOS = permissionOperationService.queryOperationByAppId(spaceAppBid);
            Map<String,List<PermissionPlmRuleCondition>> ruleConditionMap = ruleConditions.stream().collect(Collectors.groupingBy(PermissionPlmRuleCondition::getPermissionBid));
            for(PermissionPlmRule permissionPlmRule : permissionPlmRules){
                Map<String,List<String>> appRoleOperatorMap = new HashMap<>(16);
                Map<String,List<String>> objRoleOperatorMap = new HashMap<>(16);
                buildAddOperators(addRuleItemMap.get(permissionPlmRule.getBid()),appRoleOperatorMap,objRoleOperatorMap);
                buildListOperators(listRuleItemMap.get(permissionPlmRule.getBid()),appRoleOperatorMap,objRoleOperatorMap);
                buildOperators(operationRuleItemMap.get(permissionPlmRule.getBid()),appRoleOperatorMap,objRoleOperatorMap,spaceAppBid,permissionPlmOperationMapVOS);
                List<PermissionOperationItemVo> appPermissionOperationList = new ArrayList<>();
                if(CollectionUtils.isNotEmpty(appRoleOperatorMap)){
                    for(Map.Entry<String,List<String>> entry : appRoleOperatorMap.entrySet()){
                        PermissionOperationItemVo permissionOperationItemVo = new PermissionOperationItemVo();
                        permissionOperationItemVo.setRoleCode(entry.getKey());
                        permissionOperationItemVo.setOperatorList(entry.getValue());
                        appPermissionOperationList.add(permissionOperationItemVo);
                    }
                }
                AppConditionPermissionVo appConditionPermissionVo = new AppConditionPermissionVo();
                appConditionPermissionVo.setAppPermissionOperationList(appPermissionOperationList);
                appConditionPermissionVo.setPermissionPlmRuleConditionList(ruleConditionMap.get(permissionPlmRule.getBid()));
                appConditionPermissionVo.setSpaceAppBid(spaceAppBid);
                appConditionPermissionVo.setPermissionBid(permissionPlmRule.getBid());
                appConditionPermissionVo.setRuleName(permissionPlmRule.getRuleName());
                appConditionPermissionVo.setCreatedBy(permissionPlmRule.getCreatedBy());
                appConditionPermissionVo.setCreatedTime(permissionPlmRule.getCreatedTime());
                appConditionPermissionVo.setUpdatedTime(permissionPlmRule.getUpdatedTime());
                appConditionPermissionVo.setUpdatedBy(permissionPlmRule.getUpdatedBy());
                appConditionPermissionVos.add(appConditionPermissionVo);
            }
        }
        return appConditionPermissionVos;
    }

    @Override
    public AppBasePermissionVo listAppBasePermissions(String spaceAppBid,String instanceBid){
        String permissionBid = "APP:"+spaceAppBid;
        if(StringUtils.isNotEmpty(instanceBid)){
            permissionBid = "PRI:"+spaceAppBid+":"+instanceBid;
        }
        PermissionPlmRule permissionPlmRule = permissionPlmRuleService.getOne(new LambdaQueryWrapper<PermissionPlmRule>().eq(PermissionPlmRule::getBid,permissionBid));
        if(permissionPlmRule == null){
            return null;
        }
        //查询ITEM表
        List<PermissionPlmAddRuleItem> addRuleItems = permissionPlmAddRuleItemService.list(new LambdaQueryWrapper<PermissionPlmAddRuleItem>().eq(PermissionPlmAddRuleItem::getPermissionBid, permissionPlmRule.getBid()));
        List<PermissionPlmListRuleItem> listRuleItems = permissionPlmListRuleItemService.list(new LambdaQueryWrapper<PermissionPlmListRuleItem>().eq(PermissionPlmListRuleItem::getPermissionBid, permissionPlmRule.getBid()));
        List<PermissionPlmOperationRuleItem> operationRuleItems = permissionPlmOperationRuleItemService.list(new LambdaQueryWrapper<PermissionPlmOperationRuleItem>().eq(PermissionPlmOperationRuleItem::getPermissionBid, permissionPlmRule.getBid()));
        List<PermissionPlmOperationMapVO> permissionPlmOperationMapVOS = permissionOperationService.queryOperationByAppId(spaceAppBid);

        //组装返回结果
        AppBasePermissionVo appBasePermissionVo = new AppBasePermissionVo();
        Map<String,List<String>> appRoleOperatorMap = new HashMap<>(16);
        Map<String,List<String>> objRoleOperatorMap = new HashMap<>(16);
        buildAddOperators(addRuleItems,appRoleOperatorMap,objRoleOperatorMap);
        buildListOperators(listRuleItems,appRoleOperatorMap,objRoleOperatorMap);
        buildOperators(operationRuleItems,appRoleOperatorMap,objRoleOperatorMap,spaceAppBid,permissionPlmOperationMapVOS);
        appBasePermissionVo.setSpaceAppBid(spaceAppBid);
        appBasePermissionVo.setPermissionBid(permissionPlmRule.getBid());
        appBasePermissionVo.setRuleName(permissionPlmRule.getRuleName());
        List<PermissionOperationItemVo> appPermissionOperationList = new ArrayList<>();
        List<PermissionOperationItemVo> objPermissionOperationList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(appRoleOperatorMap)){
            for(Map.Entry<String,List<String>> entry : appRoleOperatorMap.entrySet()){
                PermissionOperationItemVo permissionOperationItemVo = new PermissionOperationItemVo();
                permissionOperationItemVo.setRoleCode(entry.getKey());
                if(entry.getKey().startsWith(CommonConst.PRI_KEY)){
                    permissionOperationItemVo.setRoleCode(entry.getKey().substring(CommonConst.PRI_KEY.length()));
                    permissionOperationItemVo.setRoleType(CommonConst.PRI_ROLE_TYPE);
                }else{
                    permissionOperationItemVo.setRoleType(CommonConst.SPACE_ROLE_TYPE);
                }
                permissionOperationItemVo.setOperatorList(entry.getValue());
                appPermissionOperationList.add(permissionOperationItemVo);
            }
        }
        if(CollectionUtils.isNotEmpty(objRoleOperatorMap)){
            for(Map.Entry<String,List<String>> entry : objRoleOperatorMap.entrySet()){
                PermissionOperationItemVo permissionOperationItemVo = new PermissionOperationItemVo();
                permissionOperationItemVo.setRoleCode(entry.getKey());
                permissionOperationItemVo.setOperatorList(entry.getValue());
                objPermissionOperationList.add(permissionOperationItemVo);
            }
        }
        appBasePermissionVo.setPermissionOperationList(appPermissionOperationList);
        appBasePermissionVo.setObjPermissionOperationList(objPermissionOperationList);
        if(StringUtils.isNotEmpty(instanceBid)){
            appBasePermissionVo.setInstanceBid(instanceBid);
        }
        return appBasePermissionVo;
    }

    private Map<String,String> getBaseAttrCodeMap(String spaceAppBid, List<PermissionPlmOperationMapVO> permissionPlmOperationMapVOS){
        Map<String,String> baseAttrCodeMap = new HashMap<>(16);
        baseAttrCodeMap.put("operationDelete",OperatorEnum.DELETE.getCode());
        baseAttrCodeMap.put("operationEdit",OperatorEnum.EDIT.getCode());
        baseAttrCodeMap.put("operationDetail",OperatorEnum.DETAIL.getCode());
        baseAttrCodeMap.put("operationRevise",OperatorEnum.REVISE.getCode());
        baseAttrCodeMap.put("operationPromote",OperatorEnum.PROMOTE.getCode());
        baseAttrCodeMap.put("operationMove",OperatorEnum.MOVE.getCode());
        baseAttrCodeMap.put("operationImport",OperatorEnum.IMPORT.getCode());
        baseAttrCodeMap.put("operationExport",OperatorEnum.EXPORT.getCode());
        baseAttrCodeMap.put("modelViewTableView",OperatorEnum.VIEW_MODEL_tableView.getCode());
        baseAttrCodeMap.put("modelViewTreeView",OperatorEnum.VIEW_MODEL_treeView.getCode());
        baseAttrCodeMap.put("modelViewMultiTreeView",OperatorEnum.VIEW_MODEL_multiTreeView.getCode());
        baseAttrCodeMap.put("modelViewOverview",OperatorEnum.VIEW_MODEL_overview.getCode());
        baseAttrCodeMap.put("modelViewMeasureReport",OperatorEnum.VIEW_MODEL_measureReport.getCode());
        baseAttrCodeMap.put("modelViewKanbanView",OperatorEnum.VIEW_MODEL_kanbanView.getCode());
        baseAttrCodeMap.put("modelViewGanttView",OperatorEnum.VIEW_MODEL_ganttView.getCode());
        baseAttrCodeMap.put("instanceLock",OperatorEnum.LOCK.getCode());
        baseAttrCodeMap.put("instanceUnlock",OperatorEnum.UNLOCK.getCode());
        baseAttrCodeMap.put("sync",OperatorEnum.SYNC.getCode());
        baseAttrCodeMap.put("modelViewAll",OperatorEnum.VIEW_MODEL_ALL.getCode());
        //去查询扩展按钮
        if (!StringUtil.isNotBlank(spaceAppBid)){
            log.debug("没有自定义按钮数据");
            return baseAttrCodeMap;
        }
        if (org.springframework.util.CollectionUtils.isEmpty(permissionPlmOperationMapVOS)) {
            log.debug("没有自定义按钮数据");
            return baseAttrCodeMap;
        }
        //不为空则组装按钮返回
        for (PermissionPlmOperationMapVO operationMapVO : permissionPlmOperationMapVOS) {
            baseAttrCodeMap.put(operationMapVO.getAccessOperationKey().replace("_",""),operationMapVO.getOperationCode());
        }
        return baseAttrCodeMap;

    }

    private Map<String,String> getBaseAttrCodeNameMap(String spaceAppBid, List<PermissionPlmOperationMapVO> permissionPlmOperationMapVOS){
        Map<String,String> baseAttrCodeMap = new HashMap<>(16);
        baseAttrCodeMap.put(OperatorEnum.DELETE.getCode(),"删除");
        baseAttrCodeMap.put(OperatorEnum.EDIT.getCode(),"编辑");
        baseAttrCodeMap.put(OperatorEnum.DETAIL.getCode(),"详情");
        baseAttrCodeMap.put(OperatorEnum.REVISE.getCode(),"修订");
        baseAttrCodeMap.put(OperatorEnum.PROMOTE.getCode(),"提升");
        baseAttrCodeMap.put(OperatorEnum.IMPORT.getCode(),"导入");
        baseAttrCodeMap.put(OperatorEnum.EXPORT.getCode(),"导出");

        baseAttrCodeMap.put(OperatorEnum.VIEW_MODEL_tableView.getCode(),"视图模式-列表");
        baseAttrCodeMap.put(OperatorEnum.VIEW_MODEL_treeView.getCode(),"视图模式-层级");
        baseAttrCodeMap.put(OperatorEnum.VIEW_MODEL_multiTreeView.getCode(),"视图模式-多维表格");
        baseAttrCodeMap.put(OperatorEnum.VIEW_MODEL_overview.getCode(),"视图模式-概览");
        baseAttrCodeMap.put(OperatorEnum.VIEW_MODEL_measureReport.getCode(),"视图模式-度量报表");
        baseAttrCodeMap.put(OperatorEnum.VIEW_MODEL_kanbanView.getCode(),"视图模式-看板");
        baseAttrCodeMap.put(OperatorEnum.VIEW_MODEL_ganttView.getCode(),"视图模式-甘特图");
        baseAttrCodeMap.put(OperatorEnum.VIEW_MODEL_ALL.getCode(),"视图模式（全）");
        baseAttrCodeMap.put(OperatorEnum.LOCK.getCode(),"加锁");
        baseAttrCodeMap.put(OperatorEnum.UNLOCK.getCode(),"解锁");
        baseAttrCodeMap.put(OperatorEnum.SYNC.getCode(),"同步");



        //去查询扩展按钮
        if (!StringUtil.isNotBlank(spaceAppBid)){
            log.debug("没有自定义按钮数据");
            return baseAttrCodeMap;
        }
        if (org.springframework.util.CollectionUtils.isEmpty(permissionPlmOperationMapVOS)) {
            log.debug("没有自定义按钮数据");
            return baseAttrCodeMap;
        }
        //不为空则组装按钮返回
        for (PermissionPlmOperationMapVO operationMapVO : permissionPlmOperationMapVOS) {
            baseAttrCodeMap.put(operationMapVO.getOperationCode(),operationMapVO.getOperationName());
        }
        return baseAttrCodeMap;
    }

//    private Map<String,String> getBaseAttrCodeMap(){
//        Map<String,String> baseAttrCodeMap = new HashMap<>();
//        baseAttrCodeMap.put("operationDelete",OperatorEnum.DELETE.getCode());
//        baseAttrCodeMap.put("operationEdit",OperatorEnum.EDIT.getCode());
//        baseAttrCodeMap.put("operationDetail",OperatorEnum.DETAIL.getCode());
//        baseAttrCodeMap.put("operationRevise",OperatorEnum.REVISE.getCode());
//        baseAttrCodeMap.put("operationPromote",OperatorEnum.PROMOTE.getCode());
//        return baseAttrCodeMap;
//    }

//    private Map<String,String> getBaseAttrCodeNameMap(){
//        Map<String,String> baseAttrMap = new HashMap<>();
//        baseAttrMap.put(OperatorEnum.DELETE.getCode(),"删除");
//        baseAttrMap.put(OperatorEnum.EDIT.getCode(),"编辑");
//        baseAttrMap.put(OperatorEnum.DETAIL.getCode(),"详情");
//        baseAttrMap.put(OperatorEnum.REVISE.getCode(),"修订");
//        baseAttrMap.put(OperatorEnum.PROMOTE.getCode(),"提升");
//        return baseAttrMap;
//    }

    private List<String> getOperatorVos(PermissionPlmOperationRuleItem operationRuleItem,String spaceAppBid,List<PermissionPlmOperationMapVO> permissionPlmOperationMapVOS){
        List<String> operatorVos = new ArrayList<>();
        Map<String,String> getBaseAttrCodeMap = getBaseAttrCodeMap(spaceAppBid, permissionPlmOperationMapVOS);
        Map<String,String> getBaseAttrCodeNameMap = getBaseAttrCodeNameMap(spaceAppBid, permissionPlmOperationMapVOS );
        if(operationRuleItem != null){
            Map<String,Object> objectMap = BeanUtil.beanToMap(operationRuleItem);
            for(Map.Entry<String,Object> entry : objectMap.entrySet()){
                String key = entry.getKey();
                if(getBaseAttrCodeMap.containsKey(key)){
                    Object value = entry.getValue();
                    if(value != null && value instanceof Boolean && (Boolean)value){
                        operatorVos.add(getBaseAttrCodeMap.get(key));
                    }
                }
            }
        }
        return operatorVos;
    }

    private void buildOperators(List<PermissionPlmOperationRuleItem> operationRuleItems,Map<String,List<String>> appRoleOperatorMap,Map<String,List<String>> objRoleOperatorMap,String spaceAppBid,List<PermissionPlmOperationMapVO> permissionPlmOperationMapVOS){
        if(CollectionUtils.isNotEmpty(operationRuleItems)){
            for(PermissionPlmOperationRuleItem operationRuleItem : operationRuleItems){
                List<String> operatorVos = getOperatorVos(operationRuleItem,spaceAppBid,permissionPlmOperationMapVOS);
                if("0".equals(operationRuleItem.getPath())){
                    List<String> appOperatorVos = appRoleOperatorMap.get(operationRuleItem.getRoleCode());
                    if(appOperatorVos == null){
                        appOperatorVos = new ArrayList<>();
                    }
                    appOperatorVos.addAll(operatorVos);
                    appRoleOperatorMap.put(operationRuleItem.getRoleCode(),appOperatorVos);
                }else{
                    List<String> objOperatorVos = objRoleOperatorMap.get(operationRuleItem.getRoleCode());
                    if(objOperatorVos == null){
                        objOperatorVos = new ArrayList<>();
                    }
                    objOperatorVos.addAll(operatorVos);
                    objRoleOperatorMap.put(operationRuleItem.getRoleCode(),objOperatorVos);
                }
            }
        }
    }

    private void buildAddOperators(List<PermissionPlmAddRuleItem> addRuleItems,Map<String,List<String>> appRoleOperatorMap,Map<String,List<String>> objRoleOperatorMap){
        if(CollectionUtils.isNotEmpty(addRuleItems)){
            for(PermissionPlmAddRuleItem addRuleItem : addRuleItems){
                if("0".equals(addRuleItem.getPath())){
                    List<String> appOperatorVos = appRoleOperatorMap.get(addRuleItem.getRoleCode());
                    if(appOperatorVos == null){
                        appOperatorVos = new ArrayList<>();
                    }
                    appOperatorVos.add("ADD");
                    appRoleOperatorMap.put(addRuleItem.getRoleCode(),appOperatorVos);
                }else{
                    List<String> objOperatorVos = objRoleOperatorMap.get(addRuleItem.getRoleCode());
                    if(objOperatorVos == null){
                        objOperatorVos = new ArrayList<>();
                    }
                    objOperatorVos.add("ADD");
                    objRoleOperatorMap.put(addRuleItem.getRoleCode(),objOperatorVos);
                }
            }
        }
    }

    public void buildListOperators(List<PermissionPlmListRuleItem> listRuleItems,Map<String,List<String>> appRoleOperatorMap,Map<String,List<String>> objRoleOperatorMap){
        if(CollectionUtils.isNotEmpty(listRuleItems)){
            for(PermissionPlmListRuleItem listRuleItem : listRuleItems){
                if("0".equals(listRuleItem.getPath())){
                    List<String> appOperatorVos = appRoleOperatorMap.get(listRuleItem.getRoleCode());
                    if(appOperatorVos == null){
                        appOperatorVos = new ArrayList<>();
                    }
                    appOperatorVos.add("LIST");
                    appRoleOperatorMap.put(listRuleItem.getRoleCode(),appOperatorVos);
                }else{
                    List<String> objOperatorVos = objRoleOperatorMap.get(listRuleItem.getRoleCode());
                    if(objOperatorVos == null){
                        objOperatorVos = new ArrayList<>();
                    }
                    objOperatorVos.add("LIST");
                    objRoleOperatorMap.put(listRuleItem.getRoleCode(),objOperatorVos);
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "AppConditionPermission", key = "#appPermissionDto.spaceAppBid")
    public boolean saveAppConditionPermission(AppPermissionDto appPermissionDto){
        //加工权限，操作列表
        handelAppPermissionDto(appPermissionDto);
        if(StringUtils.isEmpty(appPermissionDto.getPermissionBid())){
            //新增
           return addConditionConfigRule(appPermissionDto);
        }else{
            //修改
            return editConfigRule(appPermissionDto);
        }
    }

    /**
     * 条件规则权限删除
     * @param permissionBid
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "AppConditionPermission", key = "#spaceAppBid")
    public boolean deleteConditionPermission(String spaceAppBid,String permissionBid){
        /*PermissionPlmRule permissionPlmRule = getPermissionPlmRuleByAppBid(spaceAppBid);
        if(permissionPlmRule == null){
            throw new BusinessException("应用权限规则不存在");
        }*/
        permissionPlmAddRuleItemMapper.deleteByPermissionBid(permissionBid);
        permissionPlmListRuleItemMapper.deleteByPermissionBid(permissionBid);
        permissionPlmOperationRuleItemMapper.deleteByPermissionBid(permissionBid);
        permissionPlmRuleConditionMapper.deleteByPermissionBid(permissionBid);
        permissionPlmRuleMapper.deleteByPermissionBid(permissionBid);
        //实例数据变更
        //return permissionInstanceService.updateOldPermissionBidToNewBid(spaceAppBid,permissionBid,permissionPlmRule.getBid());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteInstancePermission(String spaceAppBid,String instanceBid){
        String permissionBid = "PRI:"+spaceAppBid+":"+instanceBid;
        permissionPlmAddRuleItemMapper.deleteByPermissionBid(permissionBid);
        permissionPlmListRuleItemMapper.deleteByPermissionBid(permissionBid);
        permissionPlmOperationRuleItemMapper.deleteByPermissionBid(permissionBid);
        permissionPlmRuleMapper.deleteByPermissionBid(permissionBid);
        //实例数据变更
        MSpaceAppData mSpaceAppData = apmSpaceAppDataDrivenService.get(spaceAppBid,instanceBid,false);
        String newPermissionBid = permissionRuleService.getPermissionBidByAppData(mSpaceAppData);
        return permissionInstanceService.updateInstanceByBid(spaceAppBid,instanceBid,newPermissionBid);
    }

    /**
     * 保存应用基础权限配置
     *
     * @param appPermissionDto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateAppBasePermissionConfig(AppPermissionDto appPermissionDto) {
        //加工权限，操作列表
        handelAppPermissionDto(appPermissionDto);
        if(StringUtils.isEmpty(appPermissionDto.getPermissionBid()) && StringUtils.isEmpty(appPermissionDto.getInstanceBid())){
            //判断是否已经存在权限
            String permissionBid = "APP:" + appPermissionDto.getSpaceAppBid();
            PermissionPlmRule permissionPlmRule = permissionPlmRuleService.getOne(new LambdaQueryWrapper<PermissionPlmRule>().eq(PermissionPlmRule::getBid, permissionBid));
            if(permissionPlmRule != null){
                appPermissionDto.setPermissionBid(permissionPlmRule.getBid());
            }
        }
        if(StringUtils.isEmpty(appPermissionDto.getPermissionBid())){
            //新增
            addConfigRule(appPermissionDto);
            //修改实例数据
            return permissionInstanceService.updateAppPermission(appPermissionDto.getPermissionBid(),appPermissionDto.getSpaceAppBid());
        }else{
            //修改
            return editConfigRule(appPermissionDto);
        }
    }

    /**
     * 新增或修改实例权限数据
     *
     * @param appPermissionDto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateInstancePermissionConfig(AppPermissionDto appPermissionDto) {
        //加工权限，操作列表
        handelAppPermissionDto(appPermissionDto);
        if(StringUtils.isEmpty(appPermissionDto.getPermissionBid())){
            //第一次新增需要默认加创建人 责任人的列表，详情权限
            List<String> operationList = new ArrayList<>();
            operationList.add(OperatorEnum.DETAIL.getCode());
            operationList.add(OperatorEnum.LIST.getCode());
            List<PermissionOperationItemDto> permissionOperationItemDtoList = new ArrayList<>();
            List<String> roleCodeList = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(appPermissionDto.getAppPermissionOperationList())){
                roleCodeList = appPermissionDto.getAppPermissionOperationList().stream().map(PermissionOperationItemVo::getRoleCode).collect(Collectors.toList());
            }
            if(!roleCodeList.contains(InnerRoleEnum.CREATER.getCode())){
                permissionOperationItemDtoList.addAll(getPermissionOperationItemDtoList(InnerRoleEnum.CREATER.getCode(),operationList));
            }
            if(!roleCodeList.contains((InnerRoleEnum.PERSON_RESPONSIBLE.getCode()))){
                permissionOperationItemDtoList.addAll(getPermissionOperationItemDtoList(InnerRoleEnum.PERSON_RESPONSIBLE.getCode(),operationList));
            }
            if(CollectionUtils.isNotEmpty(appPermissionDto.getOperationItems())){
                appPermissionDto.getOperationItems().addAll(permissionOperationItemDtoList);
            }else{
                appPermissionDto.setOperationItems(permissionOperationItemDtoList);
            }
            //新增
            return addInstanceConfigRule(appPermissionDto);
        }else{
            //修改
            return editInstanceConfigRule(appPermissionDto);
        }
    }

    private boolean addInstanceConfigRule(AppPermissionDto appPermissionDto) {
        //实例数据新增
        addConfigRule(appPermissionDto);
        //修改当前实例数据
        String permissionBid = "PRI:"+appPermissionDto.getSpaceAppBid()+":"+appPermissionDto.getInstanceBid();
        permissionInstanceService.updateInstanceByBid(appPermissionDto.getSpaceAppBid(),appPermissionDto.getInstanceBid(),permissionBid);
        return true;
    }

    private boolean editInstanceConfigRule(AppPermissionDto appPermissionDto) {
        //实例数据变更
        return editConfigRule(appPermissionDto);
    }

    @Override
    public List<PermissionPlmRuleCondition> getPermissionPlmRuleConditionByAppBid(String spaceAppBid) {
        List<PermissionPlmRuleCondition> permissionPlmRuleConditions = permissionPlmRuleConditionService.list(new LambdaQueryWrapper<PermissionPlmRuleCondition>().eq(PermissionPlmRuleCondition::getAppBid, spaceAppBid));
        return permissionPlmRuleConditions;
    }

    @Override
    public PermissionPlmRule getPermissionPlmRuleByAppBid(String spaceAppBid) {
        PermissionPlmRule permissionPlmRule = permissionPlmRuleService.getOne(new LambdaQueryWrapper<PermissionPlmRule>().eq(PermissionPlmRule::getBid, "APP:"+spaceAppBid));
        if(permissionPlmRule == null){
            //查询内置权限
            permissionPlmRule = permissionPlmRuleService.getOne(new LambdaQueryWrapper<PermissionPlmRule>().likeRight(PermissionPlmRule::getBid, "DEFAULT:"));
        }
        return permissionPlmRule;
    }

    @Override
    public List<PermissionPlmRule> getPermissionPlmRuleByAppBids(List<String> spaceAppBids) {
        if (CollectionUtils.isEmpty(spaceAppBids)) {
            return Collections.emptyList();
        }
        List<String> bids = setAppPermissionBid(spaceAppBids);
        QueryWrapper<PermissionPlmRule> queryWrapper = new QueryWrapper<>();
        for (String term : bids) {
            queryWrapper.or().likeRight("bid", term);
        }
        queryWrapper.and(wrapper -> wrapper.eq("delete_flag", 0));
        return permissionPlmRuleService.list(queryWrapper);
    }

    @Override
    public List<PermissionPlmAddRuleItem> getPermissionPlmAddRuleItemByAppBids(List<String> spaceAppBids) {
        if (CollectionUtils.isEmpty(spaceAppBids)) {
            return Collections.emptyList();
        }
        List<String> bids = setAppPermissionBid(spaceAppBids);
        QueryWrapper<PermissionPlmAddRuleItem> queryWrapper = new QueryWrapper<>();
        for (String term : bids) {
            queryWrapper.or().likeRight("permission_bid", term);
        }
        queryWrapper.and(wrapper -> wrapper.eq("delete_flag", 0));
        return permissionPlmAddRuleItemService.list(queryWrapper);
    }

    @Override
    public List<PermissionPlmListRuleItem> getPermissionPlmListRuleItemByAppBids(List<String> spaceAppBids) {
        if (CollectionUtils.isEmpty(spaceAppBids)) {
            return Collections.emptyList();
        }
        List<String> bids = setAppPermissionBid(spaceAppBids);
        QueryWrapper<PermissionPlmListRuleItem> queryWrapper = new QueryWrapper<>();
        for (String term : bids) {
            queryWrapper.or().likeRight("permission_bid", term);
        }
        queryWrapper.and(wrapper -> wrapper.eq("delete_flag", 0));
        return permissionPlmListRuleItemService.list(queryWrapper);

    }

    @Override
    public List<PermissionPlmRuleCondition> getPermissionPlmRuleConditionByAppBids(List<String> spaceAppBids) {
        if (CollectionUtils.isEmpty(spaceAppBids)) {
            return Collections.emptyList();
        }
        List<String> bids = setAppPermissionBid(spaceAppBids);
        QueryWrapper<PermissionPlmRuleCondition> queryWrapper = new QueryWrapper<>();
        for (String term : bids) {
            queryWrapper.or().likeRight("permission_bid", term);
        }
        queryWrapper.and(wrapper -> wrapper.eq("delete_flag", 0));
        return permissionPlmRuleConditionService.list(queryWrapper);

    }

    @Override
    public List<PermissionPlmOperationRuleItem> getPermissionPlmOperationRuleItemByAppBids(List<String> spaceAppBids) {
        if (CollectionUtils.isEmpty(spaceAppBids)) {
            return Collections.emptyList();
        }
        List<String> bids = setAppPermissionBid(spaceAppBids);
        QueryWrapper<PermissionPlmOperationRuleItem> queryWrapper = new QueryWrapper<>();
        for (String term : bids) {
            queryWrapper.or().likeRight("permission_bid", term);
        }
        queryWrapper.and(wrapper -> wrapper.eq("delete_flag", 0));
        return permissionPlmOperationRuleItemService.list(queryWrapper);
    }

    @NotNull
    private static List<String> setAppPermissionBid(List<String> spaceAppBids) {
        return spaceAppBids.stream().map(t ->
                "LC:" + t
        ).collect(Collectors.toList());
    }

    private void getPermissionRuleItems(AppPermissionDto appPermissionDto, List<PermissionPlmAddRuleItem> addRuleItems, List<PermissionPlmListRuleItem> listRuleItems, List<PermissionPlmOperationRuleItem> operationRuleItems, String permissionBid){
        if(CollectionUtils.isEmpty(appPermissionDto.getOperationItems())){
            return;
        }
        Map<String,String> baseAttrMap = getBaseAttrMap(appPermissionDto.getSpaceAppBid());
        Map<String,Map<String,Object>>  objectAllMap = new HashMap<>(16);
        Map<String,Integer> roleTypeMap = new HashMap<>(16);
        String jobNumber = SsoHelper.getJobNumber();
        for(PermissionOperationItemDto operationItemDto : appPermissionDto.getOperationItems()){
            if(OperatorEnum.ADD.getCode().equals(operationItemDto.getOperatorCode())){
                //新增权限
                PermissionPlmAddRuleItem addRuleItem = new PermissionPlmAddRuleItem();
                addRuleItem.setPermissionBid(permissionBid);
                addRuleItem.setBid(SnowflakeIdWorker.nextIdStr());
                addRuleItem.setRoleCode(operationItemDto.getRoleCode());
                addRuleItem.setPath("0");
                addRuleItem.setDeleteFlag(false);
                addRuleItem.setRoleType(operationItemDto.getRoleType());
                addRuleItem.setEnableFlag(true);
                addRuleItem.setCreatedBy(jobNumber);
                addRuleItem.setUpdatedBy(jobNumber);
                addRuleItems.add(addRuleItem);
            }else if (OperatorEnum.LIST.getCode().equals(operationItemDto.getOperatorCode())){
                //列表权限
                PermissionPlmListRuleItem listRuleItem = new PermissionPlmListRuleItem();
                listRuleItem.setPermissionBid(permissionBid);
                listRuleItem.setBid(SnowflakeIdWorker.nextIdStr());
                listRuleItem.setRoleCode(operationItemDto.getRoleCode());
                listRuleItem.setPath("0");
                listRuleItem.setRoleType(operationItemDto.getRoleType());
                listRuleItem.setDeleteFlag(false);
                listRuleItem.setEnableFlag(true);
                listRuleItem.setCreatedBy(jobNumber);
                listRuleItem.setUpdatedBy(jobNumber);
                listRuleItems.add(listRuleItem);
            }else{
                Map<String,Object> objectMap = objectAllMap.get(operationItemDto.getRoleCode());
                roleTypeMap.put(operationItemDto.getRoleCode(),operationItemDto.getRoleType());
                if(objectMap == null){
                    objectMap = new HashMap<>(16);
                }
                objectMap.put(baseAttrMap.get(operationItemDto.getOperatorCode()),true);
                objectAllMap.put(operationItemDto.getRoleCode(),objectMap);
            }
        }
        if(!objectAllMap.isEmpty()){
            for (Map.Entry<String, Map<String, Object>> entry : objectAllMap.entrySet()) {
                PermissionPlmOperationRuleItem operationRuleItem = JSON.parseObject(JSON.toJSONString(entry.getValue()),PermissionPlmOperationRuleItem.class);
                operationRuleItem.setPermissionBid(permissionBid);
                operationRuleItem.setBid(SnowflakeIdWorker.nextIdStr());
                operationRuleItem.setRoleCode(entry.getKey());
                operationRuleItem.setPath("0");
                operationRuleItem.setRoleType(roleTypeMap.get(entry.getKey()));
                operationRuleItem.setDeleteFlag(false);
                operationRuleItem.setEnableFlag(true);
                operationRuleItem.setCreatedBy(jobNumber);
                operationRuleItem.setUpdatedBy(jobNumber);
                operationRuleItems.add(operationRuleItem);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePermission(AppPermissionDto appPermissionDto){
        //需要先删除原来的数据在更新新的数据
        if(appPermissionDto.getRoleType() == CommonConst.PRI_ROLE_TYPE){
            appPermissionDto.setRoleCode(CommonConst.PRI_KEY+appPermissionDto.getRoleCode());
        }
        String permissionBid = appPermissionDto.getPermissionBid();
        if(StringUtils.isEmpty(permissionBid)){
            throw new BusinessException("权限标识不能为空");
        }
        //判断是否是最后一个角色数据
        if(appPermissionDto.getDeleteType() == 1){
            //常规权限删除 需要校验是不是最后一条
            long countAdd = permissionPlmAddRuleItemService.count(new LambdaQueryWrapper<PermissionPlmAddRuleItem>().eq(PermissionPlmAddRuleItem::getPermissionBid,permissionBid).eq(PermissionPlmAddRuleItem::getPath,"0").ne(PermissionPlmAddRuleItem::getRoleCode,appPermissionDto.getRoleCode()));
            long countList = permissionPlmListRuleItemService.count(new LambdaQueryWrapper<PermissionPlmListRuleItem>().eq(PermissionPlmListRuleItem::getPermissionBid,permissionBid).eq(PermissionPlmListRuleItem::getPath,"0").ne(PermissionPlmListRuleItem::getRoleCode,appPermissionDto.getRoleCode()));
            long countOp = permissionPlmOperationRuleItemService.count(new LambdaQueryWrapper<PermissionPlmOperationRuleItem>().eq(PermissionPlmOperationRuleItem::getPermissionBid,permissionBid).eq(PermissionPlmOperationRuleItem::getPath,"0").ne(PermissionPlmOperationRuleItem::getRoleCode,appPermissionDto.getRoleCode()));
            long countObjAdd = permissionPlmAddRuleItemService.count(new LambdaQueryWrapper<PermissionPlmAddRuleItem>().eq(PermissionPlmAddRuleItem::getPermissionBid,permissionBid).ne(PermissionPlmAddRuleItem::getPath,"0"));
            long countObjList = permissionPlmListRuleItemService.count(new LambdaQueryWrapper<PermissionPlmListRuleItem>().eq(PermissionPlmListRuleItem::getPermissionBid,permissionBid).ne(PermissionPlmListRuleItem::getPath,"0"));
            long countObjOp = permissionPlmOperationRuleItemService.count(new LambdaQueryWrapper<PermissionPlmOperationRuleItem>().eq(PermissionPlmOperationRuleItem::getPermissionBid,permissionBid).ne(PermissionPlmOperationRuleItem::getPath,"0"));
            if(countAdd + countList + countOp + countObjAdd + countObjList +countObjOp == 0){
                throw new BusinessException("常规权限最后一个角色数据不能删除,如果不需要权限，请配置成全部成员");
            }
        }
        List<PermissionPlmAddRuleItem> oldAddRuleItems = permissionPlmAddRuleItemService.list(new LambdaQueryWrapper<PermissionPlmAddRuleItem>().eq(PermissionPlmAddRuleItem::getPermissionBid,permissionBid).eq(PermissionPlmAddRuleItem::getPath,"0").eq(PermissionPlmAddRuleItem::getRoleCode,appPermissionDto.getRoleCode()));
        if(CollectionUtils.isNotEmpty(oldAddRuleItems)){
            List<String> oldAddRuleItemBids = oldAddRuleItems.stream().map(PermissionPlmAddRuleItem::getBid).collect(Collectors.toList());
            permissionPlmAddRuleItemMapper.deleteByBids(oldAddRuleItemBids);
        }
        List<PermissionPlmListRuleItem> oldListRuleItems = permissionPlmListRuleItemService.list(new LambdaQueryWrapper<PermissionPlmListRuleItem>().eq(PermissionPlmListRuleItem::getPermissionBid,permissionBid).eq(PermissionPlmListRuleItem::getPath,"0").eq(PermissionPlmListRuleItem::getRoleCode,appPermissionDto.getRoleCode()));
        if(CollectionUtils.isNotEmpty(oldListRuleItems)){
            List<String> oldListRuleItemBids = oldListRuleItems.stream().map(PermissionPlmListRuleItem::getBid).collect(Collectors.toList());
            permissionPlmListRuleItemMapper.deleteByBids(oldListRuleItemBids);
        }
        List<PermissionPlmOperationRuleItem> oldOperationRuleItems = permissionPlmOperationRuleItemService.list(new LambdaQueryWrapper<PermissionPlmOperationRuleItem>().eq(PermissionPlmOperationRuleItem::getPermissionBid,permissionBid).eq(PermissionPlmOperationRuleItem::getPath,"0").eq(PermissionPlmOperationRuleItem::getRoleCode,appPermissionDto.getRoleCode()));
        if(CollectionUtils.isNotEmpty(oldOperationRuleItems)){
            List<String> oldOperationRuleItemBids = oldOperationRuleItems.stream().map(PermissionPlmOperationRuleItem::getBid).collect(Collectors.toList());
            permissionPlmOperationRuleItemMapper.deleteByBids(oldOperationRuleItemBids);
        }
        return true;
    }

    private boolean editConfigRule(AppPermissionDto appPermissionDto) {
        String permissionBid = appPermissionDto.getPermissionBid();
        List<PermissionPlmAddRuleItem> addRuleItems = new ArrayList<>();
        List<PermissionPlmListRuleItem> listRuleItems = new ArrayList<>();
        List<PermissionPlmOperationRuleItem> operationRuleItems = new ArrayList<>();
        getPermissionRuleItems(appPermissionDto, addRuleItems, listRuleItems, operationRuleItems, permissionBid);
        //需要先删除原来的数据在更新新的数据
        List<PermissionPlmAddRuleItem> oldAddRuleItems = permissionPlmAddRuleItemService.list(new LambdaQueryWrapper<PermissionPlmAddRuleItem>().eq(PermissionPlmAddRuleItem::getPermissionBid,permissionBid).eq(PermissionPlmAddRuleItem::getPath,"0"));
        if(CollectionUtils.isNotEmpty(oldAddRuleItems)){
            List<String> oldAddRuleItemBids = oldAddRuleItems.stream().map(PermissionPlmAddRuleItem::getBid).collect(Collectors.toList());
            permissionPlmAddRuleItemMapper.deleteByBids(oldAddRuleItemBids);
        }
        List<PermissionPlmListRuleItem> oldListRuleItems = permissionPlmListRuleItemService.list(new LambdaQueryWrapper<PermissionPlmListRuleItem>().eq(PermissionPlmListRuleItem::getPermissionBid,permissionBid).eq(PermissionPlmListRuleItem::getPath,"0"));
        if(CollectionUtils.isNotEmpty(oldListRuleItems)){
            List<String> oldListRuleItemBids = oldListRuleItems.stream().map(PermissionPlmListRuleItem::getBid).collect(Collectors.toList());
            permissionPlmListRuleItemMapper.deleteByBids(oldListRuleItemBids);
        }
        List<PermissionPlmOperationRuleItem> oldOperationRuleItems = permissionPlmOperationRuleItemService.list(new LambdaQueryWrapper<PermissionPlmOperationRuleItem>().eq(PermissionPlmOperationRuleItem::getPermissionBid,permissionBid).eq(PermissionPlmOperationRuleItem::getPath,"0"));
        if(CollectionUtils.isNotEmpty(oldOperationRuleItems)){
            List<String> oldOperationRuleItemBids = oldOperationRuleItems.stream().map(PermissionPlmOperationRuleItem::getBid).collect(Collectors.toList());
            permissionPlmOperationRuleItemMapper.deleteByBids(oldOperationRuleItemBids);
        }
        List<PermissionPlmRuleCondition> oldRuleConditions = permissionPlmRuleConditionService.list(new LambdaQueryWrapper<PermissionPlmRuleCondition>().eq(PermissionPlmRuleCondition::getPermissionBid, permissionBid));
        if(CollectionUtils.isNotEmpty(oldRuleConditions)){
            List<Integer> oldRuleConditionsIds = oldRuleConditions.stream().map(PermissionPlmRuleCondition::getId).collect(Collectors.toList());
            permissionPlmRuleConditionService.removeBatchByIds(oldRuleConditionsIds);
        }
        String jobNumber = SsoHelper.getJobNumber();
        List<PermissionPlmRuleCondition> permissionPlmRuleConditionList = appPermissionDto.getPermissionPlmRuleConditionList();
        for (PermissionPlmRuleCondition permissionPlmRuleCondition : permissionPlmRuleConditionList) {
            permissionPlmRuleCondition.setBid(SnowflakeIdWorker.nextIdStr());
            permissionPlmRuleCondition.setPermissionBid(permissionBid);
            permissionPlmRuleCondition.setCreatedBy(jobNumber);
            permissionPlmRuleCondition.setUpdatedBy(jobNumber);
            permissionPlmRuleCondition.setAppBid(appPermissionDto.getSpaceAppBid());
            permissionPlmRuleCondition.setEnableFlag(true);
            permissionPlmRuleCondition.setDeleteFlag(false);
        }
        if (CollectionUtils.isNotEmpty(permissionPlmRuleConditionList)){
            permissionPlmRuleConditionService.saveBatch(permissionPlmRuleConditionList);
        }
        if(CollectionUtils.isNotEmpty(addRuleItems)){
            permissionPlmAddRuleItemService.saveBatch(addRuleItems);
        }
        if(CollectionUtils.isNotEmpty(listRuleItems)){
            permissionPlmListRuleItemService.saveBatch(listRuleItems);
        }
        if(CollectionUtils.isNotEmpty(operationRuleItems)){
            permissionPlmOperationRuleItemService.saveBatch(operationRuleItems);
        }
        if(StringUtils.isNotEmpty(appPermissionDto.getRuleName())){
            PermissionPlmRule permissionPlmRule = permissionPlmRuleService.getOne(new LambdaQueryWrapper<PermissionPlmRule>().eq(PermissionPlmRule::getBid,permissionBid));
            permissionPlmRule.setRuleName(appPermissionDto.getRuleName());
            permissionPlmRuleService.updateById(permissionPlmRule);
        }
        return true;
    }

    private boolean addConditionConfigRule(AppPermissionDto appPermissionDto){
        List<PermissionPlmRuleCondition> permissionPlmRuleConditionList = appPermissionDto.getPermissionPlmRuleConditionList();
        if (CollectionUtils.isNotEmpty(permissionPlmRuleConditionList)) {
            for (PermissionPlmRuleCondition permissionPlmRuleCondition : permissionPlmRuleConditionList) {
                if(permissionPlmRuleCondition == null || StringUtils.isBlank(permissionPlmRuleCondition.getAttrCode()) || StringUtils.isBlank(permissionPlmRuleCondition.getOperator())){
                    throw new BusinessException("条件配置不能为空");
                }
            }
        }
        String permissionBid = "LC:"+ appPermissionDto.getSpaceAppBid()+":"+SnowflakeIdWorker.nextIdStr();
        if(CollectionUtils.isEmpty(appPermissionDto.getOperationItems())){
            throw new RuntimeException("操作项不能为空");
        }
        List<PermissionPlmAddRuleItem> addRuleItems = new ArrayList<>();
        List<PermissionPlmListRuleItem> listRuleItems = new ArrayList<>();
        List<PermissionPlmOperationRuleItem> operationRuleItems = new ArrayList<>();
        getPermissionRuleItems(appPermissionDto, addRuleItems, listRuleItems, operationRuleItems, permissionBid);
        //获取对象穿透的权限
        String jobNumber = SsoHelper.getJobNumber();
        PermissionPlmRule permissionPlmRuleNew = new PermissionPlmRule();
        permissionPlmRuleNew.setBid(permissionBid);
        permissionPlmRuleNew.setPath("0");
        permissionPlmRuleNew.setDeleteFlag(false);
        permissionPlmRuleNew.setEnableFlag(true);
        permissionPlmRuleNew.setCreatedBy(jobNumber);
        permissionPlmRuleNew.setUpdatedBy(jobNumber);
        permissionPlmRuleNew.setRuleName(appPermissionDto.getRuleName());
        permissionPlmRuleService.save(permissionPlmRuleNew);
        if(CollectionUtils.isNotEmpty(addRuleItems)){
            permissionPlmAddRuleItemService.saveBatch(addRuleItems);
        }
        if(CollectionUtils.isNotEmpty(listRuleItems)){
            permissionPlmListRuleItemService.saveBatch(listRuleItems);
        }
        if(CollectionUtils.isNotEmpty(operationRuleItems)){
            permissionPlmOperationRuleItemService.saveBatch(operationRuleItems);
        }
        if (CollectionUtils.isNotEmpty(permissionPlmRuleConditionList)) {
            for (PermissionPlmRuleCondition permissionPlmRuleCondition : permissionPlmRuleConditionList) {
                permissionPlmRuleCondition.setBid(SnowflakeIdWorker.nextIdStr());
                permissionPlmRuleCondition.setPermissionBid(permissionBid);
                permissionPlmRuleCondition.setCreatedBy(jobNumber);
                permissionPlmRuleCondition.setUpdatedBy(jobNumber);
                permissionPlmRuleCondition.setAppBid(appPermissionDto.getSpaceAppBid());
                permissionPlmRuleCondition.setEnableFlag(true);
                permissionPlmRuleCondition.setDeleteFlag(false);
            }
            permissionPlmRuleConditionService.saveBatch(permissionPlmRuleConditionList);
        }
        //修改实例数据
        //permissionInstanceService.updateInstanceByCondition(permissionBid,appPermissionDto);
        return true;
    }

    private boolean addConfigRule(AppPermissionDto appPermissionDto){
        String permissionBid = "APP:" + appPermissionDto.getSpaceAppBid();
        if (StringUtils.isNotEmpty(appPermissionDto.getInstanceBid())) {
            permissionBid = "PRI:" + appPermissionDto.getSpaceAppBid() + ":" + appPermissionDto.getInstanceBid();
        }
        List<PermissionPlmAddRuleItem> addRuleItems = new ArrayList<>();
        List<PermissionPlmListRuleItem> listRuleItems = new ArrayList<>();
        List<PermissionPlmOperationRuleItem> operationRuleItems = new ArrayList<>();
        getPermissionRuleItems(appPermissionDto, addRuleItems, listRuleItems, operationRuleItems, permissionBid);
        //获取对象穿透的权限
        String jobNumber = SsoHelper.getJobNumber();
        PermissionPlmRule permissionPlmRuleNew = new PermissionPlmRule();
        if (StringUtils.isEmpty(appPermissionDto.getInstanceBid())) {
            ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(appPermissionDto.getSpaceAppBid());
            if (apmSpaceApp == null) {
                throw new RuntimeException("应用不存在");
            }
            getPermissionByModelCode(apmSpaceApp.getModelCode(), permissionBid, operationRuleItems, addRuleItems, listRuleItems);
            permissionPlmRuleNew.setPath(apmSpaceApp.getModelCode() + ":" + apmSpaceApp.getBid());
        } else {
            permissionPlmRuleNew.setPath("0");
        }
        permissionPlmRuleNew.setBid(permissionBid);
        permissionPlmRuleNew.setDeleteFlag(false);
        permissionPlmRuleNew.setEnableFlag(true);
        permissionPlmRuleNew.setCreatedBy(jobNumber);
        permissionPlmRuleNew.setUpdatedBy(jobNumber);
        permissionPlmRuleNew.setRuleName(appPermissionDto.getRuleName());
        if (CollectionUtils.isNotEmpty(addRuleItems) || CollectionUtils.isNotEmpty(listRuleItems) || CollectionUtils.isNotEmpty(operationRuleItems)) {
            permissionPlmRuleService.save(permissionPlmRuleNew);
            if (CollectionUtils.isNotEmpty(addRuleItems)) {
                permissionPlmAddRuleItemService.saveBatch(addRuleItems);
            }
            if (CollectionUtils.isNotEmpty(listRuleItems)) {
                permissionPlmListRuleItemService.saveBatch(listRuleItems);
            }
            if (CollectionUtils.isNotEmpty(operationRuleItems)) {
                permissionPlmOperationRuleItemService.saveBatch(operationRuleItems);
            }
        }
        appPermissionDto.setPermissionBid(permissionBid);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean copyPermissionByModelCode(ApmSpaceApp apmSpaceApp) {
        String permissionBid = "APP:" + apmSpaceApp.getBid();
        List<PermissionPlmAddRuleItem> addRuleItems = new ArrayList<>();
        List<PermissionPlmListRuleItem> listRuleItems = new ArrayList<>();
        List<PermissionPlmOperationRuleItem> operationRuleItems = new ArrayList<>();
        //获取对象穿透的权限
        String jobNumber = SsoHelper.getJobNumber();
        PermissionPlmRule permissionPlmRuleNew = new PermissionPlmRule();
        PermissionPlmRule permissionByModelCode = getPermissionByModelCode(apmSpaceApp.getModelCode(), permissionBid, operationRuleItems, addRuleItems, listRuleItems);
        if (permissionByModelCode == null) {
            Log.info("{}：对象没有配置权限规则条目", apmSpaceApp.getModelCode());
            return false;
        }
        permissionPlmRuleNew.setRuleName(permissionByModelCode.getRuleName() != null ? permissionByModelCode.getRuleName() : "");
        permissionPlmRuleNew.setPath(apmSpaceApp.getModelCode() + ":" + apmSpaceApp.getBid());
        permissionPlmRuleNew.setBid(permissionBid);
        permissionPlmRuleNew.setDeleteFlag(false);
        permissionPlmRuleNew.setEnableFlag(true);
        permissionPlmRuleNew.setCreatedBy(jobNumber);
        permissionPlmRuleNew.setUpdatedBy(jobNumber);
        return saveBathPermissionData(Collections.singletonList(permissionPlmRuleNew), addRuleItems, listRuleItems, operationRuleItems, null);
    }

    private PermissionPlmRule getPermissionByModelCode(String modelCode,
                                          String permissionBid,
                                          List<PermissionPlmOperationRuleItem> operationRuleItems,
                                          List<PermissionPlmAddRuleItem> addRuleItems,
                                          List<PermissionPlmListRuleItem> listRuleItems) {
        String jobNumber = SsoHelper.getJobNumber();
        PermissionPlmRule permissionPlmRuleObj = permissionPlmRuleService.getOne(new LambdaQueryWrapper<PermissionPlmRule>().eq(PermissionPlmRule::getBid, "OBJ:" + modelCode));
        if (permissionPlmRuleObj != null) {
            List<PermissionPlmOperationRuleItem> operationRuleItemsObj = permissionPlmOperationRuleItemService.list(new LambdaQueryWrapper<PermissionPlmOperationRuleItem>().eq(PermissionPlmOperationRuleItem::getPermissionBid, permissionPlmRuleObj.getBid()));
            for (PermissionPlmOperationRuleItem operationRuleItem : operationRuleItemsObj) {
                PermissionPlmOperationRuleItem operationRuleItemNew = new PermissionPlmOperationRuleItem();
                operationRuleItemNew.setPermissionBid(permissionBid);
                operationRuleItemNew.setBid(SnowflakeIdWorker.nextIdStr());
                operationRuleItemNew.setRoleCode(operationRuleItem.getRoleCode());
                operationRuleItemNew.setPath(permissionBid.replace("APP:",modelCode+":"));
                operationRuleItemNew.setDeleteFlag(false);
                operationRuleItemNew.setEnableFlag(true);
                operationRuleItemNew.setCreatedBy(jobNumber);
                operationRuleItemNew.setUpdatedBy(jobNumber);
                operationRuleItemNew.setOperationDelete(operationRuleItem.getOperationDelete());
                operationRuleItemNew.setOperationDetail(operationRuleItem.getOperationDetail());
                operationRuleItemNew.setOperationEdit(operationRuleItem.getOperationEdit());
                operationRuleItemNew.setOperationMove(operationRuleItem.getOperationMove());
                operationRuleItemNew.setOperationPromote(operationRuleItem.getOperationDetail());
                operationRuleItemNew.setOperationMove(operationRuleItem.getOperationMove());
                operationRuleItemNew.setOperationRevise(operationRuleItem.getOperationDetail());
                operationRuleItemNew.setOperationImport(operationRuleItem.getOperationImport());
                operationRuleItemNew.setOperationExport(operationRuleItem.getOperationExport());
                operationRuleItems.add(operationRuleItemNew);
            }
            List<PermissionPlmAddRuleItem> addRuleItemsObj = permissionPlmAddRuleItemService.list(new LambdaQueryWrapper<PermissionPlmAddRuleItem>().eq(PermissionPlmAddRuleItem::getPermissionBid, permissionPlmRuleObj.getBid()));
            for (PermissionPlmAddRuleItem addRuleItem : addRuleItemsObj) {
                PermissionPlmAddRuleItem addRuleItemNew = new PermissionPlmAddRuleItem();
                addRuleItemNew.setPermissionBid(permissionBid);
                addRuleItemNew.setBid(SnowflakeIdWorker.nextIdStr());
                addRuleItemNew.setRoleCode(addRuleItem.getRoleCode());
                addRuleItemNew.setPath(permissionBid.replace("APP:",modelCode+":"));
                addRuleItemNew.setDeleteFlag(false);
                addRuleItemNew.setEnableFlag(true);
                addRuleItemNew.setCreatedBy(jobNumber);
                addRuleItemNew.setUpdatedBy(jobNumber);
                addRuleItems.add(addRuleItemNew);
            }
            List<PermissionPlmListRuleItem> listRuleItemsObj = permissionPlmListRuleItemService.list(new LambdaQueryWrapper<PermissionPlmListRuleItem>().eq(PermissionPlmListRuleItem::getPermissionBid, permissionPlmRuleObj.getBid()));
            for (PermissionPlmListRuleItem listRuleItem : listRuleItemsObj) {
                PermissionPlmListRuleItem listRuleItemNew = new PermissionPlmListRuleItem();
                listRuleItemNew.setPermissionBid(permissionBid);
                listRuleItemNew.setBid(SnowflakeIdWorker.nextIdStr());
                listRuleItemNew.setRoleCode(listRuleItem.getRoleCode());
                listRuleItemNew.setPath(permissionBid.replace("APP:",modelCode+":"));
                listRuleItemNew.setDeleteFlag(false);
                listRuleItemNew.setEnableFlag(true);
                listRuleItemNew.setCreatedBy(jobNumber);
                listRuleItemNew.setUpdatedBy(jobNumber);
                listRuleItems.add(listRuleItemNew);
            }
        }
        return permissionPlmRuleObj;
    }

    private Map<String,String> getBaseAttrMap(String spaceAppBid){
        Map<String,String> baseAttrMap = new HashMap<>(16);
        baseAttrMap.put(OperatorEnum.DELETE.getCode(),"operationDelete");
        baseAttrMap.put(OperatorEnum.EDIT.getCode(),"operationEdit");
        baseAttrMap.put(OperatorEnum.DETAIL.getCode(),"operationDetail");
        baseAttrMap.put(OperatorEnum.REVISE.getCode(),"operationRevise");
        baseAttrMap.put(OperatorEnum.PROMOTE.getCode(),"operationPromote");
        baseAttrMap.put(OperatorEnum.MOVE.getCode(),"operationMove");
        baseAttrMap.put(OperatorEnum.IMPORT.getCode(),"operationImport");
        baseAttrMap.put(OperatorEnum.EXPORT.getCode(),"operationExport");


        baseAttrMap.put(OperatorEnum.VIEW_MODEL_tableView.getCode(),"modelViewTableView");
        baseAttrMap.put(OperatorEnum.VIEW_MODEL_treeView.getCode(),"modelViewTreeView");
        baseAttrMap.put(OperatorEnum.VIEW_MODEL_multiTreeView.getCode(),"modelViewMultiTreeView");
        baseAttrMap.put(OperatorEnum.VIEW_MODEL_overview.getCode(),"modelViewOverview");
        baseAttrMap.put(OperatorEnum.VIEW_MODEL_measureReport.getCode(),"modelViewMeasureReport");
        baseAttrMap.put(OperatorEnum.VIEW_MODEL_kanbanView.getCode(),"modelViewKanbanView");
        baseAttrMap.put(OperatorEnum.VIEW_MODEL_ganttView.getCode(),"modelViewGanttView");
        baseAttrMap.put(OperatorEnum.LOCK.getCode(),"instanceLock");
        baseAttrMap.put(OperatorEnum.UNLOCK.getCode(),"instanceUnlock");
        baseAttrMap.put(OperatorEnum.SYNC.getCode(),"sync");
        baseAttrMap.put(OperatorEnum.VIEW_MODEL_ALL.getCode(),"modelViewAll");
        List<PermissionPlmOperationMapVO> permissionPlmOperationMapVOS = permissionOperationService.queryOperationByAppId(spaceAppBid);
        if (CollectionUtils.isNotEmpty(permissionPlmOperationMapVOS)) {
            for (PermissionPlmOperationMapVO permissionPlmOperationMapVO : permissionPlmOperationMapVOS) {
                if (!StringUtils.isAnyBlank(permissionPlmOperationMapVO.getOperationCode(),permissionPlmOperationMapVO.getAccessOperationKey())) {
                    baseAttrMap.put(permissionPlmOperationMapVO.getOperationCode(),permissionPlmOperationMapVO.getAccessOperationKey());
                }
            }
        }
        return baseAttrMap;
    }

    private List<PermissionOperationItemDto> getPermissionOperationItemDtoList(String roleCode,List<String> operationList){
        List<PermissionOperationItemDto> list = new ArrayList<>();
        for(String operation:operationList) {
            PermissionOperationItemDto permissionOperationItemDto = new PermissionOperationItemDto();
            permissionOperationItemDto.setRoleCode(roleCode);
            permissionOperationItemDto.setOperatorCode(operation);
            list.add(permissionOperationItemDto);
        }
        return list;
    }

    private void handelAppPermissionDto(AppPermissionDto appPermissionDto){
        if(appPermissionDto != null){
            if(CollectionUtils.isNotEmpty(appPermissionDto.getAppPermissionOperationList())){
                List<PermissionOperationItemDto> operationItems = new ArrayList<>();
                List<String> operationCodes = new ArrayList<>();
                for(PermissionOperationItemVo operationItemVo : appPermissionDto.getAppPermissionOperationList()){
                    if(CollectionUtils.isNotEmpty(operationItemVo.getOperatorList())){
                        for(String operatorCode : operationItemVo.getOperatorList()){
                            if(!operationCodes.contains(operatorCode+"_"+operationItemVo.getRoleCode())){
                                PermissionOperationItemDto operationItem = new PermissionOperationItemDto();
                                operationItem.setRoleType(operationItemVo.getRoleType());
                                if(operationItemVo.getRoleType() == CommonConst.PRI_ROLE_TYPE){
                                    //私有角色
                                    operationItem.setRoleCode(CommonConst.PRI_KEY+operationItemVo.getRoleCode());
                                }else{
                                    operationItem.setRoleCode(operationItemVo.getRoleCode());
                                }
                                operationItem.setOperatorCode(operatorCode);
                                operationItems.add(operationItem);
                                operationCodes.add(operatorCode+"_"+operationItem.getRoleCode());
                            }
                        }
                    }
                }
                appPermissionDto.setOperationItems(operationItems);
            }
        }
        if(CollectionUtils.isEmpty(appPermissionDto.getOperationItems())){
            throw new BusinessException("操作权限不能为空");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBathPermissionData(List<PermissionPlmRule> permissionPlmRuleNews,
                                          List<PermissionPlmAddRuleItem> permissionPlmAddRuleItemNews,
                                          List<PermissionPlmListRuleItem> permissionPlmListRuleItemNews,
                                          List<PermissionPlmOperationRuleItem> permissionPlmOperationRuleItemNews,
                                          List<PermissionPlmRuleCondition> permissionPlmRuleConditionListNe) {
        try {
            if (CollectionUtils.isNotEmpty(permissionPlmRuleNews)) {
                permissionPlmRuleService.saveList(permissionPlmRuleNews);
            }
            if (CollectionUtils.isNotEmpty(permissionPlmAddRuleItemNews)) {
                permissionPlmAddRuleItemService.saveList(permissionPlmAddRuleItemNews);
            }
            if (CollectionUtils.isNotEmpty(permissionPlmListRuleItemNews)) {
                permissionPlmListRuleItemService.saveList(permissionPlmListRuleItemNews);
            }
            if (CollectionUtils.isNotEmpty(permissionPlmOperationRuleItemNews)) {
                permissionPlmOperationRuleItemService.saveList(permissionPlmOperationRuleItemNews);
            }
            if (CollectionUtils.isNotEmpty(permissionPlmRuleConditionListNe)) {
                permissionPlmRuleConditionService.saveBatch(permissionPlmRuleConditionListNe);
            }
        } catch (Exception e) {
            Log.error("保存数据出错====:{}", e);
           throw new RuntimeException();
        }
        return true;
    }

    /**
     * 校验新增权限
     * @param spaceAppBid
     * @param roleCodes
     * @return
     */
    @Override
    public boolean checkAddPermission(String spaceAppBid,List<String> roleCodes){
        return checkPermission(spaceAppBid,roleCodes,OperatorEnum.ADD.getCode());
    }

    /**
     * 校验权限
     * @param spaceAppBid
     * @param roleCodes
     * @return
     */
    @Override
    public boolean checkPermission(String spaceAppBid,List<String> roleCodes, String operatorCode){
        //查询属性权限规则
        List<AppConditionPermissionVo> appConditionPermissionVos = listAppConditionPermission(spaceAppBid);
        if (CollectionUtils.isEmpty(appConditionPermissionVos)) {
            return false;
        }
        for (AppConditionPermissionVo appConditionPermissionVo : appConditionPermissionVos) {
            List<PermissionOperationItemVo> appPermissionOperationListItem = appConditionPermissionVo.getAppPermissionOperationList();
            for (PermissionOperationItemVo permissionOperationItemVo : appPermissionOperationListItem) {
                if (roleCodes.contains(permissionOperationItemVo.getRoleCode()) && permissionOperationItemVo.getOperatorList().contains(operatorCode)){
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public boolean checkInstancePermission(MBaseData appData, String spaceAppBid, String operatorCode ,List<String> roleCodes){
        //查询属性权限规则
        List<AppConditionPermissionVo> appConditionPermissionVos = listAppConditionPermission(spaceAppBid);
        if (CollectionUtils.isEmpty(appConditionPermissionVos)) {
            return false;
        }
        for (AppConditionPermissionVo appConditionPermissionVo : appConditionPermissionVos) {
            if (matchConditionPermission(appData, appConditionPermissionVo.getPermissionPlmRuleConditionList())){
                List<PermissionOperationItemVo> appPermissionOperationListItem = appConditionPermissionVo.getAppPermissionOperationList();
                for (PermissionOperationItemVo permissionOperationItemVo : appPermissionOperationListItem) {
                    if (permissionOperationItemVo.getOperatorList().contains(operatorCode) && roleCodes.contains(permissionOperationItemVo.getRoleCode())){
                        return true;
                    }
                }
            }
        }
        return false;
    }
    /**
     * 校验列表权限
     * @param spaceAppBid
     * @param roleCodes
     * @return
     */
    @Override
    public boolean checkListPermission(String spaceAppBid,List<String> roleCodes){
        return checkPermission(spaceAppBid,roleCodes,OperatorEnum.LIST.getCode());
    }

    /**
     * 获取流程角色信息
     * @param userNo 用户编号
     * @param instanceBid 实例编号
     * @return
     */
    public List<String> getFlowInstanceRoleCodes(String userNo,String instanceBid,String spaceAppBid){
        List<ApmFlowInstanceRoleUser> roleUsers = apmFlowInstanceRoleUserService.listByInstanceBidAndUserNo(instanceBid,userNo,spaceAppBid);
        if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(roleUsers)){
            List<String> roleBids = roleUsers.stream().map(ApmFlowInstanceRoleUser::getRoleBid).collect(Collectors.toList());
            List<ApmRole> apmRoles = apmRoleService.listByBids(roleBids);
            if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(apmRoles)){
                return apmRoles.stream().map(ApmRole::getCode).collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }

    /**
     * 其他校验权限
     * @param roleCodes
     * @param permissionBid
     * @param operatorCode
     * @return
     */
    @Override
    public boolean checkPermission(List<String> roleCodes,String permissionBid,String operatorCode){
        String defaultPermissionPrefix = "DEFAULT:";
        if(StringUtils.isBlank(permissionBid) || permissionBid.startsWith(defaultPermissionPrefix)){
            return true;
        }
        if(StringUtils.isEmpty(operatorCode)){
            throw new BusinessException("operatorCode不能为空");
        }
        if(CollectionUtils.isEmpty(roleCodes)){
            return false;
        }
        LambdaQueryWrapper<PermissionPlmOperationRuleItem> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(PermissionPlmOperationRuleItem::getPermissionBid, permissionBid).in(PermissionPlmOperationRuleItem::getRoleCode, roleCodes);
        if(OperatorEnum.EDIT.getCode().equals(operatorCode)){
            lambdaQueryWrapper.eq(PermissionPlmOperationRuleItem::getOperationEdit,true);
        }else if(OperatorEnum.DELETE.getCode().equals(operatorCode)){
            lambdaQueryWrapper.eq(PermissionPlmOperationRuleItem::getOperationDelete,true);
        }else if(OperatorEnum.DETAIL.getCode().equals(operatorCode)){
            lambdaQueryWrapper.eq(PermissionPlmOperationRuleItem::getOperationDetail,true);
        }else if(OperatorEnum.REVISE.getCode().equals(operatorCode)){
            lambdaQueryWrapper.eq(PermissionPlmOperationRuleItem::getOperationRevise,true);
        }else if(OperatorEnum.PROMOTE.getCode().equals(operatorCode)){
            lambdaQueryWrapper.eq(PermissionPlmOperationRuleItem::getOperationPromote,true);
        }else if(OperatorEnum.MOVE.getCode().equals(operatorCode)){
            lambdaQueryWrapper.eq(PermissionPlmOperationRuleItem::getOperationMove,true);
        }else if(OperatorEnum.IMPORT.getCode().equals(operatorCode)){
            lambdaQueryWrapper.eq(PermissionPlmOperationRuleItem::getOperationImport,true);
        }else if(OperatorEnum.EXPORT.getCode().equals(operatorCode)){
            lambdaQueryWrapper.eq(PermissionPlmOperationRuleItem::getOperationExport,true);
        }else {
            return false;
        }
        long count = permissionPlmOperationRuleItemService.count(lambdaQueryWrapper);
        return count > 0;
    }


    /**
     * 校验操作权限
     * @param spaceAppBid
     * @param data
     * @param roleCodes
     * @return
     */
    @Override
    public boolean checkPermission(String spaceAppBid, MBaseData data,List<String> roleCodes, String operatorCode){
        if(StringUtils.isEmpty(operatorCode)){
            throw new BusinessException("operatorCode不能为空");
        }
        if(CollectionUtils.isEmpty(roleCodes)){
            return false;
        }
        AppBasePermissionVo instancePermissionByBaseData = getInstancePermissionByBaseData(spaceAppBid, data, roleCodes);
        if (instancePermissionByBaseData == null || CollectionUtils.isEmpty(instancePermissionByBaseData.getPermissionOperationList())) {
            return false;
        }
        return instancePermissionByBaseData.getPermissionOperationList().stream().anyMatch(v->v.getOperatorList().contains(operatorCode));
    }

    @Override
    public boolean checkButtonPermission(List<String> roleCodes, String permissionBid, String operatorCode) {
        if(StringUtils.isBlank(permissionBid) || StringUtils.isEmpty(operatorCode) || CollectionUtils.isEmpty(roleCodes)){
            return false;
        }

        LambdaQueryWrapper<PermissionPlmOperationRuleItem> permissionPlmOperationRuleItemWrapper = new LambdaQueryWrapper<>();
        permissionPlmOperationRuleItemWrapper.eq(PermissionPlmOperationRuleItem::getPermissionBid, permissionBid).in(PermissionPlmOperationRuleItem::getRoleCode, roleCodes);
        List<PermissionPlmOperationRuleItem> list = permissionPlmOperationRuleItemService.list(permissionPlmOperationRuleItemWrapper);
        List<String> roleCodeList = list.stream().map(PermissionPlmOperationRuleItem::getRoleCode).collect(Collectors.toList());

        if (CollectionUtil.isEmpty(roleCodeList)) {
            return Boolean.FALSE;
        }

        if (operatorCode.equals(OperatorEnum.ADD.getCode())) {
            LambdaQueryWrapper<PermissionPlmAddRuleItem> permissionPlmAddRuleItemWrapper = new LambdaQueryWrapper<>();
            permissionPlmAddRuleItemWrapper.eq(PermissionPlmAddRuleItem::getPermissionBid, permissionBid);
            permissionPlmAddRuleItemWrapper.in(PermissionPlmAddRuleItem::getRoleCode, roleCodeList);
            long count = permissionPlmAddRuleItemService.count(permissionPlmAddRuleItemWrapper);
            return count > 0;
        } else if (operatorCode.equals(OperatorEnum.LIST.getCode())) {
            LambdaQueryWrapper<PermissionPlmListRuleItem> permissionPlmListRuleItemWrapper = new LambdaQueryWrapper<>();
            permissionPlmListRuleItemWrapper.eq(PermissionPlmListRuleItem::getPermissionBid, permissionBid);
            permissionPlmListRuleItemWrapper.in(PermissionPlmListRuleItem::getRoleCode, roleCodeList);
            long count = permissionPlmListRuleItemService.count(permissionPlmListRuleItemWrapper);
            return  count > 0;
        } else {
            return checkPermission(roleCodes, permissionBid, operatorCode);
        }
    }

    @Override
    public boolean checkPermissionList(List<String> roleCodes,List<String> permissionBids,String operatorCode){
        if(CollectionUtils.isEmpty(permissionBids)){
            return true;
        }
        for(int i = permissionBids.size() - 1; i >= 0; i--){
           if(permissionBids.get(i).startsWith("DEFAULT:")){
               permissionBids.remove(i);
           }
        }
        if(CollectionUtils.isEmpty(permissionBids)){
            return true;
        }
        if(StringUtils.isEmpty(operatorCode)){
            throw new BusinessException("operatorCode不能为空");
        }
        if(CollectionUtils.isEmpty(roleCodes)){
            return false;
        }
        LambdaQueryWrapper<PermissionPlmOperationRuleItem> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.in(PermissionPlmOperationRuleItem::getPermissionBid, permissionBids).in(PermissionPlmOperationRuleItem::getRoleCode, roleCodes);
        if(OperatorEnum.EDIT.getCode().equals(operatorCode)){
            lambdaQueryWrapper.eq(PermissionPlmOperationRuleItem::getOperationEdit,true);
        }else if(OperatorEnum.DELETE.getCode().equals(operatorCode)){
            lambdaQueryWrapper.eq(PermissionPlmOperationRuleItem::getOperationDelete,true);
        }else if(OperatorEnum.DETAIL.getCode().equals(operatorCode)){
            lambdaQueryWrapper.eq(PermissionPlmOperationRuleItem::getOperationDetail,true);
        }else if(OperatorEnum.REVISE.getCode().equals(operatorCode)){
            lambdaQueryWrapper.eq(PermissionPlmOperationRuleItem::getOperationRevise,true);
        }else if(OperatorEnum.PROMOTE.getCode().equals(operatorCode)){
            lambdaQueryWrapper.eq(PermissionPlmOperationRuleItem::getOperationPromote,true);
        }else if(OperatorEnum.MOVE.getCode().equals(operatorCode)){
            lambdaQueryWrapper.eq(PermissionPlmOperationRuleItem::getOperationMove,true);
        }else {
            return false;
        }
        long count = permissionPlmOperationRuleItemService.count(lambdaQueryWrapper);
        return count > 0;
    }


    @Override
    public List<ConditionOperatorVo> getConditionOperators(){
        List<ConditionOperatorVo> conditionOperatorVos = new ArrayList<>();
        ConditionOperatorVo conditionOperatorVo = new ConditionOperatorVo();
        conditionOperatorVo.setLabel("等于");
        conditionOperatorVo.setValue("EQ");
        conditionOperatorVos.add(conditionOperatorVo);
        ConditionOperatorVo conditionOperatorVo1 = new ConditionOperatorVo();
        conditionOperatorVo1.setLabel("不等于");
        conditionOperatorVo1.setValue("NE");
        conditionOperatorVos.add(conditionOperatorVo1);
        ConditionOperatorVo conditionOperatorVo2 = new ConditionOperatorVo();
        conditionOperatorVo2.setLabel("大于");
        conditionOperatorVo2.setValue("GT");
        conditionOperatorVos.add(conditionOperatorVo2);
        ConditionOperatorVo conditionOperatorVo3 = new ConditionOperatorVo();
        conditionOperatorVo3.setLabel("小于");
        conditionOperatorVo3.setValue("LT");
        conditionOperatorVos.add(conditionOperatorVo3);
        ConditionOperatorVo conditionOperatorVo4 = new ConditionOperatorVo();
        conditionOperatorVo4.setLabel("大于等于");
        conditionOperatorVo4.setValue("GE");
        conditionOperatorVos.add(conditionOperatorVo4);
        ConditionOperatorVo conditionOperatorVo5 = new ConditionOperatorVo();
        conditionOperatorVo5.setLabel("小于等于");
        conditionOperatorVo5.setValue("LE");
        conditionOperatorVos.add(conditionOperatorVo5);
        ConditionOperatorVo conditionOperatorVo6 = new ConditionOperatorVo();
        conditionOperatorVo6.setLabel("为空");
        conditionOperatorVo6.setValue("NULL");
        conditionOperatorVos.add(conditionOperatorVo6);
        ConditionOperatorVo conditionOperatorVo7 = new ConditionOperatorVo();
        conditionOperatorVo7.setLabel("不为空");
        conditionOperatorVo7.setValue("NOT NULL");
        conditionOperatorVos.add(conditionOperatorVo7);
        return conditionOperatorVos;
    }

    /**
     * 通过实例数据获取当前用户的角色
     * @param spaceAppBid
     * @param data
     * @return
     */
    @Override
    public List<String> listRoleCodeByBaseData(String spaceAppBid, MBaseData data) {
        List<String> roleCodes = Lists.newArrayList();
        Assert.hasText(spaceAppBid, "spaceAppBid不能为空");
        ApmSpaceApp spaceApp = apmSpaceAppService.getByBid(spaceAppBid);
        String spaceBid = spaceApp.getSpaceBid();
        String userNO = SsoHelper.getJobNumber();
        ApmSphere apmSphere = apmSphereService.getByBizBidAndType(spaceBid, TypeEnum.SPACE.getCode());
        if(apmSphere != null){
            List<ApmRole> apmRoles = apmRoleDomainService.listRoleByJobNumAndSphereBid(userNO, apmSphere.getBid());
            if(CollectionUtils.isNotEmpty(apmRoles)){
                roleCodes.addAll(apmRoles.stream().map(ApmRole::getCode).collect(Collectors.toList()));
                // 如果在该空间下有任意非"ALL"角色，则补充空间角色
                if (apmRoles.stream().anyMatch(role-> apmSphere.getBid().equals(role.getSphereBid()) && !RoleConstant.ALL.equals(role.getCode()))) {
                    roleCodes.add(RoleConstant.SPACE_MEMBER_EN);
                }
            }
        }
        /*String persmissionBid = data.get(ObjectEnum.PERMISSION_BID.getCode())+"";
        //私有角色权限判断
        if(data.get(SpaceAppDataEnum.WORK_ITEM_TYPE.getCode()) != null && persmissionBid.equals(CommonConst.APP_FLAG+spaceAppBid)){
            //查询流程角色
            List<String> flowCodes = getFlowInstanceRoleCodes(userNO,data.getBid(),spaceAppBid);
            if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(flowCodes)){
                for(String flowCode:flowCodes){
                    roleCodes.add(CommonConst.PRI_KEY+flowCode);
                }
            }
        }*/
        addInstanceRole(data, userNO, roleCodes);
        return roleCodes;
    }

    public void addInstanceRole(MBaseData data, String userNO, List<String> roleCodes) {
        // 加密人
        if("IS_YES".equals(data.get(ObjectEnum.IS_CONL_REQUIRE.getCode())) && data.get(ObjectEnum.CONFIDENTIAL_MEMBER.getCode()) != null){
            Object value = data.get(ObjectEnum.CONFIDENTIAL_MEMBER.getCode());
            if(value instanceof List){
                try{
                    List<String> values = JSON.parseArray(JSON.toJSONString(value), String.class);
                    if(values.contains(userNO)){
                        roleCodes.add(InnerRoleEnum.CONFIDENTIAL_MEMBER.getCode());
                    }
                }catch (Exception e){
                    log.error("checkPermission error",e);
                }
            }else if(value instanceof String){
                String person = value.toString();
                if(userNO.equals(person)){
                    roleCodes.add(InnerRoleEnum.CONFIDENTIAL_MEMBER.getCode());
                }
            }
        }
        permissionCheckService.getInnerRole(data, userNO, roleCodes);
    }

    /**
     * 根据实例数据和角色编码获取权限
     * @param spaceAppBid
     * @param data
     * @param roleCodes
     * @return
     */
    @Override
    public AppBasePermissionVo getInstancePermissionByBaseData(String spaceAppBid, MBaseData data,List<String> roleCodes) {
        //查询属性权限规则
        List<AppConditionPermissionVo> appConditionPermissionVos = listAppConditionPermission(spaceAppBid);
        if (CollectionUtils.isEmpty(appConditionPermissionVos)) {
            return null;
        }
        List<PermissionOperationItemVo> appPermissionOperationList = new ArrayList<>();
        for (AppConditionPermissionVo appConditionPermissionVo : appConditionPermissionVos) {
            if (matchConditionPermission(data, appConditionPermissionVo.getPermissionPlmRuleConditionList())){
                List<PermissionOperationItemVo> appPermissionOperationListItem = appConditionPermissionVo.getAppPermissionOperationList();
                for (PermissionOperationItemVo permissionOperationItemVo : appPermissionOperationListItem) {
                    if (roleCodes.contains(permissionOperationItemVo.getRoleCode())){
                        appPermissionOperationList.add(permissionOperationItemVo);
                    }
                }
            }
        }
        //组装返回结果
        AppBasePermissionVo appBasePermissionVo = new AppBasePermissionVo();
        appBasePermissionVo.setPermissionOperationList(appPermissionOperationList);
        appBasePermissionVo.setObjPermissionOperationList(new ArrayList<>());
        return appBasePermissionVo;
    }

    /**
     * 查询当前用户的角色的权限管理下的按钮
     * @param spaceAppBid
     * @param instanceBid
     * @param data
     * @return
     */
    @Override
    public List<ApmActionConfigUserVo> listAppPermissions(String spaceAppBid, String instanceBid, MBaseData data) {
        List<ApmActionConfigUserVo> result = Lists.newArrayList();
        List<String> roleCodes = listRoleCodeByBaseData(spaceAppBid,data);
        AppBasePermissionVo vo = getInstancePermissionByBaseData(spaceAppBid,data,roleCodes);
        Set<String> operationSet = Sets.newHashSet();
        //当前处理人默认有编辑，查看详情权限
        if(roleCodes.contains(InnerRoleEnum.PERSON_RESPONSIBLE.getCode())){
            operationSet.addAll(Arrays.asList(OperatorEnum.EDIT.getCode(), OperatorEnum.DETAIL.getCode(), OperatorEnum.REVISE.getCode(), OperatorEnum.PROMOTE.getCode()));
        }
        // 当前关注人默认有查看详情权限
        if(roleCodes.contains(InnerRoleEnum.FOLLOW_MEMBER.getCode())){
            operationSet.add(OperatorEnum.DETAIL.getCode());
        }
        // 当前加密人默认有查看详情权限
        if(roleCodes.contains(InnerRoleEnum.CONFIDENTIAL_MEMBER.getCode())){
            operationSet.add(OperatorEnum.DETAIL.getCode());
        }
        // 如果没有配置权限,则默认全部按钮都可用
        if(vo == null){
            vo = new AppBasePermissionVo();
            vo.setPermissionOperationList(Lists.newArrayList());
            vo.setObjPermissionOperationList(Lists.newArrayList());
            operationSet.addAll(OPERATOR_ORDER_LIST);
        }
        List<PermissionOperationItemVo> operationList = new ArrayList<>();
        operationList.addAll(vo.getPermissionOperationList());
        operationList.addAll(vo.getObjPermissionOperationList());
        // 去重后的按钮CODE集合
        operationSet.addAll(operationList.stream().flatMap(operation -> operation.getOperatorList().stream() ).collect(Collectors.toSet()));
        // 查询按钮分组的字典
        Map<String, String> codeGroupMap = defaultAppExcelTemplateService.getDictCodeAndEnNameMap(BUTTON_CODE_GROUP);
        Set<String> extraOperations = new HashSet<>();
        for (String operation : operationSet) {
            if (codeGroupMap.containsKey(operation)) {
                Optional.ofNullable( codeGroupMap.get(operation)).ifPresent(obj -> {
                    extraOperations.addAll(JSONArray.parseArray(obj,String.class));
                });
            } else {
                extraOperations.add(operation);
            }
        }
        operationSet.addAll(extraOperations);
        operationSet.forEach(code -> {
            ApmActionConfigUserVo actionConfigUserVo = new ApmActionConfigUserVo();
            actionConfigUserVo.setAction(code);
            actionConfigUserVo.setActionType("inner");
            result.add(actionConfigUserVo);
        });
        return result;
    }


    private Boolean matchConditionPermission(MBaseData appData, List<PermissionPlmRuleCondition> ruleConditions) {
        if (CollectionUtils.isEmpty(ruleConditions)) {
            return true;
        }
        if (MapUtils.isEmpty(appData)) {
            return false;
        }
        //对动态字段值进行解析转换
        analysis(ruleConditions);
        //获取对象表的元数据
        TableDefinition tableDefinition = ObjectTools.fillTableDefinition(appData.get(ObjectEnum.MODEL_CODE.getCode()).toString());
        Set<String> jsonFiledSet = tableDefinition.getTableAttributeDefinitions().stream().filter(tableAttributeDefinition -> "json".equals(tableAttributeDefinition.getType()))
                .map(TableAttributeDefinition::getProperty).collect(Collectors.toSet());
        UniversalComparator universalComparator = new UniversalComparator();
        for (PermissionPlmRuleCondition ruleCondition : ruleConditions) {
            String attrCode = ruleCondition.getAttrCode();
            Object attrValue = ruleCondition.getAttrCodeValue();
            if (!Arrays.asList(ConditionOperatorConstant.IS_NULL, ConditionOperatorConstant.IS_NOT_NULL).contains(ruleCondition.getOperator())
                    && ObjectUtils.isEmpty(attrValue)) {
                return false;
            }
            switch (ruleCondition.getOperator()) {
                case ConditionOperatorConstant.EQUAL:
                    boolean matched = attrValue.equals(appData.get(attrCode)) || (jsonFiledSet.contains(attrCode) && jsonEqualOrContains(appData.get(attrCode), attrValue));
                    if (!matched) {
                        return false;
                    }
                    break;
                case ConditionOperatorConstant.NOT_EQUAL:
                    if (universalComparator.compare(appData.get(attrCode), attrValue) == 0) {
                        return false;
                    }
                    break;
                case ConditionOperatorConstant.GREATER_THAN:
                    if (!(universalComparator.compare(appData.get(attrCode), attrValue) > 0)) {
                        return false;
                    }
                    break;
                case ConditionOperatorConstant.GREATER_THAN_OR_EQUAL:
                    if (!(universalComparator.compare(appData.get(attrCode), attrValue) >= 0)) {
                        return false;
                    }
                    break;
                case ConditionOperatorConstant.LESS_THAN:
                    if (!(universalComparator.compare(appData.get(attrCode), attrValue) < 0)) {
                        return false;
                    }
                    break;
                case ConditionOperatorConstant.LESS_THAN_OR_EQUAL:
                    if (!(universalComparator.compare(appData.get(attrCode), attrValue) <= 0)) {
                        return false;
                    }
                    break;
                case ConditionOperatorConstant.IN:
                    if (attrValue instanceof List) {
                        if (appData.get(attrCode) instanceof List) {
                            if (!((List<?>) attrValue).containsAll((List<?>) appData.get(attrCode))) {
                                return false;
                            }
                        } else if (!((List<?>) attrValue).contains(appData.get(attrCode))) {
                            return false;
                        }
                    } else if (appData.get(attrCode) instanceof List) {
                        if (((List<?>) appData.get(attrCode)).contains(attrValue)) {
                            return true;
                        } else {
                            return false;
                        }
                    } else if (!(universalComparator.compare(appData.get(attrCode), attrValue) == 0)) {
                        return false;
                    }
                    break;
                case ConditionOperatorConstant.NOT_IN:
                    if (attrValue instanceof List) {
                        if (appData.get(attrCode) instanceof List) {
                            if (((List<?>) attrValue).containsAll((List<?>) appData.get(attrCode))) {
                                return false;
                            }
                        } else if (((List<?>) attrValue).contains(appData.get(attrCode))) {
                            return false;
                        }
                    } else if ((universalComparator.compare(appData.get(attrCode), attrValue) == 0)) {
                        return false;
                    }
                    break;
                case ConditionOperatorConstant.LIKE:
                    if (appData.get(attrCode) instanceof List) {
                        if (!((List<?>) appData.get(attrCode)).contains(attrValue)) {
                            return false;
                        }
                    } else if (!String.valueOf(appData.get(attrCode)).contains(String.valueOf(attrValue))) {
                        return false;
                    }
                    break;
                case ConditionOperatorConstant.IS_NULL:
                    if (ObjectUtil.isNotEmpty(appData.get(attrCode)) && !"[]".equals(appData.get(attrCode))) {
                        return false;
                    }
                    break;
                case ConditionOperatorConstant.IS_NOT_NULL:
                    if (ObjectUtil.isEmpty(appData.get(attrCode)) || "[]".equals(appData.get(attrCode))) {
                        return false;
                    }
                    break;
            }
        }
        return true;
    }


    private static boolean jsonEqualOrContains(Object obj1, Object obj2) {
        if (ObjectUtils.isEmpty(obj1) && ObjectUtils.isEmpty(obj2)) {
            return true;
        }
        if (ObjectUtils.isEmpty(obj1) || ObjectUtils.isEmpty(obj2)) {
            return false;
        }
        if(obj1 instanceof JSONArray && obj2 instanceof String) {
            return JSON.toJSONString(obj1).contains(obj2.toString());
        }
        if (obj1 instanceof String) {
            obj1 = JSON.parse((String) obj1);
        }
        if (obj2 instanceof String) {
            obj2 = JSON.parse((String) obj2);
        }
        return obj1.equals(obj2);
    }


    /**
     * 对动态字段进行解析
     *
     * @param conditionList 条件列表
     */
    private void analysis(List<PermissionPlmRuleCondition> conditionList) {
        if (CollectionUtils.isNotEmpty(conditionList)) {
            conditionList.forEach(condition -> {
                Object value = condition.getAttrCodeValue();
                condition.setAttrCodeValue(dynamicFieldConverter.convert(value));
            });
        }
    }

}
