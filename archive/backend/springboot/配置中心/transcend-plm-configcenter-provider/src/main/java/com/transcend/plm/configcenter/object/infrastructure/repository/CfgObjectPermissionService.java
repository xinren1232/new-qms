package com.transcend.plm.configcenter.object.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectPermissionPo;
import com.transcend.plm.configcenter.api.model.object.qo.CfgObjectPermissionQo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectPermissionVo;
import com.transsion.framework.dto.BaseRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-03-01 15:16
 **/
public interface CfgObjectPermissionService extends IService<CfgObjectPermissionPo> {
    /**
     * 根据bid进行更新
     * @param po
     * @return
     */
    CfgObjectPermissionPo updateByBid(CfgObjectPermissionPo po);

    /**
     * 根据Bid获取详细
     * @param bid
     * @return
     */
    CfgObjectPermissionPo getByBid(String bid);

    /**
     * 分页查询
     * @param pageQo
     * @return
     */
    PagedResult<CfgObjectPermissionVo> pageByQo(BaseRequest<CfgObjectPermissionQo> pageQo);

    /**
     * 删除
     * @param bid
     * @return
     */
    boolean logicalDeleteByBid(String bid);

}
