package com.transcend.plm.configcenter.dictionary.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.dictionary.infrastructure.repository.po.CfgDictionaryPo;
import com.transcend.plm.configcenter.api.model.dictionary.qo.CfgDictionaryQo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryVo;
import com.transsion.framework.dto.BaseRequest;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @author jie.luo <jie.luo1@transsion.com>
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
public interface CfgDictionaryService extends IService<CfgDictionaryPo> {
    /**
     * 根据bid进行更新
     * @param cfgDictionary
     * @return
     */
    CfgDictionaryPo updateByBid(CfgDictionaryPo cfgDictionary);

    /**
     * 根据Bid获取详细
     * @param bid
     * @return
     */
    CfgDictionaryPo getByBid(String bid);

    PagedResult<CfgDictionaryVo> pageByQo(BaseRequest<CfgDictionaryQo> pageQo);

    boolean logicalDeleteByBid(String bid);
}
