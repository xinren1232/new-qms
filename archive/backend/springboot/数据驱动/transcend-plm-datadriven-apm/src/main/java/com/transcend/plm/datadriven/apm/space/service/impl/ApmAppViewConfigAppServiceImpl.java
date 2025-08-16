package com.transcend.plm.datadriven.apm.space.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.apm.constants.RoleConstant;
import com.transcend.plm.datadriven.apm.constants.SpaceConstant;
import com.transcend.plm.datadriven.apm.enums.TypeEnum;
import com.transcend.plm.datadriven.apm.permission.pojo.bo.ApmUser;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmRole;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmSphere;
import com.transcend.plm.datadriven.apm.permission.repository.mapper.ApmRoleMapper;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmSphereService;
import com.transcend.plm.datadriven.apm.permission.service.impl.PlatformUserWrapper;
import com.transcend.plm.datadriven.apm.space.converter.ApmAppViewConfigConverter;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmAppViewConfigDto;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmAppViewConfigVo;
import com.transcend.plm.datadriven.apm.space.repository.mapper.ApmAppViewConfigMapper;
import com.transcend.plm.datadriven.apm.space.repository.mapper.ApmAppViewUserRecordMapper;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmAppViewConfig;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmAppViewUserRecord;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceAppViewModelPo;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmAppViewConfigService;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmAppViewUserRecordService;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppViewModelService;
import com.transcend.plm.datadriven.apm.space.service.IApmAppViewConfigAppService;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.common.tool.CommonConstant;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.common.util.SnowflakeIdWorker;
import com.transsion.framework.exception.BusinessException;
import com.transsion.framework.uac.model.dto.DepartmentDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Service
public class ApmAppViewConfigAppServiceImpl implements IApmAppViewConfigAppService{
    @Resource
    private ApmAppViewConfigService apmAppViewConfigService;

    @Resource
    private PlatformUserWrapper platformUserWrapper;

    @Resource
    private ApmRoleMapper apmRoleMapper;
    @Resource
    private ApmAppViewConfigMapper apmAppViewConfigMapper;
    @Resource
    private ApmAppViewUserRecordService apmAppViewUserRecordService;

    @Resource
    private ApmSpaceAppViewModelService apmSpaceAppViewModelService;

    @Resource
    private ApmSpaceAppService apmSpaceAppService;

    @Resource
    private ApmRoleService apmRoleService;

    @Resource
    private ApmSphereService apmSphereService;
    @Resource
    private ApmAppViewUserRecordMapper apmAppViewUserRecordMapper;

    /**
     * @param apmAppViewUserRecord
     * @return boolean
     */
    @Override
    public boolean saveApmAppViewUserRecord(ApmAppViewUserRecord apmAppViewUserRecord){
        if(StringUtils.isEmpty(apmAppViewUserRecord.getCreatedBy())){
            apmAppViewUserRecord.setCreatedBy(SsoHelper.getJobNumber());
        }
        ApmAppViewUserRecord apmAppViewUserRecord1 = apmAppViewUserRecordService.getOne(new LambdaQueryWrapper<ApmAppViewUserRecord>().eq(ApmAppViewUserRecord::getCreatedBy,apmAppViewUserRecord.getCreatedBy()).eq(ApmAppViewUserRecord::getSpaceAppBid,apmAppViewUserRecord.getSpaceAppBid()).eq(ApmAppViewUserRecord::getShowType,apmAppViewUserRecord.getShowType()));
        if(apmAppViewUserRecord1 != null){
            apmAppViewUserRecord1.setAppViewConfigBid(apmAppViewUserRecord.getAppViewConfigBid());
            return apmAppViewUserRecordService.updateById(apmAppViewUserRecord1);
        }else{
            apmAppViewUserRecord.setBid(SnowflakeIdWorker.nextIdStr());
            return apmAppViewUserRecordService.save(apmAppViewUserRecord);
        }
    }

    /**
     * @param spaceAppBid
     * @return boolean
     */
    @Override
    public boolean deleteApmAppViewUserRecord(String spaceAppBid){
        return apmAppViewUserRecordMapper.delete(spaceAppBid,SsoHelper.getJobNumber()) > 0;
    }

