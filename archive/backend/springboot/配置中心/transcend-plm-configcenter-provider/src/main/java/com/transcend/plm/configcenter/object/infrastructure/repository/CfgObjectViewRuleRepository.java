package com.transcend.plm.configcenter.object.infrastructure.repository;

import com.google.common.collect.Sets;
import com.transcend.plm.configcenter.common.exception.PlmBizException;
import com.transcend.plm.configcenter.common.util.CollectionUtils;
import com.transcend.plm.configcenter.common.validator.UniqueValidateParam;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectViewRulePo;
import com.transcend.plm.configcenter.object.infrastructure.repository.mapper.CfgObjectViewRuleMapper;
import com.transcend.plm.configcenter.object.infrastructure.repository.mapper.ConfigCommonMapper;
import com.transcend.plm.configcenter.object.pojo.CfgObjectViewRuleConverter;
import com.transcend.plm.configcenter.api.model.object.dto.CfgObjectViewRuleEditParam;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectViewRuleVo;
import com.transcend.plm.configcenter.view.infrastructure.repository.po.CfgViewPo;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * CfgObjectViewRuleRepository
 *
 * @author jie.luo1
 * @version: 1.0
 * @date 2023/02/18 10:45
 */
@Repository
public class CfgObjectViewRuleRepository {
    @Resource
    private CfgObjectViewRuleMapper cfgObjectViewRuleMapper;
    @Resource
    private CfgObjectViewRuleService cfgObjectViewRuleService;
    @Resource
    private ConfigCommonMapper commonMapper;

    public List<CfgObjectViewRuleVo> listByModelCode(String modelCode) {
        return CfgObjectViewRuleConverter.INSTANCE.pos2vos(
                cfgObjectViewRuleMapper.listInModelCode(Sets.newHashSet(modelCode))
        );
    }

    public CfgObjectViewRuleVo save(CfgObjectViewRuleEditParam param) {
        CfgObjectViewRulePo po = CfgObjectViewRuleConverter.INSTANCE.saveParam2po(param);
        // 权限唯一校验
        UniqueValidateParam saveParam = UniqueValidateParam.builder()
                .tableName("cfg_object_view_rule")
                .columnName("view_bid")
                .value(po.getViewBid())
                .excludeCurrentRecord(Boolean.FALSE)
                .excludeLogicDeleteItems(Boolean.TRUE)
                .logicDeleteFieldName("delete_flag")
                .logicDeleteValue(Boolean.FALSE).build();
        if (commonMapper.countByField(saveParam) > 0) {
            throw new PlmBizException("视图权限已存在");
        }
        boolean save = cfgObjectViewRuleService.save(po);
        Assert.isTrue(save, "保存失败");
        return CfgObjectViewRuleConverter.INSTANCE.po2vo(po);
    }


    public CfgObjectViewRuleVo updateByBid(CfgObjectViewRuleEditParam param) {
        CfgObjectViewRulePo po = CfgObjectViewRuleConverter.INSTANCE.saveParam2po(param);
        return CfgObjectViewRuleConverter.INSTANCE.po2vo(cfgObjectViewRuleService.updateByBid(po));
    }

    public Boolean logicalDeleteByBid(String bid) {
        return cfgObjectViewRuleService.logicalDeleteByBid(bid);
    }

    public Set<String> listBidByModelCode(String modelCode) {
        return cfgObjectViewRuleMapper.listBidByModelCode(modelCode);
    }

    public Boolean logicalDeleteByBids(Set<String> bidSet) {
        if (CollectionUtils.isEmpty(bidSet)) {
            return false;
        }
        return cfgObjectViewRuleMapper.logicalDeleteByBids(bidSet) > 0;
    }

    public List<CfgObjectViewRuleVo> listByCondition(List<String> modelCodes) {
        return listByCondition(modelCodes, null, null, null, null);
    }


    public List<CfgObjectViewRuleVo> listByCondition(List<String> modelCodes, Byte roleType, Set<String> roleSet, String lcCode,
                                                     String tag) {
        return CfgObjectViewRuleConverter.INSTANCE.pos2vos(
                cfgObjectViewRuleMapper.listByCondition(modelCodes, roleType, roleSet, lcCode, tag)
        );
    }

    public String getViewBidByModelCodeAndTag(String modelCode, String tag) {
        return cfgObjectViewRuleMapper.listByModelCodeAndTag(modelCode, tag);
    }

    public List<CfgObjectViewRuleVo> listByViewBids(List<String> viewBids) {
        return CfgObjectViewRuleConverter.INSTANCE.pos2vos(cfgObjectViewRuleMapper.listByViewBids(viewBids));
    }

    public List<CfgObjectViewRulePo> listByViewPoBids(List<String> viewBids) {
        return cfgObjectViewRuleMapper.listByViewBids(viewBids);
    }

    public boolean saveBatch(List<CfgObjectViewRulePo> ruleList) {
        return cfgObjectViewRuleService.saveBatch(ruleList);
    }

}
