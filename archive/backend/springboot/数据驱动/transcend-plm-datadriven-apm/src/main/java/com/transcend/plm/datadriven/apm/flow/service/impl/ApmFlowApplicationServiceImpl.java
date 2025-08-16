package com.transcend.plm.datadriven.apm.flow.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Maps;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.apm.constants.FlowNodeTypeConstant;
import com.transcend.plm.datadriven.apm.flow.maspstruct.*;
import com.transcend.plm.datadriven.apm.flow.pojo.ao.*;
import com.transcend.plm.datadriven.apm.flow.pojo.type.FlowTypeEnum;
import com.transcend.plm.datadriven.apm.flow.pojo.vo.*;
import com.transcend.plm.datadriven.apm.flow.repository.po.*;
import com.transcend.plm.datadriven.apm.flow.service.*;
import com.transcend.plm.datadriven.apm.mapstruct.ApmFlowLineEventConverter;
import com.transcend.plm.datadriven.apm.mapstruct.ApmFlowNodeDirectionConditionConverter;
import com.transcend.plm.datadriven.apm.mapstruct.ApmFlowNodeDirectionConverter;
import com.transcend.plm.datadriven.apm.mapstruct.ApmFlowNodeLineConverter;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleAndIdentityVo;
import com.transcend.plm.datadriven.apm.permission.service.impl.ApmRoleDomainService;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmStateQo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmLifeCycleStateVO;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmStateVO;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppConfigManageService;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.common.tool.CommonConstant;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author unknown
 */
@Service
public class ApmFlowApplicationServiceImpl implements ApmFlowApplicationService {

    @Resource
    private ApmFlowNodeDirectionConditionService apmFlowNodeDirectionConditionService;
    @Resource
    private ApmFlowNodeDirectionService apmFlowNodeDirectionService;

    @Resource
    private ApmFlowTemplateService apmFlowTemplateService;
    @Resource
    private ApmFlowTemplateNodeService apmFlowTemplateNodeService;
    @Resource
    private ApmFlowNodeDisplayConditionService apmFlowNodeDisplayConditionService;
    @Resource
    private ApmFlowNodeTaskService apmFlowNodeTaskService;
    @Resource
    private ApmFlowNodeEventService apmFlowNodeEventService;
    @Resource
    private ApmFlowTemplateVersionService apmFlowTemplateVersionService;
    @Resource
    private ApmRoleDomainService apmRoleDomainService;
    @Resource
    private ApmSpaceAppService apmSpaceAppService;
    @Resource
    private ApmFlowDriveRelateService apmFlowDriveRelateService;
    @Resource
    private ApmFlowNodeLineService apmFlowNodeLineService;
    @Resource
    private ApmFlowLineEventService apmFlowLineEventService;
    @Resource
    private ApmFlowInstanceRoleUserService apmFlowInstanceRoleUserService;

    @Resource
    private IApmSpaceAppConfigManageService apmSpaceAppConfigManageService;

    @Resource
    private IRuntimeService runtimeService;

    @Override
    public List<ApmFlowNodeDirectionVO> listDirectionVOByNodeBid(String nodeBid) {
        List<ApmFlowNodeDirectionVO> result = new ArrayList<>();
        List<ApmFlowNodeDirection> list = apmFlowNodeDirectionService.list(Wrappers.<ApmFlowNodeDirection>lambdaQuery().eq(ApmFlowNodeDirection::getSourceNodeBid, nodeBid));
        if(CollectionUtils.isNotEmpty(list)){
            result = ApmFlowNodeDirectionConverter.INSTANCE.entitys2VOs(list);
            //查询条件
            List<String> directionBids = result.stream().map(ApmFlowNodeDirectionVO::getBid).collect(Collectors.toList());
            List<ApmFlowNodeDirectionCondition> apmFlowNodeDirectionConditionList = apmFlowNodeDirectionConditionService.list(Wrappers.<ApmFlowNodeDirectionCondition>lambdaQuery().in(ApmFlowNodeDirectionCondition::getFlowNodeDirectionBid, directionBids));
            if(CollectionUtils.isNotEmpty(apmFlowNodeDirectionConditionList)){
                Map<String, List<ApmFlowNodeDirectionCondition>> map = apmFlowNodeDirectionConditionList.stream().collect(Collectors.groupingBy(ApmFlowNodeDirectionCondition::getFlowNodeDirectionBid));
                result.forEach(apmFlowNodeDirectionVO -> {
                    List<ApmFlowNodeDirectionCondition> apmFlowNodeDirectionConditions = map.get(apmFlowNodeDirectionVO.getBid());
                    apmFlowNodeDirectionVO.setApmFlowNodeDirectionConditionList(apmFlowNodeDirectionConditions);
                });
            }
        }
        return result;
    }

    @Override
    public List<ApmFlowNodeDisplayCondition> getApmFlowNodeDisplayConditions(List<String> nodeBids){
        List<ApmFlowNodeDisplayCondition> apmFlowNodeDisplayConditions = apmFlowNodeDisplayConditionService.list(Wrappers.<ApmFlowNodeDisplayCondition>lambdaQuery().in(ApmFlowNodeDisplayCondition::getNodeBid, nodeBids));
        return apmFlowNodeDisplayConditions;
    }

