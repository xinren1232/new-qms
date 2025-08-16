package com.transcend.plm.configcenter.lifecycle.infrastructure.repository.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.CfgLifeCycleTemplateNodeConverter;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.CfgLifeCycleTemplateNodeDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.TemplateDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.qo.CfgLifeCycleTemplateNodeQo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.vo.CfgLifeCycleTemplateNodeVo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.mapper.CfgLifeCycleTemplateNodeMapper;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.CfgLifeCycleTemplateNodePo;
import com.transsion.framework.common.BeanUtil;
import com.transsion.framework.common.StringUtil;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 *
 */
@Service
public class CfgLifeCycleTemplateNodeServiceImpl extends
        ServiceImpl<CfgLifeCycleTemplateNodeMapper, CfgLifeCycleTemplateNodePo> implements CfgLifeCycleTemplateNodeService {

    @Override
    public void saveCfgLifeCycleTemplateNode(List<CfgLifeCycleTemplateNodeDto> dtos) {
        List<CfgLifeCycleTemplateNodePo> cfgLifeCycleTemplateNodes = CfgLifeCycleTemplateNodeConverter.INSTANCE.dtos2pos(dtos);
        for (CfgLifeCycleTemplateNodePo cfgLifeCycleTemplateNode : cfgLifeCycleTemplateNodes) {
            if (StringUtil.isBlank(cfgLifeCycleTemplateNode.getBid())) {
                cfgLifeCycleTemplateNode.setBid(SnowflakeIdWorker.nextIdStr());
            }
        }
        saveBatch(cfgLifeCycleTemplateNodes);
    }

    @Override
    public List<CfgLifeCycleTemplateNodePo> getCfgLifeCycleTemplateNode(TemplateDto templateDto){
        LambdaQueryWrapper<CfgLifeCycleTemplateNodePo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(CfgLifeCycleTemplateNodePo::getTemplateBid, templateDto.getTemplateBid());
        queryWrapper.eq(CfgLifeCycleTemplateNodePo::getVersion, templateDto.getVersion());
        queryWrapper.orderBy(true, true, CfgLifeCycleTemplateNodePo::getSort);
        List<CfgLifeCycleTemplateNodePo> list = list(queryWrapper);
        return list;
    }

    @Override
    public CfgLifeCycleTemplateNodePo getCfgLifeCycleTemplateNode(CfgLifeCycleTemplateNodeQo cfgLifeCycleTemplateNodeQo){
        LambdaQueryWrapper<CfgLifeCycleTemplateNodePo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(CfgLifeCycleTemplateNodePo::getTemplateBid, cfgLifeCycleTemplateNodeQo.getTemplateBid());
        queryWrapper.eq(CfgLifeCycleTemplateNodePo::getVersion, cfgLifeCycleTemplateNodeQo.getVersion());
        queryWrapper.eq(CfgLifeCycleTemplateNodePo::getLifeCycleCode, cfgLifeCycleTemplateNodeQo.getLifeCycleCode());
        CfgLifeCycleTemplateNodePo cfgLifeCycleTemplateNodePo = getOne(queryWrapper);
        return cfgLifeCycleTemplateNodePo;
    }
    @Override
    public List<CfgLifeCycleTemplateNodePo> getCfgLifeCycleTemplateNodeByBids(List<String> bids,String templateBid,String version){
        LambdaQueryWrapper<CfgLifeCycleTemplateNodePo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.in(CfgLifeCycleTemplateNodePo::getBid, bids);
        queryWrapper.eq(CfgLifeCycleTemplateNodePo::getTemplateBid, templateBid);
        queryWrapper.eq(CfgLifeCycleTemplateNodePo::getVersion, version);
        return list(queryWrapper);
    }

    @Override
    public List<CfgLifeCycleTemplateNodeVo> getCfgLifeCycleTemplateNodeVos(TemplateDto templateDto) {
        LambdaQueryWrapper<CfgLifeCycleTemplateNodePo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(CfgLifeCycleTemplateNodePo::getTemplateBid, templateDto.getTemplateBid());
        queryWrapper.eq(CfgLifeCycleTemplateNodePo::getVersion, templateDto.getVersion());
        List<CfgLifeCycleTemplateNodePo> list = list(queryWrapper);
        List<CfgLifeCycleTemplateNodeVo> resList = CfgLifeCycleTemplateNodeConverter.INSTANCE.pos2vos(list);
        return resList;
    }

    @Override
    public boolean deleteByTempBid(String bid){
        LambdaUpdateWrapper<CfgLifeCycleTemplateNodePo> lambdaUpdateWrapper = new LambdaUpdateWrapper<CfgLifeCycleTemplateNodePo>();
        lambdaUpdateWrapper.eq(CfgLifeCycleTemplateNodePo::getTemplateBid, bid);
        lambdaUpdateWrapper.set(CfgLifeCycleTemplateNodePo::getDeleteFlag, 1);
        lambdaUpdateWrapper.set(CfgLifeCycleTemplateNodePo::getUpdatedTime, new Date());
        return update(lambdaUpdateWrapper);
    }
}
