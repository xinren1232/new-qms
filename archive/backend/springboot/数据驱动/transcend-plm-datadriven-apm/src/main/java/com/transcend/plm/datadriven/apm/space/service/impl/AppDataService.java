package com.transcend.plm.datadriven.apm.space.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.dao.tool.PageResultTools;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.api.model.dto.LifeCyclePromoteDto;
import com.transcend.plm.datadriven.apm.common.ApmImplicitParameter;
import com.transcend.plm.datadriven.apm.constants.RoleConstant;
import com.transcend.plm.datadriven.apm.enums.InnerRoleEnum;
import com.transcend.plm.datadriven.apm.enums.TypeEnum;
import com.transcend.plm.datadriven.apm.permission.configcenter.SysRoleDomainService;
import com.transcend.plm.datadriven.apm.permission.enums.OperatorEnum;
import com.transcend.plm.datadriven.apm.permission.pojo.dto.PermissionCheckDto;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleVO;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.CheckPermissionResultVo;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmRole;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmSphere;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmSphereService;
import com.transcend.plm.datadriven.apm.permission.repository.service.PermissionPlmListRuleItemService;
import com.transcend.plm.datadriven.apm.permission.service.IApmRoleDomainService;
import com.transcend.plm.datadriven.apm.permission.service.IPermissionCheckService;
import com.transcend.plm.datadriven.apm.permission.service.IPermissionConfigService;
import com.transcend.plm.datadriven.apm.permission.service.IPermissionRuleConditionService;
import com.transcend.plm.datadriven.apm.permission.service.impl.PermissionRuleService;
import com.transcend.plm.datadriven.apm.space.enums.PermissionCheckEnum;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.space.service.ApmSpaceApplicationService;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppConfigDrivenService;
import com.transcend.plm.datadriven.apm.space.service.IAppDataService;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transcend.plm.datadriven.common.tool.SecrecyWrapperHandler;
import com.transcend.plm.datadriven.common.util.TranscendObjectUtil;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.domain.object.base.VersionModelDomainService;
import com.transsion.framework.dto.BaseRequest;
import com.transsion.framework.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author unknown
 */
@Slf4j
@Service
public class AppDataService implements IAppDataService {
    @Resource
    private ObjectModelStandardI objectModelCrudI;
    @Resource
    private PermissionRuleService permissionRuleService;
    @Resource
    private ApmSpaceApplicationService apmSpaceApplicationService;
    @Resource
    private PermissionPlmListRuleItemService permissionPlmListRuleItemService;

    @Resource
    private IApmRoleDomainService apmRoleDomainService;
    @Resource
    private ApmRoleService apmRoleService;

    @Resource
    private ApmSphereService apmSphereService;
    @Resource
    private IPermissionConfigService permissionConfigService;

    @Resource
    private IPermissionCheckService permissionCheckService;

    @Resource
    private VersionModelDomainService versionModelDomainService;

    @Resource
    IPermissionRuleConditionService permissionRuleConditionService;


    @Resource
    private ApmSpaceAppService apmSpaceAppService;
    @Resource
    private SysRoleDomainService sysRoleDomainService;

    @Resource
    private IAppDataService appDataService;

    @Resource
    private IApmSpaceAppConfigDrivenService apmSpaceAppConfigDrivenService;

    private static final String LIST_PERMISSION_SQL = "EXISTS (\n" +
            "\tSELECT\n" +
            "\t\t1 \n" +
            "\tFROM\n" +
            "\t\tpermission_plm_list_rule_item c \n" +
            "\tWHERE\n" +
            "\t\tc.permission_bid = a.permission_bid \n" +
            "\t\tAND (c.role_code IN ( %s ) \n" +
            "\t\tOR ( c.role_code IN ( 'INNER_CREATER' ) AND a.created_by = '%s') \n" +
            "\t\tOR ( c.role_code IN ( 'INNER_PERSON_RESPONSIBLE' ) AND a.person_responsible like '%%%s%%' ) )\n" +
            "\t)";

