package com.transcend.plm.configcenter.permission.application.service;


import com.transcend.plm.configcenter.permission.infrastructure.repository.dto.PermissionResult;
import com.transcend.plm.configcenter.permission.infrastructure.repository.po.PermissionPlmAddRuleItem;
import com.transcend.plm.configcenter.permission.infrastructure.repository.po.PermissionPlmListRuleItem;
import com.transcend.plm.configcenter.permission.infrastructure.repository.po.PermissionPlmOperationRuleItem;
import com.transcend.plm.configcenter.permission.infrastructure.repository.po.PermissionPlmRule;
import com.transcend.plm.configcenter.permission.pojo.dto.AppPermissionDto;
import com.transcend.plm.configcenter.permission.pojo.vo.ObjPermissionVo;

import java.util.List;
import java.util.Map;

public interface IPermissionConfigService {
    boolean saveOrUpdateBasePermissionConfig(AppPermissionDto appPermissionDto);

    boolean deleteByPermissionBid(String modelCode, String permissionBid);

    boolean deletePermission(AppPermissionDto appPermissionDto);

    ObjPermissionVo listObjPermissions(String modelCode);


    /**
     * 根据对象CODE下发对象所有权限
     *
     * @param modelCode 对象CODE
     * @return {@link Boolean}
     * @date 2024/5/11 9:51
     * @author quan.cheng
     */
    Boolean distributePermission(String modelCode);

    /**
     * 批量穿透所有对象权限到空间应用
     *
     * @param modelCodes 对象CODE集合
     * @return {@link Boolean}
     * @date 2024/5/13 18:15
     * @author quan.cheng
     */

    Boolean distributePermissions(List<String> modelCodes);

    PermissionResult delPerByCodeAndGetRes(String modelCode);
    PermissionResult delPerByCodeAndGetRes(List<String> modelCodes);


    /**
     * 根据对象Code集合查询出所有对象权限集合
     *
     * @param modelCodes      对象code集合
     * @param parentModelCode
     * @return {@link PermissionResult}
     * @date 2024/5/13 14:44
     * @author quan.cheng
     */
    PermissionResult getObjPerByMc(List<String> modelCodes, String parentModelCode);

    /**
     * 批量保存权限相关数据
     * @param permissionPlmRuleNews 权限规则
     * @param permissionPlmAddRuleItemNews 新增权限规则
     * @param permissionPlmListRuleItemNews 列表权限规则
     * @param permissionPlmOperationRuleItemNews 操作权限
     * @return boolean
     */
    boolean saveBathPermissionData(List<PermissionPlmRule> permissionPlmRuleNews, List<PermissionPlmAddRuleItem> permissionPlmAddRuleItemNews, List<PermissionPlmListRuleItem> permissionPlmListRuleItemNews, List<PermissionPlmOperationRuleItem> permissionPlmOperationRuleItemNews);

    /**
     * 下发对象权限到其所有子对象中
     *
     * @param modelCode 对象CODE
     * @return {@link Boolean}
     * @date 2024/5/13 13:36
     * @author quan.cheng
     */
    Boolean distributeObjectPermission(String modelCode);

    /**
     * 查询对象下所有按钮
     * @param modelCode
     * @return
     */
    List<Map<String,String>> queryAllOperationByModeCode(String modelCode);

    /**
     * 根据permissionBid 删除本身权限 不包括继承权限
     * @param permissionBid permissionBid
     */
    void delPermissionByPerBid(String permissionBid);
}
