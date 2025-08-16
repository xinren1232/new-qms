package com.transcend.plm.configcenter.lifecycle.infrastructure.repository.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.dao.tool.PageResultTools;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.CfgLifeCycleTemplateDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.qo.CfgLifeCycleTemplateQo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.vo.CfgLifeCycleTemplateVo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.CfgLifeCycleTemplateConverter;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.mapper.CfgLifeCycleTemplateMapper;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.CfgLifeCycleTemplatePo;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 *
 */
@Service
public class CfgLifeCycleTemplateServiceImpl extends ServiceImpl<CfgLifeCycleTemplateMapper, CfgLifeCycleTemplatePo>
    implements CfgLifeCycleTemplateService {

    @Override
    public PagedResult<CfgLifeCycleTemplateVo> pageByCfgLifeCycleTemplateQo(
        BaseRequest<CfgLifeCycleTemplateQo> pageQo) {
        CfgLifeCycleTemplateQo cfgLifeCycleTemplateQo = pageQo.getParam();
        Page<CfgLifeCycleTemplatePo> page = new Page<>(pageQo.getCurrent(), pageQo.getSize());
        LambdaQueryWrapper<CfgLifeCycleTemplatePo> queryWrapper = Wrappers.<CfgLifeCycleTemplatePo> lambdaQuery();
        if (StringUtil.isNotBlank(cfgLifeCycleTemplateQo.getName())) {
            queryWrapper.like(CfgLifeCycleTemplatePo::getName, cfgLifeCycleTemplateQo.getName());
        }
        if (cfgLifeCycleTemplateQo.getEnableFlag() != null) {
            queryWrapper.eq(CfgLifeCycleTemplatePo::getEnableFlag, cfgLifeCycleTemplateQo.getEnableFlag());
        }
        queryWrapper.eq(CfgLifeCycleTemplatePo::getDeleteFlag,0);
        if (cfgLifeCycleTemplateQo.getStartDate() != null) {
            queryWrapper.ge(CfgLifeCycleTemplatePo::getUpdatedTime, cfgLifeCycleTemplateQo.getStartDate());
        }
        if (cfgLifeCycleTemplateQo.getEndDate() != null) {
            queryWrapper.le(CfgLifeCycleTemplatePo::getUpdatedTime, cfgLifeCycleTemplateQo.getEndDate());
        }
        IPage<CfgLifeCycleTemplatePo> iPage = this.page(page, queryWrapper);
        List<CfgLifeCycleTemplateVo> cfgLifeCycleTemplateVos = CfgLifeCycleTemplateConverter.INSTANCE
            .pos2vos(iPage.getRecords());
        return PageResultTools.create(iPage, cfgLifeCycleTemplateVos);
    }

    @Override
    public CfgLifeCycleTemplateVo saveCfgLifeCycleTemplate(CfgLifeCycleTemplateDto cfgLifeCycleTemplateDto) {
        CfgLifeCycleTemplatePo cfgLifeCycleTemplate = CfgLifeCycleTemplateConverter.INSTANCE
            .dto2po(cfgLifeCycleTemplateDto);
        save(cfgLifeCycleTemplate);
        CfgLifeCycleTemplateVo cfgLifeCycleTemplateVo = CfgLifeCycleTemplateConverter.INSTANCE
            .po2vo(cfgLifeCycleTemplate);
        return cfgLifeCycleTemplateVo;
    }

    @Override
    public boolean setEnable(CfgLifeCycleTemplateDto cfgLifeCycleTemplateDto){
        LambdaUpdateWrapper<CfgLifeCycleTemplatePo> lambdaUpdateWrapper = new LambdaUpdateWrapper<CfgLifeCycleTemplatePo>();
        lambdaUpdateWrapper.eq(CfgLifeCycleTemplatePo::getBid, cfgLifeCycleTemplateDto.getBid());
        lambdaUpdateWrapper.set(CfgLifeCycleTemplatePo::getEnableFlag, cfgLifeCycleTemplateDto.getEnableFlag());
        lambdaUpdateWrapper.set(CfgLifeCycleTemplatePo::getUpdatedTime, new Date());
        return update(lambdaUpdateWrapper);
    }

    @Override
    public CfgLifeCycleTemplateVo updateDescription(CfgLifeCycleTemplateDto cfgLifeCycleTemplateDto) {
        CfgLifeCycleTemplatePo cfgLifeCycleTemplate = CfgLifeCycleTemplateConverter.INSTANCE
            .dto2po(cfgLifeCycleTemplateDto);
        LambdaUpdateWrapper<CfgLifeCycleTemplatePo> lambdaUpdateWrapper = new LambdaUpdateWrapper<CfgLifeCycleTemplatePo>();
        lambdaUpdateWrapper.eq(CfgLifeCycleTemplatePo::getBid, cfgLifeCycleTemplateDto.getBid());
        lambdaUpdateWrapper.set(CfgLifeCycleTemplatePo::getDescription, cfgLifeCycleTemplateDto.getDescription());
        lambdaUpdateWrapper.set(CfgLifeCycleTemplatePo::getPhaseState, cfgLifeCycleTemplateDto.getPhaseState());
        lambdaUpdateWrapper.set(CfgLifeCycleTemplatePo::getUpdatedTime, new Date());
        update(lambdaUpdateWrapper);
        CfgLifeCycleTemplateVo cfgLifeCycleTemplateVo = CfgLifeCycleTemplateConverter.INSTANCE
            .po2vo(cfgLifeCycleTemplate);
        return cfgLifeCycleTemplateVo;
    }

    @Override
    public boolean setVersion(CfgLifeCycleTemplateDto cfgLifeCycleTemplateDto) {
        LambdaUpdateWrapper<CfgLifeCycleTemplatePo> lambdaUpdateWrapper = new LambdaUpdateWrapper<CfgLifeCycleTemplatePo>();
        lambdaUpdateWrapper.eq(CfgLifeCycleTemplatePo::getBid, cfgLifeCycleTemplateDto.getBid());
        lambdaUpdateWrapper.set(CfgLifeCycleTemplatePo::getCurrentVersion, cfgLifeCycleTemplateDto.getCurrentVersion());
        lambdaUpdateWrapper.set(CfgLifeCycleTemplatePo::getUpdatedTime, new Date());
        return update(lambdaUpdateWrapper);
    }

    @Override
    public boolean delete(String bid) {
        LambdaUpdateWrapper<CfgLifeCycleTemplatePo> lambdaUpdateWrapper = new LambdaUpdateWrapper<CfgLifeCycleTemplatePo>();
        lambdaUpdateWrapper.eq(CfgLifeCycleTemplatePo::getBid, bid);
        lambdaUpdateWrapper.set(CfgLifeCycleTemplatePo::getDeleteFlag, 1);
        lambdaUpdateWrapper.set(CfgLifeCycleTemplatePo::getUpdatedTime, new Date());
        return update(lambdaUpdateWrapper);
    }

    @Override
    public CfgLifeCycleTemplateVo getByBid(String bid) {
        LambdaQueryWrapper<CfgLifeCycleTemplatePo> queryWrapper = Wrappers.<CfgLifeCycleTemplatePo> lambdaQuery();
        queryWrapper.eq(CfgLifeCycleTemplatePo::getBid,bid);
        CfgLifeCycleTemplatePo cfgLifeCycleTemplate = getOne(queryWrapper);
        CfgLifeCycleTemplateVo cfgLifeCycleTemplateVo = CfgLifeCycleTemplateConverter.INSTANCE
                .po2vo(cfgLifeCycleTemplate);
        return cfgLifeCycleTemplateVo;
    }
}
