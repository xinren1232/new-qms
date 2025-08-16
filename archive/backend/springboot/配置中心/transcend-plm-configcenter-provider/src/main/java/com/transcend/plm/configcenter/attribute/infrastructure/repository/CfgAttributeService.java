package com.transcend.plm.configcenter.attribute.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.configcenter.attribute.infrastructure.repository.po.CfgAttributePo;
import com.transcend.plm.configcenter.attribute.pojo.qo.CfgAttributeQo;
import com.transcend.plm.configcenter.attribute.pojo.vo.CfgAttributeVo;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transsion.framework.dto.BaseRequest;

import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
public interface CfgAttributeService extends IService<CfgAttributePo> {
    /**
     * 根据bid进行更新
     * @param cfgAttributePo
     * @return
     */
    CfgAttributePo updateByBid(CfgAttributePo cfgAttributePo);

    /**
     * 根据Bid获取详细
     * @param bid
     * @return
     */
    CfgAttributePo getByBid(String bid);

    PagedResult<CfgAttributeVo> pageByCfgAttributeQo(BaseRequest<CfgAttributeQo> pageQo);

    boolean logicalDeleteByBid(String bid);

    List<CfgAttributeVo> listAll();

    /**
     * 根据code获取详细
     * @param code
     * @return CfgAttributePo
     */
    CfgAttributePo getByCode(String code);
}
