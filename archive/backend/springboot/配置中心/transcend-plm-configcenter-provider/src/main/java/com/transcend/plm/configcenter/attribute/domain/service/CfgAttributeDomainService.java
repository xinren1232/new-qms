package com.transcend.plm.configcenter.attribute.domain.service;

import com.transcend.plm.configcenter.attribute.infrastructure.repository.CfgAttributeRepository;
import com.transcend.plm.configcenter.attribute.pojo.dto.CfgAttributeDto;
import com.transcend.plm.configcenter.attribute.pojo.qo.CfgAttributeQo;
import com.transcend.plm.configcenter.attribute.pojo.vo.CfgAttributeVo;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:38
 **/
@Service
public class CfgAttributeDomainService {
    @Resource
    private CfgAttributeRepository repository;

    public CfgAttributeVo save(CfgAttributeDto cfgAttributeDto) {
        Assert.notNull(cfgAttributeDto, "attribute is null");
        return repository.save(cfgAttributeDto);
    }

    public CfgAttributeVo update(CfgAttributeDto cfgAttributeDto) {
        Assert.notNull(cfgAttributeDto, "attribute is null");
        Assert.hasText(cfgAttributeDto.getBid(),"attribute bid is blank");
        return repository.update(cfgAttributeDto);
    }

    public CfgAttributeVo getByBid(String bid) {
        return repository.getByBid(bid);
    }

    public PagedResult<CfgAttributeVo> page(BaseRequest<CfgAttributeQo> pageQo) {
        return repository.page(pageQo);
    }

    public List<CfgAttributeVo> bulkAdd(List<CfgAttributeDto> cfgAttributeDtos) {
        return repository.bulkAdd(cfgAttributeDtos);
    }

    public boolean logicalDeleteByBid(String bid) {
        return repository.logicalDeleteByBid(bid);
    }

    public List<CfgAttributeVo> listAll(){
        return repository.listAll();
    }

    public com.transcend.plm.configcenter.api.model.attribute.vo.CfgAttributeVo getByCode(String code) {
        return repository.getByCode(code);
    }
}