    @Override
    public <T extends MObject> T add(String modelCode, T appData) {
        //fillPermissionBid(appData);
        //判断列表权限是否有新增权限
        if (!TranscendObjectUtil.isFiledEmpty(appData, RelationEnum.SPACE_APP_BID.getCode()) && appData instanceof MSpaceAppData) {
            PermissionCheckDto permissionCheckDto = new PermissionCheckDto();
            permissionCheckDto.setSpaceBid(appData.get(RelationEnum.SPACE_BID.getCode()).toString());
            permissionCheckDto.setSpaceAppBid(appData.get(RelationEnum.SPACE_APP_BID.getCode()).toString());
            permissionCheckDto.setOperatorCode(OperatorEnum.ADD.getCode());
            if (!permissionCheckService.checkSpaceAppPermssion(permissionCheckDto)) {
                throw new BusinessException("没有新增权限");
            }
        }

        //checkAddPermission(appData);
        return (T) objectModelCrudI.add(modelCode, appData);
    }

    private <T extends MBaseData> void checkAddPermission(T appData) {
        if (appData == null) {
            return;
        }
        String userNO = SsoHelper.getJobNumber();
        String spaceBid = null;
        if (appData.get(RelationEnum.SPACE_BID.getCode()) != null) {
            spaceBid = appData.get(RelationEnum.SPACE_BID.getCode()).toString();
        }
        String spaceAppBid = null;
        if (appData.get(RelationEnum.SPACE_APP_BID.getCode()) != null) {
            spaceAppBid = appData.get(RelationEnum.SPACE_APP_BID.getCode()).toString();
        }
        if (StringUtils.isEmpty(spaceBid)) {
            if (StringUtils.isNotEmpty(spaceAppBid)) {
                ApmSpaceApp spaceApp = apmSpaceAppService.getByBid(spaceAppBid);
                spaceBid = spaceApp.getSpaceBid();
            }
        }
        List<String> roleCodes = getRoleCodeList(spaceBid, userNO);
        roleCodes.add(InnerRoleEnum.ALL.getCode());
        boolean hasPermission = permissionConfigService.checkAddPermission(spaceAppBid, roleCodes);
        if (!hasPermission) {
            throw new BusinessException("没有新增权限");
        }
    }

    @Override
    public Boolean validAddPermission(String spaceBid, String spaceAppBid, Map<String, String> appData) {
        try {
            if (StringUtils.isEmpty(spaceBid) || StringUtils.isEmpty(spaceAppBid) || ObjectUtil.isEmpty(appData)) {
                return Boolean.FALSE;
            }

            ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(spaceAppBid);
            if (ObjectUtil.isEmpty(apmSpaceApp)) {
                return Boolean.FALSE;
            }

            MObject mObject = new MObject();
            mObject.put(RelationEnum.SPACE_BID.getCode(), spaceBid);
            mObject.put(RelationEnum.SPACE_APP_BID.getCode(), spaceAppBid);
            mObject.put(ObjectEnum.PERMISSION_BID.getCode(), CommonConst.APP_FLAG + spaceAppBid);

            String operatorCode = appData.get("operatorCode");

//           return appDataService.checkButtonPermission(mObject, OperatorEnum.DETAIL.getCode());
            return appDataService.checkButtonPermission(mObject, operatorCode);
        } catch (Exception e) {
            return Boolean.FALSE;
        }
    }


    private <T extends MBaseData> void fillPermissionBid(T appData) {
        if (!TranscendObjectUtil.isFiledEmpty(appData, RelationEnum.SPACE_APP_BID.getCode()) && appData instanceof MSpaceAppData) {
            String permissionBid = permissionRuleService.getPermissionBidByAppData((MSpaceAppData) appData);
            appData.setPermissionBid(permissionBid);
        } else {
            appData.setPermissionBid(permissionRuleService.getDefaultPermissionBid());
        }
    }

