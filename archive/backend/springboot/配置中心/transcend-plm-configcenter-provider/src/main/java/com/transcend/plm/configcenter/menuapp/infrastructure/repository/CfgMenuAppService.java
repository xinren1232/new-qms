package com.transcend.plm.configcenter.menuapp.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.configcenter.menuapp.infrastructure.repository.po.CfgMenuAppPo;
import com.transcend.plm.configcenter.menuapp.pojo.qo.CfgMenuAppQo;
import com.transcend.plm.configcenter.menuapp.pojo.vo.CfgMenuAppVo;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transsion.framework.dto.BaseRequest;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
public interface CfgMenuAppService extends IService<CfgMenuAppPo> {
    /**
     * 根据bid进行更新
     * @param cfgAttribute
     * @return
     */
    CfgMenuAppPo updateByBid(CfgMenuAppPo cfgAttribute);

    /**
     * 根据Bid获取详细
     * @param bid
     * @return
     */
    CfgMenuAppPo getByBid(String bid);

    PagedResult<CfgMenuAppVo> pageByCfgAttributeQo(BaseRequest<CfgMenuAppQo> pageQo);

    Boolean logicalDeleteByBid(String bid);
}
