package com.transcend.plm.datadriven.apm.flow.pojo.ao;

import lombok.Data;

import java.util.List;

/**
 * @author unknown
 */
@Data
public class ApmFlowNodeLineAO {
    /**
     *
     */
    private String bid;

    /**
     * web_bid,source node web_bid+to+target node web_bid+LINE
     */
    private String webBid;

    /**
     * 生命周期模板id
     */
    private String templateBid;

    /**
     * 版本号
     */
    private String version;

    /**
     * 开始节点bid
     */
    private String sourceNodeBid;

    /**
     * 结束节点bid
     */
    private String targetNodeBid;

    /**
     * 开始节点web bid
     */
    private String sourceWebBid;

    /**
     * 结束节点web bid
     */
    private String targetWebBid;

    /**
     * 开始节点code
     */
    private String sourceNodeCode;

    /**
     * 结束节点code
     */
    private String targetNodeCode;


    private List<ApmFlowLineEventAO> apmFlowLineEventAOList;
}
