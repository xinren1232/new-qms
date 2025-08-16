package com.transcend.plm.configcenter.permission.domain.service;

import com.transcend.plm.configcenter.permission.infrastructure.repository.CfgPermissionRepository;
import com.transcend.plm.configcenter.permission.pojo.dto.CfgObjectPermissionOperationDto;
import com.transcend.plm.configcenter.permission.pojo.qo.CfgObjectPermissionOperationQo;
import com.transcend.plm.configcenter.permission.pojo.vo.CfgAttributeVo;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @author jie.luo <jie.luo1@transsion.com>
 * @Version 1.0
 * @Date 2023-02-22 10:38
 **/
@Service
public class CfgPermissionDomainService {
    @Resource
    private CfgPermissionRepository repository;

    public CfgAttributeVo save(CfgObjectPermissionOperationDto cfgAttributeDto) {
        Assert.notNull(cfgAttributeDto, "attribute is null");
        return repository.save(cfgAttributeDto);
    }

    public CfgAttributeVo update(CfgObjectPermissionOperationDto cfgAttributeDto) {
        Assert.notNull(cfgAttributeDto, "attribute is null");
        Assert.hasText(cfgAttributeDto.getBid(),"attribute bid is blank");
        return repository.update(cfgAttributeDto);
    }

    public CfgAttributeVo getByBid(String bid) {
        return repository.getByBid(bid);
    }

    public PagedResult<CfgAttributeVo> page(BaseRequest<CfgObjectPermissionOperationQo> pageQo) {
        return repository.page(pageQo);
    }

    public List<CfgAttributeVo> bulkAdd(List<CfgObjectPermissionOperationDto> cfgAttributeDtos) {
        return repository.bulkAdd(cfgAttributeDtos);
    }

    public boolean logicalDeleteByBid(String bid) {
        return repository.logicalDeleteByBid(bid);
    }
}
