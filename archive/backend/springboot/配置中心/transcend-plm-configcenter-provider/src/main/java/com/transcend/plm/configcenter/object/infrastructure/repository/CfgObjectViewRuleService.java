package com.transcend.plm.configcenter.object.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectViewRulePo;
import com.transcend.plm.configcenter.api.model.object.qo.CfgObjectViewRuleQo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectViewRuleVo;
import com.transsion.framework.dto.BaseRequest;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author jie.luo1
 * @Version 1.0
 * @Date 2023-03-01 15:16
 **/
public interface CfgObjectViewRuleService extends IService<CfgObjectViewRulePo> {
    /**
     * 根据bid进行更新
     * @param po
     * @return
     */
    CfgObjectViewRulePo updateByBid(CfgObjectViewRulePo po);

    /**
     * 根据Bid获取详细
     * @param bid
     * @return
     */
    CfgObjectViewRulePo getByBid(String bid);

    /**
     * 分页查询
     * @param pageQo
     * @return
     */
    PagedResult<CfgObjectViewRuleVo> pageByQo(BaseRequest<CfgObjectViewRuleQo> pageQo);

    /**
     * 删除
     * @param bid
     * @return
     */
    boolean logicalDeleteByBid(String bid);

}
