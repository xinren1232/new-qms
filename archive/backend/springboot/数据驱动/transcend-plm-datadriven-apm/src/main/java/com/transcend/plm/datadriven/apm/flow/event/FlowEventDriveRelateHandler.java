package com.transcend.plm.datadriven.apm.flow.event;

import com.google.common.collect.Lists;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.framework.sso.tool.TranscendUserContextHolder;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.RelationMObject;
import com.transcend.plm.datadriven.api.model.VersionObjectEnum;
import com.transcend.plm.datadriven.apm.constants.FlowEventTypeConstant;
import com.transcend.plm.datadriven.apm.constants.FlowNodeStateConstant;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowDriveRelate;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowTemplate;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowDriveRelateService;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowInstanceNodeService;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowTemplateService;
import com.transcend.plm.datadriven.apm.flow.service.IRuntimeService;
import com.transcend.plm.datadriven.apm.space.service.ApmSpaceApplicationService;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transsion.framework.auth.IUser;
import com.transsion.framework.auth.IUserContext;
import com.transsion.framework.auth.UserContextDto;
import com.transsion.framework.auth.dto.UserLoginDto;
import com.transsion.framework.context.holder.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 流程事件驱动关联事件处理
 * @createTime 2023-10-25 11:55:00
 */
@Component
@Slf4j
public class FlowEventDriveRelateHandler implements IFlowEventHandler {
    @Resource
    private ApmFlowDriveRelateService driveRelateService;
    @Resource
    private ApmSpaceApplicationService apmSpaceApplicationService;
    @Resource
    private IRuntimeService runtimeService;
    @Resource
    private ApmFlowInstanceNodeService apmFlowInstanceNodeService;
    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Resource
    private ApmFlowTemplateService apmFlowTemplateService;


    @Override
    public void handle(FlowEventBO eventBO) {
        try {
            UserLoginDto userLoginDto = new UserLoginDto();
            userLoginDto.setEmployeeNo(eventBO.getCompleteEmpNO());
            userLoginDto.setRealName(eventBO.getCompleteEmpName());
            IUserContext<IUser> userContextDto = new UserContextDto<>(null, userLoginDto);
            UserContextHolder.setUser(userContextDto);
            TranscendUserContextHolder.setUser(userContextDto);
            List<ApmFlowDriveRelate> apmFlowDriveRelates = driveRelateService.listByEventBid(eventBO.getEvent().getBid());
            apmFlowDriveRelates.forEach(apmFlowDriveRelate -> handleOneDriveRelate(apmFlowDriveRelate, eventBO));
        } finally {
            UserContextHolder.removeUser();
            TranscendUserContextHolder.removeUser();
        }
    }

    @Override
    public Integer getEventType() {
        return FlowEventTypeConstant.DRIVE_RELATE;
    }

    private void handleOneDriveRelate(ApmFlowDriveRelate apmFlowDriveRelate, FlowEventBO eventBO) {
        //获取节点关联驱动配置
        String instanceBid = eventBO.getInstanceNode().getInstanceBid();
        log.info("处理驱动关联事件，驱动关联配置={},事件={},节点={}", apmFlowDriveRelate.getBid(), eventBO.getEvent().getBid(), eventBO.getInstanceNode().getBid());
        if (apmFlowDriveRelate.getCompleteType().equals(FlowEventTypeConstant.RELATE_COMPLETE_TYPE_ONE)) {
            //如果是单人完成类型，直接完成源实例对应节点
            completeSourceInstanceNode(apmFlowDriveRelate, instanceBid);
        } else if (apmFlowDriveRelate.getCompleteType().equals(FlowEventTypeConstant.RELATE_COMPLETE_TYPE_ALL)) {
            //如果是全部完成类型，判断源实例下的所有目标实例对应的节点是否都完成，如果都完成，完成源实例对应节点
            checkAndComplete(apmFlowDriveRelate, eventBO, instanceBid);
        }
    }

