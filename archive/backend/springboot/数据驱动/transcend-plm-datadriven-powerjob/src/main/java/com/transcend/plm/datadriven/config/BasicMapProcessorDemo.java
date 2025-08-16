package com.transcend.plm.datadriven.config;

import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.MapProcessor;
import tech.powerjob.worker.log.OmsLogger;

/*
  powerjob 任务处理器示例
  map执行
    Map处理器（MapProcessor）对应了Map任务，即某个任务在运行过程中，允许产生子任务并分发到其他机器进行运算。
  @author quan.cheng
 * @title BasicProcessorDemo
 * @date 2024/1/9 11:03
 * @description powerjob 任务处理器示例 map执行
 */

/**
 * 支持 SpringBean 的形式
 *
 * @author guixu.shi
 * @date 2024/07/24
 */

@Component
public class BasicMapProcessorDemo implements MapProcessor {
    /**
     * @param context
     * @return {@link ProcessResult }
     * @throws Exception
     */
    @Override
    public ProcessResult process(TaskContext context) throws Exception {
        // 在线日志功能，可以直接在控制台查看任务日志，非常便捷
        OmsLogger omsLogger = context.getOmsLogger();
        omsLogger.info("BasicProcessorDemo start to process, current JobParams is {}.", context.getJobParams());
        // TaskContext为任务的上下文信息，包含了在控制台录入的任务元数据，常用字段为
        // jobParams（任务参数，在控制台录入），instanceParams（任务实例参数，通过 OpenAPI 触发的任务实例才可能存在该参数）
        // 进行实际处理...

        log.debug("map执行模式BasicProcessorDemo start to process, current JobParams is {}" , context.getJobParams());
        // 返回结果，该结果会被持久化到数据库，在前端页面直接查看，极为方便
        return new ProcessResult(true, "result is xxx");
    }
}
