package com.transcend.plm.configcenter.permission.infrastructure.repository;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @author jie.luo <jie.luo1@transsion.com>
 * @Version 1.0
 * @Date 2023-02-22 10:45
 **/

import com.transcend.plm.configcenter.permission.infrastructure.repository.po.CfgPermissionPo;
import com.transcend.plm.configcenter.permission.pojo.CfgAttributeConverter;
import com.transcend.plm.configcenter.permission.pojo.dto.CfgObjectPermissionOperationDto;
import com.transcend.plm.configcenter.permission.pojo.qo.CfgObjectPermissionOperationQo;
import com.transcend.plm.configcenter.permission.pojo.vo.CfgAttributeVo;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class CfgPermissionRepository {
    @Resource
    private CfgPermissionService cfgPermissionService;

    public CfgAttributeVo save(CfgObjectPermissionOperationDto cfgAttributeDto) {
        Assert.notNull(cfgAttributeDto,"attribute is null");
        CfgPermissionPo cfgPermissionPo = CfgAttributeConverter.INSTANCE.dto2po(cfgAttributeDto);
        cfgPermissionService.save(cfgPermissionPo);
        return CfgAttributeConverter.INSTANCE.po2vo(cfgPermissionPo);
    }

    public CfgAttributeVo update(CfgObjectPermissionOperationDto cfgAttributeDto) {
        CfgPermissionPo cfgPermissionPo = CfgAttributeConverter.INSTANCE.dto2po(cfgAttributeDto);
        cfgPermissionService.updateByBid(cfgPermissionPo);
        return CfgAttributeConverter.INSTANCE.po2vo(cfgPermissionPo);
    }

    public CfgAttributeVo getByBid(String bid) {
        CfgPermissionPo cfgPermissionPo =  cfgPermissionService.getByBid(bid);
        return CfgAttributeConverter.INSTANCE.po2vo(cfgPermissionPo);
    }

    public PagedResult<CfgAttributeVo> page(BaseRequest<CfgObjectPermissionOperationQo> pageQo) {
        return cfgPermissionService.pageByCfgAttributeQo(pageQo);
    }

    public List<CfgAttributeVo> bulkAdd(List<CfgObjectPermissionOperationDto> cfgAttributeDtos) {
        List<CfgPermissionPo> cfgPermissionPos = CfgAttributeConverter.INSTANCE.dtos2pos(cfgAttributeDtos);
        cfgPermissionService.saveBatch(cfgPermissionPos);
        return CfgAttributeConverter.INSTANCE.pos2vos(cfgPermissionPos);
    }

    public boolean logicalDeleteByBid(String bid) {
        return cfgPermissionService.logicalDeleteByBid(bid);
    }
}
