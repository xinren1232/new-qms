package com.transcend.plm.configcenter.code.infrastructure.repository;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-03-01 15:16
 **/

import com.transcend.plm.configcenter.code.infrastructure.repository.po.CfgCodeRulePo;
import com.transcend.plm.configcenter.code.pojo.CfgCodeRuleConverter;
import com.transcend.plm.configcenter.code.pojo.dto.CfgCodeRulePoDto;
import com.transcend.plm.configcenter.code.pojo.qo.CfgCodeRulePoQo;
import com.transcend.plm.configcenter.code.pojo.vo.CfgCodeRulePoVo;
import com.transcend.plm.configcenter.common.annotation.RequestAnnotation;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@Repository
@Validated
public class CfgCodeRuleRepository {
    @Resource
    private CfgCodeRuleService cfgCodeRuleService;

    @Validated(RequestAnnotation.add.class)
    public CfgCodeRulePoVo save(@Valid CfgCodeRulePoDto cfgCodeRuleDto) {
        Assert.notNull(cfgCodeRuleDto,"attribute is null");
        CfgCodeRulePo cfgCodeRule = CfgCodeRuleConverter.INSTANCE.dto2po(cfgCodeRuleDto);
        cfgCodeRuleService.save(cfgCodeRule);
        return CfgCodeRuleConverter.INSTANCE.po2vo(cfgCodeRule);
    }

    @Validated(RequestAnnotation.edit.class)
    public CfgCodeRulePoVo update(@Valid CfgCodeRulePoDto cfgCodeRuleDto) {
        CfgCodeRulePo cfgCodeRule = CfgCodeRuleConverter.INSTANCE.dto2po(cfgCodeRuleDto);
        cfgCodeRuleService.updateByBid(cfgCodeRule);
        return CfgCodeRuleConverter.INSTANCE.po2vo(cfgCodeRule);
    }

    public CfgCodeRulePoVo getByBid(String bid) {
        CfgCodeRulePo cfgCodeRule =  cfgCodeRuleService.getByBid(bid);
        return CfgCodeRuleConverter.INSTANCE.po2vo(cfgCodeRule);
    }

    public PagedResult<CfgCodeRulePoVo> page(BaseRequest<CfgCodeRulePoQo> pageQo) {
        return cfgCodeRuleService.pageByCfgCodeRuleQo(pageQo);
    }

    public List<CfgCodeRulePoVo> bulkAdd(List<CfgCodeRulePoDto> cfgCodeRuleDtos) {
        List<CfgCodeRulePo> cfgCodeRules = CfgCodeRuleConverter.INSTANCE.dtos2pos(cfgCodeRuleDtos);
        cfgCodeRuleService.saveBatch(cfgCodeRules);
        return CfgCodeRuleConverter.INSTANCE.pos2vos(cfgCodeRules);
    }

    public boolean logicalDeleteByBid(String bid) {
        return cfgCodeRuleService.logicalDeleteByBid(bid);
    }

    /**
     * 查询列表
     */
    public List<CfgCodeRulePo> listByQo(CfgCodeRulePoQo cfgCodeRulePoQo) {
        return cfgCodeRuleService.listByQo(cfgCodeRulePoQo);
    }
}

