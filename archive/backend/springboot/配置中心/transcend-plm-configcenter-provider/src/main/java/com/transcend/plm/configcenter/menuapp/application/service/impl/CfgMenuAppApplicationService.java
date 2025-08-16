package com.transcend.plm.configcenter.menuapp.application.service.impl;

import com.transcend.plm.configcenter.menuapp.application.service.ICfgMenuAppApplicationService;
import com.transcend.plm.configcenter.menuapp.domain.service.CfgMenuAppDomainService;
import com.transcend.plm.configcenter.menuapp.pojo.dto.CfgMenuAppDto;
import com.transcend.plm.configcenter.menuapp.pojo.vo.CfgMenuAppVo;
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
public class CfgMenuAppApplicationService implements ICfgMenuAppApplicationService {
    @Resource
    private CfgMenuAppDomainService cfgMenuAppDomainService;
    /**
     * 保存或新增菜单应用
     * @param dto
     * @return
     */
    @Override
    public CfgMenuAppVo saveOrUpdate(CfgMenuAppDto dto) {
        Assert.notNull(dto,"CfgMenuApp is null");
        return StringUtil.isBlank(dto.getBid()) ? cfgMenuAppDomainService.save(dto) : cfgMenuAppDomainService.update(dto);
    }
}
