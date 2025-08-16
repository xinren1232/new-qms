package com.transcend.plm.datadriven.apm.permission.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.transcend.plm.datadriven.api.model.BaseDataEnum;
import com.transcend.plm.datadriven.api.model.ObjectEnum;
import com.transcend.plm.datadriven.api.model.RelationEnum;
import com.transcend.plm.datadriven.api.model.config.TableAttributeDefinition;
import com.transcend.plm.datadriven.api.model.config.TableDefinition;
import com.transcend.plm.datadriven.apm.permission.enums.ConditionOperatorConstant;
import com.transcend.plm.datadriven.apm.permission.repository.entity.PermissionPlmRule;
import com.transcend.plm.datadriven.apm.permission.repository.entity.PermissionPlmRuleCondition;
import com.transcend.plm.datadriven.apm.permission.service.IPermissionConfigService;
import com.transcend.plm.datadriven.apm.permission.service.IPermissionRuleService;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.service.ApmSpaceApplicationService;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.apm.tools.UniversalComparator;
import com.transcend.plm.datadriven.common.tool.ObjectTools;
import com.transcend.plm.datadriven.common.util.TranscendObjectUtil;
import com.transsion.framework.exception.BusinessException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author unknown
 */
@Service
@Slf4j
public class PermissionRuleService implements IPermissionRuleService {
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

    @Override
    public String getPermissionBidByAppData(MSpaceAppData appData) {
        if (appData == null || TranscendObjectUtil.isFiledEmpty(appData, RelationEnum.SPACE_APP_BID.getCode())) {
            return defaultPermissionBid;
        }
        ApmSpaceApp spaceApp = apmSpaceApplicationService.getSpaceAppByBid(appData.getSpaceAppBid());
        if (spaceApp == null) {
            throw new BusinessException("应用不存在");
        }
        if (StringUtils.isBlank(appData.getModelCode())) {
            appData.setModelCode(spaceApp.getModelCode());
        }
        List<PermissionPlmRuleCondition> ruleConditions = permissionConfigService.getPermissionPlmRuleConditionByAppBid(appData.getSpaceAppBid());
        String permissionBid = null;
        if (!TranscendObjectUtil.isFiledEmpty(appData, BaseDataEnum.BID.getCode())) {
            MSpaceAppData oldData = apmSpaceAppDataDrivenService.get(appData.getSpaceAppBid(), appData.getBid(),false);
            String oldPermissionBid = null;
            if (oldData != null && !TranscendObjectUtil.isFiledEmpty(oldData, ObjectEnum.PERMISSION_BID.getCode())) {
                oldPermissionBid = oldData.getPermissionBid();
                oldData.putAll(appData);
            }
            if (oldPermissionBid != null && oldPermissionBid.startsWith(PERMISSION_PRIVATE_PREFIX)) {
                return oldPermissionBid;
            }
            permissionBid = matchConditionPermission(oldData, ruleConditions);
        } else {
            permissionBid = matchConditionPermission(appData, ruleConditions);
        }
        if (StringUtils.isNotBlank(permissionBid)) {
            return permissionBid;
        }
        PermissionPlmRule rule = permissionConfigService.getPermissionPlmRuleByAppBid(appData.getSpaceAppBid());
        if (rule != null) {
            return rule.getBid();
        }
        return defaultPermissionBid;
    }

    private static String matchConditionPermission(MSpaceAppData appData, List<PermissionPlmRuleCondition> ruleConditions) {
        if (appData == null || MapUtils.isEmpty(appData)) {
            return null;
        }
        boolean matched = false;
        String permissionBid = null;
        //获取对象表的元数据
        TableDefinition tableDefinition = ObjectTools.fillTableDefinition(appData.getModelCode());
        Set<String> jsonFiledSet = tableDefinition.getTableAttributeDefinitions().stream().filter(tableAttributeDefinition -> "json".equals(tableAttributeDefinition.getType()))
                .map(TableAttributeDefinition::getProperty).collect(Collectors.toSet());
        UniversalComparator universalComparator = new UniversalComparator();
        for (PermissionPlmRuleCondition ruleCondition : ruleConditions) {
            String attrCode = ruleCondition.getAttrCode();
            Object attrValue = ruleCondition.getAttrCodeValue();
            if (StringUtils.isBlank(attrCode) || ObjectUtils.isEmpty(attrValue)) {
                continue;
            }
            switch (ruleCondition.getOperator()) {
                case ConditionOperatorConstant.EQUAL:
                    matched = attrValue.equals(appData.get(attrCode)) || (jsonFiledSet.contains(attrCode) && jsonEqualOrContains(appData.get(attrCode), attrValue));
                    if(matched){
                        permissionBid = ruleCondition.getPermissionBid();
                    }
                    break;
                case ConditionOperatorConstant.NOT_EQUAL:
                    if (!attrValue.equals(appData.get(attrCode))) {
                        matched = true;
                        permissionBid = ruleCondition.getPermissionBid();
                    }
                    break;
                case ConditionOperatorConstant.GREATER_THAN:
                    if (universalComparator.compare(appData.get(attrCode), attrValue) > 0) {
                        matched = true;
                        permissionBid = ruleCondition.getPermissionBid();
                    }
                    break;
                case ConditionOperatorConstant.GREATER_THAN_OR_EQUAL:
                    if (universalComparator.compare(appData.get(attrCode), attrValue) >= 0) {
                        matched = true;
                        permissionBid = ruleCondition.getPermissionBid();
                    }
                    break;
                case ConditionOperatorConstant.LESS_THAN:
                    if (universalComparator.compare(appData.get(attrCode), attrValue) < 0) {
                        matched = true;
                        permissionBid = ruleCondition.getPermissionBid();
                    }
                    break;
                case ConditionOperatorConstant.LESS_THAN_OR_EQUAL:
                    if (universalComparator.compare(appData.get(attrCode), attrValue) <= 0) {
                        matched = true;
                        permissionBid = ruleCondition.getPermissionBid();
                    }
                    break;
                default:
                    break;
            }
            if (matched) {
                break;
            }
        }
        if (matched) {
            return permissionBid;
        }
        log.error("No matching rule found for the given app data,appBid:{}", appData.getSpaceAppBid());
        return null;
    }

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

}
