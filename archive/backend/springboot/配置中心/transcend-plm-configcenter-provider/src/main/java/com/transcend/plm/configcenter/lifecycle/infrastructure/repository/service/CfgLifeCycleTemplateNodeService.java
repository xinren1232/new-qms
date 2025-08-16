package com.transcend.plm.configcenter.lifecycle.infrastructure.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.CfgLifeCycleTemplateNodeDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.TemplateDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.qo.CfgLifeCycleTemplateNodeQo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.vo.CfgLifeCycleTemplateNodeVo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.CfgLifeCycleTemplateNodePo;

import java.util.List;

/**
 *
 */
public interface CfgLifeCycleTemplateNodeService extends IService<CfgLifeCycleTemplateNodePo> {
    void saveCfgLifeCycleTemplateNode(List<CfgLifeCycleTemplateNodeDto> dtos);

    List<CfgLifeCycleTemplateNodeVo> getCfgLifeCycleTemplateNodeVos(TemplateDto templateDto);

    List<CfgLifeCycleTemplateNodePo> getCfgLifeCycleTemplateNode(TemplateDto templateDto);

    boolean deleteByTempBid(String bid);

    CfgLifeCycleTemplateNodePo getCfgLifeCycleTemplateNode(CfgLifeCycleTemplateNodeQo cfgLifeCycleTemplateNodeQo);

    List<CfgLifeCycleTemplateNodePo> getCfgLifeCycleTemplateNodeByBids(List<String> bids,String templateBid,String version);
}


