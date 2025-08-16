package com.transcend.plm.configcenter.method.domain.service;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.method.infrastructure.repository.CfgMethodRepository;
import com.transcend.plm.configcenter.method.pojo.dto.CfgMethodDto;
import com.transcend.plm.configcenter.method.pojo.qo.CfgMethodQo;
import com.transcend.plm.configcenter.method.pojo.vo.CfgMethodVo;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-24 10:13
 **/
@Service
public class CfgMethodDomainService {

    @Resource
    private CfgMethodRepository cfgMethodRepository;

    public CfgMethodVo saveOrUpdate(CfgMethodDto cfgMethodDto) {
        cfgMethodDto.setUpdatedTime(LocalDateTime.now());
        return StringUtil.isBlank(cfgMethodDto.getBid()) ? cfgMethodRepository.save(cfgMethodDto) : cfgMethodRepository.update(cfgMethodDto);
    }

    public CfgMethodVo getByBid(String bid) {
        return cfgMethodRepository.getByBid(bid);
    }

    public PagedResult<CfgMethodVo> page(BaseRequest<CfgMethodQo> pageQo) {
        return cfgMethodRepository.page(pageQo);
    }

    public boolean logicalDeleteByBid(String bid) {
        return cfgMethodRepository.logicalDeleteByBid(bid);
    }
}
