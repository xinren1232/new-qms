package com.transcend.plm.configcenter.objectrelation.infrastructure.repository.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.dao.tool.PageResultTools;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.configcenter.objectrelation.converter.CfgObjectRelationConverter;
import com.transcend.plm.configcenter.api.model.objectrelation.dto.CfgObjectRelationDto;
import com.transcend.plm.configcenter.api.model.objectrelation.qo.CfgObjectRelationQo;
import com.transcend.plm.configcenter.api.model.objectrelation.vo.CfgObjectRelationVo;
import com.transcend.plm.configcenter.objectrelation.infrastructure.repository.mapper.CfgObjectRelationMapper;
import com.transcend.plm.configcenter.objectrelation.infrastructure.repository.po.CfgObjectRelationPo;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.context.holder.UserContextHolder;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.configcenter.objectrelation.infrastructure.repository.service.CfgObjectRelationService;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 *
 */
@Service
public class CfgObjectRelationServiceImpl extends ServiceImpl<CfgObjectRelationMapper, CfgObjectRelationPo>
        implements CfgObjectRelationService {

    @Override
    public PagedResult<CfgObjectRelationVo> page(BaseRequest<CfgObjectRelationQo> pageQo) {
        CfgObjectRelationQo cfgObjectRelationQo = pageQo.getParam();
        Page<CfgObjectRelationPo> page = new Page<>(pageQo.getCurrent(), pageQo.getSize());
        LambdaQueryWrapper<CfgObjectRelationPo> queryWrapper = Wrappers.<CfgObjectRelationPo> lambdaQuery();
        if (StringUtil.isNotBlank(cfgObjectRelationQo.getName())) {
            queryWrapper.like(CfgObjectRelationPo::getName, cfgObjectRelationQo.getName());
        }
        if (StringUtil.isNotBlank(cfgObjectRelationQo.getTabName())) {
            queryWrapper.like(CfgObjectRelationPo::getTabName, cfgObjectRelationQo.getTabName());
        }
        if (cfgObjectRelationQo.getEnableFlag() != null) {
            queryWrapper.eq(CfgObjectRelationPo::getEnableFlag, cfgObjectRelationQo.getEnableFlag());
        }
        queryWrapper.eq(CfgObjectRelationPo::getDeleteFlag, 0);
        if (StringUtil.isNotBlank(cfgObjectRelationQo.getSourceModelCode())) {
            queryWrapper.eq(CfgObjectRelationPo::getSourceModelCode, cfgObjectRelationQo.getSourceModelCode());
        }
        if (StringUtil.isNotBlank(cfgObjectRelationQo.getTargetModelCode())) {
            queryWrapper.eq(CfgObjectRelationPo::getTargetModelCode, cfgObjectRelationQo.getTargetModelCode());
        }
        IPage<CfgObjectRelationPo> iPage = this.page(page, queryWrapper);
        List<CfgObjectRelationVo> cfgLifeCycleTemplateVos = CfgObjectRelationConverter.INSTANCE
                .pos2vos(iPage.getRecords());
        return PageResultTools.create(iPage, cfgLifeCycleTemplateVos);
    }

    @Override
    public boolean updateByBid(CfgObjectRelationDto cfgObjectRelationDto) {
        LambdaUpdateWrapper<CfgObjectRelationPo> lambdaUpdateWrapper = new LambdaUpdateWrapper<CfgObjectRelationPo>();
        lambdaUpdateWrapper.eq(CfgObjectRelationPo::getBid, cfgObjectRelationDto.getBid());
        lambdaUpdateWrapper.set(CfgObjectRelationPo::getDescription, cfgObjectRelationDto.getDescription());
        lambdaUpdateWrapper.set(CfgObjectRelationPo::getUpdatedTime, new Date());
        lambdaUpdateWrapper.set(CfgObjectRelationPo::getBehavior, cfgObjectRelationDto.getBehavior());
        lambdaUpdateWrapper.set(CfgObjectRelationPo::getType, cfgObjectRelationDto.getType());
        lambdaUpdateWrapper.set(CfgObjectRelationPo::getMaxNumber, cfgObjectRelationDto.getMaxNumber());
        lambdaUpdateWrapper.set(CfgObjectRelationPo::getMinNumber, cfgObjectRelationDto.getMinNumber());
        lambdaUpdateWrapper.set(CfgObjectRelationPo::getHideTab, cfgObjectRelationDto.getHideTab());
        lambdaUpdateWrapper.set(CfgObjectRelationPo::getIsRequired, cfgObjectRelationDto.getIsRequired());
        lambdaUpdateWrapper.set(CfgObjectRelationPo::getTargetModelCode, cfgObjectRelationDto.getTargetModelCode());
        lambdaUpdateWrapper.set(CfgObjectRelationPo::getSourceModelCode, cfgObjectRelationDto.getSourceModelCode());
        lambdaUpdateWrapper.set(CfgObjectRelationPo::getTabName, cfgObjectRelationDto.getTabName());
        String jobNumber = UserContextHolder.getUser().getCode();
        lambdaUpdateWrapper.set(StringUtil.isNotBlank(jobNumber),CfgObjectRelationPo::getUpdatedBy,jobNumber);
        return update(lambdaUpdateWrapper);
    }

    @Override
    public boolean changeEnableFlag(String bid, Integer enableFlag) {
        LambdaUpdateWrapper<CfgObjectRelationPo> lambdaUpdateWrapper = new LambdaUpdateWrapper<CfgObjectRelationPo>();
        lambdaUpdateWrapper.eq(CfgObjectRelationPo::getBid, bid);
        lambdaUpdateWrapper.set(CfgObjectRelationPo::getEnableFlag, enableFlag);
        String jobNumber = SsoHelper.getJobNumber();
        lambdaUpdateWrapper.set(StringUtil.isNotBlank(jobNumber),CfgObjectRelationPo::getUpdatedBy,jobNumber);
        lambdaUpdateWrapper.set(CfgObjectRelationPo::getUpdatedTime, new Date());
        return update(lambdaUpdateWrapper);
    }

    @Override
    public List<CfgObjectRelationVo> list(CfgObjectRelationQo cfgObjectRelationQo) {
        LambdaQueryWrapper<CfgObjectRelationPo> queryWrapper = Wrappers.<CfgObjectRelationPo> lambdaQuery();
        if(StringUtil.isNotBlank(cfgObjectRelationQo.getModelCode())){
            queryWrapper.eq(CfgObjectRelationPo::getModelCode,cfgObjectRelationQo.getModelCode());
        }
        if(StringUtil.isNotBlank(cfgObjectRelationQo.getTargetModelCode())){
            queryWrapper.eq(CfgObjectRelationPo::getTargetModelCode, cfgObjectRelationQo.getTargetModelCode());
        }
        if(StringUtil.isNotBlank(cfgObjectRelationQo.getSourceModelCode())){
            queryWrapper.eq(CfgObjectRelationPo::getSourceModelCode,cfgObjectRelationQo.getSourceModelCode());
        }
        if (Objects.nonNull(cfgObjectRelationQo.getDeleteFlag())) {
            queryWrapper.eq(CfgObjectRelationPo::getDeleteFlag, cfgObjectRelationQo.getDeleteFlag());
        }
        if (Objects.nonNull(cfgObjectRelationQo.getEnableFlag())) {
            queryWrapper.eq(CfgObjectRelationPo::getEnableFlag, cfgObjectRelationQo.getEnableFlag());
        }
        List<CfgObjectRelationPo> cfgObjectRelationPos = list(queryWrapper);
        List<CfgObjectRelationVo> cfgLifeCycleTemplateVos = CfgObjectRelationConverter.INSTANCE
                .pos2vos(cfgObjectRelationPos);
        return cfgLifeCycleTemplateVos;
    }

}
