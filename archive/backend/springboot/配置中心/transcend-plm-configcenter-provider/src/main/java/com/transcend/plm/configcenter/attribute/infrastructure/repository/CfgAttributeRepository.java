package com.transcend.plm.configcenter.attribute.infrastructure.repository;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:45
 **/

import com.transcend.plm.configcenter.attribute.infrastructure.repository.po.CfgAttributePo;
import com.transcend.plm.configcenter.attribute.pojo.CfgAttributeConverter;
import com.transcend.plm.configcenter.attribute.pojo.dto.CfgAttributeDto;
import com.transcend.plm.configcenter.attribute.pojo.qo.CfgAttributeQo;
import com.transcend.plm.configcenter.attribute.pojo.vo.CfgAttributeVo;
import com.transcend.plm.configcenter.common.annotation.RequestAnnotation;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

@Repository
@Validated
public class CfgAttributeRepository {
    @Resource
    private CfgAttributeService cfgAttributeService;

    @Validated(RequestAnnotation.add.class)
    public CfgAttributeVo save(@Validated CfgAttributeDto cfgAttributeDto) {
        Assert.notNull(cfgAttributeDto,"attribute is null");
        CfgAttributePo cfgAttributePo = CfgAttributeConverter.INSTANCE.dto2po(cfgAttributeDto);
        cfgAttributeService.save(cfgAttributePo);
        return CfgAttributeConverter.INSTANCE.po2vo(cfgAttributePo);
    }

    @Validated(RequestAnnotation.edit.class)
    public CfgAttributeVo update(@Validated CfgAttributeDto cfgAttributeDto) {
        CfgAttributePo cfgAttributePo = CfgAttributeConverter.INSTANCE.dto2po(cfgAttributeDto);
        cfgAttributeService.updateByBid(cfgAttributePo);
        return CfgAttributeConverter.INSTANCE.po2vo(cfgAttributePo);
    }

    public CfgAttributeVo getByBid(String bid) {
        CfgAttributePo cfgAttributePo =  cfgAttributeService.getByBid(bid);
        return CfgAttributeConverter.INSTANCE.po2vo(cfgAttributePo);
    }

    public PagedResult<CfgAttributeVo> page(BaseRequest<CfgAttributeQo> pageQo) {
        return cfgAttributeService.pageByCfgAttributeQo(pageQo);
    }

    public List<CfgAttributeVo> bulkAdd(List<CfgAttributeDto> cfgAttributeDtos) {
        List<CfgAttributePo> cfgAttributePos = CfgAttributeConverter.INSTANCE.dtos2pos(cfgAttributeDtos);
        cfgAttributeService.saveBatch(cfgAttributePos);
        return CfgAttributeConverter.INSTANCE.pos2vos(cfgAttributePos);
    }

    public boolean logicalDeleteByBid(String bid) {
        return cfgAttributeService.logicalDeleteByBid(bid);
    }

    public List<CfgAttributeVo> listAll(){
        return cfgAttributeService.listAll();
    }

    public com.transcend.plm.configcenter.api.model.attribute.vo.CfgAttributeVo getByCode(String code) {
        CfgAttributePo cfgAttributePo =  cfgAttributeService.getByCode(code);
        return CfgAttributeConverter.INSTANCE.po2ApiVo(cfgAttributePo);
    }
}
