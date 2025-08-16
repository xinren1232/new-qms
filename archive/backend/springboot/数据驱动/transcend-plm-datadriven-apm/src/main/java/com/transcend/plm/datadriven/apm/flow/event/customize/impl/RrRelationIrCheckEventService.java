package com.transcend.plm.datadriven.apm.flow.event.customize.impl;

import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.api.model.RelationEnum;
import com.transcend.plm.datadriven.apm.flow.event.customize.IFlowCompleteCheckEvent;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transsion.framework.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author haijun.Ren
 * @date 2024/10/08
 * @description 校验RR是否关联了Ir
 */
@Service("rrRelationIrCheckEventService")
public class RrRelationIrCheckEventService implements IFlowCompleteCheckEvent {

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    /** 领域 modelCode **/
    private String rrIrRelationModelCode = "A0A";

    /** IR实现状态列表 **/
    @Value("#{'${transcend.plm.apm.ir.LifeCycleCode.develop:DEVELOP,TEST,CHECK,COMPLETE}'.split(',')}")
    private List<String> irLifeCycleCodeDevelop;

    /** IR实现状态列表 **/
    @Value("#{'${transcend.plm.apm.ir.LifeCycleCode.check:CHECK,COMPLETE}'.split(',')}")
    private List<String> irLifeCycleCodeCheck;


    /** RR分发节点bid **/
    @Value("#{'${transcend.plm.apm.rr.flow.node.distribute.dataBid:2313131321321312}'}")
    private String rrDistributeNodeDataBid;

    /** RR实现节点bid **/
    @Value("#{'${transcend.plm.apm.rr.flow.node.achieve.dataBid:23123213213213213}'}")
    private String rrAchieveNodeDataBid;


    /** 领域 modelCode **/
    private String irModelCode = "A01";

    @Override
    public void check(ApmSpaceApp app, ApmFlowInstanceNode currentNode, MObject mObject) {
        QueryWrapper qo = new QueryWrapper();
        qo.eq(RelationEnum.SOURCE_BID.getColumn(), currentNode.getInstanceBid());
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        List<MObject>  mObjects = objectModelCrudI.list(rrIrRelationModelCode, queryWrappers);
        if (CollectionUtils.isEmpty(mObjects)){
            throw new BusinessException("请先关联IR！");
        }
        List<String> irBids = mObjects.stream().map(v->v.get(RelationEnum.TARGET_BID.getCode()).toString()).collect(Collectors.toList());
        //校验IR状态
        List<MObject>  irMObjects = objectModelCrudI.listByBids(irBids, irModelCode);
        if (CollectionUtils.isEmpty(irMObjects)){
            throw new BusinessException("请先关联IR！");
        }
        //当前节点是分发,需要校验关联的IR任意一个是开发状态
        if (rrDistributeNodeDataBid.equals(currentNode.getTemplateNodeDataBid())) {
            if (irMObjects.stream().noneMatch(v-> irLifeCycleCodeDevelop.contains(v.getLifeCycleCode()))){
                throw new BusinessException("请先修改IR状态！");
            }
        }
        //当前节点是实现,需要校验关联的IR所有状态是实现状态
        if (rrAchieveNodeDataBid.equals(currentNode.getTemplateNodeDataBid())) {
            if (!irMObjects.stream().allMatch(v-> irLifeCycleCodeCheck.contains(v.getLifeCycleCode()))){
                throw new BusinessException("请先修改IR状态！");
            }
        }

    }
}
