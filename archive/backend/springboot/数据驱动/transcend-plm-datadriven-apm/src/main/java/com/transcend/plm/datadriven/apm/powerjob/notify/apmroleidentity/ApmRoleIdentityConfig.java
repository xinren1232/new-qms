package com.transcend.plm.datadriven.apm.powerjob.notify.apmroleidentity;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmRoleIdentity;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleIdentityService;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.TaskResult;
import tech.powerjob.worker.core.processor.sdk.MapReduceProcessor;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 更新角色排序
 * @author quan.cheng
 * @title ApmRoleIdentityRecordConfig
 * @date 2024/1/31 16:18
 * @description TODO
 */
@Slf4j
@Component
public class ApmRoleIdentityConfig  implements MapReduceProcessor {

    @Value("#{${apm.role.identity.record.roleBids}}")
    private List<String> roleBids;

    @Resource
    private ApmRoleIdentityService apmRoleIdentityService;


    @Override
    public ProcessResult process(TaskContext context) throws Exception {
        log.info("并行处理器====process=====NotifyExecuteRecordConfig process");
        // 判断是否为根任务
        if (isRootTask()) {
            //根据roleBids查询出所有的角色
            List<ApmRoleIdentity> apmRoleIdentityList = apmRoleIdentityService
                    .list(
                            new QueryWrapper<>(new ApmRoleIdentity())
                                    .in("role_bid", roleBids)
                    );

            if (CollectionUtils.isEmpty(apmRoleIdentityList)) {
                return new ProcessResult(true, "没有需要处理的数据");
            }
            //根据角色分组
            Map<String, List<ApmRoleIdentity>> apmRoleIdentityMap = apmRoleIdentityList.stream().collect(Collectors.groupingBy(ApmRoleIdentity::getRoleBid));
            List<SubTask> subTaskList = new ArrayList<>();
            for (Map.Entry<String, List<ApmRoleIdentity>> entry : apmRoleIdentityMap.entrySet()) {
                List<ApmRoleIdentity> objects = new ArrayList<>(entry.getValue());

                SubTask subTask = new SubTask();
                subTask.setRoleBid(entry.getKey());
                subTask.setApmRoleIdentities(objects);
                subTaskList.add(subTask);
            }
            map(subTaskList, "DATA_PROCESS_TASK");
            return new ProcessResult(true, "result is xxx");
        }

        // 非子任务，可根据 subTask 的类型 或 TaskName 来判断分支
        if (context.getSubTask() instanceof SubTask) {
            log.info("并行处理器====process=====NotifyExecuteRecordConfig SubTask");
            SubTask subTask = (SubTask) context.getSubTask();
            //根据角色bid查询出所有的角色
            List<ApmRoleIdentity> apmRoleIdentities = subTask.getApmRoleIdentities();
            //获取ApmRoleIdentity中sort最大的值
            Integer maxSort = apmRoleIdentities.stream().map(ApmRoleIdentity::getSort).max(Integer::compareTo).orElse(0);
            //apmRoleIdentities按sort从大到小
            List<ApmRoleIdentity> apmRoleIdentityList = apmRoleIdentities.stream()
                    .sorted(Comparator.comparing(ApmRoleIdentity::getSort).reversed()).collect(Collectors.toList());
            //获取最小的sort
            ApmRoleIdentity apmRoleIdentity = apmRoleIdentityList.get(apmRoleIdentityList.size() - 1);
            apmRoleIdentity.setSort(maxSort + 1);
            apmRoleIdentityService.updateById(apmRoleIdentity);

        }

        return new ProcessResult(true, "result is xxx");
    }

    @Override
    public ProcessResult reduce(TaskContext context, List<TaskResult> taskResults) {
        log.info("并行处理器====reduce=====NotifyExecuteRecordConfig reduce");

        // 所有 Task 执行结束后，reduce 将会被执行
        // taskResults 保存了所有子任务的执行结果
        // 用法举例，统计执行结果
        AtomicLong successCnt = new AtomicLong(0);
        taskResults.forEach(tr -> {
            if (tr.isSuccess()) {
                successCnt.incrementAndGet();
            }else {
                //如果有一个失败
            }
        });
        // 该结果将作为任务最终的执行结果
        return new ProcessResult(true, "success task num:" + successCnt.get());
    }


    /**
     *自定义任务实体
     */
    @Getter
    private static class SubTask {
        private String roleBid;
        //根据空间ID 进行分片的子任务数据
        private List<ApmRoleIdentity> apmRoleIdentities;

        public void setRoleBid(String roleBid) {
            this.roleBid = roleBid;
        }

        public void setApmRoleIdentities(List<ApmRoleIdentity> apmRoleIdentities) {
            this.apmRoleIdentities = apmRoleIdentities;
        }


    }

}
