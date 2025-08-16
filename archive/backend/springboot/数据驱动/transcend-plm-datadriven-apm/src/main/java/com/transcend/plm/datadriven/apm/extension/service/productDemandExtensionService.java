package com.transcend.plm.datadriven.apm.extension.service;

import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.apm.flow.pojo.vo.ApmFlowInstanceVO;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import com.transcend.plm.datadriven.apm.flow.service.IRuntimeService;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmStateQo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmNotifyExecuteRecord;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jinpeng.bai
 * @version v1.0.0
 * @description 产品需求扩展业务
 * @date 2024/04/26 11:18
 **/
@Service
public class productDemandExtensionService {


    @Resource
    private ObjectModelStandardI objectModelStandardI;
    @Resource
    private IRuntimeService runtimeService;
    /**
     * 产品需求再次确认消息发送确认
     * @param apmNotifyExecuteRecord 消息体
     */
    public boolean sendReviewMsgValidation(ApmNotifyExecuteRecord apmNotifyExecuteRecord) {
        final MObject insData = objectModelStandardI.getByBid(apmNotifyExecuteRecord.getModelCode(), apmNotifyExecuteRecord.getInstanceBid());
        if(insData == null){
            return false;
        }
        //查看流程是否处于再次确认节点
        ApmStateQo apmStateQo = new ApmStateQo();
        apmStateQo.setInstanceBid(apmNotifyExecuteRecord.getInstanceBid());
        apmStateQo.setLcTemplBid(insData.getLcTemplBid());
        apmStateQo.setLcTemplVersion(insData.getLcTemplVersion());
        final ApmFlowInstanceVO flowNodeList = runtimeService.listInstanceNodesByApmStateQo(apmStateQo);
        if(flowNodeList == null){
            return false;
        }
        List<ApmFlowInstanceNode> nodes = flowNodeList.getNodes().stream().filter(e-> e.getNodeState() == 1).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(nodes)){
            return false;
        }
        List<String> nodeName = nodes.stream().map(ApmFlowInstanceNode::getNodeName).collect(Collectors.toList());
        String matchStr = "产品需求挂起确认";
        if(nodeName.contains(matchStr)){
            return true;
        }
        return false;
    }

}
