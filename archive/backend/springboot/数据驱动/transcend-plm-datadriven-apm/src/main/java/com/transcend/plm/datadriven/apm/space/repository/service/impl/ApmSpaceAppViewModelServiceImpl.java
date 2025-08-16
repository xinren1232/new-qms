package com.transcend.plm.datadriven.apm.space.repository.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceAppViewModelPo;
import com.transcend.plm.datadriven.apm.space.repository.mapper.ApmSpaceAppViewModelMapper;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppViewModelService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author peng.qin
 * @description 针对表【apm_role】的数据库操作Service实现
 * @createDate 2023-09-20 16:15:29
 */
@Service
public class ApmSpaceAppViewModelServiceImpl extends ServiceImpl<ApmSpaceAppViewModelMapper, ApmSpaceAppViewModelPo>
        implements ApmSpaceAppViewModelService {


    @Override
    public Boolean removeByBid(String bid) {
        return remove(Wrappers.<ApmSpaceAppViewModelPo>lambdaQuery().eq(ApmSpaceAppViewModelPo::getBid, bid));
    }

    @Override
    public List<ApmSpaceAppViewModelPo> listByCondition(ApmSpaceAppViewModelPo viewModel) {
        //根据apmRole的值进行查询
        return list(Wrappers.<ApmSpaceAppViewModelPo>lambdaQuery()
                .eq(StringUtils.isNotBlank(viewModel.getBid()), ApmSpaceAppViewModelPo::getBid, viewModel.getBid())
                .eq(StringUtils.isNotBlank(viewModel.getCode()), ApmSpaceAppViewModelPo::getCode, viewModel.getCode())
                .like(StringUtils.isNotBlank(viewModel.getName()), ApmSpaceAppViewModelPo::getName, viewModel.getName())
                .eq(StringUtils.isNotBlank(viewModel.getSpaceAppBid()), ApmSpaceAppViewModelPo::getSpaceAppBid, viewModel.getSpaceAppBid())
                .eq(viewModel.getEnableFlag() != null, ApmSpaceAppViewModelPo::getEnableFlag, viewModel.getEnableFlag())
                .eq(ApmSpaceAppViewModelPo::getDeleteFlag, 0)
                .orderByDesc(Boolean.TRUE, ApmSpaceAppViewModelPo::getSort)
        );
    }

    @Override
    public Boolean changeEnableFlag(String spaceAppBid, String viewModelCode, Integer enableFlag) {
        ApmSpaceAppViewModelPo apmSpaceAppViewModelPo = new ApmSpaceAppViewModelPo();
        apmSpaceAppViewModelPo.setEnableFlag(enableFlag);
        return update(
                apmSpaceAppViewModelPo
                ,Wrappers.<ApmSpaceAppViewModelPo>lambdaUpdate()
                .eq(ApmSpaceAppViewModelPo::getSpaceAppBid, spaceAppBid)
                .eq(ApmSpaceAppViewModelPo::getCode, viewModelCode)
        );
    }

    @Override
    public Boolean copyApmSpaceAppViewModel(Map<String, String> appBidMap) {
        List<ApmSpaceAppViewModelPo> list = list(Wrappers.<ApmSpaceAppViewModelPo>lambdaQuery().in(ApmSpaceAppViewModelPo::getSpaceAppBid, appBidMap.keySet()).eq(ApmSpaceAppViewModelPo::getDeleteFlag,false));
        if(CollectionUtil.isEmpty(list)){
            return true;
        }
        for(ApmSpaceAppViewModelPo apmSpaceAppViewModelPo :list){
            apmSpaceAppViewModelPo.setId(null);
            apmSpaceAppViewModelPo.setSpaceAppBid(appBidMap.get(apmSpaceAppViewModelPo.getSpaceAppBid()));
            Map<String, Object> configContent = apmSpaceAppViewModelPo.getConfigContent();
            if(CollectionUtil.isNotEmpty(configContent)){
                String jsonStr = JSON.toJSONString(configContent);
                for(Map.Entry<String,String> entry:appBidMap.entrySet()){
                    //配置内容的bid替换
                    jsonStr = jsonStr.replaceAll(entry.getKey(),entry.getValue());
                }
                Map<String, Object> configContentNew = JSON.parseObject(jsonStr);
                apmSpaceAppViewModelPo.setConfigContent(configContentNew);
            }
        }
        return saveBatch(list);
    }


    @Override
    public boolean save(ApmSpaceAppViewModelPo entity) {
        return super.save(entity);
    }
}




