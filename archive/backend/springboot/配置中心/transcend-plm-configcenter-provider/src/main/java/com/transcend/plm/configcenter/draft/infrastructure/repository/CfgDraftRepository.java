package com.transcend.plm.configcenter.draft.infrastructure.repository;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:45
 **/

import com.transcend.plm.configcenter.draft.infrastructure.repository.mapper.CfgDraftMapper;
import com.transcend.plm.configcenter.draft.infrastructure.repository.po.CfgDraftPo;
import com.transcend.plm.configcenter.draft.pojo.CfgDraftConverter;
import com.transcend.plm.configcenter.draft.pojo.dto.CfgDraftDto;
import com.transcend.plm.configcenter.draft.pojo.vo.CfgDraftVo;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import javax.annotation.Resource;

@Repository
public class CfgDraftRepository {
    @Resource
    private CfgDraftService cfgDraftService;
    @Resource
    private CfgDraftMapper cfgDraftMapper;

    public CfgDraftVo save(CfgDraftDto cfgAttributeDto) {
        Assert.notNull(cfgAttributeDto, "attribute is null");
        CfgDraftPo cfgAttribute = CfgDraftConverter.INSTANCE.dto2po(cfgAttributeDto);
        cfgDraftService.save(cfgAttribute);
        return CfgDraftConverter.INSTANCE.po2vo(cfgAttribute);
    }

    public Boolean logicalDeleteByBid(String bid) {
        return cfgDraftService.logicalDeleteByBid(bid);
    }

    public Boolean logicalDeleteByCategoryAndBizCode(String category, String bizCode) {
        return cfgDraftService.logicalDeleteByCategoryAndBizCode(category, bizCode);
    }

    public CfgDraftVo getByCategoryAndBizCode(String category, String bizCode) {
        CfgDraftPo cfgAttribute = cfgDraftMapper.getByCategoryAndBizCode(category, bizCode);
        return CfgDraftConverter.INSTANCE.po2vo(cfgAttribute);
    }
}
