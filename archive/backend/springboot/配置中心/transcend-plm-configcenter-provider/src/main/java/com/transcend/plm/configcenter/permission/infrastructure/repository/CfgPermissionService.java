package com.transcend.plm.configcenter.permission.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.configcenter.permission.infrastructure.repository.po.CfgPermissionPo;
import com.transcend.plm.configcenter.permission.pojo.qo.CfgObjectPermissionOperationQo;
import com.transcend.plm.configcenter.permission.pojo.vo.CfgAttributeVo;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transsion.framework.dto.BaseRequest;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @author jie.luo <jie.luo1@transsion.com>
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
public interface CfgPermissionService extends IService<CfgPermissionPo> {
    /**
     * 根据bid进行更新
     * @param cfgPermissionPo
     * @return
     */
    CfgPermissionPo updateByBid(CfgPermissionPo cfgPermissionPo);

    /**
     * 根据Bid获取详细
     * @param bid
     * @return
     */
    CfgPermissionPo getByBid(String bid);

    PagedResult<CfgAttributeVo> pageByCfgAttributeQo(BaseRequest<CfgObjectPermissionOperationQo> pageQo);

    boolean logicalDeleteByBid(String bid);
}
