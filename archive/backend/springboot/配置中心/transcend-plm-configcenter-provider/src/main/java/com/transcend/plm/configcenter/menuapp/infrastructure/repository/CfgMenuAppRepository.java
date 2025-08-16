package com.transcend.plm.configcenter.menuapp.infrastructure.repository;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:45
 **/

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.menuapp.infrastructure.repository.po.CfgMenuAppPo;
import com.transcend.plm.configcenter.menuapp.pojo.CfgMenuAppConverter;
import com.transcend.plm.configcenter.menuapp.pojo.dto.CfgMenuAppDto;
import com.transcend.plm.configcenter.menuapp.pojo.qo.CfgMenuAppQo;
import com.transcend.plm.configcenter.menuapp.pojo.vo.CfgMenuAppVo;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class CfgMenuAppRepository {
    @Resource
    private CfgMenuAppService cfgMenuAppService;

    public CfgMenuAppVo save(CfgMenuAppDto dto) {
        Assert.notNull(dto,"Menu App is null");
        CfgMenuAppPo cfgAttribute = CfgMenuAppConverter.INSTANCE.dto2po(dto);
        cfgMenuAppService.save(cfgAttribute);
        return CfgMenuAppConverter.INSTANCE.po2vo(cfgAttribute);
    }

    public CfgMenuAppVo update(CfgMenuAppDto cfgAttributeDto) {
        CfgMenuAppPo cfgMenuAppPo = CfgMenuAppConverter.INSTANCE.dto2po(cfgAttributeDto);
        cfgMenuAppService.updateByBid(cfgMenuAppPo);
        return CfgMenuAppConverter.INSTANCE.po2vo(cfgMenuAppPo);
    }

    public CfgMenuAppVo getByBid(String bid) {
        CfgMenuAppPo cfgAttribute =  cfgMenuAppService.getByBid(bid);
        return CfgMenuAppConverter.INSTANCE.po2vo(cfgAttribute);
    }

    public PagedResult<CfgMenuAppVo> page(BaseRequest<CfgMenuAppQo> pageQo) {
        return cfgMenuAppService.pageByCfgAttributeQo(pageQo);
    }

    public List<CfgMenuAppVo> listAll() {
        LambdaQueryWrapper<CfgMenuAppPo> queryWrapper = Wrappers.lambdaQuery();
        List<CfgMenuAppPo> roles = cfgMenuAppService.list(queryWrapper);
        return CfgMenuAppConverter.INSTANCE.pos2vos(roles);
    }

    public long getCount(String typeCode,String typeValue){
        LambdaQueryWrapper<CfgMenuAppPo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(CfgMenuAppPo::getTypeCode,typeCode);
        queryWrapper.eq(CfgMenuAppPo::getTypeValue,typeValue);
        return cfgMenuAppService.count(queryWrapper);
    }

    public Boolean logicalDeleteByBid(String bid) {
        return cfgMenuAppService.logicalDeleteByBid(bid);
    }
}
