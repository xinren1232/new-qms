package com.transcend.plm.configcenter.lifecycle.infrastructure.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.CfgLifeCycleTemplateTransitionLineDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.TemplateDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.vo.CfgLifeCycleTemplateTransitionLineVo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.CfgLifeCycleTemplateTransitionLinePo;

import java.util.List;

/**
 *
 */
public interface CfgLifeCycleTemplateTransitionLineService extends IService<CfgLifeCycleTemplateTransitionLinePo> {

    void saveCfgLifeCycleTemplateTransitionLineDto(List<CfgLifeCycleTemplateTransitionLineDto> dtos);

    List<CfgLifeCycleTemplateTransitionLineVo> getCfgLifeCycleTemplateTransitionLineVos(TemplateDto templateDto);

    List<CfgLifeCycleTemplateTransitionLinePo> getCfgLifeCycleTemplateTransitionLine(TemplateDto templateDto);

    boolean deleteByTempBid(String bid);
}
