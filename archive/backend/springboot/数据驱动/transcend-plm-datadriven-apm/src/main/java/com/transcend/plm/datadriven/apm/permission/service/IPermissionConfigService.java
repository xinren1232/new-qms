package com.transcend.plm.datadriven.apm.permission.service;

import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.apm.permission.pojo.dto.AppPermissionDto;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmActionConfigUserVo;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.AppBasePermissionVo;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.AppConditionPermissionVo;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ConditionOperatorVo;
import com.transcend.plm.datadriven.apm.permission.repository.entity.*;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;

import java.util.List;

/**
 * @author unknown
 */
public interface IPermissionConfigService {
    /**
     * saveOrUpdateAppBasePermissionConfig
     *
     * @param appPermissionDto appPermissionDto
     * @return {@link boolean}
     */
    boolean saveOrUpdateAppBasePermissionConfig(AppPermissionDto appPermissionDto);

    /**
     * deleteConditionPermission
     *
     * @param spaceAppBid   spaceAppBid
     * @param permissionBid permissionBid
     * @return {@link boolean}
     */
    boolean deleteConditionPermission(String spaceAppBid, String permissionBid);

    /**
     * deleteInstancePermission
     *
     * @param spaceAppBid spaceAppBid
     * @param instanceBid instanceBid
     * @return {@link boolean}
     */
    boolean deleteInstancePermission(String spaceAppBid, String instanceBid);

    /**
     * saveAppConditionPermission
     *
     * @param appPermissionDto appPermissionDto
     * @return {@link boolean}
     */
    boolean saveAppConditionPermission(AppPermissionDto appPermissionDto);

    /**
     * 根据spaceAppBid获取权限规则条件
     *
     * @param spaceAppBid spaceAppBid
     * @return {@link List< AppConditionPermissionVo>}
     */
    List<PermissionPlmRuleCondition> getPermissionPlmRuleConditionByAppBid(String spaceAppBid);

    /**
     * saveOrUpdateInstancePermissionConfig
     *
     * @param appPermissionDto appPermissionDto
     * @return {@link boolean}
     */
    boolean saveOrUpdateInstancePermissionConfig(AppPermissionDto appPermissionDto);

    /**
     * 根据spaceAppBid获取基础权限
     *
     * @param spaceAppBid spaceAppBid
     * @return {@link AppBasePermissionVo}
     */
    PermissionPlmRule getPermissionPlmRuleByAppBid(String spaceAppBid);

    /**
     * 根据应用ID批量获取基础权限
     *
     * @param spaceAppBids 应用ID集合
     * @return {@link List< PermissionPlmRule>}
     * @date 2024/5/8 17:57
     * @author quan.cheng
     */
    List<PermissionPlmRule> getPermissionPlmRuleByAppBids(List<String> spaceAppBids);

    /**
     * getPermissionPlmAddRuleItemByAppBids
     *
     * @param spaceAppBids spaceAppBids
     * @return {@link List<PermissionPlmAddRuleItem>}
     */
    List<PermissionPlmAddRuleItem> getPermissionPlmAddRuleItemByAppBids(List<String> spaceAppBids);

    /**
     * getPermissionPlmListRuleItemByAppBids
     *
     * @param spaceAppBids spaceAppBids
     * @return {@link List<PermissionPlmListRuleItem>}
     */
    List<PermissionPlmListRuleItem> getPermissionPlmListRuleItemByAppBids(List<String> spaceAppBids);

    /**
     * getPermissionPlmOperationRuleItemByAppBids
     *
     * @param spaceAppBids spaceAppBids
     * @return {@link List<PermissionPlmOperationRuleItem>}
     */
    List<PermissionPlmOperationRuleItem> getPermissionPlmOperationRuleItemByAppBids(List<String> spaceAppBids);


    List<PermissionPlmRuleCondition> getPermissionPlmRuleConditionByAppBids(List<String> spaceAppBids);

    /**
     * listAppBasePermissions
     *
     * @param spaceAppBid spaceAppBid
     * @param instanceBid instanceBid
     * @return {@link AppBasePermissionVo}
     */
    AppBasePermissionVo listAppBasePermissions(String spaceAppBid, String instanceBid);

    /**
     * listAppConditionPermission
     *
     * @param spaceAppBid spaceAppBid
     * @return {@link List<AppConditionPermissionVo>}
     */
    List<AppConditionPermissionVo> listAppConditionPermission(String spaceAppBid);

    /**
     * 单个角色删除
     *
     * @param appPermissionDto appPermissionDto
     * @return {@link boolean}
     */
    boolean deletePermission(AppPermissionDto appPermissionDto);

