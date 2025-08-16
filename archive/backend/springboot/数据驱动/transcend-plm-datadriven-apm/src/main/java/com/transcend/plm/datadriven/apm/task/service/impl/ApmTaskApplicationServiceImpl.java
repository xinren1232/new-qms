package com.transcend.plm.datadriven.apm.task.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.common.util.StringUtil;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.framework.sso.tool.TranscendUserContextHolder;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.apm.constants.FlowNodeCompleteType;
import com.transcend.plm.datadriven.apm.constants.FlowNodeStateConstant;
import com.transcend.plm.datadriven.apm.constants.TaskConstant;
import com.transcend.plm.datadriven.apm.enums.CommonEnum;
import com.transcend.plm.datadriven.apm.flow.repository.mapper.ApmFlowInstanceNodeMapper;
import com.transcend.plm.datadriven.apm.flow.repository.po.*;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowInstanceNodeService;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowInstanceRoleUserService;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowTemplateNodeService;
import com.transcend.plm.datadriven.apm.flow.service.IRuntimeService;
import com.transcend.plm.datadriven.apm.notice.PushCenterFeishuBuilder;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceAppVo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.apm.task.ao.ApmTaskAO;
import com.transcend.plm.datadriven.apm.task.ao.ApmTaskDeleteAO;
import com.transcend.plm.datadriven.apm.task.domain.ApmTask;
import com.transcend.plm.datadriven.apm.task.mapstruct.ApmTaskConverter;
import com.transcend.plm.datadriven.apm.task.service.ApmTaskApplicationService;
import com.transcend.plm.datadriven.apm.task.service.ApmTaskService;
import com.transcend.plm.datadriven.apm.task.vo.ApmTaskNumVO;
import com.transcend.plm.datadriven.apm.task.vo.ApmTaskVO;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transcend.plm.datadriven.common.pool.SimpleThreadPool;
import com.transcend.plm.datadriven.common.tool.CommonConstant;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transsion.framework.auth.IUser;
import com.transsion.framework.auth.IUserContext;
import com.transsion.framework.context.holder.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 任务处理
 * @author unknown
 */
@Service
@Slf4j
public class ApmTaskApplicationServiceImpl implements ApmTaskApplicationService {
    @Resource
    private ApmTaskService apmTaskService;

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;
    @Resource
    private ApmFlowInstanceNodeService apmFlowInstanceNodeService;
    @Resource
    private ApmFlowInstanceNodeMapper apmFlowInstanceNodeMapper;

    @Resource
    private ApmFlowTemplateNodeService apmFlowTemplateNodeService;
    @Resource
    private ApmTaskApplicationService apmTaskApplicationService;
    @Resource
    private ApmFlowInstanceRoleUserService apmFlowInstanceRoleUserService;
    @Resource
    private ApmSpaceAppService apmSpaceAppService;

    @Resource
    private IRuntimeService runtimeService;

    @Resource
    private IApmSpaceAppDataDrivenService apmSpaceAppDataDrivenService;

    @Value("${transcend.plm.apm.flow.task.notify:false}")
    private boolean flowTaskNotify;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> saveOrUpdateApmTask(ApmTaskAO apmTaskAO){
        List<String> resList = new ArrayList<>();
        List<ApmTask> apmTaskList = apmTaskService.listApmTasks(apmTaskAO.getTaskType(),apmTaskAO.getBizBid(), TaskConstant.COMPLETED);
        Map<String,ApmTask> apmTaskMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        if(CollectionUtils.isNotEmpty(apmTaskList)){
            for(ApmTask apmTask:apmTaskList){
                apmTaskMap.put(apmTask.getTaskHandler(),apmTask);
            }
        }
        List<ApmTask> apmTasks = new ArrayList<>();
        Map<String,String> oldTaskMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        for(String hander:apmTaskAO.getHandlers()){
            if(!apmTaskMap.containsKey(hander)){
                //ApmTask apmTaskStatus = apmTaskApplicationService.getApmTask(apmTaskAO.getBizBid(), hander);
                long notCompleteTaskCount = apmTaskApplicationService.getNotCompleteTaskCount(apmTaskAO.getBizBid(), hander);
                if (notCompleteTaskCount == 0){
                    apmTaskService.logicDeleteComplete(apmTaskAO.getBizBid(), hander, TaskConstant.FLOW_TYPE);
                    //新增
                    ApmTask apmTask = new ApmTask();
                    apmTask.setBid(SnowflakeIdWorker.nextIdStr());
                    apmTask.setBizBid(apmTaskAO.getBizBid());
                    apmTask.setTaskType(apmTaskAO.getTaskType());
                    apmTask.setTaskHandler(hander);
                    apmTask.setTaskState(TaskConstant.NOT_START);
                    apmTasks.add(apmTask);
                    resList.add(hander);
                }
            }else{
                oldTaskMap.put(hander,hander);
            }
        }
        if(CollectionUtils.isNotEmpty(apmTaskList)){
            List<Integer> ids = new ArrayList<>();
            for(ApmTask apmTask:apmTaskList){
                if(!oldTaskMap.containsKey(apmTask.getTaskHandler())){
                    ids.add(apmTask.getId());
                }
            }
            if(CollectionUtils.isNotEmpty(ids)){
                apmTaskService.deleteByTaskIds(ids);
            }
        }
        if(CollectionUtils.isNotEmpty(apmTasks)){
            apmTaskService.saveBatch(apmTasks);
        }
        return resList;
    }

