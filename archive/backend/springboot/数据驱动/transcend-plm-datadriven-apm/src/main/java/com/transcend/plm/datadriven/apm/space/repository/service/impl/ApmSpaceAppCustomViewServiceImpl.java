package com.transcend.plm.datadriven.apm.space.repository.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.datadriven.apm.space.repository.mapper.ApmSpaceAppCustomViewMapper;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceAppCustomViewPo;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppCustomViewService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author jie.luo1
 * @description ApmSpaceAppCustomView
 * @createDate 2023-09-20 16:15:29
 */
@Service
public class ApmSpaceAppCustomViewServiceImpl extends ServiceImpl<ApmSpaceAppCustomViewMapper, ApmSpaceAppCustomViewPo>
        implements ApmSpaceAppCustomViewService {


    @Override
    public Boolean removeByBid(String bid) {
        return remove(Wrappers.<ApmSpaceAppCustomViewPo>lambdaQuery().eq(ApmSpaceAppCustomViewPo::getBid, bid));
    }

    @Override
    public List<ApmSpaceAppCustomViewPo> listByCondition(ApmSpaceAppCustomViewPo viewModel) {
        //根据apmRole的值进行查询
        return list(Wrappers.<ApmSpaceAppCustomViewPo>lambdaQuery()
                .eq(StringUtils.isNotBlank(viewModel.getBid()), ApmSpaceAppCustomViewPo::getBid, viewModel.getBid())
                .eq(StringUtils.isNotBlank(viewModel.getCategory()), ApmSpaceAppCustomViewPo::getCategory, viewModel.getCategory())
                .like(StringUtils.isNotBlank(viewModel.getName()), ApmSpaceAppCustomViewPo::getName, viewModel.getName())
                .eq(StringUtils.isNotBlank(viewModel.getSpaceAppBid()), ApmSpaceAppCustomViewPo::getSpaceAppBid, viewModel.getSpaceAppBid())
                .eq(viewModel.getEnableFlag() != null, ApmSpaceAppCustomViewPo::getEnableFlag, viewModel.getEnableFlag())
                .eq(ApmSpaceAppCustomViewPo::getDeleteFlag, 0)
                .orderByDesc(Boolean.TRUE, ApmSpaceAppCustomViewPo::getSort)
        );
    }

    @Override
    public Boolean changeEnableFlag(String bid, Integer enableFlag) {
        ApmSpaceAppCustomViewPo po = new ApmSpaceAppCustomViewPo();
        po.setEnableFlag(enableFlag);
        return update(
                po
                ,Wrappers.<ApmSpaceAppCustomViewPo>lambdaUpdate()
                .eq(ApmSpaceAppCustomViewPo::getBid, bid)
        );
    }

    @Override
    public Boolean copy(Map<String, String> appBidMap) {
        List<ApmSpaceAppCustomViewPo> list = list(Wrappers.<ApmSpaceAppCustomViewPo>lambdaQuery().in(ApmSpaceAppCustomViewPo::getSpaceAppBid, appBidMap.keySet()).eq(ApmSpaceAppCustomViewPo::getDeleteFlag,false));
        if(CollectionUtil.isEmpty(list)){
            return true;
        }
        for(ApmSpaceAppCustomViewPo apmSpaceAppViewModel:list){
            apmSpaceAppViewModel.setId(null);
            apmSpaceAppViewModel.setSpaceAppBid(appBidMap.get(apmSpaceAppViewModel.getSpaceAppBid()));
            Map<String, Object> configContent = apmSpaceAppViewModel.getConfigContent();
            if(CollectionUtil.isNotEmpty(configContent)){
                String jsonStr = JSON.toJSONString(configContent);
                for(Map.Entry<String,String> entry:appBidMap.entrySet()){
                    //配置内容的bid替换
                    jsonStr = jsonStr.replaceAll(entry.getKey(),entry.getValue());
                }
                Map<String, Object> configContentNew = JSON.parseObject(jsonStr);
                apmSpaceAppViewModel.setConfigContent(configContentNew);
            }
        }
        return saveBatch(list);
    }

    /**
     * @param apmSpaceAppCustomViewPo
     * @return
     */
    @Override
    public Boolean updateByBid(ApmSpaceAppCustomViewPo po) {
        return this.update(po, Wrappers.<ApmSpaceAppCustomViewPo>lambdaUpdate()
                .eq(ApmSpaceAppCustomViewPo::getBid, po.getBid())
        );
    }

    /**
     * @param customViewBid
     * @return
     */
    @Override
    public ApmSpaceAppCustomViewPo getByBid(String customViewBid) {
        return getOne(Wrappers.<ApmSpaceAppCustomViewPo>lambdaQuery().eq(ApmSpaceAppCustomViewPo::getBid, customViewBid));
    }


    @Override
    public boolean save(ApmSpaceAppCustomViewPo entity) {
        return super.save(entity);
    }
}




