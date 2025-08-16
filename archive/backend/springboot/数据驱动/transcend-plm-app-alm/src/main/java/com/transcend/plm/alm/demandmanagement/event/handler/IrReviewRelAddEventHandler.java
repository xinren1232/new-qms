package com.transcend.plm.alm.demandmanagement.event.handler;

import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.apm.constants.FlowInstanceNodeProperties;
import com.transcend.plm.datadriven.apm.constants.FlowNodeStateConstant;
import com.transcend.plm.datadriven.apm.event.entity.RelationAddEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.relation.AbstractRelationAddEventHandler;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowInstanceNodeService;
import com.transcend.plm.datadriven.apm.flow.util.FlowCheckConstant;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.AddExpandAo;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.model.SpaceAppDataEnum;
import com.transcend.plm.datadriven.apm.space.service.IBaseApmSpaceAppDataDrivenService;
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
 * @Describe IR评审信息关系新增处理事件
 * @Author yuanhu.huang
 * @Date 2024/8/14
 */
@Slf4j
@Component
public class IrReviewRelAddEventHandler extends AbstractRelationAddEventHandler {

    /**
     * RELATION_MODELCODE
     */
    private static final String RELATION_MODELCODE = "A6A";

    @Resource
    private ApmFlowInstanceNodeService apmFlowInstanceNodeService;

    @Resource
    private FlowInstanceNodeProperties flowInstanceNodeProperties;

    /**
     * SOURCE_MODELCODE
     */
    private static final String SOURCE_MODELCODE = "A5G";
    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Resource
    private IBaseApmSpaceAppDataDrivenService iBaseApmSpaceAppDataDrivenService;

    @Override
    public RelationAddEventHandlerParam preHandle(RelationAddEventHandlerParam param) {
        return super.preHandle(param);
    }

    @Override
    public Object postHandle(RelationAddEventHandlerParam param, Object result) {
        AddExpandAo addExpandAo = param.getAddExpandAo();
        String sourceBid = addExpandAo.getSourceBid();
        List<MSpaceAppData> targetMObjects = addExpandAo.getTargetMObjects();
        if(CollectionUtils.isNotEmpty(targetMObjects)){
            //将最新一条的IR评审信息的 需求优先级和需求等级更新到 IR实例中
            MSpaceAppData targetMObject = targetMObjects.get(targetMObjects.size()-1);
            MObject updateMobject = new MObject();
            updateMobject.put("requirementlevel",targetMObject.get("requirementlevel"));
            updateMobject.put("demandPriority",targetMObject.get("demandPriority"));
            List<String> sourceBids = new ArrayList<>();
            sourceBids.add(sourceBid);
            objectModelCrudI.batchUpdatePartialContentByIds(SOURCE_MODELCODE,updateMobject,sourceBids);
            //因为这个字段是关系属性，所以要使用
            MSpaceAppData updateData = new MSpaceAppData();
            //updateData.put("approvalosversion", targetMObject.get("approvalosversion"));
            MObject sourceData = objectModelCrudI.getByBid(SOURCE_MODELCODE, sourceBid);
            //如果当前流程节点是【价值决策】,并且结论值为Go或者Go with risk，评论内容为需求价值，将结论值和核定tOS版本更新到IR中
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
            iBaseApmSpaceAppDataDrivenService.updatePartialContent(sourceData.get(SpaceAppDataEnum.SPACE_APP_BID.getCode()).toString(), sourceBid, updateData);
        }
        return super.postHandle(param, result);
    }

    @Override
    public boolean isMatch(RelationAddEventHandlerParam param) {
        AddExpandAo addExpandAo = param.getAddExpandAo();
        if (addExpandAo != null) {
            return RELATION_MODELCODE.equals(addExpandAo.getRelationModelCode());
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
