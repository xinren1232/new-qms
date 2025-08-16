package com.transcend.plm.configcenter.code.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.configcenter.code.infrastructure.repository.po.CfgCodeRulePo;
import com.transcend.plm.configcenter.code.pojo.qo.CfgCodeRulePoQo;
import com.transcend.plm.configcenter.code.pojo.vo.CfgCodeRulePoVo;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transsion.framework.dto.BaseRequest;

import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-03-01 15:16
 **/
public interface CfgCodeRuleService extends IService<CfgCodeRulePo> {
    /**
     * 根据bid进行更新
     * @param cfgCodeRule
     * @return
     */
    CfgCodeRulePo updateByBid(CfgCodeRulePo cfgCodeRule);

    /**
     * 根据Bid获取详细
     * @param bid
     * @return
     */
    CfgCodeRulePo getByBid(String bid);

    /**
     * 分页查询
     * @param pageQo
     * @return
     */
    PagedResult<CfgCodeRulePoVo> pageByCfgCodeRuleQo(BaseRequest<CfgCodeRulePoQo> pageQo);

    /**
     * 删除
     * @param bid
     * @return
     */
    boolean logicalDeleteByBid(String bid);

    /**
     * 获取列表
     */
    List<CfgCodeRulePo> listByQo(CfgCodeRulePoQo qo);
}
