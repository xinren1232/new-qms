package com.transcend.plm.configcenter.language.infrastructure.repository;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:45
 **/

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.language.infrastructure.repository.po.CfgLanguagePo;
import com.transcend.plm.configcenter.language.pojo.CfgLanguageConverter;
import com.transcend.plm.configcenter.language.pojo.dto.CfgLanguageDto;
import com.transcend.plm.configcenter.language.pojo.qo.CfgLanguageQo;
import com.transcend.plm.configcenter.language.pojo.vo.CfgLanguageVo;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class CfgLanguageRepository {
    @Resource
    private CfgLanguageService cfgLanguageService;

    public CfgLanguageVo save(CfgLanguageDto cfgLanguageDto) {
        Assert.notNull(cfgLanguageDto,"Language is null");
        CfgLanguagePo cfgLanguagePo = CfgLanguageConverter.INSTANCE.dto2po(cfgLanguageDto);
        cfgLanguageService.save(cfgLanguagePo);
        return CfgLanguageConverter.INSTANCE.po2vo(cfgLanguagePo);
    }

    public CfgLanguageVo update(CfgLanguageDto cfgLanguageDto) {
        CfgLanguagePo cfgLanguagePo = CfgLanguageConverter.INSTANCE.dto2po(cfgLanguageDto);
        cfgLanguageService.updateByBid(cfgLanguagePo);
        return CfgLanguageConverter.INSTANCE.po2vo(cfgLanguagePo);
    }

    public CfgLanguageVo getByBid(String bid) {
        CfgLanguagePo cfgLanguagePo =  cfgLanguageService.getByBid(bid);
        return CfgLanguageConverter.INSTANCE.po2vo(cfgLanguagePo);
    }

    public PagedResult<CfgLanguageVo> page(BaseRequest<CfgLanguageQo> pageQo) {
        return cfgLanguageService.pageByCfgLanguageQo(pageQo);
    }

    public List<CfgLanguageVo> bulkAdd(List<CfgLanguageDto> cfgLanguageDtos) {
        List<CfgLanguagePo> cfgLanguagePos = CfgLanguageConverter.INSTANCE.dtos2pos(cfgLanguageDtos);
        cfgLanguageService.saveBatch(cfgLanguagePos);
        return CfgLanguageConverter.INSTANCE.pos2vos(cfgLanguagePos);
    }

    public boolean logicalDeleteByBid(String bid) {
        return cfgLanguageService.logicalDeleteByBid(bid);
    }
}
