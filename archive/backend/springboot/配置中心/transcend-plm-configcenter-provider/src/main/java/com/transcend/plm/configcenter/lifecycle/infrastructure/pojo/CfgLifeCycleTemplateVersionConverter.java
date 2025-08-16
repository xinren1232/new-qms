package com.transcend.plm.configcenter.lifecycle.infrastructure.pojo;

import java.util.List;

import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.vo.CfgLifeCycleTemplateVersionVo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.CfgLifeCycleTemplateVersionPo;
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
public interface CfgLifeCycleTemplateVersionConverter {

    CfgLifeCycleTemplateVersionConverter INSTANCE = Mappers.getMapper(CfgLifeCycleTemplateVersionConverter.class);


    /**
     * do 转化为 vo
     * @param cfgLifeCycleTemplateVersionPo
     * @return
     */
    CfgLifeCycleTemplateVersionVo po2vo(CfgLifeCycleTemplateVersionPo cfgLifeCycleTemplateVersionPo);

    CfgLifeCycleTemplateVersionPo dto2po(CfgLifeCycleTemplateDto cfgLifeCycleTemplateDto);


    /**
     * do 转化为 vo
     * @param cfgLifeCycleTemplateVersionPos
     * @return
     */
    List<CfgLifeCycleTemplateVersionVo> pos2vos(List<CfgLifeCycleTemplateVersionPo> cfgLifeCycleTemplateVersionPos);


}
