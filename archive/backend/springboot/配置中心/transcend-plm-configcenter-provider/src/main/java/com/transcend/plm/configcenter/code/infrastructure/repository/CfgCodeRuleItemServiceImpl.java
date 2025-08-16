package com.transcend.plm.configcenter.code.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.configcenter.code.infrastructure.repository.mapper.CfgCodeRuleItemMapper;
import com.transcend.plm.configcenter.code.infrastructure.repository.po.CfgCodeRuleItemPo;
import com.transcend.plm.configcenter.code.pojo.CfgCodeRuleItemConverter;
import com.transcend.plm.configcenter.code.pojo.qo.CfgCodeRuleItemPoQo;
import com.transcend.plm.configcenter.code.pojo.vo.CfgCodeRuleItemPoVo;
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
public class CfgCodeRuleItemServiceImpl extends ServiceImpl<CfgCodeRuleItemMapper, CfgCodeRuleItemPo>
implements CfgCodeRuleItemService {

    /**
     * 根据bid进行更新
     *
     * @param cfgCodeRuleItem
     * @return
     */
    @Override
    public CfgCodeRuleItemPo updateByBid(CfgCodeRuleItemPo cfgCodeRuleItem) {
        Assert.hasText(cfgCodeRuleItem.getBid(),"bid is blank");
        this.update(cfgCodeRuleItem,Wrappers.<CfgCodeRuleItemPo>lambdaUpdate()
                .eq(CfgCodeRuleItemPo::getBid, cfgCodeRuleItem.getBid())
        );
        return cfgCodeRuleItem;
    }

    /**
     * 根据Bid获取详细
     *
     * @param bid
     * @return
     */
    @Override
    public CfgCodeRuleItemPo getByBid(String bid) {
        Assert.hasText(bid, "bid is blank");
        return this.getOne(Wrappers.<CfgCodeRuleItemPo>lambdaQuery().eq(CfgCodeRuleItemPo::getBid, bid));
    }

    @Override
    public PagedResult<CfgCodeRuleItemPoVo> pageByCfgCodeRuleItemQo(BaseRequest<CfgCodeRuleItemPoQo> pageQo) {
        CfgCodeRuleItemPo cfgCodeRuleItem = CfgCodeRuleItemConverter.INSTANCE.qo2po(pageQo.getParam());
        Page<CfgCodeRuleItemPo> page = new Page<>(pageQo.getCurrent(), pageQo.getSize());
        LambdaQueryWrapper<CfgCodeRuleItemPo> queryWrapper = Wrappers.<CfgCodeRuleItemPo>lambdaQuery()
                .eq(StringUtil.isNotBlank(cfgCodeRuleItem.getCodeRuleBid()), CfgCodeRuleItemPo::getCodeRuleBid, cfgCodeRuleItem.getCodeRuleBid())
                .eq(StringUtil.isNotBlank(cfgCodeRuleItem.getType()), CfgCodeRuleItemPo::getType, cfgCodeRuleItem.getType())
                .eq(StringUtil.isNotBlank(cfgCodeRuleItem.getName()), CfgCodeRuleItemPo::getName, cfgCodeRuleItem.getName())
                .eq(StringUtil.isNotBlank(cfgCodeRuleItem.getCreatedBy()), CfgCodeRuleItemPo::getCreatedBy, cfgCodeRuleItem.getCreatedBy())
                .eq(StringUtil.isNotBlank(cfgCodeRuleItem.getUpdatedBy()), CfgCodeRuleItemPo::getUpdatedBy, cfgCodeRuleItem.getUpdatedBy());
        IPage<CfgCodeRuleItemPo> iPage = this.page(page,queryWrapper);
        List<CfgCodeRuleItemPoVo> cfgCodeRuleItemVos = CfgCodeRuleItemConverter.INSTANCE.pos2vos(iPage.getRecords());
        return PageResultTools.create(iPage,cfgCodeRuleItemVos);
    }

    @Override
    @CheckTableDataExist(tableName = "cfg_code_rule_item", fieldName = "bid",exist = true)
    public boolean deleteByBid(String bid) {
        Assert.hasText(bid,"bid is blank");
        return this.remove(Wrappers.<CfgCodeRuleItemPo>lambdaQuery().eq(CfgCodeRuleItemPo::getBid, bid));
    }
}
