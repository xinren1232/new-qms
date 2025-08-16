package com.transcend.plm.configcenter.lifecycle.infrastructure.pojo;

import java.util.List;

import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.CfgLifeCycleTemplateNodeDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.vo.CfgLifeCycleTemplateNodeVo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.CfgLifeCycleTemplateNodePo;
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
public interface CfgLifeCycleTemplateNodeConverter {

    CfgLifeCycleTemplateNodeConverter INSTANCE = Mappers.getMapper(CfgLifeCycleTemplateNodeConverter.class);

    /**
     * dto 转化为 do
     * @param dto
     * @return
     */
    CfgLifeCycleTemplateNodePo dto2po(CfgLifeCycleTemplateNodeDto dto);

    /**
     * do 转化为 vo
     * @param cfgLifeCycleTemplateNodePo
     * @return
     */
    CfgLifeCycleTemplateNodeVo po2vo(CfgLifeCycleTemplateNodePo cfgLifeCycleTemplateNodePo);


    /**
     * do 转化为 vo
     * @param cfgLifeCycleTemplateNodePos
     * @return
     */
    List<CfgLifeCycleTemplateNodeVo> pos2vos(List<CfgLifeCycleTemplateNodePo> cfgLifeCycleTemplateNodePos);

    /**
     * dto 批量转化为 po
     * @param cfgLifeCycleTemplateNodeDtos
     * @return
     */
    List<CfgLifeCycleTemplateNodePo> dtos2pos(List<CfgLifeCycleTemplateNodeDto> cfgLifeCycleTemplateNodeDtos);
}
