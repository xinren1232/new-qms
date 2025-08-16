package com.transcend.plm.configcenter.lifecycle.infrastructure.pojo;

import java.util.List;

import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.CfgLifeCycleTemplateDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.qo.CfgLifeCycleTemplateQo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.vo.CfgLifeCycleTemplateVo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.CfgLifeCycleTemplatePo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

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
     * @param cfgLifeCycleTemplatePo
     * @return
     */
    CfgLifeCycleTemplateVo po2vo(CfgLifeCycleTemplatePo cfgLifeCycleTemplatePo);

    /**
     * qo 转化为 po
     * @param cfgLifeCycleTemplateQo
     * @return
     */
    CfgLifeCycleTemplatePo qo2po(CfgLifeCycleTemplateQo cfgLifeCycleTemplateQo);

    /**
     * do 转化为 vo
     * @param cfgLifeCycleTemplatePos
     * @return
     */
    List<CfgLifeCycleTemplateVo> pos2vos(List<CfgLifeCycleTemplatePo> cfgLifeCycleTemplatePos);

    /**
     * dto 批量转化为 po
     * @param cfgAttributeDtos
     * @return
     */
    List<CfgLifeCycleTemplatePo> dtos2pos(List<CfgLifeCycleTemplateDto> cfgAttributeDtos);
}
