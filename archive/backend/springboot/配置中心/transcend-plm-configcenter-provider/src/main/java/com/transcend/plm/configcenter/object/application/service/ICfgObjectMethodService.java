package com.transcend.plm.configcenter.object.application.service;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.api.model.object.dto.ModelMethodDTO;
import com.transcend.plm.configcenter.api.model.object.qo.ModelMethodQO;
import com.transcend.plm.configcenter.api.model.object.vo.ModelMethodVO;
import com.transsion.framework.dto.BaseRequest;

/**
 * @author pingzhang.chen
 * @version: 1.0
 * @date 2021/11/29 10:19
 */

public interface ICfgObjectMethodService {

    PagedResult<ModelMethodVO> page(BaseRequest<ModelMethodQO> modelMethodQO);

    Boolean deleteByBid(String bid);

    Boolean save(ModelMethodDTO modelMethodDTO);

    ModelMethodVO getOne(String bid);

    Boolean update(ModelMethodDTO modelMethodDTO);
}
