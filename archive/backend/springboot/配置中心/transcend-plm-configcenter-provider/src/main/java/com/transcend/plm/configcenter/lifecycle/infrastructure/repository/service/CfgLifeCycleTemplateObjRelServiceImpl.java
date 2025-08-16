package com.transcend.plm.configcenter.lifecycle.infrastructure.repository.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.TemplateDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.qo.CfgLifeCycleTemplateObjRelQo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.mapper.CfgLifeCycleTemplateObjRelMapper;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.CfgLifeCycleTemplateObjRelPo;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 *
 */
@Service
public class CfgLifeCycleTemplateObjRelServiceImpl
        extends ServiceImpl<CfgLifeCycleTemplateObjRelMapper, CfgLifeCycleTemplateObjRelPo>
        implements CfgLifeCycleTemplateObjRelService {

    @Override
    public List<CfgLifeCycleTemplateObjRelPo> getCfgLifeCycleTemplateObjRels(TemplateDto templateDto) {
        LambdaQueryWrapper<CfgLifeCycleTemplateObjRelPo> queryWrapper = Wrappers
                .lambdaQuery();
        queryWrapper.eq(CfgLifeCycleTemplateObjRelPo::getTemplateBid, templateDto.getTemplateBid());
        queryWrapper.eq(CfgLifeCycleTemplateObjRelPo::getTemplateVersion, templateDto.getVersion());
        List<CfgLifeCycleTemplateObjRelPo> list = list(queryWrapper);
        return list;
    }

    @Override
    public CfgLifeCycleTemplateObjRelPo getCfgLifeCycleTemplateObjRel(CfgLifeCycleTemplateObjRelQo cfgLifeCycleTemplateObjRelQo){
        LambdaQueryWrapper<CfgLifeCycleTemplateObjRelPo> queryWrapper = Wrappers
                .lambdaQuery();
        queryWrapper.eq(CfgLifeCycleTemplateObjRelPo::getTemplateBid, cfgLifeCycleTemplateObjRelQo.getTemplateBid());
        queryWrapper.eq(CfgLifeCycleTemplateObjRelPo::getTemplateVersion, cfgLifeCycleTemplateObjRelQo.getVersion());
        queryWrapper.eq(CfgLifeCycleTemplateObjRelPo::getTargetModelCode,cfgLifeCycleTemplateObjRelQo.getModelCode());
        queryWrapper.eq(CfgLifeCycleTemplateObjRelPo::getLifeCycleCode,cfgLifeCycleTemplateObjRelQo.getLifeCycleCode());
        CfgLifeCycleTemplateObjRelPo cfgLifeCycleTemplateObjRelPo = getOne(queryWrapper);
        return cfgLifeCycleTemplateObjRelPo;
    }

    @Override
    public boolean deleteByTempBid(String bid){
        LambdaUpdateWrapper<CfgLifeCycleTemplateObjRelPo> lambdaUpdateWrapper = new LambdaUpdateWrapper<CfgLifeCycleTemplateObjRelPo>();
        lambdaUpdateWrapper.eq(CfgLifeCycleTemplateObjRelPo::getTemplateBid, bid);
        lambdaUpdateWrapper.set(CfgLifeCycleTemplateObjRelPo::getDeleteFlag, 1);
        lambdaUpdateWrapper.set(CfgLifeCycleTemplateObjRelPo::getUpdatedTime, new Date());
        return update(lambdaUpdateWrapper);
    }
}
