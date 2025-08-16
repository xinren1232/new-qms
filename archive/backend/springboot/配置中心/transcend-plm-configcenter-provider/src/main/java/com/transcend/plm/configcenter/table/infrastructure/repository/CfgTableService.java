package com.transcend.plm.configcenter.table.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.table.infrastructure.repository.po.CfgTablePo;
import com.transcend.plm.configcenter.api.model.table.qo.CfgTableQo;
import com.transcend.plm.configcenter.api.model.table.vo.CfgTableVo;
import com.transsion.framework.dto.BaseRequest;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @author jie.luo <jie.luo1@transsion.com>
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
public interface CfgTableService extends IService<CfgTablePo> {
    /**
     * 根据bid进行更新
     * @param cfgDictionary
     * @return
     */
    CfgTablePo updateByBid(CfgTablePo cfgDictionary);

    /**
     * 根据Bid获取详细
     * @param bid
     * @return
     */
    CfgTablePo getByBid(String bid);

    PagedResult<CfgTableVo> pageByQo(BaseRequest<CfgTableQo> pageQo);

    boolean logicalDeleteByBid(String bid);
}
