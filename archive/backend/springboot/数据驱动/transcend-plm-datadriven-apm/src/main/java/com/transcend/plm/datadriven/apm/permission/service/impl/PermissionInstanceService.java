package com.transcend.plm.datadriven.apm.permission.service.impl;

import com.transcend.plm.datadriven.api.model.config.TableAttributeDefinition;
import com.transcend.plm.datadriven.api.model.config.TableDefinition;
import com.transcend.plm.datadriven.apm.permission.pojo.dto.AppPermissionDto;
import com.transcend.plm.datadriven.apm.permission.pojo.dto.PermissionInstanceDto;
import com.transcend.plm.datadriven.apm.permission.repository.entity.PermissionPlmRuleCondition;
import com.transcend.plm.datadriven.apm.permission.repository.mapper.PermissionInsanceMapper;
import com.transcend.plm.datadriven.apm.permission.service.IPermissionInstanceService;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.common.tool.ObjectTools;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author unknown
 */
@Service
public class PermissionInstanceService implements IPermissionInstanceService {
    @Resource
    private PermissionInsanceMapper permissionInsanceMapper;

    @Resource
    private ApmSpaceAppService apmSpaceAppService;

    /**
     *根据实例bid更新permissinoBid
     * @param spaceAppBid
     * @param instanceBid
     * @param permissionBid
     * @return
     */
    @Override
    public boolean updateInstanceByBid(String spaceAppBid,String instanceBid,String permissionBid){
        ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(spaceAppBid);
        TableDefinition table = ObjectTools.fillTableDefinition(apmSpaceApp.getModelCode());
        PermissionInstanceDto permissionInstanceDto = new PermissionInstanceDto();
        permissionInstanceDto.setPermissionBid(permissionBid);
        permissionInstanceDto.setWhereCondition("bid = '"+instanceBid+"' and space_app_bid = '"+spaceAppBid+"'");
       return permissionInsanceMapper.updatePermissionBid(table,permissionInstanceDto) > 0;
    }

    /**
     * 通过条件更新实例permissionBid
     * @param permissionBid
     * @param appPermissionDto
     * @return
     */
    @Override
    public boolean updateInstanceByCondition(String permissionBid, AppPermissionDto appPermissionDto){
        /*ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(appPermissionDto.getSpaceAppBid());
        TableDefinition table = ObjectTools.fillTableDefinition(apmSpaceApp.getModelCode());
        PermissionInstanceDto permissionInstanceDto = new PermissionInstanceDto();
        permissionInstanceDto.setPermissionBid(permissionBid);
        //条件说明 EQ等于，GT大于,LT小于，，NULL为空，NOT NULL非空，NOT LT大于等于，NOT GT 小于等于,NE不等于
        Map<String,String> conditionMap = getConditionMap();
        List<PermissionPlmRuleCondition> permissionPlmRuleConditionList = appPermissionDto.getPermissionPlmRuleConditionList();
        Map<String,String> columnNameMap = getColumnName(table.getTableAttributeDefinitions(),permissionPlmRuleCondition.getAttrCode());
        if(CollectionUtils.isNotEmpty(columnNameMap)){
            String columnName = columnNameMap.get("columnName");
            String type = columnNameMap.get("type");
            StringBuffer condition = new StringBuffer();
            if("json".equals(type) && "EQ".equals(permissionPlmRuleCondition.getOperator())){
                StringBuffer jsonCondition = new StringBuffer();
                jsonCondition.append(" (JSON_CONTAINS(").append(columnName).append(", JSON_ARRAY('").append(permissionPlmRuleCondition.getAttrCodeValue()).append("')) or "+columnName+" in ('"+permissionPlmRuleCondition.getAttrCodeValue()+"'))");
                condition.append(jsonCondition);
            }else if ("json".equals(type) && "NE".equals(permissionPlmRuleCondition.getOperator())){
                StringBuffer jsonCondition = new StringBuffer();
                jsonCondition.append(" (JSON_CONTAINS("+columnName+", JSON_ARRAY('").append(permissionPlmRuleCondition.getAttrCodeValue()).append("')) = 0 or "+columnName+" not in ('"+permissionPlmRuleCondition.getAttrCodeValue()+"'))");
                condition.append(jsonCondition);
            }else{
                condition.append(columnName).append(" ").append(conditionMap.get(permissionPlmRuleCondition.getOperator())).append(" ");
                if(StringUtils.isNotEmpty(permissionPlmRuleCondition.getAttrCodeValue())){
                    condition.append("'").append(permissionPlmRuleCondition.getAttrCodeValue()).append("'");
                }
            }
            condition.append(" and (permission_bid not like 'PRI:%' or permission_bid is null) and delete_flag = 0 and space_app_bid = '").append(appPermissionDto.getSpaceAppBid()).append("'");
            permissionInstanceDto.setWhereCondition(condition.toString());
            return permissionInsanceMapper.updatePermissionBid(table,permissionInstanceDto) > 0;
        }*/
        return true;
    }

