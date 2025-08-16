package com.transcend.plm.alm.demandmanagement.event.handler;

import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.apm.constants.FlowInstanceNodeProperties;
import com.transcend.plm.datadriven.apm.constants.FlowNodeStateConstant;
import com.transcend.plm.datadriven.apm.event.entity.UpdateEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.instance.AbstractUpdateEventHandler;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowInstanceNodeService;
import com.transcend.plm.datadriven.apm.flow.util.FlowCheckConstant;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.model.SpaceAppDataEnum;
import com.transcend.plm.datadriven.apm.space.service.IBaseApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Describe IR评审信息更新事件
 * @Author yuanhu.huang
 * @Date 2024/8/14
 */
@Slf4j
@Component
public class IrReviewUpdateEventHandler extends AbstractUpdateEventHandler {

    private static final String TARGET_MODELCODE = "A69";

    private static final String RELATION_MODELCODE = "A6A";

    private static final String SOURCE_MODELCODE = "A5G";

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Resource
    private ApmFlowInstanceNodeService apmFlowInstanceNodeService;

    @Resource
    private FlowInstanceNodeProperties flowInstanceNodeProperties;

    @Resource
    private IBaseApmSpaceAppDataDrivenService iBaseApmSpaceAppDataDrivenService;
    @Override
    public Boolean postHandle(UpdateEventHandlerParam param, Boolean result) {
        MSpaceAppData mSpaceAppData = param.getMSpaceAppData();
        //最后一个数据更新IR
        if(mSpaceAppData != null){
            if(mSpaceAppData.containsKey(FlowCheckConstant.REQUIREMENTLEVEL) 
                    || mSpaceAppData.containsKey(FlowCheckConstant.DEMAND_PRIORITY) 
                    || mSpaceAppData.containsKey(FlowCheckConstant.APPROVALOSVERSION)
                    || mSpaceAppData.containsKey(FlowCheckConstant.RMTREVIEWCONTENT)
                    || mSpaceAppData.containsKey( FlowCheckConstant.REVIEWRESULTS)){
               //判断当前数据是不是最后一个数据
                QueryWrapper qo = new QueryWrapper();
                qo.in(RelationEnum.TARGET_BID.getColumn(), mSpaceAppData.getBid());
                List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
                List<MObject> relationList = objectModelCrudI.listByQueryWrapper(RELATION_MODELCODE,queryWrappers);
                if(CollectionUtils.isNotEmpty(relationList)) {
                    QueryWrapper resQo = new QueryWrapper();
                    String sourceBid = relationList.get(0).get(RelationEnum.SOURCE_BID.getCode()) + "";
                    resQo.eq(RelationEnum.SOURCE_BID.getColumn(), sourceBid);
                    List<QueryWrapper> relQueryWrappers = QueryWrapper.buildSqlQo(resQo);
                    List<MObject> allRelationList = objectModelCrudI.listByQueryWrapper(RELATION_MODELCODE, relQueryWrappers);
                    List<String> targetBids = new ArrayList<>();
                    for (MObject relation : allRelationList) {
                        targetBids.add(relation.get(RelationEnum.TARGET_BID.getCode()) + "");
                    }
                    QueryWrapper targetQo = new QueryWrapper();
                    targetQo.in(RelationEnum.BID.getColumn(), targetBids).and().eq("delete_flag", "0");
                    List<QueryWrapper> targetQoQueryWrappers = QueryWrapper.buildSqlQo(targetQo);
                    QueryCondition queryCondition = new QueryCondition();
                    queryCondition.setQueries(targetQoQueryWrappers);
                    Order order = new Order();
                    order.setColumn("created_time");
                    List<Order> orders = new ArrayList<>();
                    orders.add(order);
                    queryCondition.setOrders(orders);
                    List<MObject> targetList = objectModelCrudI.listByQueryCondition(TARGET_MODELCODE, queryCondition);
                    if (CollectionUtils.isNotEmpty(targetList)) {
                        //判断当前修改的是不是最后一条数据
                        if (mSpaceAppData.getBid().equals(targetList.get(targetList.size() - 1).get(TranscendModelBaseFields.BID) + "")) {
                            //更新源数据
                            MObject updateMobject = new MObject();
                            if (mSpaceAppData.containsKey(FlowCheckConstant.REQUIREMENTLEVEL)) {
                                updateMobject.put(FlowCheckConstant.REQUIREMENTLEVEL, mSpaceAppData.get(FlowCheckConstant.REQUIREMENTLEVEL));
                            }
                            if (mSpaceAppData.containsKey(FlowCheckConstant.DEMAND_PRIORITY)) {
                                updateMobject.put(FlowCheckConstant.DEMAND_PRIORITY, mSpaceAppData.get(FlowCheckConstant.DEMAND_PRIORITY));
                            }
                            if (!updateMobject.isEmpty()){
                                List<String> sourceBids = new ArrayList<>();
                                sourceBids.add(sourceBid);
                                objectModelCrudI.batchUpdatePartialContentByIds(SOURCE_MODELCODE, updateMobject, sourceBids);
                            }
                            MSpaceAppData updateData = new MSpaceAppData();
                            MObject targetMObject = targetList.get(targetList.size() - 1);
                            if (!ObjectUtils.isEmpty(targetMObject.get("reviewResults")) &&
                                    Arrays.asList("GO","Go with risk").contains(targetMObject.get(FlowCheckConstant.REVIEWRESULTS).toString()) &&
                                    "Demandvalue".equals(targetMObject.get(FlowCheckConstant.RMTREVIEWCONTENT))) {
                                List<ApmFlowInstanceNode> apmFlowInstanceNodes = apmFlowInstanceNodeService.listByInstanceBidAndState(sourceBid, FlowNodeStateConstant.ACTIVE);
                                if (CollectionUtils.isNotEmpty(apmFlowInstanceNodes) &&
                                        apmFlowInstanceNodes.stream().anyMatch(v->flowInstanceNodeProperties.getIrValueDecision().equals(v.getWebBid()))) {
                                    updateData.put("valueAssessment", targetMObject.get("reviewResults"));
                                    updateData.put("approvalosversion", targetMObject.get("approvalosversion"));
                                }
                            }
                            if  (!updateData.isEmpty()){
                                MObject sourceData = objectModelCrudI.getByBid(SOURCE_MODELCODE, sourceBid);
                                iBaseApmSpaceAppDataDrivenService.updatePartialContent(sourceData.get(SpaceAppDataEnum.SPACE_APP_BID.getCode()).toString(), sourceBid, updateData);
                            }
                        }
                    }
                }
            }
        }
        return super.postHandle(param, result);
    }

    @Override
    public boolean isMatch(UpdateEventHandlerParam param) {
        CfgObjectVo cfgObjectVo = param.getCfgObjectVo();
        if(cfgObjectVo != null){
            return TARGET_MODELCODE.equals(cfgObjectVo.getModelCode());
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
