package com.transcend.plm.configcenter.code.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.configcenter.code.infrastructure.repository.po.CfgCodeRuleItemPo;
import com.transcend.plm.configcenter.code.pojo.qo.CfgCodeRuleItemPoQo;
import com.transcend.plm.configcenter.code.pojo.vo.CfgCodeRuleItemPoVo;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transsion.framework.dto.BaseRequest;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-03-01 15:16
 **/
public interface CfgCodeRuleItemService extends IService<CfgCodeRuleItemPo> {
    /**
     * 根据bid进行更新
     * @param CfgCodeRuleItem
     * @return
     */
    CfgCodeRuleItemPo updateByBid(CfgCodeRuleItemPo CfgCodeRuleItem);

    /**
     * 根据Bid获取详细
     * @param bid
     * @return
     */
    CfgCodeRuleItemPo getByBid(String bid);

    /**
     * 分页查询
     * @param pageQo
     * @return
     */
    PagedResult<CfgCodeRuleItemPoVo> pageByCfgCodeRuleItemQo(BaseRequest<CfgCodeRuleItemPoQo> pageQo);

    /**
     * 删除
     * @param bid
     * @return
     */
    boolean deleteByBid(String bid);
}
