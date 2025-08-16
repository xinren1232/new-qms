package com.transcend.plm.datadriven.apm.space.repository.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.datadriven.apm.space.converter.ApmAppExportTemplateConverter;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmAppExportTemplateDto;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmAppExportTemplateVo;
import com.transcend.plm.datadriven.apm.space.repository.mapper.ApmAppExportTemplateMapper;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmAppExportTemplatePo;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmAppExportTemplateService;
import com.transcend.plm.datadriven.common.dao.ConfigCommonMapper;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.common.util.SnowflakeIdWorker;
import com.transsion.framework.common.StringUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author shu.zhang
 * @version 1.0
 * @className ApmAppExportTemplateServiceImpl
 * @description desc
 * @date 2024/5/29 17:38
 */
@Service
public class ApmAppExportTemplateServiceImpl extends ServiceImpl<ApmAppExportTemplateMapper, ApmAppExportTemplatePo> implements ApmAppExportTemplateService {

    @Resource
    private ConfigCommonMapper commonMapper;

    @Resource
    private ApmAppExportTemplateMapper apmAppExportTemplateMapper;

    @Override
    public ApmAppExportTemplateVo save(ApmAppExportTemplateDto dto) {
        Assert.notNull(dto, "attribute is null");
        List<ApmAppExportTemplatePo> list = this.list(new LambdaQueryWrapper<ApmAppExportTemplatePo>()
                .eq(ApmAppExportTemplatePo::getTemplateName, dto.getTemplateName())
                .eq(ApmAppExportTemplatePo::getSpaceBid, dto.getSpaceBid())
                .eq(ApmAppExportTemplatePo::getSpaceAppBid, dto.getSpaceAppBid())
                .eq(ApmAppExportTemplatePo::getDeleteFlag, Boolean.FALSE)
        );
        if (CollectionUtils.isNotEmpty(list)) {
            throw new PlmBizException("模版名称已存在");
        }
        dto.setBid(SnowflakeIdWorker.nextIdStr());
        ApmAppExportTemplatePo po = ApmAppExportTemplateConverter.INSTANCE.dto2po(dto);
        this.save(po);
        return ApmAppExportTemplateConverter.INSTANCE.po2vo(po);
    }

    @Override
    public ApmAppExportTemplateVo update(ApmAppExportTemplateDto dto) {
        Assert.notNull(dto, "attribute is null");
        Assert.hasText(dto.getBid(), "view bid is blank");
        List<ApmAppExportTemplatePo> list = this.list(new LambdaQueryWrapper<ApmAppExportTemplatePo>()
                .ne(ApmAppExportTemplatePo::getBid, dto.getBid())
                .eq(ApmAppExportTemplatePo::getSpaceBid, dto.getSpaceBid())
                .eq(ApmAppExportTemplatePo::getSpaceAppBid, dto.getSpaceAppBid())
                .eq(ApmAppExportTemplatePo::getTemplateName, dto.getTemplateName())
                .eq(ApmAppExportTemplatePo::getDeleteFlag, Boolean.FALSE)
        );
        if (CollectionUtils.isNotEmpty(list)) {
            throw new PlmBizException("模版名称已存在");
        }
        ApmAppExportTemplatePo po = ApmAppExportTemplateConverter.INSTANCE.dto2po(dto);
        this.update(po, Wrappers.<ApmAppExportTemplatePo>lambdaUpdate().eq(ApmAppExportTemplatePo::getBid, po.getBid()));
        return ApmAppExportTemplateConverter.INSTANCE.po2vo(po);
    }

    @Override
    public boolean logicalDeleteByBid(String bid) {
        Assert.hasText(bid, "bid is blank");
        return this.remove(Wrappers.<ApmAppExportTemplatePo>lambdaQuery().eq(ApmAppExportTemplatePo::getBid, bid));
    }

    @Override
    public List<ApmAppExportTemplateVo> queryByCondition(ApmAppExportTemplateDto apmAppExportTemplateDto) {
        List<ApmAppExportTemplatePo> pos = this.list(
                Wrappers.<ApmAppExportTemplatePo>lambdaQuery()
                        .eq(StringUtil.isNotBlank(apmAppExportTemplateDto.getSpaceBid()), ApmAppExportTemplatePo::getSpaceBid, apmAppExportTemplateDto.getSpaceBid())
                        .eq(StringUtil.isNotBlank(apmAppExportTemplateDto.getSpaceAppBid()), ApmAppExportTemplatePo::getSpaceAppBid, apmAppExportTemplateDto.getSpaceAppBid())
                        .eq(ApmAppExportTemplatePo::getDeleteFlag, Boolean.FALSE));
        return ApmAppExportTemplateConverter.INSTANCE.pos2vos(pos);

    }

    @Override
    public ApmAppExportTemplateVo getByBid(String bid) {
        Assert.hasText(bid, "bid is blank");
        ApmAppExportTemplatePo po = this.getOne(Wrappers.<ApmAppExportTemplatePo>lambdaQuery().eq(ApmAppExportTemplatePo::getBid, bid));
        return ApmAppExportTemplateConverter.INSTANCE.po2vo(po);
    }
}