    @Override
    public boolean copyFlow(Map<String,String> appBidMap, Map<String,String> roleBidMap){

        List<ApmFlowTemplate> apmFlowTemplateList = apmFlowTemplateService.list(Wrappers.<ApmFlowTemplate>lambdaQuery().in(ApmFlowTemplate::getSpaceAppBid, appBidMap.keySet()).eq(ApmFlowTemplate::getDeleteFlag,false));
        if(CollectionUtils.isEmpty(apmFlowTemplateList)){
            return true;
        }
        List<String> flowTemplateBids = apmFlowTemplateList.stream().map(ApmFlowTemplate::getBid).collect(Collectors.toList());
        List<ApmFlowTemplateVersion> apmFlowTemplateVersions = apmFlowTemplateVersionService.list(Wrappers.<ApmFlowTemplateVersion>lambdaQuery().in(ApmFlowTemplateVersion::getSpaceAppBid, appBidMap.keySet()));
        List<ApmFlowTemplateNode> apmFlowTemplateNodes = apmFlowTemplateNodeService.list(Wrappers.<ApmFlowTemplateNode>lambdaQuery().in(ApmFlowTemplateNode::getFlowTemplateBid, flowTemplateBids));
        List<ApmFlowNodeEvent> apmFlowNodeEvents = apmFlowNodeEventService.list(Wrappers.<ApmFlowNodeEvent>lambdaQuery().in(ApmFlowNodeEvent::getFlowTemplateBid, flowTemplateBids));
        List<ApmFlowNodeTask> apmFlowNodeTasks = apmFlowNodeTaskService.list(Wrappers.<ApmFlowNodeTask>lambdaQuery().in(ApmFlowNodeTask::getFlowTemplateBid, flowTemplateBids));
        List<ApmFlowNodeDisplayCondition> apmFlowNodeDisplayConditions = apmFlowNodeDisplayConditionService.list(Wrappers.<ApmFlowNodeDisplayCondition>lambdaQuery().in(ApmFlowNodeDisplayCondition::getFlowTemplateBid, flowTemplateBids));
        List<ApmFlowNodeLine> apmFlowNodeLineList = apmFlowNodeLineService.list(Wrappers.<ApmFlowNodeLine>lambdaQuery().in(ApmFlowNodeLine::getTemplateBid, flowTemplateBids));
        List<ApmFlowLineEvent> apmFlowLineEvents = apmFlowLineEventService.list(Wrappers.<ApmFlowLineEvent>lambdaQuery().in(ApmFlowLineEvent::getFlowTemplateBid, flowTemplateBids));
        //流程数据按照模板+版本过滤
        Map<String,String> tempBidAndVersionMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        Map<String,String> tempBidMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        for(ApmFlowTemplate apmFlowTemplate:apmFlowTemplateList){
            String bid = SnowflakeIdWorker.nextIdStr();
            tempBidMap.put(apmFlowTemplate.getBid(),bid);
        }
        Map<String,String> tempVersionMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        for(ApmFlowTemplate apmFlowTemplate:apmFlowTemplateList){
            tempBidAndVersionMap.put(apmFlowTemplate.getBid()+"-"+apmFlowTemplate.getVersion(),apmFlowTemplate.getBid());
            apmFlowTemplate.setId(null);
            apmFlowTemplate.setSpaceAppBid(appBidMap.get(apmFlowTemplate.getSpaceAppBid()));
            tempVersionMap.put(apmFlowTemplate.getBid(),tempBidMap.get(apmFlowTemplate.getBid()));
            apmFlowTemplate.setBid(tempBidMap.get(apmFlowTemplate.getBid()));
            //更新布局的roleBid
            String layout = apmFlowTemplate.getLayout();
            if(StringUtils.isNotEmpty(layout)){
                for(Map.Entry<String,String> entry:roleBidMap.entrySet()){
                    layout = layout.replaceAll(entry.getKey(),entry.getValue());
                }
                for(Map.Entry<String,String> entry:appBidMap.entrySet()){
                    layout = layout.replaceAll(entry.getKey(),entry.getValue());
                }
                for(Map.Entry<String,String> entry:tempBidMap.entrySet()){
                    layout = layout.replaceAll(entry.getKey(),entry.getValue());
                }
                apmFlowTemplate.setLayout(layout);
            }
        }
        //过滤版本历史表数据
        if(CollectionUtils.isNotEmpty(apmFlowTemplateVersions)){
            for(int i=apmFlowTemplateVersions.size()-1;i>=0;i--){
                if(!tempBidAndVersionMap.containsKey(apmFlowTemplateVersions.get(i).getFlowTemplateBid()+"-"+apmFlowTemplateVersions.get(i).getVersion())){
                    apmFlowTemplateVersions.remove(i);
                }else{
                    apmFlowTemplateVersions.get(i).setId(null);
                    apmFlowTemplateVersions.get(i).setBid(SnowflakeIdWorker.nextIdStr());
                    apmFlowTemplateVersions.get(i).setFlowTemplateBid(tempVersionMap.get(apmFlowTemplateVersions.get(i).getFlowTemplateBid()));
                    apmFlowTemplateVersions.get(i).setSpaceAppBid(appBidMap.get(apmFlowTemplateVersions.get(i).getSpaceAppBid()));
                }
            }
        }
        //过滤节点数据
        Map<String,String> nodeBidMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        //Map<String,String> nodeWebBidMap = new HashMap<>();
        Map<String,Map<String,String>> flowNodeBidMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        Map<String,String> nodeOldWebBidRelMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        if(CollectionUtils.isNotEmpty(apmFlowTemplateNodes)){
            for(int i=apmFlowTemplateNodes.size()-1;i>=0;i--){
                if(!tempBidAndVersionMap.containsKey(apmFlowTemplateNodes.get(i).getFlowTemplateBid()+"-"+apmFlowTemplateNodes.get(i).getVersion())){
                    apmFlowTemplateNodes.remove(i);
                }else{
                    apmFlowTemplateNodes.get(i).setId(null);
                    String bid = SnowflakeIdWorker.nextIdStr();
                    nodeOldWebBidRelMap.put(apmFlowTemplateNodes.get(i).getBid(),apmFlowTemplateNodes.get(i).getWebBid());
                    nodeBidMap.put(apmFlowTemplateNodes.get(i).getBid(),bid);
                    apmFlowTemplateNodes.get(i).setBid(bid);
                    apmFlowTemplateNodes.get(i).setDataBid(bid);
                    apmFlowTemplateNodes.get(i).setNodeState(0);
                    apmFlowTemplateNodes.get(i).setFlowTemplateBid(tempBidMap.get(apmFlowTemplateNodes.get(i).getFlowTemplateBid()));
                    apmFlowTemplateNodes.get(i).setNodeRoleBids(getNewRoleBids(apmFlowTemplateNodes.get(i).getNodeRoleBids(),roleBidMap));
                    apmFlowTemplateNodes.get(i).setComplateRoleBids(getNewRoleBids(apmFlowTemplateNodes.get(i).getComplateRoleBids(),roleBidMap));
                    Map<String,String> nodeWebBidMap = flowNodeBidMap.get(apmFlowTemplateNodes.get(i).getFlowTemplateBid());
                    if(nodeWebBidMap == null){
                        nodeWebBidMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
                    }
                    nodeWebBidMap.put(apmFlowTemplateNodes.get(i).getWebBid(),bid);
                    apmFlowTemplateNodes.get(i).setWebBid(bid);
                    flowNodeBidMap.put(apmFlowTemplateNodes.get(i).getFlowTemplateBid(),nodeWebBidMap);
                }
            }
            for(ApmFlowTemplateNode apmFlowTemplateNode:apmFlowTemplateNodes){
                apmFlowTemplateNode.setBeforeNodeBids(getNewRoleBids(apmFlowTemplateNode.getBeforeNodeBids(),nodeBidMap));
            }
        }
        //更新layout 节点 webBid
        for(ApmFlowTemplate apmFlowTemplate:apmFlowTemplateList){
            Map<String,String> nodeWebBidMap = flowNodeBidMap.get(apmFlowTemplate.getBid());
            if(CollectionUtils.isNotEmpty(nodeWebBidMap)){
                String layout = apmFlowTemplate.getLayout();
                if(StringUtils.isNotEmpty(layout)) {
                    for (Map.Entry<String, String> entry : nodeWebBidMap.entrySet()) {
                        layout = layout.replaceAll(entry.getKey(), entry.getValue());
                    }
                    apmFlowTemplate.setLayout(layout);
                }
            }
        }
        //过滤节点连线数据
        Map<String,String> lineBidMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        Map<String,String> lineWebBidMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        if(CollectionUtils.isNotEmpty(apmFlowNodeLineList)){
            for(int i=apmFlowNodeLineList.size()-1;i>=0;i--){
                if(!tempBidAndVersionMap.containsKey(apmFlowNodeLineList.get(i).getTemplateBid()+"-"+apmFlowNodeLineList.get(i).getVersion())){
                    apmFlowNodeLineList.remove(i);
                }else {
                    apmFlowNodeLineList.get(i).setId(null);
                    //设置webBid
                    Map<String,String> nodeWebBidMap = flowNodeBidMap.get(apmFlowNodeLineList.get(i).getTemplateBid());
                    if(CollectionUtils.isNotEmpty(nodeWebBidMap)){
                        String newWebBid = nodeWebBidMap.get(nodeOldWebBidRelMap.get(apmFlowNodeLineList.get(i).getSourceNodeBid()))+nodeWebBidMap.get(nodeOldWebBidRelMap.get(apmFlowNodeLineList.get(i).getTargetNodeBid()))+"#LINE";
                        lineWebBidMap.put(apmFlowNodeLineList.get(i).getWebBid(),newWebBid);
                        apmFlowNodeLineList.get(i).setWebBid(newWebBid);
                    }
                    String bid = SnowflakeIdWorker.nextIdStr();
                    lineBidMap.put(apmFlowNodeLineList.get(i).getBid(),bid);
                    apmFlowNodeLineList.get(i).setBid(bid);
                    apmFlowNodeLineList.get(i).setTemplateBid(tempBidMap.get(apmFlowNodeLineList.get(i).getTemplateBid()));
                    apmFlowNodeLineList.get(i).setSourceNodeBid(nodeBidMap.get(apmFlowNodeLineList.get(i).getSourceNodeBid()));
                    apmFlowNodeLineList.get(i).setTargetNodeBid(nodeBidMap.get(apmFlowNodeLineList.get(i).getTargetNodeBid()));

                }
            }
        }
        //过滤节点连线事件数据
        if(CollectionUtils.isNotEmpty(apmFlowLineEvents)){
            for(int i=apmFlowLineEvents.size()-1;i>=0;i--){
                if(!tempBidAndVersionMap.containsKey(apmFlowLineEvents.get(i).getFlowTemplateBid()+"-"+apmFlowLineEvents.get(i).getVersion())){
                    apmFlowLineEvents.remove(i);
                }else{
                    apmFlowLineEvents.get(i).setId(null);
                    String bid = SnowflakeIdWorker.nextIdStr();
                    apmFlowLineEvents.get(i).setBid(bid);
                    apmFlowLineEvents.get(i).setFlowTemplateBid(tempBidMap.get(apmFlowLineEvents.get(i).getFlowTemplateBid()));
                    apmFlowLineEvents.get(i).setLineBid(lineBidMap.get(apmFlowLineEvents.get(i).getLineBid()));
                    //设置line_web_bid
                    apmFlowLineEvents.get(i).setLineWebBid(lineWebBidMap.get(apmFlowLineEvents.get(i).getLineWebBid()));
                }
            }
        }
        //过滤节点事件表
        Map<String,String> eventBidMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        if(CollectionUtils.isNotEmpty(apmFlowNodeEvents)){
            for(int i=apmFlowNodeEvents.size()-1;i>=0;i--){
                if(!tempBidAndVersionMap.containsKey(apmFlowNodeEvents.get(i).getFlowTemplateBid()+"-"+apmFlowNodeEvents.get(i).getVersion())){
                    apmFlowNodeEvents.remove(i);
                }else {
                    apmFlowNodeEvents.get(i).setId(null);
                    String bid = SnowflakeIdWorker.nextIdStr();
                    eventBidMap.put(apmFlowNodeEvents.get(i).getBid(),bid);
                    apmFlowNodeEvents.get(i).setBid(bid);
                    apmFlowNodeEvents.get(i).setFlowTemplateBid(tempBidMap.get(apmFlowNodeEvents.get(i).getFlowTemplateBid()));
                    apmFlowNodeEvents.get(i).setNodeBid(nodeBidMap.get(apmFlowNodeEvents.get(i).getNodeBid()));
                }
            }
        }
        //过滤节点任务
        if(CollectionUtils.isNotEmpty(apmFlowNodeTasks)){
            for(int i=apmFlowNodeTasks.size()-1;i>=0;i--){
                if(!tempBidAndVersionMap.containsKey(apmFlowNodeTasks.get(i).getFlowTemplateBid()+"-"+apmFlowNodeTasks.get(i).getVersion())){
                    apmFlowNodeTasks.remove(i);
                }else {
                    apmFlowNodeTasks.get(i).setId(null);
                    String bid = SnowflakeIdWorker.nextIdStr();
                    apmFlowNodeTasks.get(i).setBid(bid);
                    apmFlowNodeTasks.get(i).setFlowTemplateBid(tempBidMap.get(apmFlowNodeTasks.get(i).getFlowTemplateBid()));
                    apmFlowNodeTasks.get(i).setNodeBid(nodeBidMap.get(apmFlowNodeTasks.get(i).getNodeBid()));
                }
            }
        }
        //过滤显示条件表
        if(CollectionUtils.isNotEmpty(apmFlowNodeDisplayConditions)){
            for(int i=apmFlowNodeDisplayConditions.size()-1;i>=0;i--){
                if(!tempBidAndVersionMap.containsKey(apmFlowNodeDisplayConditions.get(i).getFlowTemplateBid()+"-"+apmFlowNodeDisplayConditions.get(i).getVersion())){
                    apmFlowNodeDisplayConditions.remove(i);
                }else {
                    apmFlowNodeDisplayConditions.get(i).setId(null);
                    String bid = SnowflakeIdWorker.nextIdStr();
                    apmFlowNodeDisplayConditions.get(i).setBid(bid);
                    apmFlowNodeDisplayConditions.get(i).setFlowTemplateBid(tempBidMap.get(apmFlowNodeDisplayConditions.get(i).getFlowTemplateBid()));
                    apmFlowNodeDisplayConditions.get(i).setNodeBid(nodeBidMap.get(apmFlowNodeDisplayConditions.get(i).getNodeBid()));
                }
            }
        }
        //复制apm_flow_drive_relate数据
        if(CollectionUtils.isNotEmpty(eventBidMap)){
            List<ApmFlowDriveRelate> apmFlowDriveRelates = apmFlowDriveRelateService.list(Wrappers.<ApmFlowDriveRelate>lambdaQuery().in(ApmFlowDriveRelate::getEventBid, eventBidMap.keySet()));
            if(CollectionUtils.isNotEmpty(apmFlowDriveRelates)){
                for(ApmFlowDriveRelate apmFlowDriveRelate:apmFlowDriveRelates){
                    apmFlowDriveRelate.setId(null);
                    apmFlowDriveRelate.setBid(SnowflakeIdWorker.nextIdStr());
                    apmFlowDriveRelate.setEventBid(eventBidMap.get(apmFlowDriveRelate.getEventBid()));
                    apmFlowDriveRelate.setSourceFlowTemplateBid(tempBidMap.get(apmFlowDriveRelate.getSourceFlowTemplateBid()));
                    apmFlowDriveRelate.setSourceNodeBid(nodeBidMap.get(apmFlowDriveRelate.getSourceNodeBid()));
                }
                apmFlowDriveRelateService.saveBatch(apmFlowDriveRelates);
            }
        }
        //保存处理后的数据
        apmFlowTemplateService.saveBatch(apmFlowTemplateList);
        apmFlowTemplateVersionService.saveBatch(apmFlowTemplateVersions);
        if(CollectionUtils.isNotEmpty(apmFlowTemplateNodes)){
            apmFlowTemplateNodeService.saveBatch(apmFlowTemplateNodes);
        }
        if(CollectionUtils.isNotEmpty(apmFlowNodeEvents)){
            apmFlowNodeEventService.saveBatch(apmFlowNodeEvents);
        }
        if(CollectionUtils.isNotEmpty(apmFlowNodeTasks)){
            apmFlowNodeTaskService.saveBatch(apmFlowNodeTasks);
        }
        if(CollectionUtils.isNotEmpty(apmFlowNodeDisplayConditions)){
            apmFlowNodeDisplayConditionService.saveBatch(apmFlowNodeDisplayConditions);
        }
        if(CollectionUtils.isNotEmpty(apmFlowNodeLineList)){
            apmFlowNodeLineService.saveBatch(apmFlowNodeLineList);
        }
        if(CollectionUtils.isNotEmpty(apmFlowLineEvents)){
            apmFlowLineEventService.saveBatch(apmFlowLineEvents);
        }
        apmSpaceAppConfigManageService.copyViews(lineWebBidMap,"#");
        return true;
    }

