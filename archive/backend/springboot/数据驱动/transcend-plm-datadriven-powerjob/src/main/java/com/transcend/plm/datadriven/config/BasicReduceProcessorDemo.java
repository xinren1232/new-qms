package com.transcend.plm.datadriven.config;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.TaskResult;
import tech.powerjob.worker.core.processor.WorkflowContext;
import tech.powerjob.worker.core.processor.sdk.MapReduceProcessor;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/*
  powerjob 任务处理器示例
  MapReduce执行
  MapReduce 处理器（MapReduceProcessor）对应了 MapReduce 任务，在 Map 任务的基础上，增加了所有任务结束后的汇总统计。

  @author quan.cheng
 * @title BasicProcessorDemo
 * @date 2024/1/9 11:03
 * @description powerjob 任务处理器示例 MapReduce执行
 */

/**
 * 支持 SpringBean 的形式
 *
 * @author guixu.shi
 * @date 2024/07/24
 */
@Component
@Slf4j
public class BasicReduceProcessorDemo implements MapReduceProcessor {


    /**
     * @param context
     * @return {@link ProcessResult }
     * @throws Exception
     */
    @Override
    public ProcessResult process(TaskContext context) throws Exception {
        log.debug("并行处理器====process=====BasicReduceProcessorDemo process");
        // 判断是否为根任务
        if (isRootTask()) {
            log.debug("并行处理器====根任务=====BasicReduceProcessorDemo process");

            // 构造子任务
            List<SubTask> subTaskList = Lists.newLinkedList();
            SubTask subTask1 = new SubTask();
            subTask1.siteId = 1L;
            subTask1.idList = Lists.newArrayList(1L, 2L, 3L, 4L, 5L);
            subTaskList.add(subTask1);

            SubTask subTask2 = new SubTask();
            subTask2.siteId = 2L;
            subTask2.idList = Lists.newArrayList(1L, 2L, 3L, 4L, 5L);
            subTaskList.add(subTask2);

            SubTask subTask3 = new SubTask();

            subTask3.siteId = 3L;
            subTask3.idList = Lists.newArrayList(1L, 2L, 3L, 4L, 5L);
            subTaskList.add(subTask3);

            /*
             * 子任务的构造由开发者自己定义
             * eg. 现在需要从文件中读取100W个ID，并处理数据库中这些ID对应的数据，那么步骤如下：
             * 1. 根任务（RootTask）读取文件，流式拉取100W个ID，并按1000个一批的大小组装成子任务进行派发
             * 2. 非根任务获取子任务，完成业务逻辑的处理
             */

            // 调用 map 方法，派发子任务（map 可能会失败并抛出异常，做好业务操作）
            map(subTaskList, "DATA_PROCESS_TASK");
            return  new ProcessResult(true, "ProcessResult xxx");
        }

        // 非子任务，可根据 subTask 的类型 或 TaskName 来判断分支
        if (context.getSubTask() instanceof SubTask) {
            log.debug("并行处理器====子任务=====BasicReduceProcessorDemo process");
            Object subTask = context.getSubTask();
            SubTask subTask1 = (SubTask) subTask;
            WorkflowContext workflowContext = context.getWorkflowContext();
            WorkflowContext workflowContext1 = new WorkflowContext(1L, "test");
            workflowContext1.appendData2WfContext("test", "test");
            workflowContext.appendData2WfContext(subTask1.toString(), subTask1.toString());
            context.setWorkflowContext(workflowContext1);
            // 执行子任务，注：子任务人可以 map 产生新的子任务，可以构建任意级的 MapReduce 处理器
            return new ProcessResult(true, "result is xxx");
        }

        return new ProcessResult(true, "result is xxx");
    }

    /**
     * @param taskContext
     * @param taskResults
     * @return {@link ProcessResult }
     */
    @Override
    public ProcessResult reduce(TaskContext taskContext, List<TaskResult> taskResults) {
        log.debug("并行处理器====reduce=====BasicReduceProcessorDemo reduce");

        // 所有 Task 执行结束后，reduce 将会被执行
        // taskResults 保存了所有子任务的执行结果

        // 用法举例，统计执行结果
        AtomicLong successCnt = new AtomicLong(0);
        taskResults.forEach(tr -> {
            if (tr.isSuccess()) {
                successCnt.incrementAndGet();
            }
        });
        // 该结果将作为任务最终的执行结果
        return new ProcessResult(true, "success task num:" + successCnt.get());
    }

    /**
     * 自定义的子任务
      */
    private static class SubTask {
        private Long siteId;
        private List<Long> idList;
    }
}
