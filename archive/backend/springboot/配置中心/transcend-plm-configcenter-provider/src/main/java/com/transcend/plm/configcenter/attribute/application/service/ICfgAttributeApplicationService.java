package com.transcend.plm.configcenter.attribute.application.service;

import com.transcend.plm.configcenter.attribute.pojo.dto.CfgAttributeDto;
import com.transcend.plm.configcenter.attribute.pojo.vo.CfgAttributeVo;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:34
 **/
public interface ICfgAttributeApplicationService {
    /**
     * 保存或新增基础属性
     * @param cfgAttributeDto
     * @return
     */
    CfgAttributeVo saveOrUpdate(CfgAttributeDto cfgAttributeDto);
}
