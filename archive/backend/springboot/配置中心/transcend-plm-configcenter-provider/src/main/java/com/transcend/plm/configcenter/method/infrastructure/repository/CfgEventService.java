package com.transcend.plm.configcenter.method.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.method.infrastructure.repository.po.CfgEvent;
import com.transcend.plm.configcenter.method.pojo.qo.CfgEventQo;
import com.transcend.plm.configcenter.method.pojo.vo.CfgEventVo;
import com.transsion.framework.dto.BaseRequest;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-24 10:00
 **/
public interface CfgEventService extends IService<CfgEvent> {

    /**
     * 修改
     * @param cfgEvent
     * @return
     */
    boolean updateByBid(CfgEvent cfgEvent);

    /**
     * 根据bid获取数据
     * @param bid
     * @return
     */
    CfgEvent getByBid(String bid);

    /**
     * 分页查询
     * @param pageQo
     * @return
     */
    PagedResult<CfgEventVo> pageByCfgEventQo(BaseRequest<CfgEventQo> pageQo);

    /**
     * 根据bid删除
     * @param bid
     * @return
     */
    boolean logicalDeleteByBid(String bid);
}
