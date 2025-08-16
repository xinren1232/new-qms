package com.transcend.plm.datadriven.apm.flow.event.customize.impl;

import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.RelationMObject;
import com.transcend.plm.datadriven.apm.flow.event.customize.IFlowCompleteCheckEvent;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transsion.framework.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName SRPlanNodeCompleteCheckEventService
 * @Author WeiQiang
 * @Date 2024/9/23 16:57
 * @Description SR完成节点校验逻辑
 **/
@Slf4j
@Service(value = "srPlanNodeCompleteCheckEventService")
public class SRPlanNodeCompleteCheckEventService implements IFlowCompleteCheckEvent {


    public static final String ESTIMATED_WORK_LOAD_FIELD_KEY = "estimatedrdworkload";

    public static final String ACTUAL_WORK_LOAD_FIELD_KEY = "actualrdWorkload";

    @Value("${transcend.plm.apm.moudle.arModelCode:A5X}")
    private String arModelCode;

    @Value("${transcend.plm.apm.moudle.arSrRelModelCode:A5Y}")
    private String arSrRelModelCode;

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    /**
     * @param app         app
     * @param currentNode currentNode
     * @param mObject     mObject
     * @Description SR完成节点校验逻辑
     * 前提：预估工时大于0才做校验
     * 1. 校验SR的预估研发工作量和实际研发工作量是否大于0
     * 2. 校验SR下面的AR每条实例数据的预估研发工作量和实际研发工作量是否大于0
     */
    @Override
    public void check(ApmSpaceApp app, ApmFlowInstanceNode currentNode, MObject mObject) {
        // 1. 校验SR的预估研发工作量和实际研发工作量是否大于0
        // 预估研发工作量
        Integer estimatedrdworkload = MapUtils.getInteger(mObject, ESTIMATED_WORK_LOAD_FIELD_KEY);
        // 实际研发工作量
        Integer actualrdWorkload = MapUtils.getInteger(mObject, ACTUAL_WORK_LOAD_FIELD_KEY);
        if ((estimatedrdworkload != null && estimatedrdworkload > 0) && (actualrdWorkload == null || actualrdWorkload <= 0)) {
            throw new BusinessException("当前SR下的预估研发工作量大于0，但实际研发工作量不能为空或小于等于0!");
        }
        // 2. 校验SR下面的AR每条实例数据的预估研发工作量和实际研发工作量是否大于0
        RelationMObject relationMObject = RelationMObject.builder().sourceModelCode(app.getModelCode()).targetModelCode(arModelCode)
                .relationModelCode(arSrRelModelCode).sourceBid(mObject.getBid()).build();
        List<MObject> arMObjects = objectModelCrudI.listRelationMObjects(relationMObject);
        if (CollectionUtils.isNotEmpty(arMObjects)) {
            for (MObject ar : arMObjects) {
                // 预估研发工作量
                Integer arEstimatedrdworkload = MapUtils.getInteger(ar, ESTIMATED_WORK_LOAD_FIELD_KEY);
                // 实际研发工作量
                Integer arActualWorkload = MapUtils.getInteger(ar, ACTUAL_WORK_LOAD_FIELD_KEY);
                if ((arEstimatedrdworkload != null && arEstimatedrdworkload > 0) && (arActualWorkload == null || arActualWorkload <= 0)) {
                    throw new BusinessException("当前SR下的AR[" + MapUtils.getString(ar, "name") + "]预估研发工作量大于0，但实际研发工作量不能为空或小于等于0!");
                }
            }
        }

    }
}