    /**
     * @param bid
     * @return boolean
     */
    @Override
    public boolean deleteByBid(String bid) {
        int res = apmAppViewConfigMapper.deleteByBid(bid);
        return res > 0;
    }

    /**
     * 分享视图到其他空间
     *
     * @param appViewConfigBid
     * @param spaceBidList
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean shareAppView(String appViewConfigBid,List<String> spaceBidList){
        //获取视图配置
        ApmAppViewConfig apmAppViewConfig = apmAppViewConfigService.getOne(new LambdaQueryWrapper<ApmAppViewConfig>().eq(ApmAppViewConfig::getBid,appViewConfigBid));
        if(apmAppViewConfig == null){
            throw new BusinessException("视图配置不存在");
        }
        if(CollectionUtils.isEmpty(spaceBidList)){
            throw new BusinessException("空间bid不能为空");
        }
        spaceBidList.remove(apmAppViewConfig.getSpaceBid());
        if (CollectionUtils.isEmpty(spaceBidList)) {
            throw new BusinessException("不能选择当前空间");
        }
        //获取应用信息
        ApmSpaceApp spaceApp = apmSpaceAppService.getByBid(apmAppViewConfig.getSpaceAppBid());
        Map<String,String> navSpaceAppMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        Map<String,Map<String, Object>> navSpaceAppContentMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        Map<String,Map<String,String>> spaceAppAttrConfigMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        if(StringUtils.isNotEmpty(apmAppViewConfig.getNavSpaceAppBid())){
            //有导航应用，需要根据空间转换对应的导航应用
            ApmSpaceApp navSpaceApp = apmSpaceAppService.getByBid(apmAppViewConfig.getNavSpaceAppBid());
            List<ApmSpaceApp> navSpaceAppList = apmSpaceAppService.list(new LambdaQueryWrapper<ApmSpaceApp>().in(ApmSpaceApp::getSpaceBid,spaceBidList).eq(ApmSpaceApp::getModelCode,navSpaceApp.getModelCode()).eq(ApmSpaceApp::getDeleteFlag,false));
            if(CollectionUtils.isEmpty(navSpaceAppList)){
                throw new BusinessException("目标所有空间应用不存在");
            }
            if(CollectionUtils.isNotEmpty(navSpaceAppList)){
                for(ApmSpaceApp navSpaceApp1 : navSpaceAppList){
                    navSpaceAppMap.put(navSpaceApp1.getSpaceBid(),navSpaceApp1.getBid());
                }
                for(String spaceBid:spaceBidList){
                    if(!navSpaceAppMap.containsKey(spaceBid)){
                        throw new BusinessException("空间"+spaceBid+"应用不存在");
                    }
                }
                List<String> navSpaceAppBidList = navSpaceAppList.stream().map(ApmSpaceApp::getBid).collect(Collectors.toList());
                //导航视图需要根据应用bid和code查询对应的视图
                LambdaQueryWrapper<ApmSpaceAppViewModelPo> viewModelQueryWrapper = Wrappers.<ApmSpaceAppViewModelPo>lambdaQuery().in(ApmSpaceAppViewModelPo::getSpaceAppBid, navSpaceAppBidList)
                        .eq(ApmSpaceAppViewModelPo::getCode, apmAppViewConfig.getNavCode()).eq(ApmSpaceAppViewModelPo::getDeleteFlag,false);
                List<ApmSpaceAppViewModelPo> apmSpaceAppViewModelPos = apmSpaceAppViewModelService.list(viewModelQueryWrapper);
                if(CollectionUtils.isNotEmpty(apmSpaceAppViewModelPos)){
                    for(ApmSpaceAppViewModelPo apmSpaceAppViewModelPo : apmSpaceAppViewModelPos){
                        navSpaceAppContentMap.put(apmSpaceAppViewModelPo.getSpaceAppBid(),apmSpaceAppViewModelPo.getConfigContent());
                    }
                }
            }
            Map<String,String> attrConfigMap = apmAppViewConfig.getNavAttrConfig();
            for(Map.Entry<String,String> entry : attrConfigMap.entrySet()){
                ApmSpaceApp navSpaceApp2 = apmSpaceAppService.getByBid(entry.getKey());
                List<ApmSpaceApp> navSpaceAppList2 = apmSpaceAppService.list(new LambdaQueryWrapper<ApmSpaceApp>().in(ApmSpaceApp::getSpaceBid,spaceBidList).eq(ApmSpaceApp::getModelCode,navSpaceApp2.getModelCode()).eq(ApmSpaceApp::getDeleteFlag,false));
                if(CollectionUtils.isNotEmpty(navSpaceAppList2)){
                    for(ApmSpaceApp apmSpaceApp2 : navSpaceAppList2){
                        Map<String,String> attrConfigMap2 = spaceAppAttrConfigMap.get(apmSpaceApp2.getSpaceBid());
                        if(attrConfigMap2 == null){
                            attrConfigMap2 = new HashMap<>(CommonConstant.START_MAP_SIZE);
                        }
                        attrConfigMap2.put(apmSpaceApp2.getBid(),entry.getValue());
                        spaceAppAttrConfigMap.put(apmSpaceApp2.getSpaceBid(),attrConfigMap2);
                    }
                }
            }
        }
        //有角色，需要根据空间进行角色转换
        Map<String,List<String>> roleSpaceMap = getCopySpaceRoles(apmAppViewConfig.getSpaceRoleBids(), spaceBidList);
        if(spaceApp != null){
            //查询空间下所有的应用
            List<ApmSpaceApp> spaceAppList = apmSpaceAppService.list(new LambdaQueryWrapper<ApmSpaceApp>().in(ApmSpaceApp::getSpaceBid,spaceBidList).eq(ApmSpaceApp::getModelCode,spaceApp.getModelCode()).eq(ApmSpaceApp::getDeleteFlag,false));
            if (CollectionUtils.isEmpty(spaceAppList)){
                throw new BusinessException("目标所有空间应用不存在");
            }
            //找到应用与BID的映射关系
            Map<String, String> appBidNewBidMap = new HashMap<>(16);
            List<ApmAppViewConfig> copyList = new ArrayList<>();
            for(ApmSpaceApp spaceAppCopy : spaceAppList){
                ApmAppViewConfig apmAppViewConfigCopy = ApmAppViewConfigConverter.INSTANCE.po2po(apmAppViewConfig);
                apmAppViewConfigCopy.setId(null);
                apmAppViewConfigCopy.setBid(SnowflakeIdWorker.nextIdStr());
                apmAppViewConfigCopy.setSpaceAppBid(spaceAppCopy.getBid());
                apmAppViewConfigCopy.setSpaceBid(spaceAppCopy.getSpaceBid());
                apmAppViewConfigCopy.setNavSpaceAppBid(navSpaceAppMap.get(spaceAppCopy.getSpaceBid()));
                apmAppViewConfigCopy.setSpaceRoleBids(roleSpaceMap.get(spaceAppCopy.getSpaceBid()));
                apmAppViewConfigCopy.setNavConfigContent(navSpaceAppContentMap.get(apmAppViewConfigCopy.getNavSpaceAppBid()));
                apmAppViewConfigCopy.setNavAttrConfig(spaceAppAttrConfigMap.get(apmAppViewConfigCopy.getSpaceBid()));
                apmAppViewConfigCopy.setShareViewConfigBid(appViewConfigBid);
                apmAppViewConfigCopy.setUserIds(new ArrayList<>());
                apmAppViewConfigCopy.setDepartmentIds(new ArrayList<>());
                apmAppViewConfigCopy.setUpdatedTime(new Date());
                copyList.add(apmAppViewConfigCopy);
                appBidNewBidMap.put(spaceAppCopy.getBid(), apmAppViewConfigCopy.getBid());
            }
            //保存前先清理原来的数据
            List<ApmAppViewConfig> oldList = apmAppViewConfigService.list(new LambdaQueryWrapper<ApmAppViewConfig>().in(ApmAppViewConfig::getSpaceBid,spaceBidList).eq(ApmAppViewConfig::getShareViewConfigBid, appViewConfigBid));
            if(CollectionUtils.isNotEmpty(oldList)){
                Map<String, String> oldBidAppbidMap = oldList.stream().collect(Collectors.toMap(ApmAppViewConfig::getBid, ApmAppViewConfig::getSpaceAppBid, (k1, k2) -> k2));
                List<Integer> oldIdList = oldList.stream().map(ApmAppViewConfig::getId).collect(Collectors.toList());
                List<String> oldBidList = oldList.stream().map(ApmAppViewConfig::getBid).collect(Collectors.toList());
                //如果要清理的视图有分享过视图，需要更新其他视图的分享视图bid
                List<ApmAppViewConfig> list = apmAppViewConfigService.list(new LambdaQueryWrapper<ApmAppViewConfig>().in(ApmAppViewConfig::getShareViewConfigBid, oldBidList));
                List<ApmAppViewConfig> updateList = new ArrayList<>();
                for (ApmAppViewConfig appViewConfig : list) {
                    String shareViewConfigBid = appViewConfig.getShareViewConfigBid();
                    String appBid = oldBidAppbidMap.get(shareViewConfigBid);
                    if (appBidNewBidMap.containsKey(appBid)){
                        appViewConfig.setShareViewConfigBid(appBidNewBidMap.get(appBid));
                        updateList.add(appViewConfig);
                    }
                }
                if (CollectionUtils.isNotEmpty(updateList)){
                    apmAppViewConfigService.updateBatchById(updateList);
                }
                apmAppViewConfigMapper.deleteByIds(oldIdList);
            }
            return apmAppViewConfigService.saveBatch(copyList);
        }
        return false;
    }

    /**
     * @param roleBidList
     * @param spaceBidList
     * @return {@link Map }<{@link String },{@link List }<{@link String }>>
     */
    private Map<String,List<String>> getCopySpaceRoles(List<String> roleBidList,List<String> spaceBidList){
       Map<String,List<String>> roleSpaceMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        if(CollectionUtils.isEmpty(roleBidList) || CollectionUtils.isEmpty(spaceBidList)){
           return roleSpaceMap;
       }
       List<ApmRole> apmRoles = apmRoleService.list(new LambdaQueryWrapper<ApmRole>().in(ApmRole::getBid,roleBidList).eq(ApmRole::getDeleteFlag,false));
       List<String> codes = apmRoles.stream().map(ApmRole::getCode).collect(Collectors.toList());
       List<ApmSphere> apmSphereList = apmSphereService.list(new LambdaQueryWrapper<ApmSphere>().in(ApmSphere::getBizBid,spaceBidList).eq(ApmSphere::getType, TypeEnum.SPACE.getCode()).eq(ApmSphere::getDeleteFlag,false));
       List<String> sphereBids = apmSphereList.stream().map(ApmSphere::getBid).collect(Collectors.toList());
       List<ApmRole> apmSpaceRoles = apmRoleService.list(new LambdaQueryWrapper<ApmRole>().in(ApmRole::getSphereBid,sphereBids).in(ApmRole::getCode,codes).eq(ApmRole::getDeleteFlag,false));
       if(CollectionUtils.isNotEmpty(apmSpaceRoles)){
           Map<String,String> sphereAndSpaceMap = apmSphereList.stream().collect(Collectors.toMap(ApmSphere::getBid,ApmSphere::getBizBid));
           for(ApmRole apmRole : apmSpaceRoles){
               List<String> tempList = roleSpaceMap.get(sphereAndSpaceMap.get(apmRole.getSphereBid()));
               if(tempList == null){
                   tempList = new ArrayList<>();
               }
               tempList.add(apmRole.getBid());
               roleSpaceMap.put(sphereAndSpaceMap.get(apmRole.getSphereBid()),tempList);
           }
       }
       return roleSpaceMap;
    }

