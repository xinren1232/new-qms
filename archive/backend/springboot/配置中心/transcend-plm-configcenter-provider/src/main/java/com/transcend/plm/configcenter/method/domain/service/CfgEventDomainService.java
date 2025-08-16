package com.transcend.plm.configcenter.method.domain.service;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.method.infrastructure.repository.CfgEventRepository;
import com.transcend.plm.configcenter.method.pojo.dto.CfgEventDto;
import com.transcend.plm.configcenter.method.pojo.qo.CfgEventQo;
import com.transcend.plm.configcenter.method.pojo.vo.CfgEventVo;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-24 10:13
 **/
@Service
public class CfgEventDomainService {

    @Resource
    private CfgEventRepository cfgEventRepository;

    public CfgEventVo saveOrUpdate(CfgEventDto cfgEventDto) {
        return StringUtil.isBlank(cfgEventDto.getBid()) ? cfgEventRepository.save(cfgEventDto) : cfgEventRepository.update(cfgEventDto);
    }

    public CfgEventVo getByBid(String bid) {
        return cfgEventRepository.getByBid(bid);
    }

    public PagedResult<CfgEventVo> page(BaseRequest<CfgEventQo> pageQo) {
        return cfgEventRepository.page(pageQo);
    }

    public boolean logicalDeleteByBid(String bid) {
        return cfgEventRepository.logicalDeleteByBid(bid);
    }
}