    private void checkAndComplete(ApmFlowDriveRelate apmFlowDriveRelate, FlowEventBO eventBO, String instanceBid) {
        List<MObject> sourceMObjects = objectModelCrudI.listSourceMObjects(apmFlowDriveRelate.getRelationModelCode(), apmFlowDriveRelate.getSourceModelCode(), Collections.singletonList(instanceBid));
        //分别查看每个源对象对应的目标对象下的节点是否都完成
        sourceMObjects.forEach(sourceMObject -> {
            List<MObject> targetMObjects = objectModelCrudI.listRelationMObjects(RelationMObject.builder()
                    .sourceModelCode(apmFlowDriveRelate.getSourceModelCode())
                    .relationModelCode(apmFlowDriveRelate.getRelationModelCode())
                    .sourceBid(sourceMObject.getBid())
                    .targetModelCode(apmSpaceApplicationService.getModelCodeByAppBid((eventBO.getInstanceNode().getSpaceAppBid())))
                    .build());
            List<ApmFlowInstanceNode> apmFlowInstanceNodes = listFlowInstanceNode(eventBO.getInstanceNode().getTemplateNodeDataBid(), targetMObjects);
            if (CollectionUtils.isEmpty(apmFlowInstanceNodes)){
                return;
            }
            boolean allCompleted = apmFlowInstanceNodes.stream().allMatch(apmFlowInstanceNode -> FlowNodeStateConstant.COMPLETED.equals(apmFlowInstanceNode.getNodeState()));
            if (allCompleted) {
                completeInstanceNode(apmFlowDriveRelate, sourceMObject.get(VersionObjectEnum.DATA_BID.getCode()).toString());
            }
        });
    }

    @Nullable
    private List<ApmFlowInstanceNode> listActiveFlowInstanceNode(String templateNodeDataBid, List<MObject> mObjects) {
        if (CollectionUtils.isEmpty(mObjects)) {
            return Lists.newArrayList();
        }
        List<String> targetInstanceBids = mObjects.stream().map(e -> e.get(VersionObjectEnum.DATA_BID.getCode()).toString()).collect(Collectors.toList());
        return apmFlowInstanceNodeService.listActiveNodesByInstanceAndNode(targetInstanceBids, templateNodeDataBid);
    }

    @Nullable
    private List<ApmFlowInstanceNode> listFlowInstanceNode(String templateNodeDataBid, List<MObject> mObjects) {
        if (CollectionUtils.isEmpty(mObjects)) {
            return Lists.newArrayList();
        }
        List<String> targetInstanceBids = mObjects.stream().map(e -> e.get(VersionObjectEnum.DATA_BID.getCode()).toString()).collect(Collectors.toList());
        return apmFlowInstanceNodeService.listNodesByInstanceAndNode(targetInstanceBids, templateNodeDataBid);
    }

    private void completeSourceInstanceNode(ApmFlowDriveRelate apmFlowDriveRelate, String instanceBid) {
        List<MObject> sourceMObjects = objectModelCrudI.listSourceMObjects(apmFlowDriveRelate.getRelationModelCode(), apmFlowDriveRelate.getSourceModelCode(), Collections.singletonList(instanceBid));
        List<ApmFlowInstanceNode> apmFlowInstanceNodes = listActiveFlowInstanceNode(apmFlowDriveRelate.getSourceNodeBid(), sourceMObjects);
        if (CollectionUtils.isEmpty(apmFlowInstanceNodes)) {
            return;
        }
        apmFlowInstanceNodes.forEach(apmFlowInstanceNode -> runtimeService.completeNodeForce(apmFlowInstanceNode.getBid()));
    }

    private void completeInstanceNode(ApmFlowDriveRelate apmFlowDriveRelate, String instanceBid) {
        List<ApmFlowInstanceNode> apmFlowInstanceNodes =apmFlowInstanceNodeService.listActiveNodesByInstanceAndNode(Lists.newArrayList(instanceBid), apmFlowDriveRelate.getSourceNodeBid());
        if (CollectionUtils.isEmpty(apmFlowInstanceNodes)) {
            return;
        }
        apmFlowInstanceNodes.forEach(apmFlowInstanceNode -> runtimeService.completeNodeForce(apmFlowInstanceNode.getBid()));
    }
}
