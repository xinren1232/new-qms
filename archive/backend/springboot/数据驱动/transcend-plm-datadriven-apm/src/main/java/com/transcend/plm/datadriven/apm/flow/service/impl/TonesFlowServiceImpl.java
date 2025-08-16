package com.transcend.plm.datadriven.apm.flow.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.transcend.framework.core.exception.TranscendBizException;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.apm.constants.FlowNodeTypeConstant;
import com.transcend.plm.datadriven.apm.constants.FlowStateConstant;
import com.transcend.plm.datadriven.apm.flow.pojo.qo.ApmFlowQo;
import com.transcend.plm.datadriven.apm.flow.pojo.type.TonesFlowLifeCycleEnum;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmTonesFlowRecord;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowInstanceNodeService;
import com.transcend.plm.datadriven.apm.flow.service.ApmTonesFlowRecordService;
import com.transcend.plm.datadriven.apm.flow.service.IRuntimeService;
import com.transcend.plm.datadriven.apm.flow.service.ITonesFlowService;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmRoleUserAO;
import com.transcend.plm.datadriven.apm.permission.pojo.bo.ApmUser;
import com.transcend.plm.datadriven.apm.task.service.ApmTaskService;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.common.tool.CommonConstant;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author unknown
 */
@Service
@Slf4j
public class TonesFlowServiceImpl implements ITonesFlowService {
    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Resource
    private ApmFlowInstanceNodeService apmFlowInstanceNodeService;
    @Resource
    private IRuntimeService runtimeService;
    @Resource
    private ApmTaskService apmTaskService;
    @Resource
    private ApmTonesFlowRecordService apmTonesFlowRecordService;

