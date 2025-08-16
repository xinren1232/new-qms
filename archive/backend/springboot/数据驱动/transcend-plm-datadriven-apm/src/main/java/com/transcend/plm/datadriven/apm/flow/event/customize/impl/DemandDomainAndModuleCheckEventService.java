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
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yuanhu.huang
 * @date 2024/10/08
 * @description 校验RR，IR需求下是否有领域和模块
 */
@Service("demandDomainAndModuleCheckEventService")
public class DemandDomainAndModuleCheckEventService implements IFlowCompleteCheckEvent {

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    /** 领域组件 modelCode **/
    private String domainComponentModelCode = "A0K";
    /** 领域组件 rr_bid **/
    private String rrBidCoumn = "rr_bid";
    /** 模块 modelCode **/
    private String moudleModelCode = "A07";

    /** 领域 modelCode **/
    private String domainModelCode = "A06";

    /** 领域 modelCode字段 **/
    private String domainModelCodeCoumn = "domainModelCode";

    /** 领域 bid字段 **/
    private String bidCoumn = "bid";

    @Override
    public void check(ApmSpaceApp app, ApmFlowInstanceNode currentNode, MObject mObject) {
        QueryWrapper qo = new QueryWrapper();
        qo.eq(RelationEnum.SPACE_BID.getColumn(), app.getSpaceBid()).and().eq(rrBidCoumn, currentNode.getInstanceBid());
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        List<MObject>  mObjects = objectModelCrudI.list(domainComponentModelCode, queryWrappers);
        if (CollectionUtils.isNotEmpty(mObjects)){
            //校验领域下是否有模块
            Map<String,String> parentMap = new HashMap<>();
            for(MObject mObject1 : mObjects){
                if(moudleModelCode.equals(mObject1.get(domainModelCodeCoumn))){
                    parentMap.put(mObject1.get("parentBid")+"",mObject1.get(bidCoumn)+"");
                }
            }
            for(MObject mObject1 : mObjects){
                if(domainModelCode.equals(mObject1.get(domainModelCodeCoumn)) && !parentMap.containsKey(mObject1.get(bidCoumn)+"")){
                    throw new BusinessException("请选择领域及领域内的模块，模块必选！");
                }
            }
        }

    }
}