    @Override
    public Boolean addBatch(String modelCode, List<? extends MBaseData> mObjects, Boolean ignorePermission) {
        if (CollectionUtils.isEmpty(mObjects)) {
            return false;
        }
        MBaseData mBaseData = mObjects.get(0);
        if (!TranscendObjectUtil.isFiledEmpty(mBaseData, RelationEnum.SPACE_APP_BID.getCode()) && mBaseData instanceof MSpaceAppData) {
            checkAddPermission(mBaseData);
        }
        return objectModelCrudI.addBatch(modelCode, mObjects);
    }

    @Override
    public Boolean addBatch(String modelCode, List<? extends MBaseData> mObjects) {
        return addBatch(modelCode, mObjects, false);
    }

    @Override
    public List<MObject> list(String spaceBid, String spaceAppBid, QueryCondition queryCondition) {
        /*boolean hasPermission = checkListPermission(spaceBid, spaceAppBid);
        if(!hasPermission){
            return new ArrayList<>();
        }*/
        return this.list(spaceBid, spaceAppBid, SsoHelper.getJobNumber(), queryCondition);
    }

    @Override
    public List<MObject> signObjectRecursionList(String spaceBid, String spaceAppBid, QueryCondition queryCondition) {
        String userNO = SsoHelper.getJobNumber();
        String permissionSql = String.format(LIST_PERMISSION_SQL, getRoleCodeStr(spaceBid, userNO), userNO, userNO);
        //判断列表权限是否有实例角色
        List<String> insanceRoleCodes = permissionPlmListRuleItemService.listInstanceRoleCodes(CommonConst.APP_FLAG + spaceAppBid);
        String instanceRoleCodeStr = null;
        if (CollectionUtils.isNotEmpty(insanceRoleCodes)) {
            ApmSphere apmSphere = apmSphereService.getByBizBidAndType(spaceAppBid, TypeEnum.OBJECT.getCode());
            if (apmSphere != null) {
                List<ApmRoleVO> apmRoleVOS = apmRoleService.listByRoleBidsByCodes(insanceRoleCodes, apmSphere.getBid());
                if (CollectionUtils.isNotEmpty(apmRoleVOS)) {
                    List<String> roleBids = apmRoleVOS.stream().map(ApmRoleVO::getBid).collect(Collectors.toList());
                    instanceRoleCodeStr = getInstanceCodeSql(spaceAppBid, userNO, roleBids);
                }
            }
        }
        if (StringUtils.isEmpty(instanceRoleCodeStr)) {
            queryCondition.setAdditionalSql(permissionSql);
        } else {
            StringBuffer conditionSql = new StringBuffer();
            conditionSql.append("((").append(permissionSql).append(") OR (").append(instanceRoleCodeStr).append("))");
            queryCondition.setAdditionalSql(conditionSql.toString());
        }

        ApmSpaceApp spaceApp = apmSpaceApplicationService.getSpaceAppByBid(spaceAppBid);
        //如没有找到对应的应用，则视为在查询关系列表，不做权限控制
        if (spaceApp == null) {
            return objectModelCrudI.signObjectRecursionTreeList(spaceAppBid, queryCondition);
        } else {
            return objectModelCrudI.signObjectRecursionTreeList(spaceApp.getModelCode(), queryCondition);
        }
    }

    private boolean checkListPermission(String spaceBid, String spaceAppBid) {
        String userNO = SsoHelper.getJobNumber();
        if (StringUtils.isEmpty(userNO)) {
            return true;
        }
        List<String> roleCodes = getRoleCodeList(spaceBid, userNO);
        roleCodes.add(InnerRoleEnum.ALL.getCode());
        boolean hasPermission = permissionConfigService.checkListPermission(spaceAppBid, roleCodes);
        return hasPermission;
    }

    @Override
    public List<MObject> list(String spaceBid, String spaceAppBid, String userNO, QueryCondition queryCondition) {
        //判断列表权限是否有实例角色
        CheckPermissionResultVo permissionResultVo = permissionRuleConditionService.getPermissionSql(spaceAppBid, OperatorEnum.LIST.getCode());
        if (Boolean.FALSE.equals(permissionResultVo.getCheckResult())) {
            return Lists.newArrayList();
        }
        if (StringUtils.isNotEmpty(permissionResultVo.getSplString())) {
            queryCondition.setAdditionalSql(permissionResultVo.getSplString());
        }
        ApmSpaceApp spaceApp = apmSpaceApplicationService.getSpaceAppByBid(spaceAppBid);
        //如没有找到对应的应用，则视为在查询关系列表，不做权限控制
        if (spaceApp == null) {
            return objectModelCrudI.list(spaceAppBid, queryCondition);
        } else {
            return objectModelCrudI.list(spaceApp.getModelCode(), queryCondition);
        }
    }

