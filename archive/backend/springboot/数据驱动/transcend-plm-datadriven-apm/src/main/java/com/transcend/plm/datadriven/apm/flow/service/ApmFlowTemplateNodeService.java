package com.transcend.plm.datadriven.apm.flow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowTemplateNode;

import java.util.List;
import java.util.Map;

/**
 * @author unknown
 */
public interface ApmFlowTemplateNodeService extends IService<ApmFlowTemplateNode> {

    /**
     * 根据模板业务ID和版本号查询流程模板节点列表。
     *
     * @param templateBid 模板业务ID
     * @param version 版本号
     * @return 符合条件的流程模板节点列表
     */
    List<ApmFlowTemplateNode> listByTemplateBidAndVersion(String templateBid,String version);

    /**
     * 根据节点业务ID获取流程模板节点。
     *
     * @param bid 节点业务ID
     * @return 匹配的流程模板节点
     */
    ApmFlowTemplateNode getByBid(String bid);

    /**
     * 根据源流程模板业务ID和源节点业务ID获取匹配的流程模板节点。
     *
     * @param sourceFlowTemplateBid 源流程模板业务ID
     * @param sourceNodeBid 源节点业务ID
     * @return 匹配的流程模板节点
     */
    ApmFlowTemplateNode getByTemplateAndWebBid(String sourceFlowTemplateBid, String sourceNodeBid);

    /**
     * 根据流程模板业务ID和节点业务ID列表，获取数据业务ID的映射关系。
     *
     * @param flowTemplateBid 流程模板业务ID
     * @param webBids 节点业务ID列表
     * @return 包含数据业务ID的映射关系的Map
     */
    Map<String,String> getDataBidMap(String flowTemplateBid,List<String> webBids);
}
