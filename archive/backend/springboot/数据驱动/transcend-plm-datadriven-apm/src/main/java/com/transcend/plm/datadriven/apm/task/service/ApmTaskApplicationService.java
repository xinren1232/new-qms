package com.transcend.plm.datadriven.apm.task.service;

import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.task.ao.ApmTaskAO;
import com.transcend.plm.datadriven.apm.task.ao.ApmTaskDeleteAO;
import com.transcend.plm.datadriven.apm.task.domain.ApmTask;
import com.transcend.plm.datadriven.apm.task.vo.ApmTaskNumVO;
import com.transcend.plm.datadriven.apm.task.vo.ApmTaskVO;

import java.util.List;
import java.util.Set;

/**
 * @author unknown
 */
public interface ApmTaskApplicationService {
    /**
     * saveOrUpdateApmTask
     *
     * @param apmTaskAO apmTaskAO
     * @return List<String>
     */
    List<String> saveOrUpdateApmTask(ApmTaskAO apmTaskAO);

    /**
     * 根据任务状态获取当前用户任务
     *
     * @param taskState
     * @return
     */
    List<ApmTaskVO> listUserApmTasks(int taskState);

    /**
     * deleteByApmTaskDeleteAO
     *
     * @param apmTaskDeleteAO apmTaskDeleteAO
     * @return {@link boolean}
     */
    boolean deleteByApmTaskDeleteAO(ApmTaskDeleteAO apmTaskDeleteAO);

    /**
     * 校验是否存在待办任务
     *
     * @param bizBid
     * @param taskHandler
     * @return
     */
    long checkNeedDealTask(String bizBid, String taskHandler);

    /**
     * 校验除自己外是否完成了所以任务
     *
     * @param bizBid
     * @param taskHandler
     * @return
     */
    boolean checkCompleteTask(String bizBid, String taskHandler);
    /**
     * 校验除自己外是授权角色否完成了所以任务
     *
     * @param bizBid
     * @param taskHandler
     * @return
     */
    List<ApmTask> checkCompleteRoleTask(String bizBid, List<String> taskHandler);

    /**
     * 完成工号对应的任务
     *
     * @param bizBid
     * @param taskHandler
     * @return
     */
    boolean completeTask(String bizBid, String taskHandler);

    /**
     * deleteNotComplete
     *
     * @param bizBid   bizBid
     * @param handler  handler
     * @param taskType taskType
     * @return {@link boolean}
     */
    boolean deleteNotComplete(String bizBid, String handler, String taskType);

    /**
     * 查询工号对应的任务是否完成
     *
     * @param bizBid
     * @param handler
     * @param taskType
     * @return
     */
    boolean checkCurrentUserComplete(String bizBid, String handler, String taskType);

    /**
     * 获取任务数量
     *
     * @return {@link List<ApmTaskNumVO>}
     */
    List<ApmTaskNumVO> getTaskNum();

    /**
     * 根据任务类型和业务id删除
     *
     * @param taskType taskType
     * @param bizBids  bizBids
     * @return {@link boolean}
     */
    boolean deleteByBizBids(String taskType, List<String> bizBids);

    /**
     * 根据bid获取当前用户任务信息
     * @param bizBid
     * @param userNo
     * @return
     */
    ApmTask getApmTask(String bizBid,String userNo);

    long countCompleteApmTask(String bizBid,String userNo);

    long countNotCompleteApmTask(String bizBid,String userNo);

    /**
     * 统计未完成任务数量
     * @param bizBid
     * @param userNo
     * @return
     */
    long getNotCompleteTaskCount(String bizBid,String userNo);
    /**
     * 根据bid获取用户节点状态
     * @param bizBid
     * @param userNo
     * @return
     */
    List<ApmTask> getUserNodeTaskState(String bizBid,String userNo);

    /**
     * 更新流程节点状态
     *
     * @param spaceAppBid
     * @param bid
     * @param nodeBid
     * @param mSpaceAppData
     */
    void updateFlowNodeStatus(String spaceAppBid, String bid, String nodeBid, MSpaceAppData mSpaceAppData);

    /**
     * 完成当前所有待办
     * @param nodeBid
     */
    void overAllTask(String nodeBid);


    /**
     * 更新状态流程任务
     *
     * @param instanceBid instanceBid
     * @param taskHandlers taskHandlers
     * @return boolean
     */
    boolean updateStateTask(String instanceBid, String instanceName, String currentLifeCycleCode, Set<String> taskHandlers, Boolean isLastState);
}