    @Override
    public List<ApmTaskNumVO> getTaskNum(){
        String jobNumber = SsoHelper.getJobNumber();
        long undoNum = apmTaskService.countUserApmTask(jobNumber,TaskConstant.NOT_START);
        long doneNum = apmTaskService.countUserApmTask(jobNumber,TaskConstant.COMPLETED);
        List<ApmTaskNumVO> res = new ArrayList<>();
        ApmTaskNumVO apmTaskNumVO1 = new ApmTaskNumVO();
        apmTaskNumVO1.setTaskType("待办");
        apmTaskNumVO1.setTaskState(TaskConstant.NOT_START);
        apmTaskNumVO1.setNum(undoNum);
        ApmTaskNumVO apmTaskNumVO2 = new ApmTaskNumVO();
        apmTaskNumVO2.setTaskType("已办");
        apmTaskNumVO2.setTaskState(TaskConstant.COMPLETED);
        apmTaskNumVO2.setNum(doneNum);
        res.add(apmTaskNumVO1);
        res.add(apmTaskNumVO2);
        return res;
    }

    @Override
    public boolean deleteByBizBids(String taskType, List<String> bizBids) {
        return apmTaskService.deleteByBizBids(taskType,bizBids) == 0?false:true;
    }

    @Override
    public ApmTask getApmTask(String bizBid,String userNo) {
        return  apmTaskService.getBaseMapper()
                .selectOne(new LambdaQueryWrapper<>(ApmTask.class)
                        .eq(ApmTask::getBizBid, bizBid)
                        .eq(ApmTask::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED)
                        .eq(ApmTask::getTaskHandler, userNo)
                        .eq(ApmTask::getTaskState,  TaskConstant.COMPLETED));
    }

    public long countCompleteApmTask(String bizBid,String userNo){
        return  apmTaskService.getBaseMapper()
                .selectCount(new LambdaQueryWrapper<>(ApmTask.class)
                        .eq(ApmTask::getBizBid, bizBid)
                        .eq(ApmTask::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED)
                        .eq(ApmTask::getTaskHandler, userNo)
                        .eq(ApmTask::getTaskState,  TaskConstant.COMPLETED));
    }

    public long countNotCompleteApmTask(String bizBid,String userNo){
        return  apmTaskService.getBaseMapper()
                .selectCount(new LambdaQueryWrapper<>(ApmTask.class)
                        .eq(ApmTask::getBizBid, bizBid)
                        .eq(ApmTask::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED)
                        .eq(ApmTask::getTaskHandler, userNo)
                        .ne(ApmTask::getTaskState,  TaskConstant.COMPLETED));
    }

    public long getNotCompleteTaskCount(String bizBid,String userNo){
        return  apmTaskService.getBaseMapper()
                .selectCount(new LambdaQueryWrapper<>(ApmTask.class)
                        .eq(ApmTask::getBizBid, bizBid)
                        .eq(ApmTask::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED)
                        .eq(ApmTask::getTaskHandler, userNo)
                        .ne(ApmTask::getTaskState,  TaskConstant.COMPLETED));
    }