    @Override
    public boolean delete(String templateBid) {
        return apmFlowTemplateService.delete(templateBid);
    }

    @Override
    public List<ApmFlowTemplateVO> listBySpaceBidAndModelCode(String spaceBid, String modelCode) {
        ApmSpaceApp apmSpaceApp = apmSpaceAppService.getBySpaceBidAndModelCode(spaceBid, modelCode);
        if(apmSpaceApp == null){
            throw new PlmBizException("当前空间下未配置应用");
        }
        return listBySpaceAppBid(apmSpaceApp.getBid());
    }

    @Override
    public List<ApmFlowTemplateNodeVO> listNodesByTemplateBid(String templateBid) {
        ApmFlowTemplate flowTemplate = apmFlowTemplateService.getByBid(templateBid);
        List<ApmFlowTemplateNode> apmFlowTemplateNodes = apmFlowTemplateNodeService.listByTemplateBidAndVersion(templateBid, flowTemplate.getVersion());
        return AmpFlowTemplateNodeConerter.INSTANCE.entitys2Vos(apmFlowTemplateNodes);
    }

    @Override
    public ApmLifeCycleStateVO getLifeCycleState(String spaceAppBid) {
        List<ApmFlowTemplate> apmFlowTemplateList = apmFlowTemplateService.listStateFlowBySpaceAppBid(spaceAppBid);
        if(CollectionUtils.isNotEmpty(apmFlowTemplateList)){
            ApmFlowTemplate apmFlowTemplate = apmFlowTemplateList.get(0);
            ApmLifeCycleStateVO apmLifeCycleStateVO = new ApmLifeCycleStateVO();
            apmLifeCycleStateVO.setLcTemplBid(apmFlowTemplate.getBid());
            apmLifeCycleStateVO.setLcTemplVersion(apmFlowTemplate.getVersion());
            apmLifeCycleStateVO.setLifeCycleStateType("stateFlow");
            //查收所有状态数据
            List<ApmFlowTemplateNode> apmFlowTemplateNodes = apmFlowTemplateNodeService.list(Wrappers.<ApmFlowTemplateNode>lambdaQuery().eq(ApmFlowTemplateNode::getFlowTemplateBid, apmFlowTemplate.getBid()).eq(ApmFlowTemplateNode::getVersion,apmFlowTemplate.getVersion()).eq(ApmFlowTemplateNode::getDeleteFlag,false).orderByAsc(ApmFlowTemplateNode::getSort));
            List<ApmStateVO> apmStateVOList = new ArrayList<>();
            for(ApmFlowTemplateNode apmFlowTemplateNode : apmFlowTemplateNodes){
                if("1".equals(apmFlowTemplateNode.getLifeCycleCodeType())){
                    apmLifeCycleStateVO.setInitState(apmFlowTemplateNode.getLifeCycleCode());
                    apmLifeCycleStateVO.setInitStateName(apmFlowTemplateNode.getNodeName());
                }
                ApmStateVO apmStateVO = new ApmStateVO();
                apmStateVO.setLifeCycleCode(apmFlowTemplateNode.getLifeCycleCode());
                apmStateVO.setLifeCycleName(apmFlowTemplateNode.getNodeName());
                apmStateVO.setNodeBid(apmFlowTemplateNode.getBid());
                setApmStateVOWebAttr(apmStateVO);
                apmStateVOList.add(apmStateVO);
            }
            // 设置生命周期阶段映射
            apmLifeCycleStateVO.setPhaseStateMap(getPhaseStateMap(apmFlowTemplate.getLayout()));
            apmLifeCycleStateVO.setApmStateVOList(apmStateVOList);
            return apmLifeCycleStateVO;
        }
        return null;
    }