    @Override
    public <T extends MObject> PagedResult<T> page(String spaceBid, String spaceAppBid, BaseRequest<QueryCondition> pageQo, boolean filterRichText) {
        return this.page(spaceBid, spaceAppBid, SsoHelper.getJobNumber(), pageQo, filterRichText);
    }

    /**
     * 组织私有角色SQL
     *
     * @param spaceAppBid 应用BID
     * @param userNO      用户工号
     * @param roleBids    角色BID
     * @return
     */
    private String getInstanceCodeSql(String spaceAppBid, String userNO, List<String> roleBids) {
        StringBuffer bids = new StringBuffer();
        for (int i = 0; i < roleBids.size(); i++) {
            String roleBid = roleBids.get(i);
            if (i > 0) {
                bids.append(",");
            }
            bids.append("'").append(roleBid).append("'");
        }
        StringBuffer sql = new StringBuffer();
        sql.append("EXISTS (\n").append("select 1 from apm_flow_instance_role_user d where a.bid = d.instance_bid and a.permission_Bid ='").append(CommonConst.APP_FLAG).append(spaceAppBid).append("'");
        sql.append("\n").append(" and d.user_no = '").append(userNO).append("'\n").append(" and d.space_app_bid = '").append(spaceAppBid).append("'\n");
        sql.append(" and d.role_bid in (").append(bids).append("))\n");
        return sql.toString();
    }

    @Override
    public <T extends MObject> PagedResult<T> page(String spaceBid, String spaceAppBid, String userNO, BaseRequest<QueryCondition> pageQo, boolean filterRichText) {
        //判断列表权限是否有实例角色
        CheckPermissionResultVo permissionResultVo = permissionRuleConditionService.getPermissionSql(spaceAppBid, OperatorEnum.LIST.getCode());
        if (Boolean.FALSE.equals(permissionResultVo.getCheckResult())) {
            return PageResultTools.createEmpty();
        }
        ApmSpaceApp spaceApp = apmSpaceApplicationService.getSpaceAppByBid(spaceAppBid);
        if (spaceApp == null) {
            return objectModelCrudI.page(spaceAppBid, pageQo, filterRichText);
        } else {
            if (StringUtils.isNotEmpty(permissionResultVo.getSplString())) {
                // 拼接加密权限
                String secrecyWrapperAndPermissionForStringSql = SecrecyWrapperHandler
                        .getSecrecyWrapperAndPermissionFilterModelForStringSql(SsoHelper.getJobNumber(), spaceApp.getModelCode(), permissionResultVo.getSplString());
                pageQo.getParam().setAdditionalSql(secrecyWrapperAndPermissionForStringSql);
            }

            return objectModelCrudI.page(spaceApp.getModelCode(), pageQo, filterRichText);
        }
    }


    @Override
    public <T extends MObject> PagedResult<T> pageWithoutPermission(String spaceBid, String spaceAppBid, BaseRequest<QueryCondition> pageQo, boolean filterRichText) {
        ApmSpaceApp spaceApp = apmSpaceApplicationService.getSpaceAppByBid(spaceAppBid);
        //如没有找到对应的应用，则视为传入的参数为modelCode，不做权限控制
        if (spaceApp == null) {
            return objectModelCrudI.page(spaceAppBid, pageQo, filterRichText);
        } else {
            return objectModelCrudI.page(spaceApp.getModelCode(), pageQo, filterRichText);
        }
    }

