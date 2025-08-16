package com.transcend.plm.configcenter.lifecycle.infrastructure.repository.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.dao.tool.PageResultTools;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.configcenter.api.model.lifecycle.qo.LifeCycleStateListQo;
import com.transcend.plm.configcenter.api.model.lifecycle.vo.LifeCycleStateVo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.CfgLifeCycleStateConverter;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.LifeCycleStateDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.mapper.CfgLifeCycleStateMapper;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.CfgLifeCycleStatePo;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class CfgLifeCycleStateServiceImpl extends ServiceImpl<CfgLifeCycleStateMapper, CfgLifeCycleStatePo>
    implements CfgLifeCycleStateService{

    @Resource
    private CfgLifeCycleStateMapper cfgLifeCycleStateMapper;

    @Override
    public List<LifeCycleStateVo> list(LifeCycleStateListQo lifeCycleStateListQo){
        LambdaQueryWrapper<CfgLifeCycleStatePo> queryWrapper = Wrappers.<CfgLifeCycleStatePo> lambdaQuery();

        if (StringUtil.isNotBlank(lifeCycleStateListQo.getName())) {
            queryWrapper.like(CfgLifeCycleStatePo::getName, lifeCycleStateListQo.getName());
        }
        if (lifeCycleStateListQo.getEnableFlag() != null) {
            queryWrapper.eq(CfgLifeCycleStatePo::getEnableFlag, lifeCycleStateListQo.getEnableFlag());
        }
        queryWrapper.eq(CfgLifeCycleStatePo::getDeleteFlag,0);
        if (lifeCycleStateListQo.getStartDate() != null) {
            queryWrapper.ge(CfgLifeCycleStatePo::getUpdatedTime, lifeCycleStateListQo.getStartDate());
        }
        if (lifeCycleStateListQo.getEndDate() != null) {
            queryWrapper.le(CfgLifeCycleStatePo::getUpdatedTime, lifeCycleStateListQo.getEndDate());
        }
        List<CfgLifeCycleStatePo> list = list(queryWrapper);
        List<LifeCycleStateVo> resList = CfgLifeCycleStateConverter.INSTANCE.pos2vos(list);
        return resList;
    }

    @Override
    public List<CfgLifeCycleStatePo> listByCodes(List<String> codes) {
        LambdaQueryWrapper<CfgLifeCycleStatePo> queryWrapper = Wrappers.<CfgLifeCycleStatePo> lambdaQuery();
        queryWrapper.in(CfgLifeCycleStatePo::getCode,codes);
        return list(queryWrapper);
    }

    @Override
    public boolean deleteByBid(String bid) {
        int count = cfgLifeCycleStateMapper.deleteByBid(bid);
        if(count > 0){
            return true;
        }
        return false;
    }

    public boolean checkNameAndCode(CfgLifeCycleStatePo po){
        long count = count(Wrappers.<CfgLifeCycleStatePo>lambdaQuery().eq(CfgLifeCycleStatePo::getName, po.getName()).or().eq(CfgLifeCycleStatePo::getCode, po.getCode()));
        if (count > 0) {
            return true;
        }
        return false;
    }

    public boolean checkName(String name){
        long count = count(Wrappers.<CfgLifeCycleStatePo>lambdaQuery().eq(CfgLifeCycleStatePo::getName, name));
        if (count > 0) {
            return true;
        }
        return false;
    }

    public boolean checkCode(String code){
        long count = count(Wrappers.<CfgLifeCycleStatePo>lambdaQuery().eq(CfgLifeCycleStatePo::getCode, code));
        if (count > 0) {
            return true;
        }
        return false;
    }

    public boolean checkNameAndCodes(List<CfgLifeCycleStatePo> poList){
        List<String> names = poList.stream().map(CfgLifeCycleStatePo::getName).collect(Collectors.toList());
        List<String> codes = poList.stream().map(CfgLifeCycleStatePo::getCode).collect(Collectors.toList());
        long count = count(Wrappers.<CfgLifeCycleStatePo>lambdaQuery().in(CfgLifeCycleStatePo::getName, names).or().in(CfgLifeCycleStatePo::getCode, codes));
        if (count > 0) {
            return true;
        }
        return false;

    }

    public CfgLifeCycleStatePo getByBid(String bid){
        CfgLifeCycleStatePo po = getOne(Wrappers.<CfgLifeCycleStatePo>lambdaQuery().eq(CfgLifeCycleStatePo::getBid, bid));
        return po;
    }

    @Override
    public boolean editByBid(LifeCycleStateDto dto) {
        String jobNumber = SsoHelper.getJobNumber();
        return  update(Wrappers.<CfgLifeCycleStatePo>lambdaUpdate()
                .set(StringUtil.isNotBlank(dto.getName()), CfgLifeCycleStatePo::getName, dto.getName())
                .set(StringUtil.isNotBlank(dto.getCode()), CfgLifeCycleStatePo::getCode, dto.getCode())
                .set(StringUtil.isNotBlank(dto.getDescription()), CfgLifeCycleStatePo::getDescription, dto.getDescription())
                .set(StringUtil.isNotBlank(dto.getGroupCode()), CfgLifeCycleStatePo::getGroupCode, dto.getGroupCode())
                .set(StringUtil.isNotBlank(dto.getColor()), CfgLifeCycleStatePo::getColor, dto.getColor())
                .set(CfgLifeCycleStatePo::getUpdatedTime,new Date())
                .set(dto.getEnableFlag() != null, CfgLifeCycleStatePo::getEnableFlag, dto.getEnableFlag())
                .set(StringUtil.isNotBlank(jobNumber), CfgLifeCycleStatePo::getUpdatedBy, jobNumber)
                .eq(CfgLifeCycleStatePo::getBid, dto.getBid()));
    }

    @Override
    public PagedResult<LifeCycleStateVo> page(BaseRequest<LifeCycleStateListQo> pageQo) {
        LifeCycleStateListQo lifeCycleStateListQo = pageQo.getParam();
        Page<CfgLifeCycleStatePo> page = new Page<>(pageQo.getCurrent(), pageQo.getSize());
        LambdaQueryWrapper<CfgLifeCycleStatePo> queryWrapper = Wrappers.<CfgLifeCycleStatePo> lambdaQuery();

        if (StringUtil.isNotBlank(lifeCycleStateListQo.getName())) {
            queryWrapper.like(CfgLifeCycleStatePo::getName, lifeCycleStateListQo.getName());
        }
        if (StringUtil.isNotBlank(lifeCycleStateListQo.getCode())) {
            queryWrapper.eq(CfgLifeCycleStatePo::getCode, lifeCycleStateListQo.getCode());
        }
        if (StringUtil.isNotBlank(lifeCycleStateListQo.getName())) {
            queryWrapper.like(CfgLifeCycleStatePo::getName, lifeCycleStateListQo.getName());
        }
        if (lifeCycleStateListQo.getEnableFlag() != null) {
            queryWrapper.eq(CfgLifeCycleStatePo::getEnableFlag, lifeCycleStateListQo.getEnableFlag());
        }
        queryWrapper.eq(CfgLifeCycleStatePo::getDeleteFlag,0);
        if (lifeCycleStateListQo.getStartDate() != null) {
            queryWrapper.ge(CfgLifeCycleStatePo::getUpdatedTime, lifeCycleStateListQo.getStartDate());
        }
        if (lifeCycleStateListQo.getEndDate() != null) {
            queryWrapper.le(CfgLifeCycleStatePo::getUpdatedTime, lifeCycleStateListQo.getEndDate());
        }
        IPage<CfgLifeCycleStatePo> iPage = page(page, queryWrapper);
        List<LifeCycleStateVo> cfgLifeCycleStateVos = CfgLifeCycleStateConverter.INSTANCE.pos2vos(iPage.getRecords());
        return PageResultTools.create(iPage, cfgLifeCycleStateVos);
    }
}




