package com.transcend.plm.configcenter.lifecycle.application.service;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.api.model.lifecycle.qo.LifeCycleStateListQo;
import com.transcend.plm.configcenter.api.model.lifecycle.vo.LifeCycleStateVo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.LifeCycleStateDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.TemplateDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.CfgLifeCycleTemplateNodePo;
import com.transsion.framework.dto.BaseRequest;

import java.util.List;

/**
 * 什么周期应用组合服务接口
 *
 * @author yuanhu.huang <yuanhu.huang@transsion.com>
 * @version V1.0.0
 * @date 2023/2/20 10:24
 * @since 1.0
 */
public interface ILifeCycleApplicationService {
    PagedResult<LifeCycleStateVo> page(BaseRequest<LifeCycleStateListQo> pageQo);
    LifeCycleStateVo add(LifeCycleStateDto dto);
    LifeCycleStateVo edit(LifeCycleStateDto dto);
    boolean delete(String id);

    List<CfgLifeCycleTemplateNodePo> getTemplateNodesOrderByLine(TemplateDto templateDto);
}
