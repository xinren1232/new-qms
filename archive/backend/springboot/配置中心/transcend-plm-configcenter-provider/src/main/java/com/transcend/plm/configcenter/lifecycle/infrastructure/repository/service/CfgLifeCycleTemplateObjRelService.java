package com.transcend.plm.configcenter.lifecycle.infrastructure.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.TemplateDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.qo.CfgLifeCycleTemplateObjRelQo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.CfgLifeCycleTemplateObjRelPo;

import java.util.List;

/**
 *
 */
public interface CfgLifeCycleTemplateObjRelService extends IService<CfgLifeCycleTemplateObjRelPo> {
    List<CfgLifeCycleTemplateObjRelPo> getCfgLifeCycleTemplateObjRels(TemplateDto templateDto);

    boolean deleteByTempBid(String bid);

    CfgLifeCycleTemplateObjRelPo getCfgLifeCycleTemplateObjRel(CfgLifeCycleTemplateObjRelQo cfgLifeCycleTemplateObjRelQo);
}
