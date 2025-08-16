package com.transcend.plm.configcenter.language.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.configcenter.common.annotation.CheckTableDataExist;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.dao.tool.PageResultTools;
import com.transcend.plm.configcenter.language.infrastructure.repository.mapper.CfgLanguageMapper;
import com.transcend.plm.configcenter.language.infrastructure.repository.po.CfgLanguagePo;
import com.transcend.plm.configcenter.language.pojo.CfgLanguageConverter;
import com.transcend.plm.configcenter.language.pojo.qo.CfgLanguageQo;
import com.transcend.plm.configcenter.language.pojo.vo.CfgLanguageVo;
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
 * @Date 2023-02-22 10:12
 **/
@Service
public class CfgLanguageServiceImpl extends ServiceImpl<CfgLanguageMapper, CfgLanguagePo>
implements CfgLanguageService {

    /**
     * 根据bid进行更新
     *
     * @param cfgLanguagePo
     * @return
     */
    @Override
    public CfgLanguagePo updateByBid(CfgLanguagePo cfgLanguagePo) {
        Assert.hasText(cfgLanguagePo.getBid(),"bid is blank");
        this.update(cfgLanguagePo,Wrappers.<CfgLanguagePo>lambdaUpdate()
                .eq(CfgLanguagePo::getBid, cfgLanguagePo.getBid())
        );
        return cfgLanguagePo;
    }

    /**
     * 根据Bid获取详细
     *
     * @param bid
     * @return
     */
    @Override
    public CfgLanguagePo getByBid(String bid) {
        Assert.hasText(bid, "bid is blank");
        return this.getOne(Wrappers.<CfgLanguagePo>lambdaQuery().eq(CfgLanguagePo::getBid, bid));
    }

    @Override
    public PagedResult<CfgLanguageVo> pageByCfgLanguageQo(BaseRequest<CfgLanguageQo> pageQo) {
        CfgLanguagePo cfgLanguagePo = CfgLanguageConverter.INSTANCE.qo2po(pageQo.getParam());
        Page<CfgLanguagePo> page = new Page<>(pageQo.getCurrent(), pageQo.getSize());
        LambdaQueryWrapper<CfgLanguagePo> queryWrapper = Wrappers.<CfgLanguagePo>lambdaQuery()
                .eq(StringUtil.isNotBlank(cfgLanguagePo.getCode()), CfgLanguagePo::getCode, cfgLanguagePo.getCode())
                .eq(StringUtil.isNotBlank(cfgLanguagePo.getName()), CfgLanguagePo::getName, cfgLanguagePo.getName())
                .eq(StringUtil.isNotBlank(cfgLanguagePo.getCreatedBy()), CfgLanguagePo::getCreatedBy, cfgLanguagePo.getCreatedBy())
                .eq(StringUtil.isNotBlank(cfgLanguagePo.getUpdatedBy()), CfgLanguagePo::getUpdatedBy, cfgLanguagePo.getUpdatedBy());
        IPage<CfgLanguagePo> iPage = this.page(page,queryWrapper);
        List<CfgLanguageVo> cfgLanguageVos = CfgLanguageConverter.INSTANCE.pos2vos(iPage.getRecords());
        return PageResultTools.create(iPage,cfgLanguageVos);
    }

    @Override
    @CheckTableDataExist(tableName = "cfg_language", fieldName = "bid",exist = true)
    public boolean logicalDeleteByBid(String bid) {
        Assert.hasText(bid,"bid is blank");
        return this.remove(Wrappers.<CfgLanguagePo>lambdaQuery().eq(CfgLanguagePo::getBid, bid));
    }
}
