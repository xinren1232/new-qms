package com.transcend.plm.configcenter.view.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.configcenter.view.infrastructure.repository.po.CfgViewPo;
import com.transcend.plm.configcenter.api.model.view.qo.CfgViewQo;
import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transsion.framework.dto.BaseRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
public interface CfgViewService extends IService<CfgViewPo> {
    /**
     * 根据bid进行更新
     * @param cfgViewPo
     * @return
     */
    CfgViewPo updateByBid(CfgViewPo cfgViewPo);

    /**
     * 根据Bid获取详细
     * @param bid
     * @return
     */
    CfgViewPo getByBid(String bid);

    /**
     * 分页查询
     * @param pageQo
     * @return
     */
    PagedResult<CfgViewVo> pageByQo(BaseRequest<CfgViewQo> pageQo);

    /**
     * 逻辑删除
     * @param bid
     * @return
     */
    boolean logicalDeleteByBid(String bid);

    /**
     * 列表查询
     * @param qo
     * @return
     */
    List<CfgViewPo> listByQo(CfgViewQo qo);

    /**
     * 根据条件查询
     * @param qo
     * @return
     */
    List<CfgViewPo> listByQoNoContent(CfgViewQo qo);

    /**
     * 条件查询
     * @param belongBid
     * @param viewScopes
     * @param viewTypes
     * @return
     */
    List<CfgViewPo> listByConditions(String belongBid, List<String> viewScopes, List<String> viewTypes);

    /**
     * 通过belongBid获取视图列表
     * @param belongBid belongBid
     * @return CfgViewPo
     */
    List<CfgViewPo> listByBelongBid(String belongBid);
}
