package com.transcend.plm.configcenter.code.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.configcenter.code.infrastructure.repository.mapper.CfgCodeRuleMapper;
import com.transcend.plm.configcenter.code.infrastructure.repository.po.CfgCodeRulePo;
import com.transcend.plm.configcenter.code.pojo.CfgCodeRuleConverter;
import com.transcend.plm.configcenter.code.pojo.qo.CfgCodeRulePoQo;
import com.transcend.plm.configcenter.code.pojo.vo.CfgCodeRulePoVo;
import com.transcend.plm.configcenter.common.annotation.CheckTableDataExist;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.dao.tool.PageResultTools;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-03-01 15:16
 **/
@Service
public class CfgCodeRuleServiceImpl extends ServiceImpl<CfgCodeRuleMapper, CfgCodeRulePo>
implements CfgCodeRuleService {

    /**
     * 根据bid进行更新
     *
     * @param cfgCodeRule
     * @return
     */
    @Override
    public CfgCodeRulePo updateByBid(CfgCodeRulePo cfgCodeRule) {
        Assert.hasText(cfgCodeRule.getBid(),"bid is blank");
        this.update(cfgCodeRule,Wrappers.<CfgCodeRulePo>lambdaUpdate()
                .eq(CfgCodeRulePo::getBid, cfgCodeRule.getBid())
        );
        return cfgCodeRule;
    }

    /**
     * 根据Bid获取详细
     *
     * @param bid
     * @return
     */
    @Override
    public CfgCodeRulePo getByBid(String bid) {
        Assert.hasText(bid, "bid is blank");
        return this.getOne(Wrappers.<CfgCodeRulePo>lambdaQuery().eq(CfgCodeRulePo::getBid, bid));
    }

    @Override
    public PagedResult<CfgCodeRulePoVo> pageByCfgCodeRuleQo(BaseRequest<CfgCodeRulePoQo> pageQo) {
        CfgCodeRulePo cfgCodeRule = CfgCodeRuleConverter.INSTANCE.qo2po(pageQo.getParam());
        Page<CfgCodeRulePo> page = new Page<>(pageQo.getCurrent(), pageQo.getSize());
        LambdaQueryWrapper<CfgCodeRulePo> queryWrapper = Wrappers.<CfgCodeRulePo>lambdaQuery()
                .eq(StringUtil.isNotBlank(cfgCodeRule.getCode()), CfgCodeRulePo::getCode, cfgCodeRule.getCode())
                .eq(StringUtil.isNotBlank(cfgCodeRule.getName()), CfgCodeRulePo::getName, cfgCodeRule.getName())
                .eq(StringUtil.isNotBlank(cfgCodeRule.getCreatedBy()), CfgCodeRulePo::getCreatedBy, cfgCodeRule.getCreatedBy())
                .eq(StringUtil.isNotBlank(cfgCodeRule.getUpdatedBy()), CfgCodeRulePo::getUpdatedBy, cfgCodeRule.getUpdatedBy());
        IPage<CfgCodeRulePo> iPage = this.page(page,queryWrapper);
        List<CfgCodeRulePoVo> cfgCodeRuleVos = CfgCodeRuleConverter.INSTANCE.pos2vos(iPage.getRecords());
        return PageResultTools.create(iPage,cfgCodeRuleVos);
    }

    @Override
    @CheckTableDataExist(tableName = "cfg_code_rule", fieldName = "bid",exist = true)
    public boolean logicalDeleteByBid(String bid) {
        Assert.hasText(bid,"bid is blank");
        return this.remove(Wrappers.<CfgCodeRulePo>lambdaQuery().eq(CfgCodeRulePo::getBid, bid));
    }

    @Override
    public List<CfgCodeRulePo> listByQo(CfgCodeRulePoQo qo) {
        LambdaQueryWrapper<CfgCodeRulePo> queryWrapper = Wrappers.<CfgCodeRulePo>lambdaQuery()
                .eq(StringUtil.isNotBlank(qo.getCode()), CfgCodeRulePo::getCode, qo.getCode())
                .eq(StringUtil.isNotBlank(qo.getName()), CfgCodeRulePo::getName, qo.getName())
                .eq(StringUtil.isNotBlank(qo.getCreatedBy()), CfgCodeRulePo::getCreatedBy, qo.getCreatedBy())
                .eq(StringUtil.isNotBlank(qo.getUpdatedBy()), CfgCodeRulePo::getUpdatedBy, qo.getUpdatedBy());
        return this.list(queryWrapper);
    }
}