    @NotNull
    private String getRoleCodeStr(String spaceBid, String userNO) {
        List<String> roleCodes = getRoleCodeList(spaceBid, userNO);
        if (CollectionUtils.isEmpty(roleCodes)) {
            throw new BusinessException("用户在此空间下没有任何角色，请联系管理员分配角色后再试！");
        }
        StringBuilder roleCodeStr = new StringBuilder();
        roleCodes.forEach(roleCode ->
                roleCodeStr.append("'").append(roleCode).append("'").append(",")
        );
        roleCodeStr.deleteCharAt(roleCodeStr.length() - 1);
        return roleCodeStr.toString();
    }

    private List<String> getRoleCodeList(String spaceBid, String userNO) {
        List<String> result = Lists.newArrayList();

        // 补充系统用户全局角色
        List<String> sysRoleCodeList = sysRoleDomainService.listSysRoleCode(userNO);
        if (CollectionUtils.isNotEmpty(sysRoleCodeList)) {
            result.addAll(sysRoleCodeList);
        }
        ApmSphere apmSphere = apmSphereService.getByBizBidAndType(spaceBid, TypeEnum.SPACE.getCode());
        if (apmSphere != null) {
            List<ApmRole> apmRoles = apmRoleDomainService.listRoleByJobNumAndSphereBid(userNO, apmSphere.getBid());
            if (CollectionUtils.isNotEmpty(apmRoles)) {
                // 如果在该空间下有任意非"ALL"角色，则补充空间角色
                if (apmRoles.stream().anyMatch(role -> apmSphere.getBid().equals(role.getSphereBid()) && !RoleConstant.ALL.equals(role.getCode()))) {
                    result.add(RoleConstant.SPACE_MEMBER_EN);
                }
                List<String> roleCodes = apmRoles.stream().map(ApmRole::getCode).collect(Collectors.toList());
                result.addAll(roleCodes);
                return result;
            }
        }
        return result;
    }

    private void checkPermission(String modelCode, String bid, String operatorCode) {
        MObject oldSpaceAppData = objectModelCrudI.getByBid(modelCode, bid);
        checkPermission(oldSpaceAppData, operatorCode);
    }

    @Override
    public <T extends MBaseData> void checkPermission(T appData, String operatorCode) {
        if (appData == null) {
            return;
        }
        String userNO = SsoHelper.getJobNumber();
        String spaceBid = null;
        if (appData.get(RelationEnum.SPACE_BID.getCode()) != null) {
            spaceBid = appData.get(RelationEnum.SPACE_BID.getCode()).toString();
        }
        String spaceAppBid = null;
        if (appData.get(RelationEnum.SPACE_APP_BID.getCode()) != null) {
            spaceAppBid = appData.get(RelationEnum.SPACE_APP_BID.getCode()).toString();
        }
        if (StringUtils.isEmpty(spaceBid)) {
            if (StringUtils.isNotEmpty(spaceAppBid)) {
                ApmSpaceApp spaceApp = apmSpaceAppService.getByBid(spaceAppBid);
                spaceBid = spaceApp.getSpaceBid();
            }
        }
        List<String> roleCodes = getRoleCodeList(spaceBid, userNO);
        String permissionBid = null;
        if (appData.get(ObjectEnum.PERMISSION_BID.getCode()) != null) {
            permissionBid = appData.get(ObjectEnum.PERMISSION_BID.getCode()).toString();
        }
        //如果有流程 需要判断流程里面的角色
        //查询流程角色
        List<String> flowCodes = permissionConfigService.getFlowInstanceRoleCodes(userNO, appData.getBid(), spaceAppBid);
        if (CollectionUtils.isNotEmpty(flowCodes)) {
            for (String flowCode : flowCodes) {
                roleCodes.add(CommonConst.PRI_KEY + flowCode);
            }
        }
        permissionCheckService.getInnerRole(appData, userNO, roleCodes);
        boolean hasPermission = permissionConfigService.checkPermission(roleCodes, permissionBid, operatorCode);
        if (!hasPermission) {
            throw new BusinessException("没有相关权限");
        }
    }

