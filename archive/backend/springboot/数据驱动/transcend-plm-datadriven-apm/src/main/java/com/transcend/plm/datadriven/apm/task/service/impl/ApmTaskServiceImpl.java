package com.transcend.plm.datadriven.apm.task.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.apm.constants.TaskConstant;
import com.transcend.plm.datadriven.apm.task.ao.ApmTaskDeleteAO;
import com.transcend.plm.datadriven.apm.task.domain.ApmTask;
import com.transcend.plm.datadriven.apm.task.mapper.ApmTaskMapper;
import com.transcend.plm.datadriven.apm.task.service.ApmTaskService;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author unknown
 */
@Service
@Slf4j
public class ApmTaskServiceImpl extends ServiceImpl<ApmTaskMapper, ApmTask>
        implements ApmTaskService {

    @Resource
    private ApmTaskMapper apmTaskMapper;

    @Override
    public List<ApmTask> listApmTasks(String taskType, String bizBid, Integer taskState) {
        List<ApmTask> list = list(Wrappers.<ApmTask>lambdaQuery().eq(ApmTask::getTaskType, taskType).eq(ApmTask::getBizBid, bizBid).ne(ApmTask::getTaskState, taskState));
        return list;
    }

    @Override
    public int deleteByTaskIds(List<Integer> ids) {
        return apmTaskMapper.deleteByTaskIds(ids);
    }

    @Override
    public int deleteByBizBids(String taskType, List<String> bizBids) {
        return apmTaskMapper.deleteByBizBids(taskType, bizBids);
    }

    @Override
    public int deleteByIds(String taskType, List<String> bizBids, int taskState) {
        List<ApmTask> list = list(Wrappers.<ApmTask>lambdaQuery().eq(ApmTask::getTaskType, taskType).eq(ApmTask::getTaskState, taskState).in(ApmTask::getBizBid, bizBids));
        if (CollectionUtils.isEmpty(list)) {
            return 0;
        }
        List<Integer> ids = list.stream().map(e -> e.getId()).collect(Collectors.toList());
        return apmTaskMapper.deleteByTaskIds(ids);
    }

    @Override
    public List<ApmTask> listUserApmTask(String jobNumber, int taskState) {
        List<ApmTask> list = list(Wrappers.<ApmTask>lambdaQuery().eq(ApmTask::getTaskHandler, jobNumber).eq(ApmTask::getTaskState, taskState).orderByDesc(ApmTask::getCreatedTime));
        return list;
    }

    @Override
    public long countUserApmTask(String jobNumber, int taskState) {
        long count = count(Wrappers.<ApmTask>lambdaQuery().eq(ApmTask::getTaskHandler, jobNumber).eq(ApmTask::getTaskState, taskState));
        return count;
    }

    @Override
    public int deleteByApmTaskDeleteAO(ApmTaskDeleteAO apmTaskDeleteAO) {
        return apmTaskMapper.deleteByApmTaskDeleteAO(apmTaskDeleteAO);
    }

    @Override
    public long countNeedDealTask(String bizBid, String taskHandler) {
        long count = apmTaskMapper.countNeedTask(bizBid, taskHandler);
        log.info("**********countNeedDealTask:" + count + ",bizBid:" + bizBid + ",taskHandler:" + taskHandler);
        return count;
    }

    @Override
    public long countCompleteTask(String bizBid, String taskHandler, String taskType) {
        if (StringUtils.isEmpty(taskType)) {
            taskType = TaskConstant.FLOW_TYPE;
        }
        return count(Wrappers.<ApmTask>lambdaQuery().ne(StringUtils.isNotEmpty(taskHandler), ApmTask::getTaskHandler, taskHandler).eq(ApmTask::getBizBid, bizBid).ne(ApmTask::getTaskState, TaskConstant.COMPLETED).eq(ApmTask::getTaskType, taskType));
    }

    public Boolean logicDeleteComplete(String bizBid, String taskHandler, String taskType) {
        LambdaUpdateWrapper<ApmTask> wrapper = new LambdaUpdateWrapper<ApmTask>();
        List<ApmTask> apmTasks = list(Wrappers.<ApmTask>lambdaQuery().eq(ApmTask::getTaskState, TaskConstant.COMPLETED).eq(ApmTask::getBizBid, bizBid).eq(ApmTask::getTaskHandler, taskHandler).eq(ApmTask::getTaskType, taskType));
        if (CollectionUtils.isNotEmpty(apmTasks)) {
            List<Integer> ids = apmTasks.stream().map(ApmTask::getId).collect(Collectors.toList());
            wrapper.set(ApmTask::getDeleteFlag, 1).in(ApmTask::getId, ids);
            return update(wrapper);
        }
        return false;
    }

    @Override
    public boolean completeTask(String bizBid, String taskHandler, String taskType) {
        LambdaUpdateWrapper<ApmTask> wrapper = new LambdaUpdateWrapper<ApmTask>();
        if (StringUtils.isEmpty(taskType)) {
            taskType = TaskConstant.FLOW_TYPE;
        }
        List<ApmTask> apmTasks = list(Wrappers.<ApmTask>lambdaQuery().eq(ApmTask::getBizBid, bizBid).eq(ApmTask::getTaskHandler, taskHandler).eq(ApmTask::getTaskType, taskType));
        if (CollectionUtils.isNotEmpty(apmTasks)) {
            /*List<Integer> ids = apmTasks.stream().map(ApmTask::getId).collect(Collectors.toList());
            wrapper.set(ApmTask::getTaskState, TaskConstant.COMPLETED).in(ApmTask::getId, ids);*/
            wrapper.set(ApmTask::getTaskState, TaskConstant.COMPLETED).eq(ApmTask::getId, apmTasks.get(0).getId());
            return update(wrapper);
        }
        return false;
    }

    @Override
    public boolean completeTaskBatch(String bizBid, String taskHandler, String taskType) {
        LambdaUpdateWrapper<ApmTask> wrapper = new LambdaUpdateWrapper<ApmTask>();
        if (StringUtils.isEmpty(taskType)) {
            taskType = TaskConstant.FLOW_TYPE;
        }
        wrapper.eq(ApmTask::getBizBid, bizBid)
                .eq(StringUtils.isNotBlank(taskHandler),ApmTask::getTaskHandler, taskHandler)
                .eq(ApmTask::getTaskType, taskType)
                .set(ApmTask::getTaskState, TaskConstant.COMPLETED);
        return update(wrapper);
    }

    @Override
    public int deleteNotComplete(String bizBid, String handler, String taskType) {
        return apmTaskMapper.deleteNotComplete(bizBid, handler, taskType);
    }

    @Override
    public long countCurrentUserComplete(String bizBid, String taskHandler, String taskType) {
        if (StringUtils.isEmpty(taskType)) {
            taskType = TaskConstant.FLOW_TYPE;
        }
        if (StringUtils.isEmpty(taskHandler)) {
            taskHandler = SsoHelper.getJobNumber();
        }
        return count(Wrappers.<ApmTask>lambdaQuery().eq(ApmTask::getTaskHandler, taskHandler).eq(ApmTask::getBizBid, bizBid).eq(ApmTask::getTaskState, TaskConstant.COMPLETED).eq(ApmTask::getTaskType, taskType));
    }

    @Override
    public List<ApmTask> countCheckCompleteRoleTask(String bizBid, List<String> taskHandler) {
        return apmTaskMapper.selectList(
                new LambdaQueryWrapper<>(ApmTask.class)
                        .eq(ApmTask::getTaskState, TaskConstant.NOT_START)
                        .eq(ApmTask::getTaskType, TaskConstant.FLOW_TYPE)
                        .eq(ApmTask::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED)
                        .eq(ApmTask::getBizBid, bizBid)
                        .in(ApmTask::getTaskHandler, taskHandler));
    }

    @Override
    public void overAllTask(String nodeBid) {
        LambdaUpdateWrapper<ApmTask> wrapper = new LambdaUpdateWrapper<ApmTask>();
        List<ApmTask> apmTasks = list(Wrappers.<ApmTask>lambdaQuery().eq(ApmTask::getBizBid, nodeBid).eq(ApmTask::getTaskType, TaskConstant.FLOW_TYPE).ne(ApmTask::getTaskState, TaskConstant.COMPLETED));
        if (CollectionUtils.isNotEmpty(apmTasks)) {
            List<Integer> ids = apmTasks.stream().map(ApmTask::getId).collect(Collectors.toList());
            wrapper.set(ApmTask::getTaskState, TaskConstant.COMPLETED).in(ApmTask::getId, ids);
            update(wrapper);
        }
    }

    @Override
    public boolean updateCompleteRoleTaskSate(String bizBid, String taskHandler) {
        if (StringUtils.isEmpty(taskHandler)) {
            taskHandler = SsoHelper.getJobNumber();
        }
        return update(Wrappers.<ApmTask>lambdaUpdate()
                .eq(ApmTask::getBizBid, bizBid)
                .eq(ApmTask::getTaskHandler, taskHandler)
                .eq(ApmTask::getTaskType, TaskConstant.FLOW_TYPE)
                .set(ApmTask::getTaskState, TaskConstant.COMPLETED));
    }

}




