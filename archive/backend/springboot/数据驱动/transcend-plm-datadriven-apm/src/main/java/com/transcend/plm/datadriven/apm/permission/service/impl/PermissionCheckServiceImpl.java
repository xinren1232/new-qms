package com.transcend.plm.datadriven.apm.permission.service.impl;

import com.alibaba.fastjson.JSON;
import com.transcend.framework.core.exception.TranscendBizException;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.ObjectEnum;
import com.transcend.plm.datadriven.apm.constants.DemandConstant;
import com.transcend.plm.datadriven.apm.constants.RoleConstant;
import com.transcend.plm.datadriven.apm.enums.InnerRoleEnum;
import com.transcend.plm.datadriven.apm.enums.TypeEnum;
import com.transcend.plm.datadriven.apm.permission.configcenter.SysRoleDomainService;
import com.transcend.plm.datadriven.apm.permission.enums.OperatorEnum;
import com.transcend.plm.datadriven.apm.permission.pojo.dto.PermissionCheckDto;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmRole;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmSphere;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmSphereService;
import com.transcend.plm.datadriven.apm.permission.service.IApmRoleDomainService;
import com.transcend.plm.datadriven.apm.permission.service.IPermissionCheckService;
import com.transcend.plm.datadriven.apm.permission.service.IPermissionConfigService;
import com.transcend.plm.datadriven.apm.space.model.SpaceAppDataEnum;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.space.service.ApmSpaceApplicationService;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.domain.object.base.VersionModelDomainService;
import com.transsion.framework.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author unknown
 */
@Slf4j
@Service
public class PermissionCheckServiceImpl implements IPermissionCheckService {

    @Resource
    private ObjectModelStandardI objectModelCrudI;

    @Resource
    private IApmRoleDomainService apmRoleDomainService;

    @Resource
    private ApmSphereService apmSphereService;
    @Resource
    private IPermissionConfigService permissionConfigService;
    @Resource
    private ApmSpaceAppService apmSpaceAppService;
    @Resource
    private SysRoleDomainService sysRoleDomainService;

    /**
     * 检查权限
     *
     * @param permissionCheckDto
     * @return
     */
    @Override
    public boolean checkPermission(PermissionCheckDto permissionCheckDto){
        if(StringUtils.isNotEmpty(permissionCheckDto.getInstanceBid())){
            return checkInstancePermission(permissionCheckDto);
        }else{
            return checkSpaceAppPermssion(permissionCheckDto);
        }
    }