    @Override
    public List<ApmTask> getUserNodeTaskState(String bizBid, String userNo) {
        return  apmTaskService.getBaseMapper()
                .selectList(new LambdaQueryWrapper<>(ApmTask.class)
                        .eq(ApmTask::getBizBid, bizBid)
                        .eq(ApmTask::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED)
                        .eq(ApmTask::getTaskHandler, userNo));
    }

    @Override
    public List<ApmTaskVO> listUserApmTasks(int taskState) {
        List<ApmTaskVO> taskList = new ArrayList<>();
        String jobNumber = SsoHelper.getJobNumber();
        //查询代办任务主表
        List<ApmTask> apmTasks = apmTaskService.listUserApmTask(jobNumber,taskState);
        if(CollectionUtils.isNotEmpty(apmTasks)){
            //按照业务bid分组 处理流程任务
            //Map<String,List<ApmTask>> flowTaskMap = getTaskTypeMap(apmTasks,TaskConstant.FLOW_TYPE);
            Map<String, List<ApmTask>> bizTaskMap = apmTasks.stream().collect(Collectors.groupingBy(ApmTask::getBizBid));
            //查询其业务bid集合
            List<String> bizBids = new ArrayList<>(bizTaskMap.keySet());
            List<ApmFlowInstanceNode> apmFlowInstanceNodeList = apmFlowInstanceNodeService.listByBids(bizBids);
            Map<String,ApmFlowInstanceNode> apmFlowInstanceNodeMap =  apmFlowInstanceNodeList.stream().collect(Collectors.toMap(ApmFlowInstanceNode::getBid, Function.identity()));
            //查询空间名称
            List<String> spaceAppBids = apmFlowInstanceNodeList.stream().map(e->e.getSpaceAppBid()).distinct().collect(Collectors.toList());
            Map<String,ApmSpaceAppVo> spaceNameMap = getSpaceNameMap(spaceAppBids);
            Map<String, MSpaceAppData> spaceAppDataMap = getMSpaceAppDataMap(apmFlowInstanceNodeList,spaceNameMap);
            List<ApmTaskVO> resList = ApmTaskConverter.INSTANCE.entitys2Vos(apmTasks);
            for(ApmTaskVO apmTaskVO:resList){
                ApmFlowInstanceNode apmFlowInstanceNode = apmFlowInstanceNodeMap.get(apmTaskVO.getBizBid());
                if(apmFlowInstanceNode != null && spaceAppDataMap.containsKey(apmFlowInstanceNode.getInstanceBid())){
                    apmTaskVO.setInstance(spaceAppDataMap.get(apmFlowInstanceNode.getInstanceBid()));
                    apmTaskVO.setApmFlowInstanceNode(apmFlowInstanceNode);
                    ApmSpaceAppVo apmSpaceAppVo = spaceNameMap.get(apmFlowInstanceNode.getSpaceAppBid());
                    if(apmSpaceAppVo != null){
                        apmTaskVO.setSpaceName(apmSpaceAppVo.getName());
                        apmTaskVO.setSpaceBid(apmSpaceAppVo.getSpaceBid());
                    }
                    apmTaskVO.setSpaceAppBid(apmFlowInstanceNode.getSpaceAppBid());
                    taskList.add(apmTaskVO);
                }
            }
        }
        //查询图标，因为这里的数据登录人可能没有空间权限，所以后端把图标返回
        if (CollectionUtils.isNotEmpty(taskList)){
            List<ApmSpaceAppVo> apmSpaceAppVos = apmSpaceAppService.listSpaceAppVoByBids(taskList.stream().map(ApmTaskVO::getSpaceAppBid).distinct().collect(Collectors.toList()));
            if (CollectionUtils.isNotEmpty(apmSpaceAppVos)) {
                Map<String, String> iconMap = apmSpaceAppVos.stream().collect(Collectors.toMap(ApmSpaceAppVo::getBid, ApmSpaceAppVo::getIconUrl));
                taskList.forEach(e->{
                    e.setIconUrl(iconMap.get(e.getSpaceAppBid()));
                });
            }
        }
        return taskList;
    }

    private Map<String,ApmSpaceAppVo> getSpaceNameMap(List<String> spaceAppBids){
        List<ApmSpaceAppVo> apmSpaceAppVos = apmSpaceAppService.listSpaceInfo(spaceAppBids);
        Map<String,ApmSpaceAppVo> resMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        if(CollectionUtils.isNotEmpty(apmSpaceAppVos)){
            for(ApmSpaceAppVo apmSpaceAppVo:apmSpaceAppVos){
                resMap.put(apmSpaceAppVo.getBid(),apmSpaceAppVo);
            }
        }
        return resMap;
    }

