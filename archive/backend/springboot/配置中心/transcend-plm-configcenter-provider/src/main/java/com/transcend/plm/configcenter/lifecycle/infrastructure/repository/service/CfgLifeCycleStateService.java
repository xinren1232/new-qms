package com.transcend.plm.configcenter.lifecycle.infrastructure.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.api.model.lifecycle.qo.LifeCycleStateListQo;
import com.transcend.plm.configcenter.api.model.lifecycle.vo.LifeCycleStateVo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.LifeCycleStateDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.CfgLifeCycleStatePo;
import com.transsion.framework.dto.BaseRequest;

import java.util.List;


/**
 *
 */
public interface CfgLifeCycleStateService extends IService<CfgLifeCycleStatePo> {

    PagedResult<LifeCycleStateVo> page(BaseRequest<LifeCycleStateListQo> pageQo);

    List<LifeCycleStateVo> list(LifeCycleStateListQo lifeCycleStateListQo);

    List<CfgLifeCycleStatePo> listByCodes(List<String> codes);

    boolean deleteByBid(String bid);

    boolean editByBid(LifeCycleStateDto dto);

    boolean checkNameAndCode(CfgLifeCycleStatePo po);

    boolean checkNameAndCodes(List<CfgLifeCycleStatePo> poList);

    boolean checkName(String name);

    boolean checkCode(String code);

    CfgLifeCycleStatePo getByBid(String bid);
}