    /**
     * 更新实例应用权限
     * @param permissionBid
     * @return
     */
    @Override
    public boolean updateAppPermission(String permissionBid, String spaceAppBid){
        ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(spaceAppBid);
        TableDefinition table = ObjectTools.fillTableDefinition(apmSpaceApp.getModelCode());
        PermissionInstanceDto permissionInstanceDto = new PermissionInstanceDto();
        permissionInstanceDto.setPermissionBid(permissionBid);
        StringBuffer condition = new StringBuffer();
        condition.append(" (permission_bid not like 'PRI:%' or permission_bid not like 'LC:%' or permission_bid is null) and delete_flag = 0 and space_app_bid = '").append(spaceAppBid).append("'");
        permissionInstanceDto.setWhereCondition(condition.toString());
        return permissionInsanceMapper.updatePermissionBid(table,permissionInstanceDto) > 0;
    }

    /**
     * 将旧的权限bid更新成新的权限bid
     * @param spaceAppBid
     * @param oldPermissionBid
     * @param newPermissionBid
     * @return
     */
    @Override
    public boolean updateOldPermissionBidToNewBid(String spaceAppBid,String oldPermissionBid,String newPermissionBid){
        ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(spaceAppBid);
        TableDefinition table = ObjectTools.fillTableDefinition(apmSpaceApp.getModelCode());
        PermissionInstanceDto permissionInstanceDto = new PermissionInstanceDto();
        permissionInstanceDto.setPermissionBid(newPermissionBid);
        permissionInstanceDto.setWhereCondition("permission_bid = '"+oldPermissionBid+"' and delete_flag = 0 and space_app_bid = '"+spaceAppBid+"'");
        return permissionInsanceMapper.updatePermissionBid(table,permissionInstanceDto) > 0;
    }

    private Map<String,String> getColumnName(List<TableAttributeDefinition> tableAttributeDefinitions,String property){
        for(TableAttributeDefinition tableAttributeDefinition : tableAttributeDefinitions){
            if(tableAttributeDefinition.getProperty().equals(property)){
                Map<String,String> map = new HashMap<>(16);
                map.put("columnName",tableAttributeDefinition.getColumnName());
                map.put("type",tableAttributeDefinition.getType());
                return map;
            }
        }
        return null;
    }


    private Map<String,String> getConditionMap() {
        Map<String, String> conditionMap = new HashMap<>(16);
        //条件说明 EQ等于，GT大于,LT小于，，NULL为空，NOT NULL非空，NOT LT大于等于，NOT GT 小于等于
        conditionMap.put("EQ", "=");
        conditionMap.put("GT", ">");
        conditionMap.put("GE", ">=");
        conditionMap.put("NE", "<>");
        conditionMap.put("LT", "<");
        conditionMap.put("LE", "<=");
        conditionMap.put("NULL", "IS NULL");
        conditionMap.put("NOT NULL", "IS NOT NULL");
        conditionMap.put("NOT LT", ">=");
        conditionMap.put("NOT GT", "<=");
        return conditionMap;
    }
}
