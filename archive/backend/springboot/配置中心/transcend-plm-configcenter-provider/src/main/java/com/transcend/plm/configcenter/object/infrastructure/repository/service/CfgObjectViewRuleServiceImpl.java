package com.transcend.plm.configcenter.object.infrastructure.repository.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.configcenter.common.annotation.CheckTableDataExist;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.dao.tool.PageResultTools;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectPermissionPo;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectViewRulePo;
import com.transcend.plm.configcenter.object.infrastructure.repository.CfgObjectPermissionService;
import com.transcend.plm.configcenter.object.infrastructure.repository.CfgObjectViewRuleService;
import com.transcend.plm.configcenter.object.infrastructure.repository.mapper.CfgObjectPermissionMapper;
import com.transcend.plm.configcenter.object.infrastructure.repository.mapper.CfgObjectViewRuleMapper;
import com.transcend.plm.configcenter.object.pojo.CfgObjectPermissionConverter;
import com.transcend.plm.configcenter.object.pojo.CfgObjectViewRuleConverter;
import com.transcend.plm.configcenter.api.model.object.qo.CfgObjectPermissionQo;
import com.transcend.plm.configcenter.api.model.object.qo.CfgObjectViewRuleQo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectPermissionVo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectViewRuleVo;
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
public class CfgObjectViewRuleServiceImpl extends ServiceImpl<CfgObjectViewRuleMapper, CfgObjectViewRulePo>
implements CfgObjectViewRuleService {

    /**
     * 根据bid进行更新
     *
     * @param po
     * @return
     */
    @Override
    public CfgObjectViewRulePo updateByBid(CfgObjectViewRulePo po) {
        Assert.hasText(po.getBid(),"bid is blank");
        this.update(po,Wrappers.<CfgObjectViewRulePo>lambdaUpdate()
                .eq(CfgObjectViewRulePo::getBid, po.getBid())
        );
        return po;
    }

    /**
     * 根据Bid获取详细
     *
     * @param bid
     * @return
     */
    @Override
    public CfgObjectViewRulePo getByBid(String bid) {
        Assert.hasText(bid, "bid is blank");
        return this.getOne(Wrappers.<CfgObjectViewRulePo>lambdaQuery().eq(CfgObjectViewRulePo::getBid, bid));
    }

    @Override
    public PagedResult<CfgObjectViewRuleVo> pageByQo(BaseRequest<CfgObjectViewRuleQo> pageQo) {
        CfgObjectViewRulePo po = CfgObjectViewRuleConverter.INSTANCE.qo2po(pageQo.getParam());
        Page<CfgObjectViewRulePo> page = new Page<>(pageQo.getCurrent(), pageQo.getSize());
        LambdaQueryWrapper<CfgObjectViewRulePo> queryWrapper = Wrappers.<CfgObjectViewRulePo>lambdaQuery()
                .eq(StringUtil.isNotBlank(po.getModelCode()), CfgObjectViewRulePo::getModelCode, po.getModelCode())
                .eq(StringUtil.isNotBlank(po.getBaseModel()), CfgObjectViewRulePo::getBaseModel, po.getBaseModel())
                .eq(StringUtil.isNotBlank(po.getCreatedBy()), CfgObjectViewRulePo::getCreatedBy, po.getCreatedBy())
                .eq(StringUtil.isNotBlank(po.getUpdatedBy()), CfgObjectViewRulePo::getUpdatedBy, po.getUpdatedBy());
        IPage<CfgObjectViewRulePo> iPage = this.page(page,queryWrapper);
        List<CfgObjectViewRuleVo> cfgObjectPermissionVos = CfgObjectViewRuleConverter.INSTANCE.pos2vos(iPage.getRecords());
        return PageResultTools.create(iPage,cfgObjectPermissionVos);
    }

    @Override
    @CheckTableDataExist(tableName = "cfg_object_view_rule", fieldName = "bid",exist = true)
    public boolean logicalDeleteByBid(String bid) {
        Assert.hasText(bid,"bid is blank");
        return this.remove(Wrappers.<CfgObjectViewRulePo>lambdaQuery().eq(CfgObjectViewRulePo::getBid, bid));
    }

}
