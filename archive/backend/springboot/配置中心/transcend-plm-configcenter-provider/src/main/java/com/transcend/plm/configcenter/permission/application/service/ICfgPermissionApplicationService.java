package com.transcend.plm.configcenter.permission.application.service;

import com.transcend.plm.configcenter.permission.pojo.dto.CfgObjectPermissionOperationDto;
import com.transcend.plm.configcenter.permission.pojo.vo.CfgAttributeVo;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @author jie.luo <jie.luo1@transsion.com>
 * @Version 1.0
 * @Date 2023-02-22 10:34
 **/
public interface ICfgPermissionApplicationService {
    /**
     * 保存或新增基础属性
     * @param cfgAttributeDto
     * @return
     */
    CfgAttributeVo saveOrUpdate(CfgObjectPermissionOperationDto cfgAttributeDto);
}
