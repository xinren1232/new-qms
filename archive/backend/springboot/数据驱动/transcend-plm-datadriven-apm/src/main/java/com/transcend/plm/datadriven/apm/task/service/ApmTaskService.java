package com.transcend.plm.datadriven.apm.task.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.apm.task.ao.ApmTaskDeleteAO;
import com.transcend.plm.datadriven.apm.task.domain.ApmTask;

import java.util.List;

/**
 * @author unknown
 */
public interface ApmTaskService extends IService<ApmTask> {
    /**
     * listApmTasks
     *
     * @param taskType taskType
     * @param bizBid   bizBid
     * @return {@link List<ApmTask>}
     */
    List<ApmTask> listApmTasks(String taskType, String bizBid, Integer taskState);

    /**
     * deleteByTaskIds
     *
     * @param ids ids
     * @return {@link int}
     */
    int deleteByTaskIds(List<Integer> ids);

    /**
     * deleteByBizBids
     *
     * @param taskType taskType
     * @param bizBids  bizBids
     * @return {@link int}
     */
    int deleteByBizBids(String taskType, List<String> bizBids);

    /**
     * deleteByIds
     *
     * @param taskType  taskType
     * @param bizBids   bizBids
     * @param taskState taskState
     * @return {@link int}
     */
    int deleteByIds(String taskType, List<String> bizBids, int taskState);

    /**
     * listUserApmTask
     *
     * @param jobNumber jobNumber
     * @param taskState taskState
     * @return {@link List<ApmTask>}
     */
    List<ApmTask> listUserApmTask(String jobNumber, int taskState);

    /**
     * countUserApmTask
     *
     * @param jobNumber jobNumber
     * @param taskState taskState
     * @return {@link long}
     */
    long countUserApmTask(String jobNumber, int taskState);

    /**
     * deleteByApmTaskDeleteAO
     *
     * @param apmTaskDeleteAO apmTaskDeleteAO
     * @return {@link int}
     */
    int deleteByApmTaskDeleteAO(ApmTaskDeleteAO apmTaskDeleteAO);

    /**
     * countNeedDealTask
     *
     * @param bizBid      bizBid
     * @param taskHandler taskHandler
     * @return {@link long}
     */
    long countNeedDealTask(String bizBid, String taskHandler);

    /**
     * 检查除自己外任务完成的数量
     *
     * @param bizBid      bizBid
     * @param taskHandler taskHandler
     * @param taskType    taskType
     * @return
     */
    long countCompleteTask(String bizBid, String taskHandler, String taskType);

    /**
     * completeTask
     *
     * @param bizBid      bizBid
     * @param taskHandler taskHandler
     * @param taskType    taskType
     * @return {@link boolean}
     */
    boolean completeTask(String bizBid, String taskHandler, String taskType);

    boolean completeTaskBatch(String bizBid, String taskHandler, String taskType);

    Boolean logicDeleteComplete(String bizBid, String taskHandler, String taskType);

    /**
     * deleteNotComplete
     *
     * @param bizBid   bizBid
     * @param handler  handler
     * @param taskType taskType
     * @return {@link int}
     */
    int deleteNotComplete(String bizBid, String handler, String taskType);

    /**
     * countCurrentUserComplete
     *
     * @param bizBid      bizBid
     * @param taskHandler taskHandler
     * @param taskType    taskType
     * @return {@link long}
     */
    long countCurrentUserComplete(String bizBid, String taskHandler, String taskType);
    /**
     * 检查除自己外授权角色任务完成的数量
     *
     * @param bizBid      bizBid
     * @param taskHandler taskHandler
     * @return
     */
    List<ApmTask> countCheckCompleteRoleTask(String bizBid, List<String> taskHandler);

    /**
     * 完成当前节点所有待办
     * @param nodeBid
     */
    void overAllTask(String nodeBid);
    /**
     * 更新授权角色流程状态
     * @param bizBid
     * @param taskHandler
     * @return
     */
    boolean updateCompleteRoleTaskSate(String bizBid, String taskHandler);
}
