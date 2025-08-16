package com.transcend.plm.configcenter.lifecycle.infrastructure.repository.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.CfgLifeCycleTemplateTransitionLineConverter;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.CfgLifeCycleTemplateTransitionLineDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.TemplateDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.vo.CfgLifeCycleTemplateTransitionLineVo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.mapper.CfgLifeCycleTemplateTransitionLineMapper;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.CfgLifeCycleTemplateTransitionLinePo;
import com.transsion.framework.common.BeanUtil;
import com.transsion.framework.common.StringUtil;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 *
 */
@Service
public class CfgLifeCycleTemplateTransitionLineServiceImpl
    extends ServiceImpl<CfgLifeCycleTemplateTransitionLineMapper, CfgLifeCycleTemplateTransitionLinePo>
    implements CfgLifeCycleTemplateTransitionLineService {

    @Override
    public void saveCfgLifeCycleTemplateTransitionLineDto(List<CfgLifeCycleTemplateTransitionLineDto> dtos) {
        List<CfgLifeCycleTemplateTransitionLinePo> cfgLifeCycleTemplateTransitionLines = BeanUtil.copy(dtos,
            CfgLifeCycleTemplateTransitionLinePo.class);
        for (CfgLifeCycleTemplateTransitionLinePo cfgLifeCycleTemplateTransitionLine : cfgLifeCycleTemplateTransitionLines) {
            if (StringUtil.isBlank(cfgLifeCycleTemplateTransitionLine.getBid())) {
                cfgLifeCycleTemplateTransitionLine.setBid(SnowflakeIdWorker.nextIdStr());
            }
        }
        saveBatch(cfgLifeCycleTemplateTransitionLines);
    }

    @Override
    public List<CfgLifeCycleTemplateTransitionLinePo> getCfgLifeCycleTemplateTransitionLine(TemplateDto templateDto){
        LambdaQueryWrapper<CfgLifeCycleTemplateTransitionLinePo> queryWrapper = Wrappers
                .lambdaQuery();
        queryWrapper.eq(CfgLifeCycleTemplateTransitionLinePo::getTemplateBid, templateDto.getTemplateBid());
        queryWrapper.eq(CfgLifeCycleTemplateTransitionLinePo::getTemplateVersion, templateDto.getVersion());
        if(StringUtil.isNotBlank(templateDto.getSourceBid())){
            queryWrapper.eq(CfgLifeCycleTemplateTransitionLinePo::getSource, templateDto.getSourceBid());
        }
        List<CfgLifeCycleTemplateTransitionLinePo> list = list(queryWrapper);
        return list;
    }

    @Override
    public List<CfgLifeCycleTemplateTransitionLineVo> getCfgLifeCycleTemplateTransitionLineVos(
        TemplateDto templateDto) {
        LambdaQueryWrapper<CfgLifeCycleTemplateTransitionLinePo> queryWrapper = Wrappers
            .lambdaQuery();
        queryWrapper.eq(CfgLifeCycleTemplateTransitionLinePo::getTemplateBid, templateDto.getTemplateBid());
        queryWrapper.eq(CfgLifeCycleTemplateTransitionLinePo::getTemplateVersion, templateDto.getVersion());
        List<CfgLifeCycleTemplateTransitionLinePo> list = list(queryWrapper);
        List<CfgLifeCycleTemplateTransitionLineVo> resList = CfgLifeCycleTemplateTransitionLineConverter.INSTANCE.pos2vos(list);
        return resList;
    }

    @Override
    public boolean deleteByTempBid(String bid){
        LambdaUpdateWrapper<CfgLifeCycleTemplateTransitionLinePo> lambdaUpdateWrapper = new LambdaUpdateWrapper<CfgLifeCycleTemplateTransitionLinePo>();
        lambdaUpdateWrapper.eq(CfgLifeCycleTemplateTransitionLinePo::getTemplateBid, bid);
        lambdaUpdateWrapper.set(CfgLifeCycleTemplateTransitionLinePo::getDeleteFlag, 1);
        lambdaUpdateWrapper.set(CfgLifeCycleTemplateTransitionLinePo::getUpdatedTime, new Date());
        return update(lambdaUpdateWrapper);
    }
}
