package com.transcend.plm.configcenter.method.infrastructure.repository;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.method.infrastructure.repository.po.CfgEvent;
import com.transcend.plm.configcenter.method.pojo.CfgEventConverter;
import com.transcend.plm.configcenter.method.pojo.dto.CfgEventDto;
import com.transcend.plm.configcenter.method.pojo.qo.CfgEventQo;
import com.transcend.plm.configcenter.method.pojo.vo.CfgEventVo;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-24 10:37
 **/
@Repository
public class CfgEventRepository {

    @Resource
    private CfgEventService cfgEventService;

    public CfgEventVo save(CfgEventDto cfgEventDto) {
        CfgEvent cfgEvent = CfgEventConverter.INSTANCE.dto2po(cfgEventDto);
        cfgEventService.save(cfgEvent);
        return CfgEventConverter.INSTANCE.po2vo(cfgEvent);
    }

    public CfgEventVo update(CfgEventDto cfgEventDto) {
        CfgEvent cfgEvent = CfgEventConverter.INSTANCE.dto2po(cfgEventDto);
        cfgEventService.updateByBid(cfgEvent);
        return CfgEventConverter.INSTANCE.po2vo(cfgEvent);
    }

    public CfgEventVo getByBid(String bid) {
        return CfgEventConverter.INSTANCE.po2vo(cfgEventService.getByBid(bid));
    }

    public PagedResult<CfgEventVo> page(BaseRequest<CfgEventQo> pageQo) {
        return cfgEventService.pageByCfgEventQo(pageQo);
    }

    public boolean logicalDeleteByBid(String bid) {
        return cfgEventService.logicalDeleteByBid(bid);
    }
}
