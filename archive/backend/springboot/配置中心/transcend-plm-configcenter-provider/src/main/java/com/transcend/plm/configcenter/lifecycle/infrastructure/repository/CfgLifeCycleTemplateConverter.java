package com.transcend.plm.configcenter.lifecycle.infrastructure.repository;

import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.CfgLifeCycleTemplateDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.qo.CfgLifeCycleTemplateQo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.vo.CfgLifeCycleTemplateVo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.CfgLifeCycleTemplatePo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 11:06
 **/
@Mapper
public interface CfgLifeCycleTemplateConverter {

    CfgLifeCycleTemplateConverter INSTANCE = Mappers.getMapper(CfgLifeCycleTemplateConverter.class);

    /**
     * dto 转化为 do
     * @param dto
     * @return
     */
    CfgLifeCycleTemplatePo dto2po(CfgLifeCycleTemplateDto dto);

    /**
     * do 转化为 vo
     * @param CfgLifeCycleTemplate
     * @return
     */
    CfgLifeCycleTemplateVo po2vo(CfgLifeCycleTemplatePo CfgLifeCycleTemplate);

    /**
     * qo 转化为 po
     * @param cfgLifeCycleTemplateQo
     * @return
     */
    CfgLifeCycleTemplatePo qo2po(CfgLifeCycleTemplateQo cfgLifeCycleTemplateQo);

    /**
     * do 转化为 vo
     * @param cfgLifeCycleTemplates
     * @return
     */
    List<CfgLifeCycleTemplateVo> pos2vos(List<CfgLifeCycleTemplatePo> cfgLifeCycleTemplates);
}
