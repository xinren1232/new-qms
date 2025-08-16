package com.transcend.plm.configcenter.role.application.service.impl;

import com.transcend.plm.configcenter.api.model.role.vo.CfgRoleVo;
import com.transcend.plm.configcenter.role.application.service.ICfgRoleApplicationService;
import com.transcend.plm.configcenter.role.domain.service.CfgRoleDomainService;
import com.transcend.plm.configcenter.role.pojo.dto.CfgRoleDto;
import com.transsion.framework.common.StringUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author jie.luo1
 * @Version 1.0
 * @Date 2023-02-22 10:36
 **/
@Service
public class CfgRoleApplicationService implements ICfgRoleApplicationService {
    @Resource
    private CfgRoleDomainService cfgRoleDomainService;

    /**
     * 保存或新增基础属性
     *
     * @param cfgAttributeDto
     * @return
     */
    @Override
    public CfgRoleVo saveOrUpdate(CfgRoleDto cfgAttributeDto) {
        Assert.notNull(cfgAttributeDto, "attribute is null");
        return StringUtil.isBlank(cfgAttributeDto.getBid()) ? cfgRoleDomainService.save(cfgAttributeDto) : cfgRoleDomainService.update(cfgAttributeDto);
    }
}
