package com.transcend.plm.configcenter.lifecycle.infrastructure.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.CfgLifeCycleTemplateDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.CfgLifeCycleTemplateVersionPo;

import java.util.List;

/**
 *
 */
public interface CfgLifeCycleTemplateVersionService extends IService<CfgLifeCycleTemplateVersionPo> {
    long countByTemplateBid(String templateBid);

    List<CfgLifeCycleTemplateVersionPo> getVersions(CfgLifeCycleTemplateDto cfgLifeCycleTemplateDto);

    boolean deleteByTempBid(String bid);

    CfgLifeCycleTemplateVersionPo getCfgLifeCycleTemplateVersion(String templateBid, String version);
}
