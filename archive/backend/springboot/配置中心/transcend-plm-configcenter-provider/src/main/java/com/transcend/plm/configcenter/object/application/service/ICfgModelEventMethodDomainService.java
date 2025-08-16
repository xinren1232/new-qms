package com.transcend.plm.configcenter.object.application.service;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.api.model.object.dto.CfgModelEventMethodDto;
import com.transcend.plm.configcenter.api.model.object.qo.CfgModelEventMethodQo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgModelEventMethodVo;
import com.transsion.framework.dto.BaseRequest;

import java.util.List;

/**
 * ModelEventMethodService
 *
 * @author shuangzhi.zeng
 * @version: 1.0
 * @date 2021/11/18 10:39
 */
public interface ICfgModelEventMethodDomainService {
    /**
     * 更新或保存
     * @param modelEventMethodDTO
     * @return
     */
    CfgModelEventMethodVo saveOrUpdate(CfgModelEventMethodDto modelEventMethodDTO);

    /**
     * 获取详情
     * @param bid
     * @return
     */
    CfgModelEventMethodVo getByBid(String bid);

    /**
     * 分页获取
     * @param pageQo
     * @return
     */
    PagedResult<CfgModelEventMethodVo> page(BaseRequest<CfgModelEventMethodQo> pageQo);

    /**
     * 批量添加
     * @param dtos
     * @return
     */
    List<CfgModelEventMethodVo> bulkAdd(List<CfgModelEventMethodDto> dtos);

    /**
     * 逻辑删除
     * @param bid
     * @return
     */
    Boolean logicalDeleteByBid(String bid);

    /**
     * 批量删除
     * @param bids
     * @return
     */
    Boolean logicalBulkDelete(List<String> bids);
}
