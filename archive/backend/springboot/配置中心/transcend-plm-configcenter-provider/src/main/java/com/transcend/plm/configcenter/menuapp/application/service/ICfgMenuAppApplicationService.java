package com.transcend.plm.configcenter.menuapp.application.service;

import com.transcend.plm.configcenter.menuapp.pojo.dto.CfgMenuAppDto;
import com.transcend.plm.configcenter.menuapp.pojo.vo.CfgMenuAppVo;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author jie.luo1
 * @Version 1.0
 * @Date 2023-02-22 10:34
 **/
public interface ICfgMenuAppApplicationService {
    /**
     * 保存或新增基础属性
     * @param cfgAttributeDto
     * @return
     */
    CfgMenuAppVo saveOrUpdate(CfgMenuAppDto cfgAttributeDto);

}
