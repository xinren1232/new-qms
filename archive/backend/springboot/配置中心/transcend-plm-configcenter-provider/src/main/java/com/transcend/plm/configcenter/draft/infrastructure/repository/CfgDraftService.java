package com.transcend.plm.configcenter.draft.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.configcenter.draft.infrastructure.repository.po.CfgDraftPo;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.draft.pojo.qo.CfgDraftQo;
import com.transcend.plm.configcenter.draft.pojo.vo.CfgDraftVo;
import com.transsion.framework.dto.BaseRequest;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
public interface CfgDraftService extends IService<CfgDraftPo> {


    Boolean logicalDeleteByBid(String bid);

    Boolean logicalDeleteByCategoryAndBizCode(String category, String bizCode);
}