    /**
     * 批量检查实例权限
     *
     * @return
     */
    @Override
    public boolean checkInstancesPermission(List<MObject> list,String spaceBid,String operatorCode){
        if(CollectionUtils.isNotEmpty(list)){
            //特殊逻辑，加锁数据不能删除
            if (OperatorEnum.DELETE.getCode().equals(operatorCode)
                    && list.stream().anyMatch(v -> Arrays.asList("A01","A02").contains(v.getModelCode()) && DemandConstant.YES_LOCK.equals(v.get(DemandConstant.LOCK_FLAG)))) {
                return false;
            }
            String userNO = SsoHelper.getJobNumber();
            List<String> roles = getRoleCodeList(spaceBid, userNO);
            roles.add(InnerRoleEnum.ALL.getCode());
            for(MObject appData : list){
                List<String> roleCodes = new ArrayList<>();
                roleCodes.addAll(roles);
                getInnerRole(appData, userNO, roleCodes);
                   /* //查看流程私有权限
                    //查询流程角色
                    String spaceAppBid = null;
                    if(appData.get(CommonConst.SPACE_APP_BID) != null){
                        spaceAppBid = appData.get(CommonConst.SPACE_APP_BID)+"";
                    }
                    List<String> flowCodes = permissionConfigService.getFlowInstanceRoleCodes(userNO,appData.getBid(),spaceAppBid);
                    if(CollectionUtils.isNotEmpty(flowCodes)){
                        for(String flowCode:flowCodes){
                            roleCodes.add(CommonConst.PRI_KEY+flowCode);
                        }
                    }*/
                if (!permissionConfigService.checkInstancePermission(appData, String.valueOf(appData.get(ObjectEnum.SPACE_APP_BID.getCode())), operatorCode, roleCodes)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 校验实例权限，主要包括删除，编辑，详情权限
     * @param permissionCheckDto
     * @return
     */
    @Override
    public boolean checkInstancePermission(PermissionCheckDto permissionCheckDto){
        String spaceBid = permissionCheckDto.getSpaceBid();
        String spaceAppBid = permissionCheckDto.getSpaceAppBid();
        String operatorCode = permissionCheckDto.getOperatorCode();
        String bid = permissionCheckDto.getInstanceBid();
        String userNO = SsoHelper.getJobNumber();
        ApmSpaceApp spaceApp = apmSpaceAppService.getByBid(spaceAppBid);
        MObject appData = objectModelCrudI.getByBid(spaceApp.getModelCode(), bid);
        List<String> roleCodes = getRoleCodeList(spaceBid, userNO);
        //查询流程角色
        List<String> flowCodes = permissionConfigService.getFlowInstanceRoleCodes(userNO,bid,spaceAppBid);
        if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(flowCodes)){
            for(String flowCode:flowCodes){
                roleCodes.add(CommonConst.PRI_KEY+flowCode);
            }
        }
        if(appData == null){
            throw new TranscendBizException("数据不存在");
        }
        String permissionBid = null;
        if(appData.get(ObjectEnum.PERMISSION_BID.getCode()) != null){
            permissionBid = appData.get(ObjectEnum.PERMISSION_BID.getCode()).toString();
        }
        //获取实例内置角色
        getInnerRole(appData, userNO, roleCodes);
        boolean hasPermission = permissionConfigService.checkPermission(roleCodes, permissionBid, operatorCode);
        return hasPermission;
    }

    public void getInnerRole(MBaseData appData, String userNO, List<String> roleCodes) {
        roleCodes.add(InnerRoleEnum.ALL.getCode());
        if(userNO.equals(appData.getCreatedBy())){
            roleCodes.add(InnerRoleEnum.CREATER.getCode());
        }
        if(appData.get(ObjectEnum.HANDLER.getCode()) != null){
            Object value = appData.get(ObjectEnum.HANDLER.getCode());
            if(value instanceof List){
                try{
                    List<String> values = JSON.parseArray(JSON.toJSONString(value), String.class);
                    if(values.contains(userNO)){
                        roleCodes.add(InnerRoleEnum.PERSON_RESPONSIBLE.getCode());
                    }
                }catch (Exception e){
                    log.error("checkPermission error",e);
                }
            }else if(value instanceof String){
                String person = value.toString();
                if(userNO.equals(person)){
                    roleCodes.add(InnerRoleEnum.PERSON_RESPONSIBLE.getCode());
                }
            }
        }
        // 关注人
        if(appData.get(ObjectEnum.FOLLOW_MEMBER.getCode()) != null){
            Object value = appData.get(ObjectEnum.FOLLOW_MEMBER.getCode());
            if(value instanceof List){
                try{
                    List<String> values = JSON.parseArray(JSON.toJSONString(value), String.class);
                    if(values.contains(userNO)){
                        roleCodes.add(InnerRoleEnum.FOLLOW_MEMBER.getCode());
                    }
                }catch (Exception e){
                    log.error("checkPermission error",e);
                }
            }else if(value instanceof String){
                String person = value.toString();
                if(userNO.equals(person)){
                    roleCodes.add(InnerRoleEnum.FOLLOW_MEMBER.getCode());
                }
            }
        }
        //技术负责人
        if(appData.get(ObjectEnum.PERSON_RESPONSIBLE.getCode()) != null){
            Object value = appData.get(ObjectEnum.PERSON_RESPONSIBLE.getCode());
            if(value instanceof List){
                try{
                    List<String> values = JSON.parseArray(JSON.toJSONString(value), String.class);
                    if(values.contains(userNO)){
                        roleCodes.add(InnerRoleEnum.TECHNICAL_DIRECTOR.getCode());
                    }
                }catch (Exception e){
                    log.error("checkPermission error",e);
                }
            }else if(value instanceof String){
                String person = value.toString();
                if(userNO.equals(person)){
                    roleCodes.add(InnerRoleEnum.TECHNICAL_DIRECTOR.getCode());
                }
            }
        }
        //UX代表
        if(appData.get(ObjectEnum.UX_SCORE.getCode()) != null){
            Object value = appData.get(ObjectEnum.UX_SCORE.getCode());
            if(value instanceof List){
                try{
                    List<String> values = JSON.parseArray(JSON.toJSONString(value), String.class);
                    if(values.contains(userNO)){
                        roleCodes.add(InnerRoleEnum.UX_AGENT.getCode());
                    }
                }catch (Exception e){
                    log.error("checkPermission error",e);
                }
            }else if(value instanceof String){
                String person = value.toString();
                if(userNO.equals(person)){
                    roleCodes.add(InnerRoleEnum.UX_AGENT.getCode());
                }
            }
        }
    }

    /**
     * 校验应用权限，主要包括新增，列表显示权限
     * @param permissionCheckDto
     * @return
     */
    @Override
    public boolean checkSpaceAppPermssion(PermissionCheckDto permissionCheckDto){
        String spaceBid = permissionCheckDto.getSpaceBid();
        String spaceAppBid = permissionCheckDto.getSpaceAppBid();
        String operatorCode = permissionCheckDto.getOperatorCode();
        String userNO = SsoHelper.getJobNumber();
        if (StringUtils.isNotBlank(permissionCheckDto.getInstanceBid())){
            ApmSpaceApp spaceApp = apmSpaceAppService.getByBid(spaceAppBid);
            MObject appData = objectModelCrudI.getByBid(spaceApp.getModelCode(), permissionCheckDto.getInstanceBid());
            if (appData == null) {
                return true;
            }
            List<String> roleCodes = permissionConfigService.listRoleCodeByBaseData(spaceAppBid,appData);
            //如果是当前处理人+关注人+加密人，默认有编辑和查看权限
            if ((roleCodes.contains(InnerRoleEnum.PERSON_RESPONSIBLE.getCode()) || roleCodes.contains(InnerRoleEnum.FOLLOW_MEMBER.getCode())
                    || roleCodes.contains(InnerRoleEnum.CONFIDENTIAL_MEMBER.getCode())
            ) &&
                    Arrays.asList(OperatorEnum.EDIT.getCode(), OperatorEnum.DETAIL.getCode(), OperatorEnum.REVISE.getCode(), OperatorEnum.PROMOTE.getCode()).contains(operatorCode)) {
                return true;
            }
            return permissionConfigService.checkInstancePermission(appData, spaceAppBid, operatorCode, roleCodes);
        } else {
            List<String> roleCodes = getRoleCodeList(spaceBid, userNO);
            roleCodes.add(InnerRoleEnum.ALL.getCode());
            return permissionConfigService.checkPermission(spaceAppBid,roleCodes,operatorCode);
        }
    }

    @Override
    public boolean checkSpaceAppPermssion(String modelCode, List<String> bids, String operatorCode){
        List<MObject> list = objectModelCrudI.listByBids(bids, modelCode);
        //由于存在跨空间数据，需要按应用分组
        Map<String, List<MObject>> spaceAppDataMap = list.stream().collect(Collectors.groupingBy(v -> v.getOrDefault(SpaceAppDataEnum.SPACE_APP_BID.getCode(), "").toString()));
        //校验删除权限
        String userNO = SsoHelper.getJobNumber();
        for (Map.Entry<String, List<MObject>> entry : spaceAppDataMap.entrySet()) {
            ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(entry.getKey());
            if (apmSpaceApp == null) {
                throw new BusinessException("应用不存在");
            }
            List<String> roles = getRoleCodeList(apmSpaceApp.getSpaceBid(), userNO);
            roles.add(InnerRoleEnum.ALL.getCode());
            for(MObject appData : list){
                List<String> roleCodes = new ArrayList<>();
                roleCodes.addAll(roles);
                //补充实例角色
                permissionConfigService.addInstanceRole(appData, userNO, roleCodes);
                //如果是当前处理人+关注人+加密人，默认有编辑和查看权限
                if ((roleCodes.contains(InnerRoleEnum.PERSON_RESPONSIBLE.getCode()) || roleCodes.contains(InnerRoleEnum.FOLLOW_MEMBER.getCode())
                        || roleCodes.contains(InnerRoleEnum.CONFIDENTIAL_MEMBER.getCode())
                ) &&
                        Arrays.asList(OperatorEnum.EDIT.getCode(), OperatorEnum.DETAIL.getCode(), OperatorEnum.REVISE.getCode(), OperatorEnum.PROMOTE.getCode()).contains(operatorCode)) {
                    return true;
                }
                if (!permissionConfigService.checkInstancePermission(appData, String.valueOf(appData.get(ObjectEnum.SPACE_APP_BID.getCode())), operatorCode, roleCodes)) {
                    throw new BusinessException("[" + appData.getName()+ "]" + "没有相应权限");
                }
            }
        }
        return true;
    }

    private List<String> getRoleCodeList(String spaceBid, String userNO){
        List<String> result = Lists.newArrayList();

        // 补充系统用户全局角色
        List<String> sysRoleCodeList = sysRoleDomainService.listSysRoleCode(userNO);
        if (CollectionUtils.isNotEmpty(sysRoleCodeList)) {
            result.addAll(sysRoleCodeList);
        }
        ApmSphere apmSphere = apmSphereService.getByBizBidAndType(spaceBid, TypeEnum.SPACE.getCode());
        if(apmSphere != null){
            List<ApmRole> apmRoles = apmRoleDomainService.listRoleByJobNumAndSphereBid(userNO, apmSphere.getBid());
            if(CollectionUtils.isNotEmpty(apmRoles)){
                List<String> roleCodes = apmRoles.stream().map(ApmRole::getCode).collect(Collectors.toList());
                result.addAll(roleCodes);
                // 如果在该空间下有任意非"ALL"角色，则补充空间角色
                if (apmRoles.stream().anyMatch(role-> apmSphere.getBid().equals(role.getSphereBid()) && !RoleConstant.ALL.equals(role.getCode()))) {
                    result.add(RoleConstant.SPACE_MEMBER_EN);
                }
                return result;
            }
        }
        return result;
    }
}