    private Map<String, ApmStateVO> getPhaseStateMap(String layout) {
        Map<String, ApmStateVO> phaseStateMap = Maps.newConcurrentMap();
        if (StringUtils.isNotBlank(layout)) {
            List<JSONObject> layouts = JSON.parseArray(layout, JSONObject.class);
            if (CollectionUtils.isNotEmpty(layouts)) {
                for (JSONObject jsonObject : layouts) {
                    String shape = (String) jsonObject.get("shape");
                    String id = (String) jsonObject.get("id");
                    // 业务数据map
                    Map<String, Object> data = (Map<String, Object>) jsonObject.get("data");
                    if ("rect".endsWith(shape) && CollectionUtils.isNotEmpty(data) && data.get("phaseState") != null) {
                        Map<String, Object> phaseState = (Map<String, Object>) data.get("phaseState");
                        ApmStateVO apmStateVO = new ApmStateVO();
                        if (phaseState.get("code") != null) {
                            apmStateVO.setLifeCycleCode(phaseState.get("code") + "");
                        }
                        if (phaseState.get("name") != null) {
                            apmStateVO.setLifeCycleName(phaseState.get("name") + "");
                        }
                        apmStateVO.setNodeBid(id);
                        setApmStateVOWebAttr(apmStateVO);
                        phaseStateMap.put((String) data.get("code"), apmStateVO);
                    }
                }
            }
        }
        return phaseStateMap;
    }

    private void setApmStateVOWebAttr(ApmStateVO apmStateVO){
        apmStateVO.setName(apmStateVO.getLifeCycleName());
        apmStateVO.setKeyCode(apmStateVO.getLifeCycleCode());
        apmStateVO.setLife_cycle_code(apmStateVO.getLifeCycleCode());
        apmStateVO.setBid(apmStateVO.getNodeBid());
        apmStateVO.setZh(apmStateVO.getName());
    }

    /**
     * 根据当前流程查下一个流程
     * @param apmStateQo
     * @return
     */
    @Override
    public List<ApmStateVO> listNextStates(ApmStateQo apmStateQo){
        List<ApmStateVO> resList = new ArrayList<>();
        if(StringUtils.isEmpty(apmStateQo.getSpaceAppBid())){
            return resList;
        }
        //根据应用查最新流程
        List<ApmFlowTemplate> apmFlowTemplateList = apmFlowTemplateService.listStateFlowBySpaceAppBid(apmStateQo.getSpaceAppBid());
        if(CollectionUtils.isEmpty(apmFlowTemplateList)){
            return resList;
        }
        List<ApmFlowNodeLine> apmFlowNodeLineList = apmFlowNodeLineService.listNodeLinesByTempBidAndVersion(apmFlowTemplateList.get(0).getBid(),apmFlowTemplateList.get(0).getVersion());
        if(CollectionUtils.isNotEmpty(apmFlowNodeLineList)){
            List<ApmFlowTemplateNode> apmFlowTemplateNodes = apmFlowTemplateNodeService.listByTemplateBidAndVersion(apmFlowTemplateList.get(0).getBid(),apmFlowTemplateList.get(0).getVersion());
            Map<String,ApmFlowTemplateNode> apmFlowTemplateNodeMap = apmFlowTemplateNodes.stream().collect(Collectors.toMap(ApmFlowTemplateNode::getLifeCycleCode, Function.identity()));
            //前端需要当前节点数据
            ApmStateVO apmStateVONow = new ApmStateVO();
            apmStateVONow.setLifeCycleCode(apmStateQo.getCurrentState());
            if(apmFlowTemplateNodeMap.get(apmStateQo.getCurrentState()) != null){
                apmStateVONow.setLifeCycleName(apmFlowTemplateNodeMap.get(apmStateQo.getCurrentState()).getNodeName());
            }
            setApmStateVOWebAttr(apmStateVONow);
            if(apmFlowTemplateNodeMap.get(apmStateVONow.getLifeCycleCode()) != null && CollectionUtils.isNotEmpty(apmFlowTemplateNodeMap.get(apmStateVONow.getLifeCycleCode()).getNodeRoleBids())){
                apmStateVONow.setRoleBid(apmFlowTemplateNodeMap.get(apmStateVONow.getLifeCycleCode()).getNodeRoleBids().get(0));
            }
            resList.add(apmStateVONow);
            for(ApmFlowNodeLine apmFlowNodeLine : apmFlowNodeLineList){
              if(apmStateQo.getCurrentState().equals(apmFlowNodeLine.getSourceNodeCode())){
                  ApmStateVO apmStateVO = new ApmStateVO();
                  apmStateVO.setLifeCycleCode(apmFlowNodeLine.getTargetNodeCode());
                  if(apmFlowTemplateNodeMap.get(apmFlowNodeLine.getTargetNodeCode()) != null){
                      apmStateVO.setLifeCycleName(apmFlowTemplateNodeMap.get(apmFlowNodeLine.getTargetNodeCode()).getNodeName());
                      apmStateVO.setNodeBid(apmFlowTemplateNodeMap.get(apmFlowNodeLine.getTargetNodeCode()).getBid());
                      if(CollectionUtils.isNotEmpty(apmFlowTemplateNodeMap.get(apmFlowNodeLine.getTargetNodeCode()).getNodeRoleBids())){
                          apmStateVO.setRoleBid(apmFlowTemplateNodeMap.get(apmFlowNodeLine.getTargetNodeCode()).getNodeRoleBids().get(0));
                      }
                  }
                  apmStateVO.setViewBid(apmFlowNodeLine.getWebBid());
                  setApmStateVOWebAttr(apmStateVO);
                  resList.add(apmStateVO);
              }
           }
        }
        if(CollectionUtils.isNotEmpty(resList)){
            List<String> lifeCycleCodes = resList.stream().map(ApmStateVO::getLifeCycleCode).collect(Collectors.toList());
            List<ApmFlowInstanceRoleUser>  apmFlowInstanceRoleUsers = apmFlowInstanceRoleUserService.listByInstanceBidAndLifeCycleCodes(apmStateQo.getInstanceBid(),lifeCycleCodes);
            if(CollectionUtils.isNotEmpty(apmFlowInstanceRoleUsers)){
                Map<String,List<String>> apmFlowInstanceRoleUserMap = apmFlowInstanceRoleUsers.stream().collect(Collectors.groupingBy(ApmFlowInstanceRoleUser::getLifeCycleCode,Collectors.mapping(ApmFlowInstanceRoleUser::getUserNo,Collectors.toList())));
                for(ApmStateVO apmStateVO : resList){
                    apmStateVO.setJobNumbers(apmFlowInstanceRoleUserMap.get(apmStateVO.getLifeCycleCode()));
                }
            }
        }
        return resList;
    }

