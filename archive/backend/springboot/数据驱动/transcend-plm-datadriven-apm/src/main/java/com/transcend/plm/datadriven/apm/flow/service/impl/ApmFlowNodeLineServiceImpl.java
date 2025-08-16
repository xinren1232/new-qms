package com.transcend.plm.datadriven.apm.flow.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.datadriven.apm.flow.repository.mapper.ApmFlowNodeLineMapper;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowNodeLine;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowNodeLineService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import com.transcend.plm.datadriven.apm.constants.CacheNameConstant;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Service
public class ApmFlowNodeLineServiceImpl extends ServiceImpl<ApmFlowNodeLineMapper, ApmFlowNodeLine>
    implements ApmFlowNodeLineService {

    @Override
    //@Cacheable(value = CacheNameConstant.FLOW_TEMPLATE_NODE_LINE, key = "#tempBid+'_'+#version")
    public List<ApmFlowNodeLine> listNodeLinesByTempBidAndVersion(String tempBid, String version) {
       List<ApmFlowNodeLine> apmFlowNodeLineList = list(Wrappers.<ApmFlowNodeLine>lambdaQuery().eq(ApmFlowNodeLine::getTemplateBid, tempBid).eq(ApmFlowNodeLine::getVersion,version).eq(ApmFlowNodeLine::getDeleteFlag,false));
       return apmFlowNodeLineList;
    }
}




