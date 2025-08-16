package com.transcend.plm.datadriven.apm.permission.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.api.model.config.TableDefinition;
import com.transcend.plm.datadriven.api.model.qo.ModelFilterQo;
import com.transcend.plm.datadriven.apm.enums.InnerRoleEnum;
import com.transcend.plm.datadriven.apm.enums.TypeEnum;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleVO;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.AppConditionPermissionVo;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.CheckPermissionResultVo;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.PermissionOperationItemVo;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmSphere;
import com.transcend.plm.datadriven.apm.permission.repository.entity.PermissionPlmRuleCondition;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmSphereService;
import com.transcend.plm.datadriven.apm.permission.service.IPermissionConfigService;
import com.transcend.plm.datadriven.apm.permission.service.IPermissionRuleConditionService;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.space.service.ApmSpaceApplicationService;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transcend.plm.datadriven.common.tool.ModelFilterQoTools;
import com.transcend.plm.datadriven.common.tool.ObjectTools;
import com.transcend.plm.datadriven.common.tool.QueryConveterTool;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author unknown
 */
@Service
@Slf4j
public class PermissionRuleConditionService implements IPermissionRuleConditionService {
    @Resource
    private IPermissionConfigService permissionConfigService;
    @Value("${permission.rule.default:DEFAULT:6666666666666}")
    @Getter
    @Setter
    private String defaultPermissionBid;
    private static final String PERMISSION_PRIVATE_PREFIX = "PRI:";
    @Resource
    private IApmSpaceAppDataDrivenService apmSpaceAppDataDrivenService;
    @Resource
    private ApmSpaceApplicationService apmSpaceApplicationService;

    @Resource
    private ApmRoleService apmRoleService;

    @Resource
    private ApmSphereService apmSphereService;

    @Resource
    private ApmSpaceAppService apmSpaceAppService;



    private static final String CREATER_SQL = "a.created_by = '%s' ";

    private static final String RESPONSIBLE_SQL = "a.handler like '%%%s%%' ";

    private static final String TECHNICAL_DIRECTOR_SQL = "a.person_responsible like '%%%s%%' ";

    private static final String UX_AGENT_SQL = "a.ux_score like '%%%s%%' ";

    private static final String FOLLOW_SQL = "a.follow_member like '%%%s%%' ";







