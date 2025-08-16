package com.transcend.plm.configcenter.lifecycle.infrastructure.pojo;

import java.util.List;

import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.CfgLifeCycleTemplateTransitionLineDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.vo.CfgLifeCycleTemplateTransitionLineVo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.CfgLifeCycleTemplateTransitionLinePo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.CfgLifeCycleTemplateDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.qo.CfgLifeCycleTemplateQo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.vo.CfgLifeCycleTemplateVo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.CfgLifeCycleTemplatePo;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 11:06
 **/
@Mapper
public interface CfgLifeCycleTemplateTransitionLineConverter {

    CfgLifeCycleTemplateTransitionLineConverter INSTANCE = Mappers.getMapper(CfgLifeCycleTemplateTransitionLineConverter.class);

    /**
     * dto 转化为 do
     * @param dto
     * @return
     */
    CfgLifeCycleTemplateTransitionLinePo dto2po(CfgLifeCycleTemplateTransitionLineDto dto);

    /**
     * do 转化为 vo
     * @param cfgLifeCycleTemplateTransitionLinePo
     * @return
     */
    CfgLifeCycleTemplateTransitionLineVo po2vo(CfgLifeCycleTemplateTransitionLinePo cfgLifeCycleTemplateTransitionLinePo);


    /**
     * do 转化为 vo
     * @param cfgLifeCycleTemplateTransitionLinePos
     * @return
     */
    List<CfgLifeCycleTemplateTransitionLineVo> pos2vos(List<CfgLifeCycleTemplateTransitionLinePo> cfgLifeCycleTemplateTransitionLinePos);

    /**
     * dto 批量转化为 po
     * @param cfgLifeCycleTemplateTransitionLineDtos
     * @return
     */
    List<CfgLifeCycleTemplateTransitionLinePo> dtos2pos(List<CfgLifeCycleTemplateTransitionLineDto> cfgLifeCycleTemplateTransitionLineDtos);
}