    public void handleHandler(MObject mObject){
        List<ApmFlowInstanceNode> apmFlowInstanceNodes = apmFlowInstanceNodeService.listByInstanceBid(mObject.getBid());
        Map<String,ApmFlowInstanceNode> nodeMap = apmFlowInstanceNodes.stream().collect(Collectors.toMap(ApmFlowInstanceNode::getWebBid, Function.identity(),(k1, k2)->k2));
        //过滤分发节点处理人
        ApmFlowInstanceNode apmFlowInstanceNode = nodeMap.get("Fe_NmrfGx1rjNIox_");
        if(apmFlowInstanceNode == null){
            return;
        }
        List<String> currentUsers = new ArrayList<>();
        List<String> nodeRoleBids = apmFlowInstanceNode.getNodeRoleBids();
        List<ApmRoleUserAO> apmRoleUserAOS = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(nodeRoleBids)){
            ApmRoleUserAO apmRoleUserAO = new ApmRoleUserAO();
            apmRoleUserAO.setRoleBid(nodeRoleBids.get(0));
            List<ApmUser> userList = new ArrayList<>();
            //1、若状态为“已创建”，则节点人员为董瑞敏
            //2、若其他状态，则节点人员为分发人
            ApmUser apmUser = new ApmUser();
            if(TonesFlowLifeCycleEnum.REQUIREMENTANALYSIS.name().equals(mObject.getLifeCycleCode())){
                apmUser.setEmpNo("18649186");
                userList.add(apmUser);
                currentUsers.add("18649186");
            }else{
                //分发人
                userList = getApmUsers(mObject.get("distributor"));
            }
            apmRoleUserAO.setUserList(userList);
            apmRoleUserAOS.add(apmRoleUserAO);
        }
        //需求价值评估 ，已经过这个节点（评估人）
        ApmFlowInstanceNode xqjzNode = nodeMap.get("kP_--C28QTqGo-mr_");
        List<String> xqjzRoleBids = xqjzNode.getNodeRoleBids();
        if(CollectionUtils.isNotEmpty(xqjzRoleBids)){
            List<ApmUser> xqjzUserList = new ArrayList<>();
            //评估人
            if(TonesFlowLifeCycleEnum.DOMAINVALUEASSESSMENT.name().equals(mObject.getLifeCycleCode())){
                //已经到这个节点 产品经理
                xqjzUserList = getApmUsers(mObject.get("tonesProductManager"));
                currentUsers.addAll(getObjectList(mObject.get("tonesProductManager")));
            }else{
                xqjzUserList = getApmUsers(mObject.get("assessor"));
            }
            ApmRoleUserAO xqjzApmRoleUserAO = new ApmRoleUserAO();
            xqjzApmRoleUserAO.setRoleBid(xqjzRoleBids.get(0));
            xqjzApmRoleUserAO.setUserList(xqjzUserList);
            apmRoleUserAOS.add(xqjzApmRoleUserAO);
        }
        //拆解关联需求  如果是当前节点之后，获取评估人
       if(TonesFlowLifeCycleEnum.COMPLETED.name().equals(mObject.getLifeCycleCode()) || TonesFlowLifeCycleEnum.INPLANING.name().equals(mObject.getLifeCycleCode())){
           ApmFlowInstanceNode cjNode = nodeMap.get("om5f9zJvXiCxHOIY_");
           List<String> cjRoleBids = cjNode.getNodeRoleBids();
           if(CollectionUtils.isNotEmpty(cjRoleBids)){
               //评估人
               List<ApmUser> cjUserList = getApmUsers(mObject.get("assessor"));
               ApmRoleUserAO cjApmRoleUserAO = new ApmRoleUserAO();
               cjApmRoleUserAO.setRoleBid(cjRoleBids.get(0));
               cjApmRoleUserAO.setUserList(cjUserList);
               apmRoleUserAOS.add(cjApmRoleUserAO);
               if(TonesFlowLifeCycleEnum.INPLANING.name().equals(mObject.getLifeCycleCode())){
                   currentUsers.addAll(getObjectList(mObject.get("assessor")));
               }
           }
       }
       //需求验证 若验证人（verifier）存在，则解析为验证人，否则解析到需求创建人
        ApmFlowInstanceNode xqyzNode = nodeMap.get("4y2JtcFrCJGUIjRa_");
        List<String> xqyzRoleBids = xqyzNode.getNodeRoleBids();
        if(CollectionUtils.isNotEmpty(xqyzRoleBids)){
            List<ApmUser> xqyzUserList = getApmUsers(mObject.get("verifier"));
            if(CollectionUtils.isEmpty(xqyzUserList)){
                ApmUser xqyzUser = new ApmUser();
                xqyzUser.setEmpNo(mObject.getCreatedBy());
                xqyzUserList.add(xqyzUser);
                if(TonesFlowLifeCycleEnum.VERIFIED.name().equals(mObject.getLifeCycleCode())){
                    currentUsers.add(mObject.getCreatedBy());
                }
            }else{
                if(TonesFlowLifeCycleEnum.VERIFIED.name().equals(mObject.getLifeCycleCode())){
                    currentUsers.addAll(getObjectList(mObject.get("verifier")));
                }
            }
            ApmRoleUserAO xqyzApmRoleUserAO = new ApmRoleUserAO();
            xqyzApmRoleUserAO.setRoleBid(xqyzRoleBids.get(0));
            xqyzApmRoleUserAO.setUserList(xqyzUserList);
            apmRoleUserAOS.add(xqyzApmRoleUserAO);
        }
        //退回确认 解析需求创建人
        ApmFlowInstanceNode backNode = nodeMap.get("zcOs7_5Y5cNgQ5iL_");
        List<String> backRoleBids = backNode.getNodeRoleBids();
        if(CollectionUtils.isNotEmpty(backRoleBids)){
            List<ApmUser> backUserList = new ArrayList<>();
            ApmUser backUser = new ApmUser();
            backUser.setEmpNo(mObject.getCreatedBy());
            backUserList.add(backUser);
            ApmRoleUserAO backApmRoleUserAO = new ApmRoleUserAO();
            backApmRoleUserAO.setRoleBid(backRoleBids.get(0));
            backApmRoleUserAO.setUserList(backUserList);
            apmRoleUserAOS.add(backApmRoleUserAO);
        }
        if(CollectionUtils.isNotEmpty(apmRoleUserAOS)){
            ApmFlowQo apmFlowQo = new ApmFlowQo();
            apmFlowQo.setRoleUserList(apmRoleUserAOS);
            apmFlowQo.setSpaceBid(mObject.get(TranscendModelBaseFields.SPACE_BID)+"");
            apmFlowQo.setSpaceAppBid(mObject.get(TranscendModelBaseFields.SPACE_APP_BID)+"");
            runtimeService.updateTonesRoleUser(mObject.getBid(),apmFlowQo);
        }
        ApmTonesFlowRecord apmTonesFlowRecord = new ApmTonesFlowRecord();
        apmTonesFlowRecord.setDeleteFlag(false);
        apmTonesFlowRecord.setResult("1");
        apmTonesFlowRecord.setRrBid(mObject.getBid());
        apmTonesFlowRecordService.save(apmTonesFlowRecord);
        MObject updateObject = new MObject();
        updateObject.put("currentHandler",currentUsers);
        List<String> bids = new ArrayList<>();
        bids.add(mObject.getBid());
        boolean res= objectModelCrudI.batchUpdatePartialContentByIds("A5E", updateObject, bids);
        log.info("res:"+res);
    }

    private List<ApmUser> getApmUsers(Object obj){
        List<String> verifiers = getObjectList(obj);
        List<ApmUser> resList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(verifiers)){
            for(String ver:verifiers){
                ApmUser xqyzUser = new ApmUser();
                xqyzUser.setEmpNo(ver);
                resList.add(xqyzUser);
            }
        }
        return resList;
    }

    private List<String> getObjectList(Object object){
        List<String> list = new ArrayList<>();
        if(object == null){
            return list;
        }
        if(object instanceof List){
            list = JSON.parseArray(object.toString(), String.class);
        }else if(object instanceof String){
            String objectStr = (String) object;
            //将objectStr中"替换成空格
            objectStr = objectStr.replaceAll("\"", "");
            if(StringUtils.isNotEmpty(objectStr)){
                list.add(objectStr);
            }
        }else{
            if(object.toString().startsWith(CommonConstant.OPEN_BRACKET)){
                list = JSON.parseArray(object.toString(), String.class);
            }else{
                list.add(object.toString());
            }
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleTonesFlowNodes(MObject mObject){
        List<ApmFlowInstanceNode> apmFlowInstanceNodes = apmFlowInstanceNodeService.listByInstanceBid(mObject.getBid());
        ApmFlowInstanceNode taskNode = null;
        if(CollectionUtils.isNotEmpty(apmFlowInstanceNodes)){


            //获取ApmFlowInstanceNode bid集合
            List<String> allNodeBids = apmFlowInstanceNodes.stream().map(e->e.getBid()).collect(Collectors.toList());
            //主流程，开始-分析过滤分发-需求价值评估-拆解/关联需求-需求验证-结束
            //开始(0000000000000000000)-分析过滤分发(Fe_NmrfGx1rjNIox_)-需求价值评估(kP_--C28QTqGo-mr_)-拆解/关联需求(om5f9zJvXiCxHOIY_)-需求验证(4y2JtcFrCJGUIjRa_)-结束(wKqaYi7-fro8Dmy5_)
            List<String> mainFlowList = Arrays.asList("0000000000000000000","Fe_NmrfGx1rjNIox_","kP_--C28QTqGo-mr_","om5f9zJvXiCxHOIY_","4y2JtcFrCJGUIjRa_","wKqaYi7-fro8Dmy5_");
            //需求驳回的流程，开始-分析过滤分发-退回确认-结束
            //开始(0000000000000000000)-分析过滤分发(Fe_NmrfGx1rjNIox_)-退回确认(zcOs7_5Y5cNgQ5iL_)-结束(lMyQ1rKhS8QlIylF_)
            List<String> bhFlowList = Arrays.asList("0000000000000000000","Fe_NmrfGx1rjNIox_","zcOs7_5Y5cNgQ5iL_","lMyQ1rKhS8QlIylF_");
            //需求拒绝的流程，开始-分析过滤分发-需求价值评估-结束（走No Go）
            //开始(0000000000000000000)-分析过滤分发(Fe_NmrfGx1rjNIox_)-需求价值评估(kP_--C28QTqGo-mr_)-结束(yHJC2TgMZQQ5eEuX_)（走No Go）
            List<String> jjFlowList = Arrays.asList("0000000000000000000","Fe_NmrfGx1rjNIox_","kP_--C28QTqGo-mr_","yHJC2TgMZQQ5eEuX_");
            List<ApmFlowInstanceNode> updateList = new ArrayList<>();
            if(TonesFlowLifeCycleEnum.COMPLETED.name().equals(mObject.getLifeCycleCode())){
               //Transcend的状态为“已完成(COMPLETED)”，当前节点为结束，按照主流程来点亮节点
               updateList.addAll(getClosedInstanceNodes(apmFlowInstanceNodes,mainFlowList));
            }
            if(TonesFlowLifeCycleEnum.REQUIREMENTANALYSIS.name().equals(mObject.getLifeCycleCode())){
                //Transcend的状态为“需求分析(REQUIREMENTANALYSIS)”，当前节点为“分析过滤分发”，按照主流程来点亮节点
                List<String> nodeList = getNodeList(mainFlowList,"Fe_NmrfGx1rjNIox_");
                updateList.addAll(getInstanceNodes(apmFlowInstanceNodes,nodeList));
                taskNode = getThisNode(apmFlowInstanceNodes,nodeList);
            }
            if(TonesFlowLifeCycleEnum.DOMAINVALUEASSESSMENT.name().equals(mObject.getLifeCycleCode())){
                //Transcend的状态为“需求分析(REQUIREMENTANALYSIS)”，当前节点为“分析过滤分发”，按照主流程来点亮节点
                List<String> nodeList = getNodeList(mainFlowList,"kP_--C28QTqGo-mr_");
                updateList.addAll(getInstanceNodes(apmFlowInstanceNodes,nodeList));
                taskNode = getThisNode(apmFlowInstanceNodes,nodeList);
            }
            if(TonesFlowLifeCycleEnum.INPLANING.name().equals(mObject.getLifeCycleCode())){
                //Transcend的状态为“规划中(INPLANING)”，当前节点为拆解/关联需求，按照主流程来点亮节点
                List<String> nodeList = getNodeList(mainFlowList,"om5f9zJvXiCxHOIY_");
                updateList.addAll(getInstanceNodes(apmFlowInstanceNodes,nodeList));
                taskNode = getThisNode(apmFlowInstanceNodes,nodeList);
            }
            if(TonesFlowLifeCycleEnum.VERIFIED.name().equals(mObject.getLifeCycleCode())){
                //Transcend的状态为“待验证(VERIFIED)”，当前节点为需求验证，按照主流程来点亮节点
                List<String> nodeList = getNodeList(mainFlowList,"4y2JtcFrCJGUIjRa_");
                updateList.addAll(getInstanceNodes(apmFlowInstanceNodes,nodeList));
                taskNode = getThisNode(apmFlowInstanceNodes,nodeList);
            }
            if(TonesFlowLifeCycleEnum.INVALID.name().equals(mObject.getLifeCycleCode())){
                //Transcend的状态为“无效(INVALID)”，当前节点为结束，按照需求驳回的流程来点亮节点
                updateList.addAll(getClosedInstanceNodes(apmFlowInstanceNodes,bhFlowList));
            }
            if(TonesFlowLifeCycleEnum.BUG_REJECTED.name().equals(mObject.getLifeCycleCode())
                    || TonesFlowLifeCycleEnum.VERIFY_REJECTED.name().equals(mObject.getLifeCycleCode())){
                //Transcend的状态为“无效(INVALID)”，当前节点为结束，按照需求驳回的流程来点亮节点
                updateList.addAll(getClosedInstanceNodes(apmFlowInstanceNodes,jjFlowList));
            }
            Map<String,String> updateMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
            if(CollectionUtils.isNotEmpty(updateList)){
                for(ApmFlowInstanceNode node : updateList){
                    updateMap.put(node.getBid(),node.getBid());
                }
            }
            for(ApmFlowInstanceNode node : apmFlowInstanceNodes){
                if(!updateMap.containsKey(node.getBid()) && !FlowNodeTypeConstant.START_NODE.equals(node.getNodeType()) && !FlowStateConstant.NOT_START.equals(node.getNodeState())){
                    //更新节点状态
                    node.setNodeState(FlowStateConstant.NOT_START);
                    updateList.add(node);
                }
            }
            //原来的删除待办任务
            apmTaskService.deleteByIds("flow", allNodeBids,0);
            apmFlowInstanceNodeService.updateBatchById(updateList);
            if(taskNode != null){
                runtimeService.createOlnyTask(taskNode);
            }
            ApmTonesFlowRecord apmTonesFlowRecord = new ApmTonesFlowRecord();
            apmTonesFlowRecord.setDeleteFlag(false);
            apmTonesFlowRecord.setResult("1");
            apmTonesFlowRecord.setRrBid(mObject.getBid());
            apmTonesFlowRecordService.save(apmTonesFlowRecord);
        }
        return true;
    }

    private List<ApmFlowInstanceNode> getClosedInstanceNodes(List<ApmFlowInstanceNode> apmFlowInstanceNodes,List<String> nodeList){
        List<ApmFlowInstanceNode> resList = new ArrayList<>();
        for(ApmFlowInstanceNode apmFlowInstanceNode:apmFlowInstanceNodes){
            if(nodeList.contains(apmFlowInstanceNode.getWebBid())){
                apmFlowInstanceNode.setNodeState(2);
                resList.add(apmFlowInstanceNode);
            }
        }
        return resList;
    }

    private ApmFlowInstanceNode getThisNode(List<ApmFlowInstanceNode> apmFlowInstanceNodes,List<String> nodeList){
        for(ApmFlowInstanceNode apmFlowInstanceNode:apmFlowInstanceNodes){
            if(nodeList.contains(apmFlowInstanceNode.getWebBid())){
                if(apmFlowInstanceNode.getWebBid().equals(nodeList.get(nodeList.size()-1))){
                    return apmFlowInstanceNode;
                }
            }
        }
        return null;
    }

    private List<ApmFlowInstanceNode> getInstanceNodes(List<ApmFlowInstanceNode> apmFlowInstanceNodes,List<String> nodeList){
        List<ApmFlowInstanceNode> resList = new ArrayList<>();
        for(ApmFlowInstanceNode apmFlowInstanceNode:apmFlowInstanceNodes){
            if(nodeList.contains(apmFlowInstanceNode.getWebBid())){
                if(apmFlowInstanceNode.getWebBid().equals(nodeList.get(nodeList.size()-1))){
                    apmFlowInstanceNode.setNodeState(1);
                }else{
                    apmFlowInstanceNode.setNodeState(2);
                }
                resList.add(apmFlowInstanceNode);
            }
        }
        return resList;
    }

    private List<String> getNodeList(List<String> list,String node){
        List<String> resList = new ArrayList<>();
        for(String str:list){
            resList.add(str);
            if(str.equals(node)){
                break;
            }
        }
        return resList;
    }

    @Override
    public boolean handleNodeState(){
        List<MObject> list = listTonesObject();
        List<ApmTonesFlowRecord> apmTonesFlowRecords = apmTonesFlowRecordService.list(new LambdaQueryWrapper<ApmTonesFlowRecord>().eq(ApmTonesFlowRecord::getResult,"1").eq(ApmTonesFlowRecord::getDeleteFlag,false));
        Map<String,String> rrBidMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        if(CollectionUtils.isNotEmpty(apmTonesFlowRecords)){
            rrBidMap = apmTonesFlowRecords.stream().collect(Collectors.toMap(e->e.getRrBid(),e->e.getRrBid()));
        }
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                15,
                60,
                120,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2048),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy());
        List<CompletableFuture<Void>> completableFutures = Lists.newArrayList();
        for(MObject mObject:list){
            if(!rrBidMap.containsKey(mObject.getBid())){
                completableFutures.add(CompletableFuture.runAsync(() -> {
                    handleTonesFlowNodes(mObject);
                }, executor));
            }
        }
        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).exceptionally(ex -> {
            log.error("项目集产品需求明细多线程查询异常", ex);
            throw new TranscendBizException("项目集产品需求明细多线程查询异常");
        }).join();
        return true;
    }

    @Override
    public boolean handleNodeHandler(){
        List<MObject> list = listTonesObject();
        List<ApmTonesFlowRecord> apmTonesFlowRecords = apmTonesFlowRecordService.list(new LambdaQueryWrapper<ApmTonesFlowRecord>().eq(ApmTonesFlowRecord::getResult,"1").eq(ApmTonesFlowRecord::getDeleteFlag,false));
        Map<String,String> rrBidMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        if(CollectionUtils.isNotEmpty(apmTonesFlowRecords)){
            rrBidMap = apmTonesFlowRecords.stream().collect(Collectors.toMap(e->e.getRrBid(),e->e.getRrBid()));
        }
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                15,
                60,
                120,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2048),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy());
        List<CompletableFuture<Void>> completableFutures = Lists.newArrayList();
        for(MObject mObject:list){
            if(!rrBidMap.containsKey(mObject.getBid())){
                completableFutures.add(CompletableFuture.runAsync(() -> {
                    handleHandler(mObject);
                }, executor));

            }
        }
        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).exceptionally(ex -> {
            log.error("项目集产品需求明细多线程查询异常", ex);
            throw new TranscendBizException("项目集产品需求明细多线程查询异常");
        }).join();
        return true;
    }

    @Override
    public List<MObject> listTonesObject(){
        //查询tones数据
        QueryWrapper qo = new QueryWrapper();
        qo.eq("historical_data_identification", "1").and().eq("delete_flag", 0);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        return objectModelCrudI.list("A5E", queryWrappers);
    }
}
