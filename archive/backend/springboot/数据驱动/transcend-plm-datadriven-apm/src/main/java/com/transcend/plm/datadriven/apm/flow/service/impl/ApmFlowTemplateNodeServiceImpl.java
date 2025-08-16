package com.transcend.plm.datadriven.apm.flow.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.datadriven.apm.constants.CacheNameConstant;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowTemplateNode;
import com.transcend.plm.datadriven.apm.flow.repository.mapper.ApmFlowTemplateNodeMapper;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowTemplateNodeService;
import com.transcend.plm.datadriven.common.tool.CommonConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author unknown
 */
@Service
public class ApmFlowTemplateNodeServiceImpl extends ServiceImpl<ApmFlowTemplateNodeMapper, ApmFlowTemplateNode>
    implements ApmFlowTemplateNodeService {

    @Override
    //@Cacheable(value = CacheNameConstant.FLOW_TEMPLATE_NODE, key = "#tempBid+'_'+#version")
    public List<ApmFlowTemplateNode> listByTemplateBidAndVersion(String templateBid, String version) {
        List<ApmFlowTemplateNode> list = list(Wrappers.<ApmFlowTemplateNode>lambdaQuery()
                .eq(ApmFlowTemplateNode::getFlowTemplateBid, templateBid)
                .eq(StringUtils.isNotBlank(version),ApmFlowTemplateNode::getVersion,version).eq(ApmFlowTemplateNode::getDeleteFlag,false));
        return list;
    }

    @Override
    public ApmFlowTemplateNode getByBid(String bid) {
        return getOne(Wrappers.<ApmFlowTemplateNode>lambdaQuery().eq(ApmFlowTemplateNode::getBid, bid));
    }

    @Override
    public ApmFlowTemplateNode getByTemplateAndWebBid(String sourceFlowTemplateBid, String sourceNodeBid) {
        List<ApmFlowTemplateNode> apmFlowTemplateNodes = list(Wrappers.<ApmFlowTemplateNode>lambdaQuery().eq(ApmFlowTemplateNode::getFlowTemplateBid, sourceFlowTemplateBid).eq(ApmFlowTemplateNode::getWebBid,sourceNodeBid).isNotNull(ApmFlowTemplateNode::getDataBid));
        ApmFlowTemplateNode apmFlowTemplateNode = null;
        if(CollectionUtils.isNotEmpty(apmFlowTemplateNodes)){
            apmFlowTemplateNode = apmFlowTemplateNodes.get(apmFlowTemplateNodes.size()-1);
        }
        return apmFlowTemplateNode;
    }

    @Override
    public Map<String, String> getDataBidMap(String flowTemplateBid, List<String> webBids) {
        List<ApmFlowTemplateNode> list = list(Wrappers.<ApmFlowTemplateNode>lambdaQuery().eq(ApmFlowTemplateNode::getFlowTemplateBid, flowTemplateBid).in(ApmFlowTemplateNode::getWebBid,webBids));
        Map<String,String> resMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        if(CollectionUtils.isNotEmpty(list)){
            for(ApmFlowTemplateNode apmFlowTemplateNode:list){
                if(StringUtils.isNotEmpty(apmFlowTemplateNode.getDataBid())){
                    resMap.put(apmFlowTemplateNode.getWebBid(),apmFlowTemplateNode.getDataBid());
                }
            }
        }
        return resMap;
    }
}




