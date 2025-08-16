package com.transcend.plm.configcenter.lifecycle.infrastructure.repository.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.CfgLifeCycleTemplateDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.mapper.CfgLifeCycleTemplateVersionMapper;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.CfgLifeCycleTemplateVersionPo;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 *
 */
@Service
public class CfgLifeCycleTemplateVersionServiceImpl extends ServiceImpl<CfgLifeCycleTemplateVersionMapper, CfgLifeCycleTemplateVersionPo>
    implements CfgLifeCycleTemplateVersionService {

    @Override
    public long countByTemplateBid(String templateBid) {
        LambdaQueryWrapper<CfgLifeCycleTemplateVersionPo> queryWrapper = Wrappers.<CfgLifeCycleTemplateVersionPo>lambdaQuery();
        queryWrapper.eq(CfgLifeCycleTemplateVersionPo::getTemplateBid,templateBid);
        long count = count(queryWrapper);
        return count;
    }

    @Override
    public CfgLifeCycleTemplateVersionPo getCfgLifeCycleTemplateVersion(String templateBid, String version){
        LambdaQueryWrapper<CfgLifeCycleTemplateVersionPo> queryWrapper = Wrappers.<CfgLifeCycleTemplateVersionPo>lambdaQuery();
        queryWrapper.eq(CfgLifeCycleTemplateVersionPo::getTemplateBid,templateBid);
        queryWrapper.eq(CfgLifeCycleTemplateVersionPo::getVersion,version);
        return getOne(queryWrapper);
    }

    @Override
    public List<CfgLifeCycleTemplateVersionPo> getVersions(CfgLifeCycleTemplateDto cfgLifeCycleTemplateDto) {
        LambdaQueryWrapper<CfgLifeCycleTemplateVersionPo> queryWrapper = Wrappers.<CfgLifeCycleTemplateVersionPo>lambdaQuery();
        queryWrapper.eq(CfgLifeCycleTemplateVersionPo::getTemplateBid,cfgLifeCycleTemplateDto.getBid());
        List<CfgLifeCycleTemplateVersionPo> list = list(queryWrapper);
        return list;
    }

    @Override
    public boolean deleteByTempBid(String bid){
        LambdaUpdateWrapper<CfgLifeCycleTemplateVersionPo> lambdaUpdateWrapper = new LambdaUpdateWrapper<CfgLifeCycleTemplateVersionPo>();
        lambdaUpdateWrapper.eq(CfgLifeCycleTemplateVersionPo::getTemplateBid, bid);
        lambdaUpdateWrapper.set(CfgLifeCycleTemplateVersionPo::getDeleteFlag, 1);
        lambdaUpdateWrapper.set(CfgLifeCycleTemplateVersionPo::getUpdatedTime, new Date());
        return update(lambdaUpdateWrapper);
    }
}




