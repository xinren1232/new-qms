package com.transcend.plm.configcenter.permission.application.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.esotericsoftware.minlog.Log;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectTreeVo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.configcenter.api.model.table.vo.CfgTableVo;
import com.transcend.plm.configcenter.common.util.CollectionUtils;
import com.transcend.plm.configcenter.object.application.service.ICfgObjectAppService;
import com.transcend.plm.configcenter.permission.application.service.IPermissionConfigService;
import com.transcend.plm.configcenter.permission.enums.OperatorEnum;
import com.transcend.plm.configcenter.permission.infrastructure.repository.dto.PermissionResult;
import com.transcend.plm.configcenter.permission.infrastructure.repository.mapper.*;
import com.transcend.plm.configcenter.permission.infrastructure.repository.po.PermissionPlmAddRuleItem;
import com.transcend.plm.configcenter.permission.infrastructure.repository.po.PermissionPlmListRuleItem;
import com.transcend.plm.configcenter.permission.infrastructure.repository.po.PermissionPlmOperationRuleItem;
import com.transcend.plm.configcenter.permission.infrastructure.repository.po.PermissionPlmRule;
import com.transcend.plm.configcenter.permission.infrastructure.repository.service.PermissionPlmAddRuleItemService;
import com.transcend.plm.configcenter.permission.infrastructure.repository.service.PermissionPlmListRuleItemService;
import com.transcend.plm.configcenter.permission.infrastructure.repository.service.PermissionPlmOperationRuleItemService;
import com.transcend.plm.configcenter.permission.infrastructure.repository.service.PermissionPlmRuleService;
import com.transcend.plm.configcenter.permission.pojo.dto.AppPermissionDto;
import com.transcend.plm.configcenter.permission.pojo.dto.PermissionOperationItemDto;
import com.transcend.plm.configcenter.permission.pojo.vo.ObjPermissionVo;
import com.transcend.plm.configcenter.permission.pojo.vo.PermissionOperationItemVo;
import com.transcend.plm.configcenter.space.repository.po.ApmSpaceApp;
import com.transcend.plm.configcenter.space.service.ApmSpaceAppService;
import com.transcend.plm.configcenter.table.domain.service.CfgTableDomainService;
import com.transsion.framework.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class PermissionConfigServiceImpl implements IPermissionConfigService {

    @Resource
    private PermissionPlmAddRuleItemService permissionPlmAddRuleItemService;
    @Resource
    private PermissionPlmListRuleItemService permissionPlmListRuleItemService;
    @Resource
    private PermissionPlmOperationRuleItemService permissionPlmOperationRuleItemService;
    @Resource
    private PermissionPlmRuleService permissionPlmRuleService;

    @Resource
    private PermissionPlmAddRuleItemMapper permissionPlmAddRuleItemMapper;
    @Resource
    private PermissionPlmListRuleItemMapper permissionPlmListRuleItemMapper;
    @Resource
    private PermissionPlmOperationRuleItemMapper permissionPlmOperationRuleItemMapper;
    @Resource
    private PermissionPlmRuleMapper permissionPlmRuleMapper;
    @Resource
    private ApmSpaceAppService apmSpaceAppService;

    @Resource
    private ICfgObjectAppService objectModelAppService;

    @Resource
    private CfgTableDomainService cfgTableDomainService;
    @Resource
    private PermissionInsanceMapper permissionInsanceMapper;

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByPermissionBid(String modelCode,String permissionBid){
        permissionPlmAddRuleItemMapper.deleteByPermissionBid(permissionBid);
        permissionPlmListRuleItemMapper.deleteByPermissionBid(permissionBid);
        permissionPlmOperationRuleItemMapper.deleteByPermissionBid(permissionBid);
        permissionPlmRuleMapper.deleteByPermissionBid(permissionBid);
        // todo 权限穿透数据需要删除
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveOrUpdateBasePermissionConfig(AppPermissionDto appPermissionDto) {
        handelAppPermissionDto(appPermissionDto);
        if(CollectionUtils.isEmpty(appPermissionDto.getOperationItems())){
            delPermissionByPerBid(appPermissionDto.getPermissionBid());
            return true;
            //throw new BusinessException("操作权限不能为空");
        }
        if(StringUtils.isEmpty(appPermissionDto.getPermissionBid())){
            //新增
            return addConfigRule(appPermissionDto);
        }else{
            //修改
            return editConfigRule(appPermissionDto);
        }
    }

    private Map<String,String> getBaseAttrMap(){
        Map<String,String> baseAttrMap = new HashMap<>();
        baseAttrMap.put(OperatorEnum.DELETE.getCode(),"operationDelete");
        baseAttrMap.put(OperatorEnum.EDIT.getCode(),"operationEdit");
        baseAttrMap.put(OperatorEnum.DETAIL.getCode(),"operationDetail");
        baseAttrMap.put(OperatorEnum.REVISE.getCode(),"operationRevise");
        baseAttrMap.put(OperatorEnum.PROMOTE.getCode(),"operationPromote");
        return baseAttrMap;
    }
    private void getPermissionRuleItems(AppPermissionDto appPermissionDto, List<PermissionPlmAddRuleItem> addRuleItems, List<PermissionPlmListRuleItem> listRuleItems, List<PermissionPlmOperationRuleItem> operationRuleItems, String permissionBid){
        if(CollectionUtils.isEmpty(appPermissionDto.getOperationItems())){
            return;
        }
        Map<String,String> baseAttrMap = getBaseAttrMap();
        Map<String,Map<String,Object>>  objectAllMap = new HashMap<>();
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
                listRuleItem.setDeleteFlag(false);
                listRuleItem.setEnableFlag(true);
                listRuleItem.setCreatedBy(jobNumber);
                listRuleItem.setUpdatedBy(jobNumber);
                listRuleItems.add(listRuleItem);
            }else{
                Map<String,Object> objectMap = objectAllMap.get(operationItemDto.getRoleCode());
                if(objectMap == null){
                    objectMap = new HashMap<>();
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
                operationRuleItem.setDeleteFlag(false);
                operationRuleItem.setEnableFlag(true);
                operationRuleItem.setCreatedBy(jobNumber);
                operationRuleItem.setUpdatedBy(jobNumber);
                operationRuleItems.add(operationRuleItem);
            }
        }
    }

    private boolean editConfigRule(AppPermissionDto appPermissionDto) {
        String permissionBid = appPermissionDto.getPermissionBid();
        List<PermissionPlmAddRuleItem> addRuleItems = new ArrayList<>();
        List<PermissionPlmListRuleItem> listRuleItems = new ArrayList<>();
        List<PermissionPlmOperationRuleItem> operationRuleItems = new ArrayList<>();
        getPermissionRuleItems(appPermissionDto, addRuleItems, listRuleItems, operationRuleItems, permissionBid);
        //需要先删除原来的数据在更新新的数据
        delPermissionByPerBid(permissionBid);
        if(CollectionUtils.isNotEmpty(addRuleItems)){
            permissionPlmAddRuleItemService.saveBatch(addRuleItems);
        }
        if(CollectionUtils.isNotEmpty(listRuleItems)){
            permissionPlmListRuleItemService.saveBatch(listRuleItems);
        }
        if(CollectionUtils.isNotEmpty(operationRuleItems)){
            permissionPlmOperationRuleItemService.saveBatch(operationRuleItems);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delPermissionByPerBid(String permissionBid) {
        if (StringUtils.isEmpty(permissionBid)){
            return;
        }
        List<PermissionPlmAddRuleItem> oldAddRuleItems = permissionPlmAddRuleItemService.list(new LambdaQueryWrapper<PermissionPlmAddRuleItem>().eq(PermissionPlmAddRuleItem::getPermissionBid, permissionBid).eq(PermissionPlmAddRuleItem::getPath,"0"));
        if(CollectionUtils.isNotEmpty(oldAddRuleItems)){
            List<String> oldAddRuleItemBids = oldAddRuleItems.stream().map(PermissionPlmAddRuleItem::getBid).collect(Collectors.toList());
            permissionPlmAddRuleItemMapper.deleteByBids(oldAddRuleItemBids);
        }
        List<PermissionPlmListRuleItem> oldListRuleItems = permissionPlmListRuleItemService.list(new LambdaQueryWrapper<PermissionPlmListRuleItem>().eq(PermissionPlmListRuleItem::getPermissionBid, permissionBid).eq(PermissionPlmListRuleItem::getPath,"0"));
        if(CollectionUtils.isNotEmpty(oldListRuleItems)){
            List<String> oldListRuleItemBids = oldListRuleItems.stream().map(PermissionPlmListRuleItem::getBid).collect(Collectors.toList());
            permissionPlmListRuleItemMapper.deleteByBids(oldListRuleItemBids);
        }
        List<PermissionPlmOperationRuleItem> oldOperationRuleItems = permissionPlmOperationRuleItemService.list(new LambdaQueryWrapper<PermissionPlmOperationRuleItem>().eq(PermissionPlmOperationRuleItem::getPermissionBid, permissionBid).eq(PermissionPlmOperationRuleItem::getPath,"0"));
        if(CollectionUtils.isNotEmpty(oldOperationRuleItems)){
            List<String> oldOperationRuleItemBids = oldOperationRuleItems.stream().map(PermissionPlmOperationRuleItem::getBid).collect(Collectors.toList());
            permissionPlmOperationRuleItemMapper.deleteByBids(oldOperationRuleItemBids);
        }
    }

    private boolean addConfigRule(AppPermissionDto appPermissionDto){
        String permissionBid = "OBJ:"+ appPermissionDto.getModelCode();
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
        permissionPlmRuleNew.setPath("0");
        permissionPlmRuleNew.setBid(permissionBid);
        permissionPlmRuleNew.setDeleteFlag(false);
        permissionPlmRuleNew.setEnableFlag(true);
        permissionPlmRuleNew.setCreatedBy(jobNumber);
        permissionPlmRuleNew.setUpdatedBy(jobNumber);
        if(CollectionUtils.isNotEmpty(addRuleItems) || CollectionUtils.isNotEmpty(listRuleItems) || CollectionUtils.isNotEmpty(operationRuleItems)){
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
        }
        return true;
    }

    private void handelAppPermissionDto(AppPermissionDto appPermissionDto){
        if(appPermissionDto != null){
            if(CollectionUtils.isNotEmpty(appPermissionDto.getAppPermissionOperationList())){
                List<PermissionOperationItemDto> operationItems = new ArrayList<>();
                for(PermissionOperationItemVo operationItemVo : appPermissionDto.getAppPermissionOperationList()){
                    if(CollectionUtils.isNotEmpty(operationItemVo.getOperatorList())){
                        for(String operatorCode : operationItemVo.getOperatorList()){
                            PermissionOperationItemDto operationItem = new PermissionOperationItemDto();
                            operationItem.setRoleCode(operationItemVo.getRoleCode());
                            operationItem.setOperatorCode(operatorCode);
                            operationItems.add(operationItem);
                        }
                    }
                }
                appPermissionDto.setOperationItems(operationItems);
            }
        }
//        if(CollectionUtils.isEmpty(appPermissionDto.getOperationItems())){
//            throw new BusinessException("操作权限不能为空");
//        }
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deletePermission(AppPermissionDto appPermissionDto){
        //需要先删除原来的数据在更新新的数据
        String permissionBid = appPermissionDto.getPermissionBid();
        if(StringUtils.isEmpty(permissionBid)){
            throw new BusinessException("权限标识不能为空");
        }
        //判断是否是最后一个角色数据
        long count1 = permissionPlmAddRuleItemService.count(new LambdaQueryWrapper<PermissionPlmAddRuleItem>().eq(PermissionPlmAddRuleItem::getPermissionBid,permissionBid).eq(PermissionPlmAddRuleItem::getPath,"0").ne(PermissionPlmAddRuleItem::getRoleCode,appPermissionDto.getRoleCode()));
        long count2 = permissionPlmListRuleItemService.count(new LambdaQueryWrapper<PermissionPlmListRuleItem>().eq(PermissionPlmListRuleItem::getPermissionBid,permissionBid).eq(PermissionPlmListRuleItem::getPath,"0").ne(PermissionPlmListRuleItem::getRoleCode,appPermissionDto.getRoleCode()));
        long count3 = permissionPlmOperationRuleItemService.count(new LambdaQueryWrapper<PermissionPlmOperationRuleItem>().eq(PermissionPlmOperationRuleItem::getPermissionBid,permissionBid).eq(PermissionPlmOperationRuleItem::getPath,"0").ne(PermissionPlmOperationRuleItem::getRoleCode,appPermissionDto.getRoleCode()));
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
        if(count1 + count2 + count3 == 0){
            //需要删除权限表数据
            permissionPlmRuleMapper.deleteByPermissionBid(permissionBid);
        }
        return true;
    }


    public ObjPermissionVo listObjPermissions(String modelCode){
        String permissionBid = "OBJ:"+modelCode;
        PermissionPlmRule permissionPlmRule = permissionPlmRuleService.getOne(new LambdaQueryWrapper<PermissionPlmRule>().eq(PermissionPlmRule::getBid,permissionBid));
        if(permissionPlmRule == null){
            return null;
        }
        //查询ITEM表
        List<PermissionPlmAddRuleItem> addRuleItems = permissionPlmAddRuleItemService.list(new LambdaQueryWrapper<PermissionPlmAddRuleItem>().eq(PermissionPlmAddRuleItem::getPermissionBid, permissionPlmRule.getBid()));
        List<PermissionPlmListRuleItem> listRuleItems = permissionPlmListRuleItemService.list(new LambdaQueryWrapper<PermissionPlmListRuleItem>().eq(PermissionPlmListRuleItem::getPermissionBid, permissionPlmRule.getBid()));
        List<PermissionPlmOperationRuleItem> operationRuleItems = permissionPlmOperationRuleItemService.list(new LambdaQueryWrapper<PermissionPlmOperationRuleItem>().eq(PermissionPlmOperationRuleItem::getPermissionBid, permissionPlmRule.getBid()));
        //组装返回结果
        ObjPermissionVo appBasePermissionVo = new ObjPermissionVo();
        Map<String,List<String>> appRoleOperatorMap = new HashMap<>();
        Map<String,List<String>> objRoleOperatorMap = new HashMap<>();
        buildAddOperators(addRuleItems,appRoleOperatorMap,objRoleOperatorMap);
        buildListOperators(listRuleItems,appRoleOperatorMap,objRoleOperatorMap);
        buildOperators(operationRuleItems,appRoleOperatorMap,objRoleOperatorMap);
        appBasePermissionVo.setModelCode(modelCode);
        appBasePermissionVo.setPermissionBid(permissionPlmRule.getBid());
        appBasePermissionVo.setRuleName(permissionPlmRule.getRuleName());
        List<PermissionOperationItemVo> appPermissionOperationList = new ArrayList<>();
        List<PermissionOperationItemVo> objPermissionOperationList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(appRoleOperatorMap)){
            for(Map.Entry<String,List<String>> entry : appRoleOperatorMap.entrySet()){
                PermissionOperationItemVo permissionOperationItemVo = new PermissionOperationItemVo();
                permissionOperationItemVo.setRoleCode(entry.getKey());
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
        //拼接继承权限
        appBasePermissionVo.setObjPermissionOperationList(objPermissionOperationList);
        appBasePermissionVo.setPermissionOperationList(appPermissionOperationList);
        return appBasePermissionVo;
    }


    private Map<String,String> getBaseAttrCodeMap(){
        Map<String,String> baseAttrCodeMap = new HashMap<>();
        baseAttrCodeMap.put("operationDelete",OperatorEnum.DELETE.getCode());
        baseAttrCodeMap.put("operationEdit",OperatorEnum.EDIT.getCode());
        baseAttrCodeMap.put("operationDetail",OperatorEnum.DETAIL.getCode());
        baseAttrCodeMap.put("operationRevise",OperatorEnum.REVISE.getCode());
        baseAttrCodeMap.put("operationPromote",OperatorEnum.PROMOTE.getCode());
        return baseAttrCodeMap;
    }

    private Map<String,String> getBaseAttrCodeNameMap(){
        Map<String,String> baseAttrMap = new HashMap<>();
        baseAttrMap.put(OperatorEnum.DELETE.getCode(),"删除");
        baseAttrMap.put(OperatorEnum.EDIT.getCode(),"编辑");
        baseAttrMap.put(OperatorEnum.DETAIL.getCode(),"详情");
        baseAttrMap.put(OperatorEnum.REVISE.getCode(),"修订");
        baseAttrMap.put(OperatorEnum.PROMOTE.getCode(),"提升");
        return baseAttrMap;
    }

    private List<String> getOperatorVos(PermissionPlmOperationRuleItem operationRuleItem){
        List<String> operatorVos = new ArrayList<>();
        Map<String,String> getBaseAttrCodeMap = getBaseAttrCodeMap();
        Map<String,String> getBaseAttrCodeNameMap = getBaseAttrCodeNameMap();
        if(operationRuleItem != null){
            Map<String,Object> objectMap = BeanUtil.beanToMap(operationRuleItem);
            for(Map.Entry<String,Object> entry : objectMap.entrySet()){
                String key = entry.getKey();
                if(getBaseAttrCodeMap.containsKey(key)){
                    Object value = entry.getValue();
                    if(value != null && value instanceof Boolean && (Boolean)value){
                        //OperatorVo operatorVo = new OperatorVo();
                        //operatorVo.setOperatorCode(getBaseAttrCodeMap.get(key));
                        //operatorVo.setOperatorName(getBaseAttrCodeNameMap.get(operatorVo.getOperatorCode()));
                        operatorVos.add(getBaseAttrCodeMap.get(key));
                    }
                }
            }
        }
        return operatorVos;
    }
    private void buildOperators(List<PermissionPlmOperationRuleItem> operationRuleItems,Map<String,List<String>> appRoleOperatorMap,Map<String,List<String>> objRoleOperatorMap){
        if(CollectionUtils.isNotEmpty(operationRuleItems)){
            for(PermissionPlmOperationRuleItem operationRuleItem : operationRuleItems){
                List<String> operatorVos = getOperatorVos(operationRuleItem);
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
                /*OperatorVo operatorVo = new OperatorVo();
                operatorVo.setOperatorCode("ADD");
                operatorVo.setOperatorName("新增");*/
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
                /*OperatorVo operatorVo = new OperatorVo();
                operatorVo.setOperatorCode("LIST");
                operatorVo.setOperatorName("列表");*/
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
    public Boolean distributePermission(String modelCode) {
        //查询对象以及子对象的所有权限
        String permissionBid = "OBJ:" + modelCode;
        PermissionPlmRule permissionPlmRule = permissionPlmRuleService.getOne(new LambdaQueryWrapper<PermissionPlmRule>().eq(PermissionPlmRule::getBid, permissionBid));
        if (permissionPlmRule == null) {
            log.info("该对象没有权限规则，应用code：" + modelCode);
            return null;
        }
        List<PermissionPlmAddRuleItem> addRuleItems = permissionPlmAddRuleItemService.list(new LambdaQueryWrapper<PermissionPlmAddRuleItem>().eq(PermissionPlmAddRuleItem::getPermissionBid, permissionPlmRule.getBid()));
        List<PermissionPlmListRuleItem> listRuleItems = permissionPlmListRuleItemService.list(new LambdaQueryWrapper<PermissionPlmListRuleItem>().eq(PermissionPlmListRuleItem::getPermissionBid, permissionPlmRule.getBid()));
        List<PermissionPlmOperationRuleItem> operationRuleItems = permissionPlmOperationRuleItemService.list(new LambdaQueryWrapper<PermissionPlmOperationRuleItem>().eq(PermissionPlmOperationRuleItem::getPermissionBid, permissionPlmRule.getBid()));
        // 根据对象code删除权限
        PermissionResult result = delPerByCodeAndGetRes(modelCode);
        log.info(JSON.toJSONString(result.targetPlmRules), result.targetAddRuleItems, result.targetListRuleItems, result.targetOperationRuleItems);
        //查询使用该对象的所有空间应用获取appId
        List<ApmSpaceApp> apps = apmSpaceAppService.getByMc(modelCode);
        if (CollectionUtils.isEmpty(apps)){
            log.info("该对象没有关联空间应用，应用code：" + modelCode);
            return true;
        }
        // 组装所有权限
        List<PermissionPlmRule> newPlmRules = new ArrayList<>();
        List<PermissionPlmAddRuleItem> newPlmAddRuleItems = new ArrayList<>();
        List<PermissionPlmListRuleItem> newPlmListRuleItems = new ArrayList<>();
        List<PermissionPlmOperationRuleItem> newPlmOpRuleItems = new ArrayList<>();
        for (ApmSpaceApp app : apps) {
            //对象权限主表
            PermissionPlmRule permissionPlmRule1 = new PermissionPlmRule();
            BeanUtils.copyProperties(permissionPlmRule,permissionPlmRule1);
            permissionPlmRule1.setBid("APP:"+app.getBid());
            permissionPlmRule1.setPath(modelCode+":"+app.getBid());
            newPlmRules.add(permissionPlmRule1);
            //新增权限
            for (PermissionPlmAddRuleItem addRuleItem : addRuleItems) {
                PermissionPlmAddRuleItem addRuleItem1 = new PermissionPlmAddRuleItem();
                BeanUtils.copyProperties(addRuleItem,addRuleItem1);
                addRuleItem1.setBid(SnowflakeIdWorker.nextIdStr());
                addRuleItem1.setPermissionBid("APP:"+app.getBid());
                addRuleItem1.setPath(modelCode+":"+app.getBid());
                newPlmAddRuleItems.add(addRuleItem1);
            }
            //列表权限
            for (PermissionPlmListRuleItem listRuleItem : listRuleItems) {
                PermissionPlmListRuleItem listRuleItem1 = new PermissionPlmListRuleItem();
                BeanUtils.copyProperties(listRuleItem,listRuleItem1);
                listRuleItem1.setBid(SnowflakeIdWorker.nextIdStr());
                listRuleItem1.setPermissionBid("APP:"+app.getBid());
                listRuleItem1.setPath(modelCode+":"+app.getBid());
                newPlmListRuleItems.add(listRuleItem1);
            }
            //操作权限
            for (PermissionPlmOperationRuleItem operationRuleItem : operationRuleItems) {
                PermissionPlmOperationRuleItem operationRuleItem1 = new PermissionPlmOperationRuleItem();
                BeanUtils.copyProperties(operationRuleItem,operationRuleItem1);
                operationRuleItem1.setBid(SnowflakeIdWorker.nextIdStr());
                operationRuleItem1.setPermissionBid("APP:"+app.getBid());
                operationRuleItem1.setPath(modelCode+":"+app.getBid());
                newPlmOpRuleItems.add(operationRuleItem1);
            }
        }
        // 批量保存所有权限
        return saveBathPermissionData(newPlmRules,newPlmAddRuleItems,newPlmListRuleItems,newPlmOpRuleItems);
    }






    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean distributePermissions(List<String> modelCodes) {
        //同对象code查询删除
        CfgTableVo cfgTableVo = cfgTableDomainService.getTableAndAttributeByModelCode(modelCodes.get(0));


        List<String> permissionBids = modelCodes.stream().map(t -> "OBJ:" + t).collect(Collectors.toList());
        //查询对象以及子对象的所有权限‘
        List<PermissionPlmRule> permissionPlmRulesAll = permissionPlmRuleService.list(new LambdaQueryWrapper<PermissionPlmRule>().in(PermissionPlmRule::getBid, permissionBids));
        Map<String, PermissionPlmRule> permissionPlmRulesMap = permissionPlmRulesAll.stream().collect(Collectors.toMap(PermissionPlmRule::getBid, t -> t));
        List<PermissionPlmAddRuleItem> addRuleItemsAll = permissionPlmAddRuleItemService.list(new LambdaQueryWrapper<PermissionPlmAddRuleItem>().in(PermissionPlmAddRuleItem::getPermissionBid,permissionBids));
        Map<String, List<PermissionPlmAddRuleItem>> addRuleItemsMap = addRuleItemsAll.stream().collect(Collectors.groupingBy(PermissionPlmAddRuleItem::getPermissionBid));
        List<PermissionPlmListRuleItem> listRuleItemsAll = permissionPlmListRuleItemService.list(new LambdaQueryWrapper<PermissionPlmListRuleItem>().in(PermissionPlmListRuleItem::getPermissionBid, permissionBids));
        Map<String, List<PermissionPlmListRuleItem>> listRuleItemsMap = listRuleItemsAll.stream().collect(Collectors.groupingBy(PermissionPlmListRuleItem::getPermissionBid));
        List<PermissionPlmOperationRuleItem> operationRuleItemsAll = permissionPlmOperationRuleItemService.list(new LambdaQueryWrapper<PermissionPlmOperationRuleItem>().in(PermissionPlmOperationRuleItem::getPermissionBid,permissionBids));
        Map<String, List<PermissionPlmOperationRuleItem>> operationRuleItemsMap = operationRuleItemsAll.stream().collect(Collectors.groupingBy(PermissionPlmOperationRuleItem::getPermissionBid));

        // 根据对象code删除权限
        PermissionResult result = delPerByCodeAndGetRes(modelCodes);
        log.info(JSON.toJSONString(result.targetPlmRules), result.targetAddRuleItems, result.targetListRuleItems, result.targetOperationRuleItems);
        //查询使用该对象的所有空间应用获取appId
        List<ApmSpaceApp> allApp = apmSpaceAppService.getByMcs(modelCodes);
        if (CollectionUtils.isEmpty(allApp)){
            log.info("该对象没有关联空间应用，应用code：" + allApp);
            return true;
        }
        //根据modelCode分组
        Map<String, List<ApmSpaceApp>> modelCodeListMap = allApp.stream().collect(Collectors.groupingBy(ApmSpaceApp::getModelCode));
        // 组装所有权限
        List<PermissionPlmRule> newPlmRules = new ArrayList<>();
        List<PermissionPlmAddRuleItem> newPlmAddRuleItems = new ArrayList<>();
        List<PermissionPlmListRuleItem> newPlmListRuleItems = new ArrayList<>();
        List<PermissionPlmOperationRuleItem> newPlmOpRuleItems = new ArrayList<>();
        for (String modelCode : modelCodes) {
            // 获取应用该对象的所有空间应用
            List<ApmSpaceApp> apps = modelCodeListMap.get(modelCode);
            if (CollectionUtils.isEmpty(apps)) {
                //该应用为关联
                continue;
            }
            PermissionPlmRule permissionPlmRule = permissionPlmRulesMap.get("OBJ:" + modelCode);
            if (permissionPlmRule == null) {
                continue;
            }
            List<PermissionPlmAddRuleItem> addRuleItems = addRuleItemsMap.get("OBJ:" + modelCode);
            List<PermissionPlmListRuleItem> listRuleItems = listRuleItemsMap.get("OBJ:" + modelCode);
            List<PermissionPlmOperationRuleItem> operationRuleItems = operationRuleItemsMap.get("OBJ:" + modelCode);
            for (ApmSpaceApp app : apps) {
                //对象权限主表
                PermissionPlmRule permissionPlmRule1 = new PermissionPlmRule();
                BeanUtils.copyProperties(permissionPlmRule, permissionPlmRule1);
                permissionPlmRule1.setBid("APP:" + app.getBid());
                permissionPlmRule1.setPath(modelCode + ":" + app.getBid());
                newPlmRules.add(permissionPlmRule1);
                //新增权限
                if (CollectionUtils.isNotEmpty(addRuleItems)) {
                    for (PermissionPlmAddRuleItem addRuleItem : addRuleItems) {
                        PermissionPlmAddRuleItem addRuleItem1 = new PermissionPlmAddRuleItem();
                        BeanUtils.copyProperties(addRuleItem, addRuleItem1);
                        addRuleItem1.setBid(SnowflakeIdWorker.nextIdStr());
                        addRuleItem1.setPermissionBid("APP:" + app.getBid());
                        addRuleItem1.setPath(modelCode + ":" + app.getBid());
                        newPlmAddRuleItems.add(addRuleItem1);
                    }
                }
                //列表权限
                if (CollectionUtils.isNotEmpty(listRuleItems)) {
                    for (PermissionPlmListRuleItem listRuleItem : listRuleItems) {
                        PermissionPlmListRuleItem listRuleItem1 = new PermissionPlmListRuleItem();
                        BeanUtils.copyProperties(listRuleItem, listRuleItem1);
                        listRuleItem1.setBid(SnowflakeIdWorker.nextIdStr());
                        listRuleItem1.setPermissionBid("APP:" + app.getBid());
                        listRuleItem1.setPath(modelCode + ":" + app.getBid());
                        newPlmListRuleItems.add(listRuleItem1);
                    }
                }
                //操作权限
                if (CollectionUtils.isNotEmpty(operationRuleItems)) {
                    for (PermissionPlmOperationRuleItem operationRuleItem : operationRuleItems) {
                        PermissionPlmOperationRuleItem operationRuleItem1 = new PermissionPlmOperationRuleItem();
                        BeanUtils.copyProperties(operationRuleItem, operationRuleItem1);
                        operationRuleItem1.setBid(SnowflakeIdWorker.nextIdStr());
                        operationRuleItem1.setPermissionBid("APP:" + app.getBid());
                        operationRuleItem1.setPath(modelCode + ":" + app.getBid());
                        newPlmOpRuleItems.add(operationRuleItem1);
                    }
                }
            }
        }


        List<String> appBids = allApp.stream().map(ApmSpaceApp::getBid).collect(Collectors.toList());
        permissionInsanceMapper.updateDefaultPermissionBid(cfgTableVo,appBids);
        List<String> permissionBids1 = allApp.stream().map(t -> "APP:" + t.getBid()).collect(Collectors.toList());
        // 查询出所有对象的应用的权限
        //        List<PermissionPlmRule> permissionPlmRulesAll1 = permissionPlmRuleService.list(new LambdaQueryWrapper<PermissionPlmRule>().in(PermissionPlmRule::getBid, permissionBids1));
        //        Map<String, PermissionPlmRule> permissionPlmRulesMap1 = permissionPlmRulesAll1.stream().collect(Collectors.toMap(PermissionPlmRule::getBid, t -> t));

        List<PermissionPlmAddRuleItem> addRuleItemsAll1 = permissionPlmAddRuleItemService.list(new LambdaQueryWrapper<PermissionPlmAddRuleItem>().in(PermissionPlmAddRuleItem::getPermissionBid,permissionBids1));
        addRuleItemsAll1.addAll(newPlmAddRuleItems);
        Map<String, List<PermissionPlmAddRuleItem>> addRuleItemsMap1 = addRuleItemsAll1.stream().collect(Collectors.groupingBy(PermissionPlmAddRuleItem::getPermissionBid));

        List<PermissionPlmListRuleItem> listRuleItemsAll1 = permissionPlmListRuleItemService.list(new LambdaQueryWrapper<PermissionPlmListRuleItem>().in(PermissionPlmListRuleItem::getPermissionBid, permissionBids1));
        listRuleItemsAll1.addAll(newPlmListRuleItems);
        Map<String, List<PermissionPlmListRuleItem>> listRuleItemsMap1 = listRuleItemsAll1.stream().collect(Collectors.groupingBy(PermissionPlmListRuleItem::getPermissionBid));

        List<PermissionPlmOperationRuleItem> operationRuleItemsAll1 = permissionPlmOperationRuleItemService.list(new LambdaQueryWrapper<PermissionPlmOperationRuleItem>().in(PermissionPlmOperationRuleItem::getPermissionBid,permissionBids1));
        operationRuleItemsAll1.addAll(newPlmOpRuleItems);
        Map<String, List<PermissionPlmOperationRuleItem>> operationRuleItemsMa1p = operationRuleItemsAll1.stream().collect(Collectors.groupingBy(PermissionPlmOperationRuleItem::getPermissionBid));

        List<String> newPermissionBids = new ArrayList<>();
        //循环判断数据是否存在
        for (ApmSpaceApp apmSpaceApp : allApp) {
            String permissionBid = "APP:" + apmSpaceApp.getBid();
            List<PermissionPlmAddRuleItem> permissionPlmAddRuleItems = addRuleItemsMap1.get(permissionBid);
            List<PermissionPlmListRuleItem> permissionPlmListRuleItems = listRuleItemsMap1.get(permissionBid);
            List<PermissionPlmOperationRuleItem> permissionPlmOperationRuleItems = operationRuleItemsMa1p.get(permissionBid);
            // 如果三个都为空 则更新实例数据和删除实例主表
            if (CollectionUtils.isEmpty(permissionPlmAddRuleItems) &&
                    CollectionUtils.isEmpty(permissionPlmListRuleItems) && CollectionUtils.isEmpty(permissionPlmOperationRuleItems)) {
                newPermissionBids.add(permissionBid);
            }
        }
        if (CollectionUtils.isNotEmpty(newPermissionBids)){
            //删除权限主表数据
            permissionPlmRuleMapper.deleteByPermissionBids(newPermissionBids);
            permissionInsanceMapper.updatePermissionBid(cfgTableVo,newPermissionBids,"DEFAULT:6666666666666");
        }



        // 批量保存所有权限
        return saveBathPermissionData(newPlmRules,newPlmAddRuleItems,newPlmListRuleItems,newPlmOpRuleItems);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean distributeObjectPermission(String modelCode) {
        // 根据modelCode查询出所有子类
        List<CfgObjectVo> cfgObjectVos = objectModelAppService.listAllByModelCode(modelCode);
        if (CollectionUtils.isEmpty(cfgObjectVos)) {
            // 不存在该对象
            log.error("不存在该对象 对象CODE:{}", modelCode);
            return true;
        }
        // 根据modelCode查询出所有子类、对象的权限
        List<String> modelCodes = cfgObjectVos.stream().map(CfgObjectVo::getModelCode).collect(Collectors.toList());
        // 删除子类从父类继承过来的属性（子类可能有自定义的属性 也需要穿透到下级去）
        PermissionResult permissionResult = getObjPerByMc(modelCodes, modelCode);
        if (permissionResult == null){
            log.error("当前对象极其子对象都没有权限 对象code:{}",modelCode);
            return true;
        }
        // 组装权限
        //根据modelCode查询权限
        setChildrenPermission(modelCode, permissionResult, cfgObjectVos);
        // 保存权限
        List<PermissionPlmRule> newPlmRules = permissionResult.getTargetPlmRules().stream().filter(t -> t.getId() == null).collect(Collectors.toList());
        List<PermissionPlmAddRuleItem> newPlmAddRuleItems = permissionResult.getTargetAddRuleItems().stream().filter(t -> t.getId() == null).collect(Collectors.toList());
        List<PermissionPlmListRuleItem> newPlmListRuleItems = permissionResult.getTargetListRuleItems().stream().filter(t -> t.getId() == null).collect(Collectors.toList());
        List<PermissionPlmOperationRuleItem> newPlmOpRuleItems = permissionResult.getTargetOperationRuleItems().stream().filter(t -> t.getId() == null).collect(Collectors.toList());
        saveBathPermissionData(newPlmRules, newPlmAddRuleItems, newPlmListRuleItems, newPlmOpRuleItems);
        distributePermissions(modelCodes);
        return true;
    }

    /**
     * 递归设置所有子类权限
     *
     * @param parentModelCode  父code
     * @param permissionResult 所有权限集合
     * @param cfgObjectVos     所有对象
     */
    private void setChildrenPermission(String parentModelCode, PermissionResult permissionResult, List<CfgObjectVo> cfgObjectVos) {
        // 获取当前对象所有权限
        PermissionResult parentPermissions = getPermissionRuleItemsByCode(parentModelCode, permissionResult);
        // 根据父类权限生成子类的权限
        generateChildrenPermissions(parentModelCode, parentPermissions, permissionResult, cfgObjectVos);
    }

    private void generateChildrenPermissions(String parentModelCode, PermissionResult parentPermissions,
                                             PermissionResult permissionResult, List<CfgObjectVo> cfgObjectVos) {
        //获取当前父类所有子类
        List<CfgObjectVo> childrenObj = cfgObjectVos.stream().
                filter(t -> t.getParentModelVersionCode()!= null
                        && parentModelCode.equals(t.getParentModelVersionCode().split(":")[0]) ).
                collect(Collectors.toList());
        if (CollectionUtils.isEmpty(childrenObj)) {
            return;
        }
        for (CfgObjectVo cfgObjectVo : childrenObj) {
            //组装子类数据
            List<PermissionPlmRule> targetPlmRules = parentPermissions.targetPlmRules;
            if (CollectionUtils.isNotEmpty(targetPlmRules)) {
                //父类权限不为空 则复制父类权限到子类权限中
                PermissionPlmRule parentPermissionPlmRule = targetPlmRules.get(0);
                //过滤出子类对象的权限规则主表
                List<PermissionPlmRule> childrenPermissionPlmRule = permissionResult.targetPlmRules.stream()
                        .filter(t -> t.getBid().equals("OBJ:" + cfgObjectVo.getModelCode())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(childrenPermissionPlmRule)) {
                    //父类权限主表存在 子了不存在直接添加
                    PermissionPlmRule childrenPermissionPlmRuleNew = new PermissionPlmRule();
                    BeanUtils.copyProperties(parentPermissionPlmRule, childrenPermissionPlmRuleNew);
                    childrenPermissionPlmRuleNew.setBid("OBJ:" + cfgObjectVo.getModelCode());
                    childrenPermissionPlmRuleNew.setPath("0");
                    childrenPermissionPlmRuleNew.setId(null);
                    //设置结果集合
                    List<PermissionPlmRule> targetPlmRules1 = permissionResult.getTargetPlmRules();
                    targetPlmRules1.add(childrenPermissionPlmRuleNew);
                }
                List<PermissionPlmAddRuleItem> parentAddRuleItems = parentPermissions.targetAddRuleItems;
                List<PermissionPlmAddRuleItem> childrenAddRuleItems = new ArrayList<>();
                for (PermissionPlmAddRuleItem parentAddRuleItem : parentAddRuleItems) {
                    //父类权限主表存在 子了不存在直接添加
                    PermissionPlmAddRuleItem plmAddRuleItem = new PermissionPlmAddRuleItem();
                    BeanUtils.copyProperties(parentAddRuleItem, plmAddRuleItem);
                    plmAddRuleItem.setBid(SnowflakeIdWorker.nextIdStr());
                    plmAddRuleItem.setPermissionBid("OBJ:" + cfgObjectVo.getModelCode());
                    plmAddRuleItem.setPath(parentModelCode + ":" + cfgObjectVo.getModelCode());
                    plmAddRuleItem.setId(null);
                    childrenAddRuleItems.add(plmAddRuleItem);

                }
                List<PermissionPlmListRuleItem> parentListRuleItems = parentPermissions.targetListRuleItems;
                List<PermissionPlmListRuleItem> childrenPlmListRuleItems = new ArrayList<>();
                for (PermissionPlmListRuleItem parentAddRuleItem : parentListRuleItems) {
                    //父类权限主表存在 子了不存在直接添加
                    PermissionPlmListRuleItem plmListRuleItem = new PermissionPlmListRuleItem();
                    BeanUtils.copyProperties(parentAddRuleItem, plmListRuleItem);
                    plmListRuleItem.setBid(SnowflakeIdWorker.nextIdStr());
                    plmListRuleItem.setPermissionBid("OBJ:" + cfgObjectVo.getModelCode());
                    plmListRuleItem.setPath(parentModelCode + ":" + cfgObjectVo.getModelCode());
                    plmListRuleItem.setId(null);
                    childrenPlmListRuleItems.add(plmListRuleItem);

                }

                List<PermissionPlmOperationRuleItem> parentOperationRuleItems = parentPermissions.targetOperationRuleItems;
                List<PermissionPlmOperationRuleItem> childrenOperationRuleItems = new ArrayList<>();
                for (PermissionPlmOperationRuleItem parentAddRuleItem : parentOperationRuleItems) {
                    //父类权限主表存在 子了不存在直接添加
                    PermissionPlmOperationRuleItem operationRuleItem = new PermissionPlmOperationRuleItem();
                    BeanUtils.copyProperties(parentAddRuleItem, operationRuleItem);
                    operationRuleItem.setBid(SnowflakeIdWorker.nextIdStr());
                    operationRuleItem.setPermissionBid("OBJ:" + cfgObjectVo.getModelCode());
                    operationRuleItem.setPath(parentModelCode + ":" + cfgObjectVo.getModelCode());
                    operationRuleItem.setId(null);
                    childrenOperationRuleItems.add(operationRuleItem);

                }
                //设置结果集
                if (CollectionUtils.isNotEmpty(childrenAddRuleItems)) {
                    List<PermissionPlmAddRuleItem> targetAddRuleItems = permissionResult.getTargetAddRuleItems();
                    targetAddRuleItems.addAll(childrenAddRuleItems);
                } //设置结果集
                if (CollectionUtils.isNotEmpty(childrenPlmListRuleItems)) {
                    List<PermissionPlmListRuleItem> targetAddRuleItems = permissionResult.getTargetListRuleItems();
                    targetAddRuleItems.addAll(childrenPlmListRuleItems);
                } //设置结果集
                if (CollectionUtils.isNotEmpty(childrenOperationRuleItems)) {
                    List<PermissionPlmOperationRuleItem> targetOperationRuleItems = permissionResult.getTargetOperationRuleItems();
                    targetOperationRuleItems.addAll(childrenOperationRuleItems);
                }

            }
            //不管父类没有权限继续走子类权限（子类权限可能独立存在）
            setChildrenPermission(cfgObjectVo.getModelCode(), permissionResult, cfgObjectVos);
        }
    }


    /**
     * 根据code获取对象所有权限
     *
     * @param modelCode        对象code
     * @param permissionResult 所有权限
     * @return PermissionResult PermissionResult
     */
    private PermissionResult getPermissionRuleItemsByCode(String modelCode, PermissionResult permissionResult) {
        String permissionBid = "OBJ:" + modelCode;
        List<PermissionPlmRule> targetPlmRules = permissionResult.targetPlmRules.stream().filter(t -> t.getBid().equals(permissionBid)).collect(Collectors.toList());
        List<PermissionPlmAddRuleItem> targetAddRuleItems = permissionResult.targetAddRuleItems.stream().filter(t -> t.getPermissionBid().equals(permissionBid)).collect(Collectors.toList());
        List<PermissionPlmListRuleItem> targetListRuleItems = permissionResult.targetListRuleItems.stream().filter(t -> t.getPermissionBid().equals(permissionBid)).collect(Collectors.toList());
        List<PermissionPlmOperationRuleItem> targetOperationRuleItems = permissionResult.targetOperationRuleItems.stream().filter(t -> t.getPermissionBid().equals(permissionBid)).collect(Collectors.toList());
        return new PermissionResult(targetPlmRules, targetAddRuleItems, targetListRuleItems, targetOperationRuleItems);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBathPermissionData(List<PermissionPlmRule> permissionPlmRuleNews,
                                          List<PermissionPlmAddRuleItem> permissionPlmAddRuleItemNews,
                                          List<PermissionPlmListRuleItem> permissionPlmListRuleItemNews,
                                          List<PermissionPlmOperationRuleItem> permissionPlmOperationRuleItemNews) {
        try {
            if (CollectionUtils.isNotEmpty(permissionPlmRuleNews)) {
                permissionPlmRuleMapper.saveList(permissionPlmRuleNews);
            }
            if (CollectionUtils.isNotEmpty(permissionPlmAddRuleItemNews)) {
                permissionPlmAddRuleItemMapper.saveList(permissionPlmAddRuleItemNews);
            }
            if (CollectionUtils.isNotEmpty(permissionPlmListRuleItemNews)) {
                permissionPlmListRuleItemMapper.saveList(permissionPlmListRuleItemNews);
            }
            if (CollectionUtils.isNotEmpty(permissionPlmOperationRuleItemNews)) {
                permissionPlmOperationRuleItemMapper.saveList(permissionPlmOperationRuleItemNews);
            }
        } catch (Exception e) {
            Log.error("保存数据出错====:{}", e);
            throw new RuntimeException();
        }



        return true;
    }
    @NotNull
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PermissionResult delPerByCodeAndGetRes(String modelCode) {
        //根据modelCode查询对象以及子对象所有权限（包括子对象以及对象所在应用的所有权限）
        List<PermissionPlmRule> targetPlmRules = permissionPlmRuleService.list(new LambdaQueryWrapper<PermissionPlmRule>()
                .likeRight(PermissionPlmRule::getPath, modelCode +":")
                        .and( (warpper )-> {
                            warpper.likeRight(PermissionPlmRule::getBid,  "APP:");
                        })
        );
        List<PermissionPlmAddRuleItem> targetAddRuleItems = permissionPlmAddRuleItemService.list(new LambdaQueryWrapper<PermissionPlmAddRuleItem>()
                .likeRight(PermissionPlmAddRuleItem::getPath, modelCode +":")
                .and( (warpper )-> {
                    warpper.likeRight(PermissionPlmAddRuleItem::getPermissionBid,  "APP:");
                })
        );
        List<PermissionPlmListRuleItem> targetListRuleItems = permissionPlmListRuleItemService.list(new LambdaQueryWrapper<PermissionPlmListRuleItem>()
                .likeRight(PermissionPlmListRuleItem::getPath, modelCode +":")
                .and( (warpper )-> {
                    warpper.likeRight(PermissionPlmListRuleItem::getPermissionBid,  "APP:");
                })
        );

        List<PermissionPlmOperationRuleItem> targetOperationRuleItems = permissionPlmOperationRuleItemService.list(new LambdaQueryWrapper<PermissionPlmOperationRuleItem>()
                .likeRight(PermissionPlmOperationRuleItem::getPath, modelCode +":")
                .and( (warpper) -> {
                    warpper.likeRight(PermissionPlmOperationRuleItem::getPermissionBid,  "APP:");
                })
        );
        //删除原有权限
        permissionPlmRuleService.deleteByRules(targetPlmRules);
        permissionPlmAddRuleItemService.deleteByRules(targetAddRuleItems);
        permissionPlmListRuleItemService.deleteByRules(targetListRuleItems);
        permissionPlmOperationRuleItemService.deleteByRules(targetOperationRuleItems);
        return new PermissionResult(targetPlmRules, targetAddRuleItems, targetListRuleItems, targetOperationRuleItems);
    }

    @Override
    public PermissionResult delPerByCodeAndGetRes(List<String> modelCodes) {
        //根据modelCode查询对象以及子对象所有权限（包括子对象以及对象所在应用的所有权限）
        LambdaQueryWrapper<PermissionPlmRule> plmRuleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        plmRuleLambdaQueryWrapper.likeRight(PermissionPlmRule::getBid,  "APP:");

        LambdaQueryWrapper<PermissionPlmAddRuleItem> plmAddRuleItemLambdaQueryWrapper = new LambdaQueryWrapper<>();
        plmAddRuleItemLambdaQueryWrapper.likeRight(PermissionPlmAddRuleItem::getPermissionBid,  "APP:");

        LambdaQueryWrapper<PermissionPlmListRuleItem> plmListRuleItemLambdaQueryWrapper = new LambdaQueryWrapper<>();
        plmListRuleItemLambdaQueryWrapper.likeRight(PermissionPlmListRuleItem::getPermissionBid,  "APP:");

        LambdaQueryWrapper<PermissionPlmOperationRuleItem> plmOperationRuleItemLambdaQueryWrapper = new LambdaQueryWrapper<>();
        plmOperationRuleItemLambdaQueryWrapper.likeRight(PermissionPlmOperationRuleItem::getPermissionBid,  "APP:");
        plmRuleLambdaQueryWrapper.and((wrapper) ->{
            for (String modelCode : modelCodes) {
                wrapper.or().likeRight(PermissionPlmRule::getPath, modelCode +":");
            }
        });
        plmAddRuleItemLambdaQueryWrapper.and((wrapper) -> {
            for (String modelCode : modelCodes) {
                wrapper.or().likeRight(PermissionPlmAddRuleItem::getPath, modelCode +":");
            }
        });

        plmListRuleItemLambdaQueryWrapper.and((wrapper) -> {
            for (String modelCode : modelCodes) {
                wrapper.or().likeRight(PermissionPlmListRuleItem::getPath, modelCode +":");
            }
        });

        plmOperationRuleItemLambdaQueryWrapper.and((wrapper) -> {
            for (String modelCode : modelCodes) {
                wrapper.or().likeRight(PermissionPlmOperationRuleItem::getPath, modelCode +":");
            }
        });


//        for (String modelCode : modelCodes) {
//            plmRuleLambdaQueryWrapper.or().likeRight(PermissionPlmRule::getPath, modelCode +":");
//            plmAddRuleItemLambdaQueryWrapper.or().likeRight(PermissionPlmAddRuleItem::getPath, modelCode +":");
//            plmListRuleItemLambdaQueryWrapper.or().likeRight(PermissionPlmListRuleItem::getPath, modelCode +":");
//            plmOperationRuleItemLambdaQueryWrapper.or().likeRight(PermissionPlmOperationRuleItem::getPath, modelCode +":");
//        }
        List<PermissionPlmRule> targetPlmRules = permissionPlmRuleService.list(plmRuleLambdaQueryWrapper);
        List<PermissionPlmAddRuleItem> targetAddRuleItems = permissionPlmAddRuleItemService.list(plmAddRuleItemLambdaQueryWrapper);
        List<PermissionPlmListRuleItem> targetListRuleItems = permissionPlmListRuleItemService.list(plmListRuleItemLambdaQueryWrapper);
        List<PermissionPlmOperationRuleItem> targetOperationRuleItems = permissionPlmOperationRuleItemService.list(plmOperationRuleItemLambdaQueryWrapper);
        //删除原有权限
        permissionPlmRuleService.deleteByRules(targetPlmRules);
        permissionPlmAddRuleItemService.deleteByRules(targetAddRuleItems);
        permissionPlmListRuleItemService.deleteByRules(targetListRuleItems);
        permissionPlmOperationRuleItemService.deleteByRules(targetOperationRuleItems);
        return new PermissionResult(targetPlmRules, targetAddRuleItems, targetListRuleItems, targetOperationRuleItems);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PermissionResult getObjPerByMc(List<String> modelCodes, String parentModelCode) {
        //根据modelCode查询对象以及子对象所有权限（包括子对象以及对象所在应用的所有权限）
        LambdaQueryWrapper<PermissionPlmRule> plmRuleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        for (String modelCode : modelCodes) {
            plmRuleLambdaQueryWrapper.or().likeRight(PermissionPlmRule::getBid, "OBJ:" + modelCode);
        }
        List<PermissionPlmRule> targetPlmRules = permissionPlmRuleService.list(plmRuleLambdaQueryWrapper);
        if (CollectionUtils.isEmpty(targetPlmRules)){
            log.error("当前对象极其子对象都没有权限 对象code:{}",parentModelCode);
            //需要删除空间应用继承过来的所有权限
            distributePermissions(modelCodes);
            return null;
        }
        // 过滤出所有permissionBid
        List<String> permissionBids = targetPlmRules.stream().map(PermissionPlmRule::getBid).collect(Collectors.toList());

        LambdaQueryWrapper<PermissionPlmAddRuleItem> plmAddRuleItemLambdaQueryWrapper = new LambdaQueryWrapper<>();
        plmAddRuleItemLambdaQueryWrapper.in(PermissionPlmAddRuleItem::getPermissionBid, permissionBids);
        List<PermissionPlmAddRuleItem> targetAddRuleItems = permissionPlmAddRuleItemService.list(plmAddRuleItemLambdaQueryWrapper);


        LambdaQueryWrapper<PermissionPlmListRuleItem> plmListRuleItemLambdaQueryWrapper = new LambdaQueryWrapper<>();
        plmListRuleItemLambdaQueryWrapper.in(PermissionPlmListRuleItem::getPermissionBid, permissionBids);
        List<PermissionPlmListRuleItem> targetListRuleItems = permissionPlmListRuleItemService.list(plmListRuleItemLambdaQueryWrapper);

        LambdaQueryWrapper<PermissionPlmOperationRuleItem> plmOperationRuleItemLambdaQueryWrapper = new LambdaQueryWrapper<>();
        plmOperationRuleItemLambdaQueryWrapper.in(PermissionPlmOperationRuleItem::getPermissionBid, permissionBids);
        List<PermissionPlmOperationRuleItem> targetOperationRuleItems = permissionPlmOperationRuleItemService.list(plmOperationRuleItemLambdaQueryWrapper);

        String parentPermissionBid = "OBJ:" + parentModelCode;
        //permissionPlmRuleService.deleteByRules(targetPlmRules.stream().filter(t-> !"0".equals(t.getPath())).collect(Collectors.toList()));
        List<PermissionPlmAddRuleItem>  targetAddRuleItems1 = targetAddRuleItems.stream().filter(t -> parentPermissionBid.equals(t.getPermissionBid()) ||  "0".equals(t.getPath())).collect(Collectors.toList());
        List<PermissionPlmListRuleItem> targetListRuleItems1  = targetListRuleItems.stream().filter(t -> parentPermissionBid.equals(t.getPermissionBid()) ||  "0".equals(t.getPath())).collect(Collectors.toList());
        List<PermissionPlmOperationRuleItem>  targetOperationRuleItems1 = targetOperationRuleItems.stream().filter(t -> parentPermissionBid.equals(t.getPermissionBid()) ||  "0".equals(t.getPath())).collect(Collectors.toList());
        //删除所有从父类parentModelCode继承过来的权限不包括主表数据 不包括父类权限
        permissionPlmAddRuleItemService.deleteByRules(targetAddRuleItems.stream().filter(t -> !parentPermissionBid.equals(t.getPermissionBid()) &&  !"0".equals(t.getPath())).collect(Collectors.toList()));
        permissionPlmListRuleItemService.deleteByRules(targetListRuleItems.stream().filter(t ->!parentPermissionBid.equals(t.getPermissionBid()) &&   !"0".equals(t.getPath())).collect(Collectors.toList()));
        permissionPlmOperationRuleItemService.deleteByRules(targetOperationRuleItems.stream().filter(t -> !parentPermissionBid.equals(t.getPermissionBid()) &&   !"0".equals(t.getPath())).collect(Collectors.toList()));
        //只返回本身数据
        return new PermissionResult(targetPlmRules, targetAddRuleItems1, targetListRuleItems1, targetOperationRuleItems1);
    }

    @Override
    public List<Map<String, String>> queryAllOperationByModeCode(String modelCode) {
        OperatorEnum[] values = OperatorEnum.values();
        List<Map<String, String>> maps = new ArrayList<>();
        for (OperatorEnum value : values) {
            Map<String, String> operatorMap = new HashMap<>();
            operatorMap.put("key", value.getCode());
            operatorMap.put("value", value.getDesc());
            maps.add(operatorMap);
        }
        // 查询扩展按钮
        return maps;
    }
}
