package com.transcend.plm.datadriven.apm.permission.service;

import com.transcend.plm.datadriven.apm.permission.pojo.vo.CheckPermissionResultVo;

/**
 * @author unknown
 */
public interface IPermissionRuleConditionService {


    CheckPermissionResultVo getPermissionSql(String spaceAppBid , String operatorCode);

}
