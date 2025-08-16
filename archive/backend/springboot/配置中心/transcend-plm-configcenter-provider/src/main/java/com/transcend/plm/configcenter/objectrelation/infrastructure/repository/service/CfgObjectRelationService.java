package com.transcend.plm.configcenter.objectrelation.infrastructure.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.api.model.objectrelation.dto.CfgObjectRelationDto;
import com.transcend.plm.configcenter.api.model.objectrelation.qo.CfgObjectRelationQo;
import com.transcend.plm.configcenter.api.model.objectrelation.vo.CfgObjectRelationVo;
import com.transcend.plm.configcenter.objectrelation.infrastructure.repository.po.CfgObjectRelationPo;
import com.transsion.framework.dto.BaseRequest;

import java.util.List;


/**
 *
 */
public interface CfgObjectRelationService extends IService<CfgObjectRelationPo> {
    PagedResult<CfgObjectRelationVo> page(BaseRequest<CfgObjectRelationQo> pageQo);
    boolean updateByBid(CfgObjectRelationDto cfgObjectRelationDto);

    boolean changeEnableFlag(String bid,Integer enableFlag);

    List<CfgObjectRelationVo> list(CfgObjectRelationQo cfgObjectRelationQo);
}
