package com.transcend.plm.datadriven.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.TaskResult;
import tech.powerjob.worker.core.processor.sdk.BroadcastProcessor;
import tech.powerjob.worker.log.OmsLogger;

import java.util.List;

/**
 * powerjob 任务处理器示例
 * <p>
 * <p>
 * 广播模式执行
 * <p>
 * <p>
 * 广播处理器（BroadcastProcessor）对应了广播任务，即某个任务的某次运行会调动集群内所有机器参与运算。
 *
 * @author quan.cheng
 * @date 2024/1/9 11:03
 */
@Component
@Slf4j
public class BasicBroadCastProcessorDemo implements BroadcastProcessor {

    /**
     * @param taskContext
     * @return {@link ProcessResult }
     * @throws Exception
     */
    @Override
    public ProcessResult preProcess(TaskContext taskContext) throws Exception {
        log.debug("广播模式执行  预执行，会在所有 worker 执行 process 方法前调用");

        // 预执行，会在所有 worker 执行 process 方法前调用
        return new ProcessResult(true, "init success");
    }


    /**
     * @param taskContext
     * @param taskResults
     * @return {@link ProcessResult }
     * @throws Exception
     */
    @Override
    public ProcessResult postProcess(TaskContext taskContext, List<TaskResult> taskResults) throws Exception {

        // taskResults 存储了所有worker执行的结果（包括preProcess）
        log.debug("广播模式执行 收尾，会在所有 worker 执行完毕 process 方法后调用，该结果将作为最终的执行结果" );

        // 收尾，会在所有 worker 执行完毕 process 方法后调用，该结果将作为最终的执行结果
        return new ProcessResult(true, "process success");
    }

    /**
     * @param context
     * @return {@link ProcessResult }
     * @throws Exception
     */
    @Override
    public ProcessResult process(TaskContext context) throws Exception {
        // 整个work集群都会执行的逻辑
        // 在线日志功能，可以直接在控制台查看任务日志，非常便捷
        OmsLogger omsLogger = context.getOmsLogger();
        omsLogger.info("BasicProcessorDemo start to process, current JobParams is {}.", context.getJobParams());
        // TaskContext为任务的上下文信息，包含了在控制台录入的任务元数据，常用字段为
        // jobParams（任务参数，在控制台录入），instanceParams（任务实例参数，通过 OpenAPI 触发的任务实例才可能存在该参数）
        // 进行实际处理...
        log.debug("广播模式执行 整个work集群都会执行的逻辑" + context.getJobParams());
        // 返回结果，该结果会被持久化到数据库，在前端页面直接查看，极为方便
        return new ProcessResult(true, "result is xxx");
    }
}
