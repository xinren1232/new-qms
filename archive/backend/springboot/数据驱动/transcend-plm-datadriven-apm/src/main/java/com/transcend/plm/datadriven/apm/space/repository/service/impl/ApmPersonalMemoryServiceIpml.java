package com.transcend.plm.datadriven.apm.space.repository.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Maps;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.apm.space.model.ApmPersonMemoryParam;
import com.transcend.plm.datadriven.apm.space.repository.mapper.ApmPersonMemoryMapper;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmPersonMemory;
import com.transcend.plm.datadriven.apm.space.service.IApmPersonalMemoryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 个人记忆功能
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/10/7 18:29
 * @since 1.0
 */
@Service
public class ApmPersonalMemoryServiceIpml implements IApmPersonalMemoryService {

    @Resource
    private ApmPersonMemoryMapper apmPersonMemoryMapper;

    @Override
    public Boolean saveOrUpdate(ApmPersonMemory apmPersonMemory) {
        if(apmPersonMemory == null) {
            return Boolean.TRUE;
        }
        ApmPersonMemory personalMemory = apmPersonMemoryMapper.selectOne(
                Wrappers.<ApmPersonMemory>lambdaQuery().eq(ApmPersonMemory::getCategory, apmPersonMemory.getCategory()));
        if(personalMemory == null) {
            initPersonalMemory(apmPersonMemory);
            apmPersonMemory.setBid(SnowflakeIdWorker.nextIdStr());
           return apmPersonMemoryMapper.insert(apmPersonMemory)>0;
        }else {
            personalMemory.setContent(apmPersonMemory.getContent());
            return apmPersonMemoryMapper.updateById(personalMemory)>0;
        }
    }

    @Override
    public Map<String, Object> get(ApmPersonMemoryParam apmPersonMemoryParam) {
       ApmPersonMemory result = apmPersonMemoryMapper.selectOne(Wrappers.<ApmPersonMemory>lambdaQuery().eq(ApmPersonMemory::getCategory, apmPersonMemoryParam.getCategory()).
                eq(ApmPersonMemory::getCode, apmPersonMemoryParam.getCode()));
       return Optional.ofNullable(result).map(ApmPersonMemory::getContent).orElse(null);
    }

    @Override
    public Boolean updatePartialContent(ApmPersonMemoryParam apmPersonMemoryParam) {
        ApmPersonMemory personalMemory = apmPersonMemoryMapper.selectOne(Wrappers.<ApmPersonMemory>lambdaQuery().eq(ApmPersonMemory::getCategory, apmPersonMemoryParam.getCategory()).
                eq(ApmPersonMemory::getCode, apmPersonMemoryParam.getCode()));
        JSONObject jsonObject = Optional.ofNullable(personalMemory).map(ApmPersonMemory::getContent).orElse(null);
        if(jsonObject == null) {
            return Boolean.FALSE;
        }
        Map<String,Object> filterMap = Maps.newHashMap();
        Map<String,Object> willUpdateMap = apmPersonMemoryParam.getContent();
         filterMap.putAll(jsonObject);
         apmPersonMemoryParam.getRemoveKeys().forEach(filterMap::remove);
         filterMap.putAll(willUpdateMap);
         personalMemory.setContent(new JSONObject(filterMap));
        return apmPersonMemoryMapper.updateById(personalMemory)>0;
    }

    @Override
    public Map<String, Object> getPartialContent(String category, String code, List<String> keys) {
        ApmPersonMemory apmPersonMemory = apmPersonMemoryMapper.selectOne(Wrappers.<ApmPersonMemory>lambdaQuery().eq(ApmPersonMemory::getCategory, category).
                eq(ApmPersonMemory::getCode, code));
        JSONObject jsonObject = Optional.ofNullable(apmPersonMemory).map(ApmPersonMemory::getContent).orElse(null);
        Map<String,Object> filterMap = Maps.newHashMap();
        if(jsonObject != null) {
              keys.forEach(key -> filterMap.put(key,jsonObject.get(key)));
        }
        return filterMap;
    }

    private void initPersonalMemory(ApmPersonMemory apmPersonMemory) {
        apmPersonMemory.setJobNumber(SsoHelper.getJobNumber());
        apmPersonMemory.setJobName(SsoHelper.getName());
        apmPersonMemory.setCreatedBy(SsoHelper.getJobNumber());
        apmPersonMemory.setCreatedTime(new Date());
        apmPersonMemory.setUpdatedBy(SsoHelper.getJobNumber());
        apmPersonMemory.setUpdatedTime(new Date());
        apmPersonMemory.setEnableFlag(Boolean.TRUE);
    }
}