    private Map<String,MSpaceAppData> getMSpaceAppDataMap(List<ApmFlowInstanceNode> apmFlowInstanceNodeList,Map<String,ApmSpaceAppVo> spaceNameMap){
        //根据ApmFlowInstanceNode查实例数据
        Map<String,MSpaceAppData> instMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        if(CollectionUtils.isEmpty(apmFlowInstanceNodeList)){
            return instMap;
        }
        Map<String,List<String>> spaceAppBidAndInstanceBidMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        for(ApmFlowInstanceNode apmFlowInstanceNode:apmFlowInstanceNodeList){
            List<String> instanceBids = spaceAppBidAndInstanceBidMap.get(apmFlowInstanceNode.getSpaceAppBid());
            if(instanceBids == null){
                instanceBids = new ArrayList<>();
            }
            instanceBids.add(apmFlowInstanceNode.getInstanceBid());
            spaceAppBidAndInstanceBidMap.put(apmFlowInstanceNode.getSpaceAppBid(),instanceBids);
        }
        List<Future<List<MObject>>> objects = new ArrayList<>();
        IUserContext<IUser> user = UserContextHolder.getUser();

        for (Map.Entry<String, List<String>> map : spaceAppBidAndInstanceBidMap.entrySet()) {
            if(spaceNameMap.get(map.getKey()) == null){
                continue;
            }
            String modelCode = spaceNameMap.get(map.getKey()).getModelCode();
            Future<List<MObject>> submit = SimpleThreadPool.getInstance().submit(() -> {
                //设置子线程数据
                try {
                    UserContextHolder.setUser(user);
                    TranscendUserContextHolder.setUser(user);
                    return objectModelCrudI.listByBids( map.getValue(),modelCode);
                } finally {
                    UserContextHolder.removeUser();
                    TranscendUserContextHolder.removeUser();
                }
            });
            objects.add(submit);
        }
        List<MObject> all = new ArrayList<>();
        objects.forEach( t ->{
            try {
                List<MObject> a = t.get(30, TimeUnit.SECONDS);
                all.addAll(a);
            } catch (Exception e) {
                log.error(String.valueOf(e));
            }
        });
        //Map<String, List<MObject>> instanceDataMap = all.stream().collect(Collectors.groupingBy(MObject::get(CommonConst.SPACE_APP_BID)));
        //Map<String, List<MSpaceAppData>> instanceDataMap = apmSpaceAppDataDrivenService.batchSpaceAppMapGet(spaceAppBidAndInstanceBidMap);
        if(CollectionUtils.isNotEmpty(all)){
            for(MObject mObject:all){
                MSpaceAppData mSpaceAppData = new MSpaceAppData();
                mSpaceAppData.putAll(mObject);
                mSpaceAppData.setSpaceAppBid(mObject.get(CommonConst.SPACE_APP_BID)+"");
                instMap.put(mObject.getBid(),mSpaceAppData);
            }
        }
        return instMap;
    }

    private Map<String,List<ApmTask>> getTaskTypeMap(List<ApmTask> apmTasks,String taskType){
        Map<String,List<ApmTask>> flowTaskMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        for(ApmTask apmTask:apmTasks){
            if(taskType.equals(apmTask.getTaskType())){
                List<ApmTask> apmTaskList = flowTaskMap.get(apmTask.getBizBid());
                if(apmTaskList == null){
                    apmTaskList = new ArrayList<>();
                }
                apmTaskList.add(apmTask);
                flowTaskMap.put(apmTask.getBizBid(),apmTaskList);
            }
        }
        return flowTaskMap;
    }

    @Override
    public boolean deleteByApmTaskDeleteAO(ApmTaskDeleteAO apmTaskDeleteAO){
        return apmTaskService.deleteByApmTaskDeleteAO(apmTaskDeleteAO) == 0 ? false : true;
    }

    @Override
    public long checkNeedDealTask(String bizBid, String taskHandler) {
        long count = apmTaskService.countNeedDealTask(bizBid,taskHandler);
        log.info("*************checkNeedDealTask:"+count);
        return count;
    }