    private List<String> getNewRoleBids(List<String> oldRoleBids,Map<String,String> roleBidMap){
        List<String> resList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(oldRoleBids)){
           for(String roleBid:oldRoleBids){
               String newRoleBid = roleBidMap.get(roleBid);
               if(StringUtils.isNotEmpty(newRoleBid)){
                   resList.add(newRoleBid);
               }
           }
        }
        return resList;
    }

    /**
     * 流程模板新增
     * @param apmFlowTemplateAO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApmFlowTemplateVO saveOrUpdate(ApmFlowTemplateAO apmFlowTemplateAO) {
        ApmFlowTemplate apmFlowTemplate = AmpFlowTemplateConverter.INSTANCE.ao2Entity(apmFlowTemplateAO);
        boolean isAdd = false;
        if(StringUtils.isEmpty(apmFlowTemplate.getBid())){
            if(FlowTypeEnum.STATE.getCode().equals(apmFlowTemplateAO.getType())){
                //状态流程只能创建一个
                long count = apmFlowTemplateService.getStateFlowCount(apmFlowTemplateAO.getSpaceAppBid());
                if(count > 0){
                    throw new PlmBizException("已经存在有状态流程，不需要重复创建");
                }
            }
            //该流程是新增流程
            isAdd = true;
            apmFlowTemplate.setCreatedBy(SsoHelper.getJobNumber());
            apmFlowTemplate.setCreatedTime(new Date());
            apmFlowTemplate.setBid(SnowflakeIdWorker.nextIdStr());
            if(StringUtils.isEmpty(apmFlowTemplate.getModelCode())){
                ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(apmFlowTemplate.getSpaceAppBid());
                apmFlowTemplate.setModelCode(apmSpaceApp.getModelCode());
            }
        }else{
            ApmFlowTemplate oldApmFlowTemplate = apmFlowTemplateService.getByBid(apmFlowTemplate.getBid());
            if(oldApmFlowTemplate != null){
                apmFlowTemplateAO.setVersion(oldApmFlowTemplate.getVersion());
            }
            apmFlowTemplate.setUpdatedBy(SsoHelper.getJobNumber());
            apmFlowTemplate.setUpdatedTime(new Date());
        }

        List<ApmFlowTemplateNodeAO> apmFlowTemplateNodeAOList = apmFlowTemplateAO.getApmFlowTemplateNodeAOList();
        List<ApmFlowTemplateNode> apmFlowTemplateNodes = new ArrayList<>();
        List<ApmFlowNodeDisplayCondition> apmFlowNodeDisplayConditions = new ArrayList<>();
        List<ApmFlowNodeTask> apmFlowNodeTasks = new ArrayList<>();
        List<ApmFlowNodeEvent> apmFlowNodeEvents = new ArrayList<>();
        List<ApmFlowDriveRelate> apmFlowDriveRelates = new ArrayList<>();
        List<ApmFlowNodeDirection> apmFlowNodeDirections = new ArrayList<>();
        List<ApmFlowNodeDirectionCondition> apmFlowNodeDirectionConditions = new ArrayList<>();
        //通过前端的webBid 映射成后端的bid
        Map<String,String> webBidRelMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        Map<String,ApmFlowTemplateNode> apmFlowTemplateNodeMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        Map<String,String> activeMatchMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        if(CollectionUtils.isNotEmpty(apmFlowTemplateNodeAOList)){
            List<String> webBidList = new ArrayList<>();
            apmFlowTemplate.setVersion(getVersion(apmFlowTemplateAO.getVersion()));
            for(ApmFlowTemplateNodeAO apmFlowTemplateNodeAO : apmFlowTemplateNodeAOList){
                apmFlowTemplateNodeAO.setBid(SnowflakeIdWorker.nextIdStr());
                webBidRelMap.put(apmFlowTemplateNodeAO.getWebBid(), apmFlowTemplateNodeAO.getBid());
                webBidList.add(apmFlowTemplateNodeAO.getWebBid());
            }
            Map<String,String> webBidMap = apmFlowTemplateNodeService.getDataBidMap(apmFlowTemplate.getBid(),webBidList);
            String jobNumber = SsoHelper.getJobNumber();
            for(ApmFlowTemplateNodeAO apmFlowTemplateNodeAO : apmFlowTemplateNodeAOList){
                //获取节点流转配置
                List<ApmFlowNodeDirectionAO> apmFlowNodeDirectionAOs = apmFlowTemplateNodeAO.getApmFlowNodeDirectionAOs();
                if(CollectionUtils.isNotEmpty(apmFlowNodeDirectionAOs)){
                    for(ApmFlowNodeDirectionAO apmFlowNodeDirectionAO : apmFlowNodeDirectionAOs){
                        ApmFlowNodeDirection apmFlowNodeDirection = ApmFlowNodeDirectionConverter.INSTANCE.ao2Entity(apmFlowNodeDirectionAO);
                        apmFlowNodeDirection.setBid(SnowflakeIdWorker.nextIdStr());
                        apmFlowNodeDirection.setTemplateBid(apmFlowTemplate.getBid());
                        apmFlowNodeDirection.setVersion(apmFlowTemplate.getVersion());
                        apmFlowNodeDirection.setLineName(apmFlowNodeDirectionAO.getLineName());
                        apmFlowNodeDirection.setSourceNodeBid(apmFlowTemplateNodeAO.getBid());
                        apmFlowNodeDirection.setTargetNodeBid(webBidRelMap.get(apmFlowNodeDirection.getTargetNodeWebBid()));
                        List<ApmFlowNodeDirectionConditionAO> apmFlowNodeDirectionConditionAOList = apmFlowNodeDirectionAO.getApmFlowNodeDirectionConditionAOList();
                        if(CollectionUtils.isNotEmpty(apmFlowNodeDirectionConditionAOList)){
                            for(ApmFlowNodeDirectionConditionAO apmFlowNodeDirectionConditionAO : apmFlowNodeDirectionConditionAOList){
                                ApmFlowNodeDirectionCondition apmFlowNodeDirectionCondition = ApmFlowNodeDirectionConditionConverter.INSTANCE.ao2Entity(apmFlowNodeDirectionConditionAO);
                                apmFlowNodeDirectionCondition.setBid(SnowflakeIdWorker.nextIdStr());
                                apmFlowNodeDirectionCondition.setFlowTemplateBid(apmFlowTemplate.getBid());
                                apmFlowNodeDirectionCondition.setVersion(apmFlowTemplate.getVersion());
                                apmFlowNodeDirectionCondition.setFlowNodeDirectionBid(apmFlowNodeDirection.getBid());
                                apmFlowNodeDirectionConditions.add(apmFlowNodeDirectionCondition);
                            }
                            //这里设置前节点存在路由条件，当前节点激活是任一路由条件满足即可
                            activeMatchMap.put(apmFlowNodeDirection.getTargetNodeBid(),"any");
                        }
                        if(StringUtils.isNotEmpty(apmFlowNodeDirection.getTargetNodeBid())){
                            apmFlowNodeDirections.add(apmFlowNodeDirection);
                        }
                    }
                }
                ApmFlowTemplateNode apmFlowTemplateNode = AmpFlowTemplateNodeConerter.INSTANCE.ao2Entity(apmFlowTemplateNodeAO);
                List<String> webBids = apmFlowTemplateNodeAO.getBeforeWebBids();
                List<String> nextWebBids = apmFlowTemplateNodeAO.getNextWebBids();
                apmFlowTemplateNode.setBeforeNodeBids(getNodeBids(webBids,webBidRelMap));
                apmFlowTemplateNode.setNextNodeBids(getNodeBids(nextWebBids,webBidRelMap));
                apmFlowTemplateNode.setCreatedBy(jobNumber);
                String dataBid = webBidMap.get(apmFlowTemplateNode.getWebBid());
                if(dataBid == null){
                    dataBid = apmFlowTemplateNode.getBid();
                }
                apmFlowTemplateNode.setDataBid(dataBid);
                apmFlowTemplateNode.setFlowTemplateBid(apmFlowTemplate.getBid());
                apmFlowTemplateNode.setVersion(apmFlowTemplate.getVersion());
                apmFlowNodeDisplayConditions.addAll(getAmpFlowNodeDisplayConditions(apmFlowTemplateNode, apmFlowTemplateNodeAO.getApmFlowNodeDisplayConditionAOList()));
                apmFlowNodeTasks.addAll(getAmpFlowNodeTasks(apmFlowTemplateNode, apmFlowTemplateNodeAO.getApmFlowNodeTaskAOList()));
                List<ApmFlowNodeEvent> ampFlowNodeEvents = getAmpFlowNodeEvents(apmFlowTemplateNode, apmFlowTemplateNodeAO.getApmFlowNodeEventAOList());
                apmFlowNodeEvents.addAll(ampFlowNodeEvents);
                //从ampFlowNodeEvents中收集DriveRelate
                ampFlowNodeEvents.stream().map(ApmFlowNodeEvent::getDriveRelateList).filter(CollectionUtils::isNotEmpty).forEach(apmFlowDriveRelates::addAll);
                apmFlowTemplateNodes.add(apmFlowTemplateNode);
                apmFlowTemplateNodeMap.put(apmFlowTemplateNode.getWebBid(),apmFlowTemplateNode);
            }
        }
        //处理连线和连线事件
        List<ApmFlowNodeLine> apmFlowNodeLineList = new ArrayList<>();
        List<ApmFlowLineEvent> apmFlowLineEvents = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(apmFlowTemplateAO.getApmFlowNodeLineAOList())){
            for(ApmFlowNodeLineAO ao: apmFlowTemplateAO.getApmFlowNodeLineAOList()){
                ApmFlowNodeLine apmFlowNodeLine = ApmFlowNodeLineConverter.INSTANCE.ao2Po(ao);
                apmFlowNodeLine.setTemplateBid(apmFlowTemplate.getBid());
                apmFlowNodeLine.setVersion(apmFlowTemplate.getVersion());
                apmFlowNodeLine.setBid(SnowflakeIdWorker.nextIdStr());
                apmFlowNodeLine.setCreatedTime(new Date());
                ApmFlowTemplateNode sourceNode = apmFlowTemplateNodeMap.get(ao.getSourceWebBid());
                ApmFlowTemplateNode targetNode = apmFlowTemplateNodeMap.get(ao.getTargetWebBid());
                apmFlowNodeLine.setSourceNodeBid(sourceNode.getBid());
                apmFlowNodeLine.setTargetNodeBid(targetNode.getBid());
                apmFlowNodeLine.setSourceNodeCode(sourceNode.getLifeCycleCode());
                apmFlowNodeLine.setTargetNodeCode(targetNode.getLifeCycleCode());
                if(StringUtils.isEmpty(apmFlowNodeLine.getWebBid())){
                    apmFlowNodeLine.setWebBid(ao.getSourceWebBid()+ao.getTargetWebBid()+"#LINE");
                }
                if(CollectionUtils.isNotEmpty(ao.getApmFlowLineEventAOList())){
                    for(ApmFlowLineEventAO eventAO: ao.getApmFlowLineEventAOList()){
                        ApmFlowLineEvent apmFlowLineEvent = ApmFlowLineEventConverter.INSTANCE.ao2Po(eventAO);
                        apmFlowLineEvent.setBid(SnowflakeIdWorker.nextIdStr());
                        apmFlowLineEvent.setFlowTemplateBid(apmFlowTemplate.getBid());
                        apmFlowLineEvent.setVersion(apmFlowTemplate.getVersion());
                        apmFlowLineEvent.setCreatedTime(new Date());
                        apmFlowLineEvent.setLineBid(apmFlowNodeLine.getBid());
                        apmFlowLineEvent.setLineWebBid(apmFlowNodeLine.getWebBid());
                        apmFlowLineEvents.add(apmFlowLineEvent);
                        if(CollectionUtils.isNotEmpty(eventAO.getDriveRelateList())){
                            for(ApmFlowDriveRelateAO apmFlowDriveRelateAO: eventAO.getDriveRelateList()){
                                ApmFlowDriveRelate apmFlowDriveRelate = ApmFlowDriveRelateConverter.INSTANCE.ao2Entity(apmFlowDriveRelateAO);
                                apmFlowDriveRelate.setBid(SnowflakeIdWorker.nextIdStr());
                                apmFlowDriveRelate.setEventBid(apmFlowLineEvent.getBid());
                                apmFlowDriveRelates.add(apmFlowDriveRelate);
                            }
                        }
                    }
                }
                apmFlowNodeLineList.add(apmFlowNodeLine);
            }
        }
        ApmFlowTemplateVersion apmFlowTemplateVersion = AmpFlowTemplateVersionConverter.INSTANCE.temp2tempVersion(apmFlowTemplate);
        apmFlowTemplateVersion.setFlowTemplateBid(apmFlowTemplate.getBid());
        apmFlowTemplateVersion.setBid(SnowflakeIdWorker.nextIdStr());
        apmFlowTemplateVersion.setId(null);
        apmFlowTemplateVersionService.save(apmFlowTemplateVersion);
        if(CollectionUtils.isNotEmpty(apmFlowTemplateNodes)){
            Map<String,String> repateMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
            for(int i = apmFlowTemplateNodes.size()-1;i>=0;i--){
                ApmFlowTemplateNode apmFlowTemplateNode = apmFlowTemplateNodes.get(i);
                if(repateMap.containsKey(apmFlowTemplateNode.getWebBid())){
                    apmFlowTemplateNodes.remove(i);
                }else{
                    repateMap.put(apmFlowTemplateNode.getWebBid(),apmFlowTemplateNode.getBid());
                    if(StringUtils.isEmpty(apmFlowTemplateNode.getActiveMatch())){
                        apmFlowTemplateNode.setActiveMatch(activeMatchMap.get(apmFlowTemplateNode.getBid()));
                    }
                }
            }
            apmFlowTemplateNodeService.saveBatch(apmFlowTemplateNodes);
        }
        if(CollectionUtils.isNotEmpty(apmFlowNodeDisplayConditions)){
            apmFlowNodeDisplayConditionService.saveBatch(apmFlowNodeDisplayConditions);
        }
        if(CollectionUtils.isNotEmpty(apmFlowNodeTasks)){
            apmFlowNodeTaskService.saveBatch(apmFlowNodeTasks);
        }
        if(CollectionUtils.isNotEmpty(apmFlowNodeEvents)){
            apmFlowNodeEventService.saveBatch(apmFlowNodeEvents);
        }
        if(CollectionUtils.isNotEmpty(apmFlowDriveRelates)){
            apmFlowDriveRelateService.saveBatch(apmFlowDriveRelates);
        }
        if(CollectionUtils.isNotEmpty(apmFlowNodeLineList)){
            apmFlowNodeLineService.saveBatch(apmFlowNodeLineList);
        }
        if(CollectionUtils.isNotEmpty(apmFlowLineEvents)){
            apmFlowLineEventService.saveBatch(apmFlowLineEvents);
        }
        if(CollectionUtils.isNotEmpty(apmFlowNodeDirections)){
            apmFlowNodeDirectionService.saveBatch(apmFlowNodeDirections);
        }
        if(CollectionUtils.isNotEmpty(apmFlowNodeDirectionConditions)){
            apmFlowNodeDirectionConditionService.saveBatch(apmFlowNodeDirectionConditions);
        }
        if(isAdd){
            apmFlowTemplateService.save(apmFlowTemplate);
        }else{
            apmFlowTemplateService.updateByBid(apmFlowTemplate);
        }
        return AmpFlowTemplateConverter.INSTANCE.entity2VO(apmFlowTemplate);
    }

    @Override
    public String getTemplateLayout(String templateBid){
        ApmFlowTemplate apmFlowTemplate = apmFlowTemplateService.getByBid(templateBid);
        if(apmFlowTemplate == null){
            return null;
        }
        return apmFlowTemplate.getLayout();
    }

    @Override
    public List<ApmFlowTemplateNodeVO> listCurrentTemplateNodes(String templateBid) {
        ApmFlowTemplate apmFlowTemplate = apmFlowTemplateService.getByBid(templateBid);
        if(apmFlowTemplate == null){
            return null;
        }
        List<ApmFlowTemplateNode> nodeList = apmFlowTemplateNodeService.listByTemplateBidAndVersion(apmFlowTemplate.getBid(),apmFlowTemplate.getVersion());
        if(CollectionUtils.isEmpty(nodeList)){
            return null;
        }
        List<ApmFlowTemplateNodeVO> nodeVOS = AmpFlowTemplateNodeConerter.INSTANCE.entitys2Vos(nodeList);
        for(ApmFlowTemplateNodeVO apmFlowTemplateNodeVO:nodeVOS){
            apmFlowTemplateNodeVO.setSpaceAppBid(apmFlowTemplate.getSpaceAppBid());
            if(apmFlowTemplateNodeVO.getNodeType().equals(FlowNodeTypeConstant.START_NODE)){
                apmFlowTemplateNodeVO.setLayout(apmFlowTemplate.getLayout());
            }
        }
        return nodeVOS;
    }

    @Override
    public ApmFlowTemplateNodeVO getNodeDetail(String nodeBid){
        ApmFlowTemplateNode apmFlowTemplateNode = apmFlowTemplateNodeService.getByBid(nodeBid);
        List<ApmFlowNodeDisplayCondition> nodeDisplayConditionList = apmFlowNodeDisplayConditionService.listByNodeBid(nodeBid);
        List<ApmFlowNodeEvent> nodeEventList = apmFlowNodeEventService.listByNodeBid(nodeBid);
        List<ApmFlowNodeTask> nodeTaskList = apmFlowNodeTaskService.listByNodeBid(nodeBid);
        ApmFlowTemplateNodeVO apmFlowTemplateNodeVO = AmpFlowTemplateNodeConerter.INSTANCE.entity2VO(apmFlowTemplateNode);
        List<ApmFlowNodeDisplayConditionVO> apmFlowNodeDisplayConditionVOS = AmpFlowNodeDisplayConditionConverter.INSTANCE.entitys2Vos(nodeDisplayConditionList);
        List<ApmFlowNodeEventVO> apmFlowNodeEventVOS = AmpFlowNodeEventConverter.INSTANCE.entitys2Vos(nodeEventList);
        List<ApmFlowNodeTaskVO> apmFlowNodeTaskVOS = taskEntity2Vos(nodeTaskList);
        apmFlowTemplateNodeVO.setApmFlowNodeDisplayConditionVOList(apmFlowNodeDisplayConditionVOS);
        apmFlowTemplateNodeVO.setApmFlowNodeEventVOList(apmFlowNodeEventVOS);
        apmFlowTemplateNodeVO.setApmFlowNodeTaskVOList(apmFlowNodeTaskVOS);
        return apmFlowTemplateNodeVO;
    }

    @Override
    public List<ApmFlowNodeEvent> listNodeEvents(int eventClassification, String nodeBid, String version) {
        List<ApmFlowNodeEvent> nodeEventList = apmFlowNodeEventService.listByNodeBidAndType(eventClassification,nodeBid,version);
        return nodeEventList;
    }

    private List<ApmFlowNodeTaskVO> taskEntity2Vos(List<ApmFlowNodeTask> nodeTaskList){
        List<ApmFlowNodeTaskVO> apmFlowNodeTaskVOS = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(nodeTaskList)){
            for(ApmFlowNodeTask apmFlowNodeTask:nodeTaskList){
                ApmFlowNodeTaskVO apmFlowNodeTaskVO = new ApmFlowNodeTaskVO();
                apmFlowNodeTaskVO.setTaskId(apmFlowNodeTask.getTaskId());
                apmFlowNodeTaskVO.setTaskName(apmFlowNodeTask.getTaskName());
                apmFlowNodeTaskVO.setNodeBid(apmFlowNodeTask.getNodeBid());
                apmFlowNodeTaskVO.setFlowTemplateBid(apmFlowNodeTask.getFlowTemplateBid());
                apmFlowNodeTaskVO.setVersion(apmFlowNodeTask.getVersion());
                apmFlowNodeTaskVOS.add(apmFlowNodeTaskVO);
            }
        }
        return apmFlowNodeTaskVOS;
    }

    @Override
    public ApmFlowTemplateVO getByBid(String bid) {
        ApmFlowTemplate apmFlowTemplate = apmFlowTemplateService.getByBid(bid);
        if(apmFlowTemplate == null){
            return null;
        }
        List<ApmFlowTemplateNode> nodeList = apmFlowTemplateNodeService.listByTemplateBidAndVersion(apmFlowTemplate.getBid(),apmFlowTemplate.getVersion());
        if(CollectionUtils.isEmpty(nodeList)){
            return null;
        }
        List<String> nodeBids = nodeList.stream().map(e->e.getBid()).collect(Collectors.toList());
        List<ApmFlowNodeDisplayCondition> nodeDisplayConditionList = apmFlowNodeDisplayConditionService.listByNodeBids(nodeBids);
        List<ApmFlowNodeEvent> nodeEventList = apmFlowNodeEventService.listByNodeBids(nodeBids);
        List<ApmFlowNodeTask> nodeTaskList = apmFlowNodeTaskService.listByNodeBids(nodeBids);
        Map<String,List<ApmFlowNodeDisplayConditionVO>> conditionVoMap = getNodeDisplayConditionVOMap(nodeDisplayConditionList);
        Map<String,List<ApmFlowNodeEventVO>> nodeEventVoMap = getApmFlowNodeEventVOMap(nodeEventList);
        Map<String,List<ApmFlowNodeTaskVO>> nodeTaskVoMap = getApmFlowNodeTaskVOMap(nodeTaskList);
        List<ApmFlowTemplateNodeVO> nodeVOS = AmpFlowTemplateNodeConerter.INSTANCE.entitys2Vos(nodeList);
        for(ApmFlowTemplateNodeVO apmFlowTemplateNodeVO:nodeVOS){
            apmFlowTemplateNodeVO.setApmFlowNodeDisplayConditionVOList(conditionVoMap.get(apmFlowTemplateNodeVO.getBid()));
            apmFlowTemplateNodeVO.setApmFlowNodeEventVOList(nodeEventVoMap.get(apmFlowTemplateNodeVO.getBid()));
            apmFlowTemplateNodeVO.setApmFlowNodeTaskVOList(nodeTaskVoMap.get(apmFlowTemplateNodeVO.getBid()));
        }
        ApmFlowTemplateVO apmFlowTemplateVO = AmpFlowTemplateConverter.INSTANCE.entity2VO(apmFlowTemplate);
        apmFlowTemplateVO.setApmFlowTemplateNodeVOList(nodeVOS);
        return apmFlowTemplateVO;
    }

    @Override
    public List<ApmFlowTemplateVO> listBySpaceAppBid(String spaceAppBid) {
        List<ApmFlowTemplate> apmFlowTemplateList = apmFlowTemplateService.listBySpaceAppBid(spaceAppBid);
        List<ApmFlowTemplateVO> apmFlowTemplateVOS = AmpFlowTemplateConverter.INSTANCE.entitys2Vos(apmFlowTemplateList);
        if(CollectionUtils.isNotEmpty(apmFlowTemplateVOS)){
            for(ApmFlowTemplateVO apmFlowTemplateVO:apmFlowTemplateVOS){
                apmFlowTemplateVO.setLayout(null);
            }
        }
        return apmFlowTemplateVOS;
    }

    private Map<String,List<ApmFlowNodeDisplayConditionVO>> getNodeDisplayConditionVOMap(List<ApmFlowNodeDisplayCondition> nodeDisplayConditionList){
        if(CollectionUtils.isEmpty(nodeDisplayConditionList)){
            return new HashMap<>(CommonConstant.START_MAP_SIZE);
        }
        List<ApmFlowNodeDisplayConditionVO> apmFlowNodeDisplayConditionVOS = AmpFlowNodeDisplayConditionConverter.INSTANCE.entitys2Vos(nodeDisplayConditionList);
        Map<String,List<ApmFlowNodeDisplayConditionVO>> resMap = apmFlowNodeDisplayConditionVOS.stream().collect(Collectors.groupingBy(ApmFlowNodeDisplayConditionVO::getNodeBid));
        return resMap;
    }

    private Map<String,List<ApmFlowNodeEventVO>> getApmFlowNodeEventVOMap(List<ApmFlowNodeEvent> apmFlowNodeEvents){
        if(CollectionUtils.isEmpty(apmFlowNodeEvents)){
            return new HashMap<>(CommonConstant.START_MAP_SIZE);
        }
        List<ApmFlowNodeEventVO> apmFlowNodeEventVOS = AmpFlowNodeEventConverter.INSTANCE.entitys2Vos(apmFlowNodeEvents);
        Map<String,List<ApmFlowNodeEventVO>> resMap = apmFlowNodeEventVOS.stream().collect(Collectors.groupingBy(ApmFlowNodeEventVO::getNodeBid));
        return resMap;
    }

    private Map<String,List<ApmFlowNodeTaskVO>> getApmFlowNodeTaskVOMap(List<ApmFlowNodeTask> apmFlowNodeTaskList){
        if(CollectionUtils.isEmpty(apmFlowNodeTaskList)){
            return new HashMap<>(CommonConstant.START_MAP_SIZE);
        }
        List<ApmFlowNodeTaskVO> apmFlowNodeTaskVOS = taskEntity2Vos(apmFlowNodeTaskList);
        Map<String,List<ApmFlowNodeTaskVO>> resMap = apmFlowNodeTaskVOS.stream().collect(Collectors.groupingBy(ApmFlowNodeTaskVO::getNodeBid));
        return resMap;
    }

    public List<ApmFlowNodeEvent> getAmpFlowNodeEvents(ApmFlowTemplateNode apmFlowTemplateNode, List<ApmFlowNodeEventAO> apmFlowNodeEventAOList){
        List<ApmFlowNodeEvent> apmFlowNodeEvents = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(apmFlowNodeEventAOList)){
            for(ApmFlowNodeEventAO apmFlowNodeEventAO : apmFlowNodeEventAOList){
                ApmFlowNodeEvent apmFlowNodeEvent = AmpFlowNodeEventConverter.INSTANCE.ao2Entity(apmFlowNodeEventAO);
                apmFlowNodeEvent.setFlowTemplateBid(apmFlowTemplateNode.getFlowTemplateBid());
                apmFlowNodeEvent.setVersion(apmFlowTemplateNode.getVersion());
                apmFlowNodeEvent.setNodeBid(apmFlowTemplateNode.getBid());
                String eventBid = SnowflakeIdWorker.nextIdStr();
                apmFlowNodeEvent.setBid(eventBid);
                apmFlowNodeEvents.add(apmFlowNodeEvent);
                List<ApmFlowDriveRelateAO> driveRelateList = apmFlowNodeEventAO.getDriveRelateList();
                if (CollectionUtils.isNotEmpty(driveRelateList)) {
                    //将ApmFlowDriveRelateAO列表转化为ApmFlowDriveRelate列表，并给bid和eventBid赋值
                    List<ApmFlowDriveRelate> apmFlowDriveRelates = getApmFlowDriveRelates(eventBid,driveRelateList);
                    apmFlowNodeEvent.setDriveRelateList(apmFlowDriveRelates);
                }
            }
        }
        return apmFlowNodeEvents;
    }

    private List<ApmFlowDriveRelate> getApmFlowDriveRelates(String eventBid, List<ApmFlowDriveRelateAO> driveRelateList) {
        List<ApmFlowDriveRelate> apmFlowDriveRelates = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(driveRelateList)){
            for(ApmFlowDriveRelateAO apmFlowDriveRelateAO:driveRelateList){
                ApmFlowDriveRelate apmFlowDriveRelate = ApmFlowDriveRelateConverter.INSTANCE.ao2Entity(apmFlowDriveRelateAO);
                if (StringUtils.isEmpty(apmFlowDriveRelate.getSourceNodeBid())) {
                    throw new PlmBizException("关联节点不能为空");
                }
                apmFlowDriveRelate.setBid(SnowflakeIdWorker.nextIdStr());
                apmFlowDriveRelate.setEventBid(eventBid);
                apmFlowDriveRelates.add(apmFlowDriveRelate);
            }
        }
        return apmFlowDriveRelates;
    }

    public List<ApmFlowNodeTask> getAmpFlowNodeTasks(ApmFlowTemplateNode apmFlowTemplateNode, List<ApmFlowNodeTaskAO> apmFlowNodeTaskAOList){
        List<ApmFlowNodeTask> apmFlowNodeTasks = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(apmFlowNodeTaskAOList)){
            for(ApmFlowNodeTaskAO apmFlowNodeTaskAO : apmFlowNodeTaskAOList){
                ApmFlowNodeTask apmFlowNodeTask = new ApmFlowNodeTask();
                apmFlowNodeTask.setTaskId(apmFlowNodeTaskAO.getTaskId());
                apmFlowNodeTask.setBid(SnowflakeIdWorker.nextIdStr());
                apmFlowNodeTask.setTaskName(apmFlowNodeTaskAO.getTaskName());
                apmFlowNodeTask.setFlowTemplateBid(apmFlowTemplateNode.getFlowTemplateBid());
                apmFlowNodeTask.setVersion(apmFlowTemplateNode.getVersion());
                apmFlowNodeTask.setNodeBid(apmFlowTemplateNode.getBid());
                apmFlowNodeTasks.add(apmFlowNodeTask);
            }
        }
        return apmFlowNodeTasks;
    }

    /**
     * 获取流程节点可见条件配置
     * @param apmFlowTemplateNode
     * @param apmFlowNodeDisplayConditionAOList
     * @return
     */
    public List<ApmFlowNodeDisplayCondition> getAmpFlowNodeDisplayConditions(ApmFlowTemplateNode apmFlowTemplateNode, List<ApmFlowNodeDisplayConditionAO> apmFlowNodeDisplayConditionAOList){
        List<ApmFlowNodeDisplayCondition> apmFlowNodeDisplayConditions = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(apmFlowNodeDisplayConditionAOList)){
            for(ApmFlowNodeDisplayConditionAO apmFlowNodeDisplayConditionAO : apmFlowNodeDisplayConditionAOList){
                ApmFlowNodeDisplayCondition apmFlowNodeDisplayCondition = AmpFlowNodeDisplayConditionConverter.INSTANCE.ao2Entity(apmFlowNodeDisplayConditionAO);
                apmFlowNodeDisplayCondition.setFlowTemplateBid(apmFlowTemplateNode.getFlowTemplateBid());
                apmFlowNodeDisplayCondition.setVersion(apmFlowTemplateNode.getVersion());
                apmFlowNodeDisplayCondition.setNodeBid(apmFlowTemplateNode.getBid());
                apmFlowNodeDisplayCondition.setBid(SnowflakeIdWorker.nextIdStr());
                apmFlowNodeDisplayConditions.add(apmFlowNodeDisplayCondition);
            }
        }
        return apmFlowNodeDisplayConditions;
    }

    private String getVersion(String oldVersion){
        if(StringUtils.isEmpty(oldVersion)){
            return "V1";
        }
        Integer v = Integer.parseInt(oldVersion.replace("V",""));
        v++;
        return "V"+v;
    }

    private List<String> getNodeBids(List<String> webBids,Map<String,String> webBidRelMap){
        List<String> bids = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(webBids) && CollectionUtils.isNotEmpty(webBidRelMap)){
            for(int i=0;i<webBids.size();i++){
                bids.add(webBidRelMap.get(webBids.get(i)));
            }
        }
        return bids;
    }


    @Override
    public List<ApmRoleAndIdentityVo> listFlowTemplateRoles(String flowTemplateBid){
        ApmFlowTemplate apmFlowTemplate = apmFlowTemplateService.getByBid(flowTemplateBid);
        if(apmFlowTemplate == null){
            return null;
        }
        List<ApmFlowTemplateNode> nodeList = apmFlowTemplateNodeService.listByTemplateBidAndVersion(apmFlowTemplate.getBid(),apmFlowTemplate.getVersion());
        if(CollectionUtils.isEmpty(nodeList)){
            return null;
        }
        Set<String> roleBidSets = new HashSet<>();
        for(ApmFlowTemplateNode apmFlowTemplateNode:nodeList){
            if(CollectionUtils.isNotEmpty(apmFlowTemplateNode.getNodeRoleBids())){
                roleBidSets.addAll(apmFlowTemplateNode.getNodeRoleBids());
            }
            if(CollectionUtils.isNotEmpty(apmFlowTemplateNode.getComplateRoleBids())){
                roleBidSets.addAll(apmFlowTemplateNode.getComplateRoleBids());
            }
        }
        List<ApmRoleAndIdentityVo> roleAndIdentityVos = apmRoleDomainService.getRoleAndIdentityByRoleBids(new ArrayList<>(roleBidSets));
        return roleAndIdentityVos;
    }

    @Override
    public ApmFlowInstanceVO listInstanceNodesByApmStateQo(ApmStateQo apmStateQo) {
        List<ApmFlowTemplate> apmFlowTemplateList = apmFlowTemplateService.list(Wrappers.<ApmFlowTemplate>lambdaQuery().eq(ApmFlowTemplate::getBid, apmStateQo.getLcTemplBid()));
        if (CollectionUtils.isNotEmpty(apmFlowTemplateList)) {
            ApmFlowTemplate apmFlowTemplate = apmFlowTemplateList.get(0);
            if(FlowTypeEnum.STATE.getCode().equals(apmFlowTemplate.getType())){
                List<ApmFlowTemplateNode> apmFlowTemplateNodes = apmFlowTemplateNodeService.list(Wrappers.<ApmFlowTemplateNode>lambdaQuery().eq(ApmFlowTemplateNode::getFlowTemplateBid, apmFlowTemplate.getBid()).eq(ApmFlowTemplateNode::getVersion, apmFlowTemplate.getVersion()).eq(ApmFlowTemplateNode::getDeleteFlag, false));
                return ApmFlowInstanceVO.builder().layout(apmFlowTemplate.getLayout()).apmFlowTemplateNodes(apmFlowTemplateNodes).build();
            }
            //查收所有状态数据
        }
        return null;
    }

    @Override
    public Map<String, Set<String>> queryNodeUsersByInstanceBids(List<String> instanceBids) {
        List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = apmFlowInstanceRoleUserService.queryNodeUsersByInstanceBids(instanceBids);
        return apmFlowInstanceRoleUsers.stream()
                .collect(
                        Collectors.groupingBy(
                                ApmFlowInstanceRoleUser::getInstanceBid,
                                Collectors.mapping(
                                        ApmFlowInstanceRoleUser::getUserNo,
                                        Collectors.toSet()
                                )
                        )
                );
    }
}
