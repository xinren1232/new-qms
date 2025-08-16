package com.transcend.plm.configcenter.method.infrastructure.repository;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.method.infrastructure.repository.po.CfgMethod;
import com.transcend.plm.configcenter.method.pojo.CfgMethodConverter;
import com.transcend.plm.configcenter.method.pojo.dto.CfgMethodDto;
import com.transcend.plm.configcenter.method.pojo.qo.CfgMethodQo;
import com.transcend.plm.configcenter.method.pojo.vo.CfgMethodVo;
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
public class CfgMethodRepository {

    @Resource
    private CfgMethodService cfgMethodService;

    public CfgMethodVo save(CfgMethodDto cfgMethodDto) {
        CfgMethod cfgMethod = CfgMethodConverter.INSTANCE.dto2po(cfgMethodDto);
        cfgMethodService.save(cfgMethod);
        return CfgMethodConverter.INSTANCE.po2vo(cfgMethod);
    }

    public CfgMethodVo update(CfgMethodDto cfgMethodDto) {
        CfgMethod cfgMethod = CfgMethodConverter.INSTANCE.dto2po(cfgMethodDto);
        cfgMethodService.updateByBid(cfgMethod);
        return CfgMethodConverter.INSTANCE.po2vo(cfgMethod);
    }

    public CfgMethodVo getByBid(String bid) {
        return CfgMethodConverter.INSTANCE.po2vo(cfgMethodService.getByBid(bid));
    }

    public PagedResult<CfgMethodVo> page(BaseRequest<CfgMethodQo> pageQo) {
        return cfgMethodService.pageByCfgMethodQo(pageQo);
    }

    public boolean logicalDeleteByBid(String bid) {
        return cfgMethodService.logicalDeleteByBid(bid);
    }
}
