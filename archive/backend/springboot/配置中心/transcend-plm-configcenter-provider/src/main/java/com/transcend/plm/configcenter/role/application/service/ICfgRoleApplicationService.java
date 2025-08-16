package com.transcend.plm.configcenter.role.application.service;

import com.transcend.plm.configcenter.api.model.role.vo.CfgRoleVo;
import com.transcend.plm.configcenter.role.pojo.dto.CfgRoleDto;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author jie.luo1
 * @Version 1.0
 * @Date 2023-02-22 10:34
 **/
public interface ICfgRoleApplicationService {
    /**
     * 保存或新增基础属性
     * @param cfgAttributeDto
     * @return
     */
    CfgRoleVo saveOrUpdate(CfgRoleDto cfgAttributeDto);

}
