package com.transcend.plm.configcenter.lifecycle.infrastructure.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.CfgLifeCycleTemplateDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.qo.CfgLifeCycleTemplateQo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.vo.CfgLifeCycleTemplateVo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.CfgLifeCycleTemplatePo;
import com.transsion.framework.dto.BaseRequest;

/**
 * @author yuanhu.huang
 */
public interface CfgLifeCycleTemplateService extends IService<CfgLifeCycleTemplatePo> {
    PagedResult<CfgLifeCycleTemplateVo> pageByCfgLifeCycleTemplateQo(BaseRequest<CfgLifeCycleTemplateQo> pageQo);

    CfgLifeCycleTemplateVo saveCfgLifeCycleTemplate(CfgLifeCycleTemplateDto cfgLifeCycleTemplateDto);

    CfgLifeCycleTemplateVo updateDescription(CfgLifeCycleTemplateDto cfgLifeCycleTemplateDto);

    boolean setVersion(CfgLifeCycleTemplateDto cfgLifeCycleTemplateDto);

    boolean delete(String bid);

    CfgLifeCycleTemplateVo getByBid(String bid);

    boolean setEnable(CfgLifeCycleTemplateDto cfgLifeCycleTemplateDto);
}