    @Override
    public Boolean checkButtonPermission(MObject appData, String operatorCode) {
        if (appData == null) {
            return Boolean.FALSE;
        }
        String userNO = SsoHelper.getJobNumber();
        String spaceBid = null;
        if (appData.get(RelationEnum.SPACE_BID.getCode()) != null) {
            spaceBid = appData.get(RelationEnum.SPACE_BID.getCode()).toString();
        }
        String spaceAppBid = null;
        if (appData.get(RelationEnum.SPACE_APP_BID.getCode()) != null) {
            spaceAppBid = appData.get(RelationEnum.SPACE_APP_BID.getCode()).toString();
        }
        if (StringUtils.isEmpty(spaceBid)) {
            if (StringUtils.isNotEmpty(spaceAppBid)) {
                ApmSpaceApp spaceApp = apmSpaceAppService.getByBid(spaceAppBid);
                spaceBid = spaceApp.getSpaceBid();
            }
        }
        List<String> roleCodes = getRoleCodeList(spaceBid, userNO);
        if (CollectionUtil.isEmpty(roleCodes)) {
            return Boolean.FALSE;
        }

        String permissionBid = null;
        if (appData.get(ObjectEnum.PERMISSION_BID.getCode()) != null) {
            permissionBid = appData.get(ObjectEnum.PERMISSION_BID.getCode()).toString();
        }
        //如果有流程 需要判断流程里面的角色
        //查询流程角色
        List<String> flowCodes = permissionConfigService.getFlowInstanceRoleCodes(userNO, appData.getBid(), spaceAppBid);
        if (CollectionUtils.isNotEmpty(flowCodes)) {
            for (String flowCode : flowCodes) {
                roleCodes.add(CommonConst.PRI_KEY + flowCode);
            }
        }
        permissionCheckService.getInnerRole(appData, userNO, roleCodes);
        boolean hasPermission = permissionConfigService.checkPermission(spaceAppBid, appData, roleCodes, operatorCode);

        return hasPermission;
    }

    @Override
    public <T extends MObject> Boolean updateByBid(String modelCode, String bid, T appData) {
        if (appData.get(PermissionCheckEnum.CHECK_PERMISSION.getCode()) == null || (Boolean) appData.get(PermissionCheckEnum.CHECK_PERMISSION.getCode())) {
            //checkPermission(modelCode,bid,OperatorEnum.EDIT.getCode());
            //判断是否有编辑权限
            PermissionCheckDto permissionCheckDto = new PermissionCheckDto();
            permissionCheckDto.setSpaceBid(appData.get(RelationEnum.SPACE_BID.getCode()).toString());
            permissionCheckDto.setSpaceAppBid(appData.get(RelationEnum.SPACE_APP_BID.getCode()).toString());
            permissionCheckDto.setInstanceBid(bid);
            permissionCheckDto.setOperatorCode(OperatorEnum.EDIT.getCode());
            if (!ApmImplicitParameter.isSkipCheckPermission()
                    && !permissionCheckService.checkSpaceAppPermssion(permissionCheckDto)) {
                throw new BusinessException("没有编辑权限");
            }
        }
        //fillPermissionBid(appData);
        return objectModelCrudI.updateByBid(modelCode, bid, appData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MSpaceAppData promoteVersionObject(String spaceBid, String appBid, LifeCyclePromoteDto promoteDto) {
        return updatePermission(spaceBid, appBid, versionModelDomainService.promote(promoteDto));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MSpaceAppData promote(String spaceBid, String appBid, LifeCyclePromoteDto promoteDto) {
        return updatePermission(spaceBid, appBid, objectModelCrudI.promote(promoteDto));
    }

    @NotNull
    private MSpaceAppData updatePermission(String spaceBid, String appBid, MVersionObject mVersionObject) {
        MSpaceAppData appData = MSpaceAppData.buildFrom(mVersionObject, spaceBid, appBid);
        String permissionBid = permissionRuleService.getPermissionBidByAppData(appData);
        appData.setPermissionBid(permissionBid);
        objectModelCrudI.updateByBid(appData.getModelCode(), appData.getBid(), appData);
        return appData;
    }
}