    @Override
    public boolean checkCompleteTask(String bizBid, String taskHandler) {
        return apmTaskService.countCompleteTask(bizBid,taskHandler,null) == 0 ? true : false;
    }

    @Override
    public List<ApmTask> checkCompleteRoleTask(String bizBid, List<String> taskHandler) {
        return apmTaskService.countCheckCompleteRoleTask(bizBid, taskHandler);
    }

    @Override
    public boolean completeTask(String bizBid, String taskHandler) {
        return apmTaskService.completeTask(bizBid,taskHandler,null);
    }
    @Override
    public void overAllTask(String nodeBid) {
        apmTaskService.overAllTask(nodeBid);
    }

    @Override
    public boolean updateStateTask(String instanceBid, String instanceName, String currentLifeCycleCode, Set<String> taskHandlers, Boolean isLastState) {
        //查询实例流程节点
        List<ApmFlowInstanceNode> apmFlowInstanceNodes = apmFlowInstanceNodeService.listByInstanceBid(instanceBid);
        if (CollectionUtils.isEmpty(apmFlowInstanceNodes)){
            return true;
        }
        //查询要完成的节点
        Set<String> existTaskHandlers = new HashSet<>();
        ApmFlowInstanceNode ongoingInstanceNode = apmFlowInstanceNodes.stream().filter(apmFlowInstanceNode -> apmFlowInstanceNode.getNodeState().equals(FlowNodeStateConstant.ACTIVE)).findFirst().orElse(null);
        if (ongoingInstanceNode != null) {
            //查询当前待完成的待办
            List<ApmTask> apmTaskList = apmTaskService.listApmTasks(TaskConstant.FLOW_TYPE_STATE,ongoingInstanceNode.getBid(), TaskConstant.ACTIVE);
            if (CollectionUtils.isNotEmpty(apmTaskList)){
                for (ApmTask apmTask : apmTaskList) {
                    existTaskHandlers.add(apmTask.getTaskHandler());
                }
            }
        }
        //找到到达的流程节点
        List<ApmFlowTemplateNode> apmFlowTemplateNodes = apmFlowTemplateNodeService.listByTemplateBidAndVersion(apmFlowInstanceNodes.get(0).getFlowTemplateBid(), apmFlowInstanceNodes.get(0).getVersion());
        if (CollectionUtils.isNotEmpty(apmFlowTemplateNodes)){
            ApmFlowTemplateNode apmFlowTemplateNode = apmFlowTemplateNodes.stream().filter(v -> v.getLifeCycleCode().equals(currentLifeCycleCode)).findFirst().orElse(null);
            if (apmFlowTemplateNode != null){
                if (ongoingInstanceNode != null && !ongoingInstanceNode.getTemplateNodeBid().equals(apmFlowTemplateNode.getBid())){
                    //如果节点不一样，需要完成自己的待办
                    apmTaskService.completeTaskBatch(ongoingInstanceNode.getBid(), SsoHelper.getJobNumber(), TaskConstant.FLOW_TYPE_STATE);
                }
                if (ongoingInstanceNode != null) {
                    //删除其他人的待办
                    apmTaskService.deleteByIds(TaskConstant.FLOW_TYPE_STATE, Collections.singletonList(ongoingInstanceNode.getBid()), FlowNodeStateConstant.NOT_START);
                }
                ApmFlowInstanceNode apmFlowInstanceNode = apmFlowInstanceNodes.stream().filter(v -> v.getTemplateNodeBid().equals(apmFlowTemplateNode.getBid())).findFirst().orElse(null);
                if (apmFlowInstanceNode == null){
                    return true;
                }
                if (ongoingInstanceNode == null || !apmFlowInstanceNode.getBid().equals(ongoingInstanceNode.getBid())){
                    //修改流程节点状态
                    apmFlowInstanceNode.setNodeState(Boolean.TRUE.equals(isLastState) ? FlowNodeStateConstant.COMPLETED : FlowNodeStateConstant.ACTIVE);
                    List<ApmFlowInstanceNode> list = Lists.newArrayList(apmFlowInstanceNode);
                    if (ongoingInstanceNode != null){
                        ongoingInstanceNode.setNodeState(FlowNodeStateConstant.COMPLETED);
                        list.add(ongoingInstanceNode);
                    }
                    apmFlowInstanceNodeService.updateBatchById(list);
                }
                //生成新的待办
                if(Boolean.FALSE.equals(isLastState)){
                    List<ApmTask> apmTasks = new ArrayList<>(CommonConstant.START_MAP_SIZE);
                    //新增待办
                    for (String taskHandler : taskHandlers) {
                        ApmTask apmTask = new ApmTask();
                        apmTask.setBid(SnowflakeIdWorker.nextIdStr());
                        apmTask.setBizBid(apmFlowInstanceNode.getBid());
                        apmTask.setTaskType(TaskConstant.FLOW_TYPE_STATE);
                        apmTask.setTaskHandler(taskHandler);
                        apmTask.setTaskState(TaskConstant.NOT_START);
                        apmTasks.add(apmTask);
                    }
                    apmTaskService.saveBatch(apmTasks);
                    //发送飞书通知
                    taskHandlers.removeIf(existTaskHandlers::contains);
                    if (CollectionUtils.isNotEmpty(taskHandlers)){
                        if (StringUtil.isNotBlank(instanceName)){
                            runtimeService.sendFeishu(apmFlowInstanceNode, new ArrayList<>(taskHandlers), instanceName);
                        } else {
                            runtimeService.sendFeishu(apmFlowInstanceNode, new ArrayList<>(taskHandlers));
                        }
                    }
                }
                return true;
            }
        }
        //删除待办
        if (ongoingInstanceNode != null) {
            apmTaskService.deleteByIds(TaskConstant.FLOW_TYPE_STATE, Collections.singletonList(ongoingInstanceNode.getBid()), FlowNodeStateConstant.NOT_START);
        }
        return true;
    }