    /**
     * 批量保存权限相关数据
     *
     * @param permissionPlmRuleNews              权限规则
     * @param permissionPlmAddRuleItemNews       新增权限规则
     * @param permissionPlmListRuleItemNews      列表权限规则
     * @param permissionPlmOperationRuleItemNews 操作权限
     * @return boolean
     */
    boolean saveBathPermissionData(List<PermissionPlmRule> permissionPlmRuleNews,
                                   List<PermissionPlmAddRuleItem> permissionPlmAddRuleItemNews,
                                   List<PermissionPlmListRuleItem> permissionPlmListRuleItemNews,
                                   List<PermissionPlmOperationRuleItem> permissionPlmOperationRuleItemNews,
                                   List<PermissionPlmRuleCondition> permissionPlmRuleConditionListNe);

    /**
     * 复制应用对象的权限到应用中
     *
     * @param apmSpaceApp 空间应用实体
     * @return {@link boolean}
     * @date 2024/5/10 11:33
     * @author quan.cheng
     */
    boolean copyPermissionByModelCode(ApmSpaceApp apmSpaceApp);

    /**
     * checkAddPermission
     *
     * @param spaceAppBid spaceAppBid
     * @param roleCodes   roleCodes
     * @return {@link boolean}
     */
    boolean checkAddPermission(String spaceAppBid, List<String> roleCodes);

    boolean checkPermission(String spaceAppBid,List<String> roleCodes, String operatorCode);

    boolean checkInstancePermission(MBaseData appData, String spaceAppBid, String operatorCode, List<String> roleCodes);

    /**
     * checkListPermission
     *
     * @param spaceAppBid spaceAppBid
     * @param roleCodes   roleCodes
     * @return {@link boolean}
     */
    boolean checkListPermission(String spaceAppBid, List<String> roleCodes);

    /**
     * getFlowInstanceRoleCodes
     *
     * @param userNo      userNo
     * @param instanceBid instanceBid
     * @param spaceAppBid spaceAppBid
     * @return {@link List<String>}
     */
    List<String> getFlowInstanceRoleCodes(String userNo, String instanceBid, String spaceAppBid);

    /**
     * checkPermission
     *
     * @param roleCodes     roleCodes
     * @param permissionBid permissionBid
     * @param operatorCode  operatorCode
     * @return {@link boolean}
     */
    boolean checkPermission(List<String> roleCodes, String permissionBid, String operatorCode);

    /**
     *  checkButtonPermission
     * @param roleCodes roleCodes
     * @param permissionBid permissionBid
     * @param operatorCode operatorCode
     * @return boolean
     */
    boolean checkButtonPermission(List<String> roleCodes, String permissionBid, String operatorCode);

    /**
     * 校验操作权限
     * @param spaceAppBid
     * @param data
     * @param roleCodes
     * @return
     */
    boolean checkPermission(String spaceAppBid, MBaseData data,List<String> roleCodes, String operatorCod);

    /**
     * checkPermissionList
     *
     * @param roleCodes      roleCodes
     * @param permissionBids permissionBids
     * @param operatorCode   operatorCode
     * @return {@link boolean}
     */
    boolean checkPermissionList(List<String> roleCodes, List<String> permissionBids, String operatorCode);

    /**
     * getConditionOperators
     *
     * @return {@link List<ConditionOperatorVo>}
     */
    List<ConditionOperatorVo> getConditionOperators();

    /**
     * 查询当前用户的角色的权限管理下的按钮
     *
     * @param spaceAppBid   spaceAppBid
     * @param instanceBid   instanceBid
     * @param mSpaceAppData mSpaceAppData
     * @return {@link List<ApmActionConfigUserVo>}
     */
    List<ApmActionConfigUserVo> listAppPermissions(String spaceAppBid, String instanceBid, MBaseData mSpaceAppData);

    /**
     * 通过实例数据获取当前用户的角色
     *
     * @param spaceAppBid spaceAppBid
     * @param data        MBaseData
     * @return {@link List<String>}
     */
    List<String> listRoleCodeByBaseData(String spaceAppBid, MBaseData data);

    void addInstanceRole(MBaseData data, String userNO, List<String> roleCodes);

    /**
     * 通过实例数据获取角色的按钮权限
     *
     * @param spaceAppBid spaceAppBid
     * @param data        MBaseData
     * @param roleCodes   roleCodes
     * @return {@link AppBasePermissionVo}
     */
    AppBasePermissionVo getInstancePermissionByBaseData(String spaceAppBid, MBaseData data, List<String> roleCodes);
}

