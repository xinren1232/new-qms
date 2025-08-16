package com.transcend.plm.configcenter.code.infrastructure.repository;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-03-01 15:16
 **/

import com.transcend.plm.configcenter.code.infrastructure.repository.po.CfgCodeRuleItemPo;
import com.transcend.plm.configcenter.code.pojo.CfgCodeRuleItemConverter;
import com.transcend.plm.configcenter.code.pojo.dto.CfgCodeRuleItemPoDto;
import com.transcend.plm.configcenter.code.pojo.qo.CfgCodeRuleItemPoQo;
import com.transcend.plm.configcenter.code.pojo.vo.CfgCodeRuleItemPoVo;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class CfgCodeRuleItemRepository {
    @Resource
    private CfgCodeRuleItemService cfgCodeRuleItemService;

    public CfgCodeRuleItemPoVo save(CfgCodeRuleItemPoDto cfgCodeRuleItemDto) {
        Assert.notNull(cfgCodeRuleItemDto,"attribute is null");
        CfgCodeRuleItemPo cfgCodeRuleItem = CfgCodeRuleItemConverter.INSTANCE.dto2po(cfgCodeRuleItemDto);
        cfgCodeRuleItemService.save(cfgCodeRuleItem);
        return CfgCodeRuleItemConverter.INSTANCE.po2vo(cfgCodeRuleItem);
    }

    public CfgCodeRuleItemPoVo update(CfgCodeRuleItemPoDto cfgCodeRuleItemDto) {
        CfgCodeRuleItemPo cfgCodeRuleItem = CfgCodeRuleItemConverter.INSTANCE.dto2po(cfgCodeRuleItemDto);
        cfgCodeRuleItemService.updateByBid(cfgCodeRuleItem);
        return CfgCodeRuleItemConverter.INSTANCE.po2vo(cfgCodeRuleItem);
    }

    public CfgCodeRuleItemPoVo getByBid(String bid) {
        CfgCodeRuleItemPo cfgCodeRuleItem =  cfgCodeRuleItemService.getByBid(bid);
        return CfgCodeRuleItemConverter.INSTANCE.po2vo(cfgCodeRuleItem);
    }

    public PagedResult<CfgCodeRuleItemPoVo> page(BaseRequest<CfgCodeRuleItemPoQo> pageQo) {
        return cfgCodeRuleItemService.pageByCfgCodeRuleItemQo(pageQo);
    }

    public List<CfgCodeRuleItemPoVo> bulkAdd(List<CfgCodeRuleItemPoDto> cfgCodeRuleItemDtos) {
        List<CfgCodeRuleItemPo> cfgCodeRuleItems = CfgCodeRuleItemConverter.INSTANCE.dtos2pos(cfgCodeRuleItemDtos);
        cfgCodeRuleItemService.saveBatch(cfgCodeRuleItems);
        return CfgCodeRuleItemConverter.INSTANCE.pos2vos(cfgCodeRuleItems);
    }

    public boolean deleteByBid(String bid) {
        return cfgCodeRuleItemService.deleteByBid(bid);
    }
}