    @Override
    public boolean deleteNotComplete(String bizBid, String handler, String taskType) {
        return apmTaskService.deleteNotComplete(bizBid,handler,taskType) == 0 ? false : true;
    }

    @Override
    public boolean checkCurrentUserComplete(String bizBid, String handler, String taskType) {
        return apmTaskService.countCurrentUserComplete(bizBid,handler,taskType) == 0 ? false : true;
    }
    @Override
    public void updateFlowNodeStatus(String spaceAppBid, String bid, String nodeBid, MSpaceAppData mSpaceAppData) {
        ApmFlowInstanceNode currentNode = apmFlowInstanceNodeService.getByBid(nodeBid);
        List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = apmFlowInstanceRoleUserService.listByInstanceBid(currentNode.getInstanceBid());
        //特殊逻辑，如果是重复需求，不走此逻辑
        ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(spaceAppBid);
        MObject mObject = objectModelCrudI.getByBid(apmSpaceApp.getModelCode(), bid);
        if ("Y".equals(mObject.get(CommonEnum.REPETITIVE_DEMAND.getCode()))){
            return;
        }
        //节点角色，和 审核节点角色如果有一个为空 按照原逻辑走
        List<String> userIds =null;
        if(CollectionUtils.isNotEmpty(currentNode.getNodeRoleBids()) && CollectionUtils.isEmpty(currentNode.getComplateRoleBids())){
            userIds = apmFlowInstanceRoleUsers.stream()
                    .filter(user -> currentNode.getNodeRoleBids().contains(user.getRoleBid()))
                    .map(ApmFlowInstanceRoleUser::getUserNo)
                    .collect(Collectors.toList());
        }
        if(CollectionUtils.isEmpty(currentNode.getNodeRoleBids()) && CollectionUtils.isNotEmpty(currentNode.getComplateRoleBids())){
            userIds = apmFlowInstanceRoleUsers.stream()
                    .filter(user -> currentNode.getComplateRoleBids().contains(user.getRoleBid()))
                    .map(ApmFlowInstanceRoleUser::getUserNo)
                    .collect(Collectors.toList());
        }
        if((CollectionUtils.isNotEmpty(currentNode.getNodeRoleBids()) && CollectionUtils.isEmpty(currentNode.getComplateRoleBids())) || (CollectionUtils.isEmpty(currentNode.getNodeRoleBids()) && CollectionUtils.isNotEmpty(currentNode.getComplateRoleBids()))){
            if(FlowNodeCompleteType.MULTI_CONFIRM_COMPLETE.equals(currentNode.getComplateType())){
                //多人完成
                if(CollectionUtils.isEmpty(userIds)){
                    apmFlowInstanceNodeMapper.completeNodeByBid(nodeBid);
                    return;
                }
                userIds.remove(SsoHelper.getJobNumber());
                if(CollectionUtils.isEmpty(userIds)){
                    apmFlowInstanceNodeMapper.completeNodeByBid(nodeBid);
                    return;
                }
                if (CollectionUtils.isNotEmpty(apmTaskApplicationService.checkCompleteRoleTask(nodeBid,userIds))){
                    currentNode.setNodeState(FlowNodeStateConstant.ACTIVE);
                    apmFlowInstanceNodeService.updateById(currentNode);
                    log.info("当前节点授权角色未完成，修改状态为进行中");
                }else{
                    apmFlowInstanceNodeMapper.completeNodeByBid(nodeBid);
                    return;
                }
            }else{
                //单人完成 强制更新节点状态
                apmFlowInstanceNodeMapper.completeNodeByBid(nodeBid);
            }
            return;
        }
        //审核节点角色如果没有一个人 也需要拦截
        if(CollectionUtils.isNotEmpty(currentNode.getNodeRoleBids()) && CollectionUtils.isNotEmpty(currentNode.getComplateRoleBids())){
            Map<String,String> completeRoleMap = currentNode.getComplateRoleBids().stream().collect(Collectors.toMap(Function.identity(),Function.identity()));
            int completeRoleUesrNum = 0;
            for(ApmFlowInstanceRoleUser apmFlowInstanceRoleUser:apmFlowInstanceRoleUsers){
                if(completeRoleMap.containsKey(apmFlowInstanceRoleUser.getRoleBid())){
                    completeRoleUesrNum ++;
                }
            }
            if(completeRoleUesrNum == 0){
                currentNode.setNodeState(FlowNodeStateConstant.ACTIVE);
                apmFlowInstanceNodeService.updateById(currentNode);
                log.info("当前授权角色审核无人员，需要卡住");
                return;
            }
        }
        //强制更新节点状态
        apmFlowInstanceNodeMapper.completeNodeByBid(nodeBid);
        assert currentNode != null;
        /**特殊逻辑，如果别的地方用到请自行修改*/
        if (CollectionUtils.isNotEmpty(apmFlowInstanceRoleUsers) ){
            List<String> matchingUserIds =null;
            if (CollectionUtils.isNotEmpty(currentNode.getComplateRoleBids())){
                matchingUserIds = apmFlowInstanceRoleUsers.stream()
                        .filter(user -> currentNode.getComplateRoleBids().contains(user.getRoleBid()))
                        .map(ApmFlowInstanceRoleUser::getUserNo)
                        .collect(Collectors.toList());
            }
            if (matchingUserIds == null || matchingUserIds.isEmpty()) {
                matchingUserIds = apmFlowInstanceRoleUsers.stream().filter(user -> currentNode.getNodeRoleBids().contains(user.getRoleBid())).map(ApmFlowInstanceRoleUser::getUserNo).distinct().collect(Collectors.toList());
            }
            //单人完成
            if (FlowNodeCompleteType.SINGLE_CONFIRM_COMPLETE.equals(currentNode.getComplateType())) {
                AtomicReference<Boolean> isFinished = new AtomicReference<>(false);
                matchingUserIds.forEach(userNo -> {
                    if (apmTaskApplicationService.countCompleteApmTask(nodeBid, userNo) > 0){
                        isFinished.set(Boolean.TRUE);
                    }
                });
                if (!isFinished.get()){
                    currentNode.setNodeState(FlowNodeStateConstant.ACTIVE);
                    apmFlowInstanceNodeService.updateById(currentNode);
                    log.info("当前无人授权角色审核，无需流转下个角色，修改状态为进行中");
                }
                //多人完成
            }else if (FlowNodeCompleteType.MULTI_CONFIRM_COMPLETE.equals(currentNode.getComplateType())){
                List<String> authUserNos = matchingUserIds.stream().filter(user -> !user.equals(SsoHelper.getJobNumber())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(authUserNos)){
                    if (CollectionUtils.isNotEmpty(apmTaskApplicationService.checkCompleteRoleTask(nodeBid,authUserNos))){
                        currentNode.setNodeState(FlowNodeStateConstant.ACTIVE);
                        apmFlowInstanceNodeService.updateById(currentNode);
                        log.info("当前节点授权角色未完成，修改状态为进行中");
                    }
                }
            }
        }
    }


}
