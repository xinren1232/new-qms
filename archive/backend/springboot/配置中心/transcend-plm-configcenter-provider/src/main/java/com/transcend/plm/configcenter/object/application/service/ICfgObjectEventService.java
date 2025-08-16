package com.transcend.plm.configcenter.object.application.service;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.api.model.object.qo.ModelEventQO;
import com.transcend.plm.configcenter.api.model.object.vo.ModelEventVO;
import com.transsion.framework.dto.BaseRequest;


public interface ICfgObjectEventService {
    PagedResult<ModelEventVO> page(BaseRequest<ModelEventQO> modelEventQO);

}
