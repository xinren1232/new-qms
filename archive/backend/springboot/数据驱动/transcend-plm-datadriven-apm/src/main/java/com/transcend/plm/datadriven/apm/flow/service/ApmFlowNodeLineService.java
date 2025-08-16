package com.transcend.plm.datadriven.apm.flow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowNodeLine;

import java.util.List;

/**
 * @author unknown
 */
public interface ApmFlowNodeLineService extends IService<ApmFlowNodeLine> {

    /**
     * 根据模板ID和版本号列出流程节点连线列表。
     *
     * @param tempBid 模板ID
     * @param version 版本号
     * @return 匹配模板ID和版本号的流程节点连线列表
     */
    List<ApmFlowNodeLine> listNodeLinesByTempBidAndVersion(String tempBid, String version);

}
