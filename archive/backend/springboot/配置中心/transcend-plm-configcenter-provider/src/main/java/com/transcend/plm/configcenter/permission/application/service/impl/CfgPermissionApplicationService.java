package com.transcend.plm.configcenter.permission.application.service.impl;

import com.transcend.plm.configcenter.permission.application.service.ICfgPermissionApplicationService;
import com.transcend.plm.configcenter.permission.domain.service.CfgPermissionDomainService;
import com.transcend.plm.configcenter.permission.pojo.dto.CfgObjectPermissionOperationDto;
import com.transcend.plm.configcenter.permission.pojo.vo.CfgAttributeVo;
import com.transsion.framework.common.StringUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @author jie.luo <jie.luo1@transsion.com>
 * @Version 1.0
 * @Date 2023-02-22 10:36
 **/
@Service
public class CfgPermissionApplicationService implements ICfgPermissionApplicationService {
    @Resource
    private CfgPermissionDomainService cfgPermissionDomainService;
    /**
     * 保存或新增基础属性
     * @param cfgAttributeDto
     * @return
     */
    @Override
    public CfgAttributeVo saveOrUpdate(CfgObjectPermissionOperationDto cfgAttributeDto) {
        Assert.notNull(cfgAttributeDto,"attribute is null");
        return StringUtil.isBlank(cfgAttributeDto.getBid()) ? cfgPermissionDomainService.save(cfgAttributeDto) : cfgPermissionDomainService.update(cfgAttributeDto);
    }
}