    /**
     * @param apmAppViewConfigDto
     * @return boolean
     */
    @Override
    public boolean saveAppViewConfig(ApmAppViewConfigDto apmAppViewConfigDto) {
        if(StringUtils.isEmpty(apmAppViewConfigDto.getBid())){
            List<String> spaceAppBids = apmAppViewConfigDto.getSpaceAppBids();
            List<ApmAppViewConfig> apmAppViewConfigs = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(spaceAppBids)){
                for(String spaceAppBid : spaceAppBids){
                    ApmAppViewConfig apmAppViewConfig = ApmAppViewConfigConverter.INSTANCE.dto2po(apmAppViewConfigDto);
                    //新增
                    apmAppViewConfig.setCreatedBy(SsoHelper.getJobNumber());
                    apmAppViewConfig.setBid(SnowflakeIdWorker.nextIdStr());
                    if(apmAppViewConfigDto.getViewType() == 2 && apmAppViewConfigDto.getNavConfigContent() == null){
                        if(StringUtils.isEmpty(apmAppViewConfigDto.getNavSpaceAppBid()) || StringUtils.isEmpty(apmAppViewConfigDto.getNavCode())){
                            throw new PlmBizException("导航视图对应的应用bid或者显示编码不能为空");
                        }
                        //导航视图需要根据应用bid和code查询对应的视图
                        LambdaQueryWrapper<ApmSpaceAppViewModelPo> viewModelQueryWrapper = Wrappers.<ApmSpaceAppViewModelPo>lambdaQuery().eq(ApmSpaceAppViewModelPo::getSpaceAppBid, apmAppViewConfigDto.getNavSpaceAppBid())
                                .eq(ApmSpaceAppViewModelPo::getCode, apmAppViewConfigDto.getNavCode());
                        ApmSpaceAppViewModelPo apmSpaceAppViewModelPo = apmSpaceAppViewModelService.getOne(viewModelQueryWrapper);
                        if(apmSpaceAppViewModelPo != null){
                            apmAppViewConfig.setNavConfigContent(apmSpaceAppViewModelPo.getConfigContent());
                        }
                    }
                    apmAppViewConfig.setSpaceAppBid(spaceAppBid);
                    apmAppViewConfigs.add(apmAppViewConfig);
                }
            }
            return apmAppViewConfigService.saveBatch(apmAppViewConfigs);
        }else{
            //修改
            ApmAppViewConfig appViewConfig = apmAppViewConfigService.getOne(new LambdaQueryWrapper<ApmAppViewConfig>().eq(ApmAppViewConfig::getBid,apmAppViewConfigDto.getBid()).eq(ApmAppViewConfig::getDeleteFlag,false));
            appViewConfig.setUpdatedBy(SsoHelper.getJobNumber());
            appViewConfig.setViewName(apmAppViewConfigDto.getViewName());
            appViewConfig.setViewType(apmAppViewConfigDto.getViewType());
            appViewConfig.setShowType(apmAppViewConfigDto.getShowType());
            appViewConfig.setTeamWorkers(apmAppViewConfigDto.getTeamWorkers());
            appViewConfig.setNavSpaceAppBid(apmAppViewConfigDto.getNavSpaceAppBid());
            appViewConfig.setNavCode(apmAppViewConfigDto.getNavCode());
            if(apmAppViewConfigDto.getNavConfigContent() != null && apmAppViewConfigDto.getNavConfigContent().size() > 0){
                appViewConfig.setNavConfigContent(apmAppViewConfigDto.getNavConfigContent());
            }
            appViewConfig.setNavAttrConfig(apmAppViewConfigDto.getNavAttrConfig());
            appViewConfig.setDepartmentIds(apmAppViewConfigDto.getDepartmentIds());
            appViewConfig.setSpaceRoleBids(apmAppViewConfigDto.getSpaceRoleBids());
            appViewConfig.setUserIds(apmAppViewConfigDto.getUserIds());
            appViewConfig.setViewCondition(apmAppViewConfigDto.getViewCondition());
            appViewConfig.setSort(apmAppViewConfigDto.getSort());
            appViewConfig.setViewDesc(apmAppViewConfigDto.getViewDesc());
            return apmAppViewConfigService.updateById(appViewConfig);
        }
    }

    /**
     * @param bid
     * @return {@link ApmAppViewConfigVo }
     */
    @Override
    public ApmAppViewConfigVo getViewByBid(String bid){
        ApmAppViewConfig apmAppViewConfig = apmAppViewConfigService.getOne(new LambdaQueryWrapper<ApmAppViewConfig>().eq(ApmAppViewConfig::getBid,bid).eq(ApmAppViewConfig::getDeleteFlag,false));
        if(apmAppViewConfig == null){
            throw new PlmBizException("视图不存在");
        }
        String userNo = SsoHelper.getJobNumber();
        List<ApmAppViewUserRecord> apmAppViewUserRecordList = apmAppViewUserRecordService.list(new LambdaQueryWrapper<ApmAppViewUserRecord>().eq(ApmAppViewUserRecord::getCreatedBy,userNo).eq(ApmAppViewUserRecord::getSpaceAppBid,apmAppViewConfig.getSpaceAppBid()));
        List<String> defuaslutViewBids = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(apmAppViewUserRecordList)){
            defuaslutViewBids = apmAppViewUserRecordList.stream().map(ApmAppViewUserRecord::getAppViewConfigBid).collect(Collectors.toList());
        }
        ApmAppViewConfigVo apmAppViewConfigVo = ApmAppViewConfigConverter.INSTANCE.po2vo(apmAppViewConfig);
        if(apmAppViewConfig.getCreatedBy().equals(userNo) || (CollectionUtils.isNotEmpty(apmAppViewConfig.getTeamWorkers()) && apmAppViewConfig.getTeamWorkers().contains(userNo))){
            apmAppViewConfigVo.setEdit(true);
        }
        if(defuaslutViewBids.contains(apmAppViewConfig.getBid())){
            apmAppViewConfigVo.setDefault(true);
        }
        return apmAppViewConfigVo;
    }

    /**
     * @param spaceBid
     * @param spaceAppBid
     * @param tabBid
     * @return {@link List }<{@link ApmAppViewConfigVo }>
     */
    @Override
    public List<ApmAppViewConfigVo> listViewConfigs(String spaceBid, String spaceAppBid, String tabBid){
      //查询当前应用的所有配置数据
        List<ApmAppViewConfigVo> apmAppViewConfigVos = new ArrayList<>();
        List<ApmAppViewConfig> apmAppViewConfigs = apmAppViewConfigService.list(
                new LambdaQueryWrapper<ApmAppViewConfig>().eq(ApmAppViewConfig::getSpaceAppBid,spaceAppBid)
                        .eq(StringUtils.isNotEmpty(tabBid), ApmAppViewConfig::getTabBid, tabBid)  // 只有当tabBid不为空时才添加这个条件
                        .eq(ApmAppViewConfig::getDeleteFlag,false)
                        .orderBy(true,false,ApmAppViewConfig::getSort));
      if(CollectionUtils.isNotEmpty(apmAppViewConfigs)){
          String userNo = SsoHelper.getJobNumber();
          List<ApmAppViewUserRecord> apmAppViewUserRecordList = apmAppViewUserRecordService.list(new LambdaQueryWrapper<ApmAppViewUserRecord>().eq(ApmAppViewUserRecord::getCreatedBy,userNo).eq(ApmAppViewUserRecord::getSpaceAppBid,spaceAppBid));
          List<String> departmentList = new ArrayList<>();
          ApmUser apmUser = platformUserWrapper.getUserBOByEmpNO(userNo);
          List<String> defuaslutViewBids = new ArrayList<>();
          if(CollectionUtils.isNotEmpty(apmAppViewUserRecordList)){
              defuaslutViewBids = apmAppViewUserRecordList.stream().map(ApmAppViewUserRecord::getAppViewConfigBid).collect(Collectors.toList());
          }
          if(apmUser != null && CollectionUtils.isNotEmpty(apmUser.getDepartmentList())){
              //查用户的所有上级部门
              List<String> userDepartmentList = apmUser.getDepartmentList();
              if (CollectionUtils.isNotEmpty(apmUser.getDepts())) {
                  apmUser.getDepts().forEach(dept -> departmentList.add(dept.getDeptNo()));
              }
              for (String departmentId : userDepartmentList) {
                  List<DepartmentDTO> departmentDTOS = platformUserWrapper.getParentDepartmentByDepartmentId(departmentId);
                  if(CollectionUtils.isNotEmpty(departmentDTOS)){
                      for(DepartmentDTO departmentDTO : departmentDTOS){
                          departmentList.add(departmentDTO.getDeptNo());
                      }
                  }
                  if(!departmentList.contains(departmentId)){
                      departmentList.add(departmentId);
                  }
              }
          }
          //当前用户所在空间的角色
          List<String> roleBids = listRoleBidBySpaceBidAndIdentity(spaceBid,userNo);
          for(ApmAppViewConfig apmAppViewConfig : apmAppViewConfigs){
             if(apmAppViewConfig.getCreatedBy().equals(userNo)
                     || (CollectionUtils.isNotEmpty(apmAppViewConfig.getTeamWorkers()) && apmAppViewConfig.getTeamWorkers().contains(userNo))
             ){
                 ApmAppViewConfigVo apmAppViewConfigVo = ApmAppViewConfigConverter.INSTANCE.po2vo(apmAppViewConfig);
                 apmAppViewConfigVo.setEdit(true);
                 if(defuaslutViewBids.contains(apmAppViewConfigVo.getBid())){
                     apmAppViewConfigVo.setDefault(true);
                 }else{
                     apmAppViewConfigVo.setDefault(false);
                 }
                 apmAppViewConfigVos.add(apmAppViewConfigVo);
             }else{
                if(checkList(roleBids,apmAppViewConfig.getSpaceRoleBids())
                        || checkList(departmentList,apmAppViewConfig.getDepartmentIds())
                        || (CollUtil.isNotEmpty(apmAppViewConfig.getUserIds()) && apmAppViewConfig.getUserIds().contains(userNo))
                ){
                    ApmAppViewConfigVo apmAppViewConfigVo = ApmAppViewConfigConverter.INSTANCE.po2vo(apmAppViewConfig);
                    apmAppViewConfigVo.setEdit(false);
                    if(defuaslutViewBids.contains(apmAppViewConfigVo.getBid())){
                        apmAppViewConfigVo.setDefault(true);
                    }else{
                        apmAppViewConfigVo.setDefault(false);
                    }
                    apmAppViewConfigVos.add(apmAppViewConfigVo);
                }
             }
          }
      }
      return apmAppViewConfigVos;
    }

    /**
     * @param roleBidList
     * @param roleBids
     * @return boolean
     */
    private boolean checkList(List<String> roleBidList,List<String> roleBids) {
        if (CollectionUtils.isNotEmpty(roleBidList) && CollectionUtils.isNotEmpty(roleBids)) {
            for (String roleBid : roleBidList) {
                if (roleBids.contains(roleBid)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** 查询空间下拥有的角色
     * @param spaceBid
     * @param userNo
     * @return boolean
     */
    private List<String> listRoleBidBySpaceBidAndIdentity(String spaceBid, String userNo) {
        List<String> roleBids = apmRoleMapper.getRoleListByBizBidAndIdentity("space",spaceBid,userNo);
        //设置默认角色
        ApmSphere apmSphere = apmSphereService.getByBizBidAndType(spaceBid, TypeEnum.SPACE.getCode());
        List<ApmRole> defaultRoleList = apmRoleService.list(Wrappers.<ApmRole>lambdaQuery().eq(ApmRole::getSphereBid,apmSphere.getBid()).in(ApmRole::getCode, SpaceConstant.DEFAULT_ROLE_SET));
        if (CollectionUtils.isNotEmpty(defaultRoleList)) {
            Map<String, String> defaultRoleCodeBidMap = defaultRoleList.stream().collect(Collectors.toMap(ApmRole::getCode, ApmRole::getBid, (k, v) -> v));
            if (CollectionUtils.isNotEmpty(roleBids) && StringUtils.isNotEmpty(defaultRoleCodeBidMap.get(RoleConstant.SPACE_MEMBER_EN))) {
                roleBids.add(defaultRoleCodeBidMap.get(RoleConstant.SPACE_MEMBER_EN));
            }
            if (StringUtils.isNotEmpty(defaultRoleCodeBidMap.get(RoleConstant.ALL))) {
                roleBids.add(defaultRoleCodeBidMap.get(RoleConstant.ALL));
            }
        }
        return roleBids;
    }
}