    private static boolean jsonEqualOrContains(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) {
            return true;
        }
        if (obj1 == null || obj2 == null) {
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

    @Override
    public CheckPermissionResultVo getPermissionSql(String spaceAppBid, String operatorCode) {
        String jobNumber = SsoHelper.getJobNumber();
        if (StringUtils.isAnyBlank(jobNumber, spaceAppBid, operatorCode)) {
            return CheckPermissionResultVo.success();
        }
        ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(spaceAppBid);
        //查询用户当前角色
        List<String> roleCodeList = apmRoleService.getRoleCodeList(apmSpaceApp.getSpaceBid(), jobNumber);
        //查询属性权限规则
        List<AppConditionPermissionVo> appConditionPermissionVos = permissionConfigService.listAppConditionPermission(spaceAppBid);
        if (CollectionUtils.isEmpty(appConditionPermissionVos)) {
            return CheckPermissionResultVo.fail();
        }
        List<String> sqlStringList = new ArrayList<>();
        for (AppConditionPermissionVo appConditionPermissionVo : appConditionPermissionVos) {
            CheckPermissionResultVo checkResultVo = getPermissionRuleSqlString(apmSpaceApp, operatorCode, appConditionPermissionVo, roleCodeList, jobNumber);
            if (Boolean.TRUE.equals(checkResultVo.getCheckResult())) {
                return CheckPermissionResultVo.success();
            } else if (StringUtils.isNotBlank(checkResultVo.getSplString())) {
                sqlStringList.add(checkResultVo.getSplString());
            }
        }
        if (CollectionUtils.isNotEmpty(sqlStringList)) {
            return CheckPermissionResultVo.sqlString(sqlStringList.stream().collect(Collectors.joining(" or ", "( "," )")));
        } else {
            return CheckPermissionResultVo.fail();
        }
    }

    @NotNull
    private CheckPermissionResultVo getPermissionRuleSqlString(ApmSpaceApp apmSpaceApp, String operatorCode, AppConditionPermissionVo appConditionPermissionVo, List<String> roleCodeList, String jobNumber) {
        //角色条件
        List<PermissionOperationItemVo> appPermissionOperationList = appConditionPermissionVo.getAppPermissionOperationList();
        List<PermissionOperationItemVo> operationItemVos = appPermissionOperationList.stream().filter(v -> v.getOperatorList().contains(operatorCode)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(operationItemVos)){
            //没有操作权限
            return CheckPermissionResultVo.fail();
        }
        StringBuilder sqlStringBuilder = new StringBuilder();

        if (operationItemVos.stream().noneMatch(v-> roleCodeList.contains(v.getRoleCode()))){
            //实例角色条件(创建着，责任人，流程角色)
            String roleSqlString = getRoleSqlString(apmSpaceApp, jobNumber, operationItemVos);
            if (StringUtils.isBlank(roleSqlString)) {
                return CheckPermissionResultVo.fail();
            }
            sqlStringBuilder.append(roleSqlString);
        }
        List<PermissionPlmRuleCondition> permissionPlmRuleConditionList = appConditionPermissionVo.getPermissionPlmRuleConditionList();
        if (CollectionUtils.isNotEmpty(permissionPlmRuleConditionList)) {
            //属性条件
            String attrSqlString = getAttrSqlString(apmSpaceApp, permissionPlmRuleConditionList);
            if (sqlStringBuilder.length() > 0){
                sqlStringBuilder.append(" and ");
            }
            sqlStringBuilder.append(attrSqlString);
        }
        if (sqlStringBuilder.length() > 0){
            sqlStringBuilder.insert(0, " ( ");
            sqlStringBuilder.append( " ) ");
        }
        return sqlStringBuilder.length() > 0 ? CheckPermissionResultVo.sqlString(sqlStringBuilder.toString()) : CheckPermissionResultVo.success();
    }

    @NotNull
    private String getAttrSqlString(ApmSpaceApp apmSpaceApp, List<PermissionPlmRuleCondition> permissionPlmRuleConditionList) {
        List<ModelFilterQo> queries = permissionPlmRuleConditionList.stream().map(v -> {
            ModelFilterQo modelFilterQo = new ModelFilterQo();
            modelFilterQo.setProperty(v.getAttrCode());
            modelFilterQo.setCondition(v.getOperator());
            modelFilterQo.setValue(v.getAttrCodeValue());
            modelFilterQo.setType(v.getType());
            return modelFilterQo;
        }).collect(Collectors.toList());
        ModelFilterQoTools.analysis(queries);
        List<QueryWrapper> queryWrappers = QueryConveterTool.convert(queries, false);

        TableDefinition table = ObjectTools.fillTableDefinition(apmSpaceApp.getModelCode());
        return QueryConveterTool.getSqlString(table, queryWrappers);
    }

    private String getRoleSqlString(ApmSpaceApp apmSpaceApp, String jobNumber, List<PermissionOperationItemVo> operationItemVos) {
        StringBuilder roleSqlString = new StringBuilder();
        //判断是否有创建者角色
        if (operationItemVos.stream().anyMatch(v->InnerRoleEnum.CREATER.getCode().equals(v.getRoleCode()))){
            roleSqlString = new StringBuilder(String.format(CREATER_SQL, jobNumber));
        }
        //判断是否有责任人角色
        if (operationItemVos.stream().anyMatch(v->InnerRoleEnum.PERSON_RESPONSIBLE.getCode().equals(v.getRoleCode()))){
            if (roleSqlString.length() > 0){
                roleSqlString.append(" or ");
            }
            roleSqlString.append(String.format(RESPONSIBLE_SQL, jobNumber));
        }

        //判断是否有技术负责人角色
        if (operationItemVos.stream().anyMatch(v->InnerRoleEnum.TECHNICAL_DIRECTOR.getCode().equals(v.getRoleCode()))){
            if (roleSqlString.length() > 0){
                roleSqlString.append(" or ");
            }
            roleSqlString.append(String.format(TECHNICAL_DIRECTOR_SQL, jobNumber));
        }
        //判断是否有UX代表角色
        if (operationItemVos.stream().anyMatch(v->InnerRoleEnum.UX_AGENT.getCode().equals(v.getRoleCode()))){
            if (roleSqlString.length() > 0){
                roleSqlString.append(" or ");
            }
            roleSqlString.append(String.format(UX_AGENT_SQL, jobNumber));
        }

        //判断是否关注人角色
        if (operationItemVos.stream().anyMatch(v->InnerRoleEnum.FOLLOW_MEMBER.getCode().equals(v.getRoleCode()))){
            if (roleSqlString.length() > 0){
                roleSqlString.append(" or ");
            }
            roleSqlString.append(String.format(FOLLOW_SQL, jobNumber));
        }

        //判断是否有流程角色
        List<String> insanceRoleCodes = operationItemVos.stream().filter(v -> v.getRoleType() == 1).map(v->v.getRoleCode().substring(CommonConst.PRI_KEY.length())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(insanceRoleCodes)) {
            ApmSphere apmSphere = apmSphereService.getByBizBidAndType(apmSpaceApp.getBid(), TypeEnum.OBJECT.getCode());
            if (apmSphere != null) {
                List<ApmRoleVO> apmRoleVOS = apmRoleService.listByRoleBidsByCodes(insanceRoleCodes,apmSphere.getBid());
                if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(apmRoleVOS)) {
                    List<String> roleBids = apmRoleVOS.stream().map(ApmRoleVO::getBid).collect(Collectors.toList());
                    String instanceRoleCodeStr = getInstanceCodeSql (apmSpaceApp.getBid(), jobNumber,roleBids);
                    if (roleSqlString.length() > 0){
                        roleSqlString.append(" or ");
                    }
                    roleSqlString.append(instanceRoleCodeStr);
                }
            }
        }
        if (roleSqlString.length() > 0){
            roleSqlString.insert(0, " ( ");
            roleSqlString.append( " ) ");
        }
        return roleSqlString.toString();
    }

    /**
     * 组织私有角色SQL
     * @param spaceAppBid 应用BID
     * @param userNO 用户工号
     * @param roleBids 角色BID
     * @return
     */
    private String getInstanceCodeSql (String spaceAppBid, String userNO,List<String> roleBids) {
        StringBuffer bids = new StringBuffer();
        for (int i = 0;i < roleBids.size();i++) {
            String roleBid = roleBids.get(i);
            if(i > 0){
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

}
