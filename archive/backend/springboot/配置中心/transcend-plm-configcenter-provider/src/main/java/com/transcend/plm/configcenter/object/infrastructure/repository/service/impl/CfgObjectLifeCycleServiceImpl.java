package com.transcend.plm.configcenter.object.infrastructure.repository.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectLifeCycle;
import com.transcend.plm.configcenter.object.infrastructure.repository.mapper.CfgObjectLifeCycleMapper;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.configcenter.object.infrastructure.repository.service.CfgObjectLifeCycleService;


/**
 *
 */
@Service
public class CfgObjectLifeCycleServiceImpl extends ServiceImpl<CfgObjectLifeCycleMapper, CfgObjectLifeCycle>
    implements CfgObjectLifeCycleService {

    @Override
    public CfgObjectLifeCycle getByModelCode(String modelCode) {
        LambdaQueryWrapper<CfgObjectLifeCycle> queryWrapper = Wrappers.<CfgObjectLifeCycle> lambdaQuery();
        queryWrapper.eq(CfgObjectLifeCycle::getModelCode,modelCode);
        return getOne(queryWrapper);
    }
}




