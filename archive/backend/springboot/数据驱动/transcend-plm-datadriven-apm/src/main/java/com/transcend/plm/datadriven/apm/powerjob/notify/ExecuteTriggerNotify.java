package com.transcend.plm.datadriven.apm.powerjob.notify;

import com.transcend.plm.datadriven.apm.aspect.notify.OperateBusiService;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.notify.vo.NotifyConfigVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.TaskResult;
import tech.powerjob.worker.core.processor.sdk.MapReduceProcessor;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author unknown
 */
@Component
@Slf4j
public class ExecuteTriggerNotify implements MapReduceProcessor {

    @Resource
    private OperateBusiService operateBusiService;

    @Override
    public ProcessResult reduce(TaskContext context, List<TaskResult> taskResults) {
        log.info("并行处理器====reduce=====ExecuteTriggerNotify reduce");

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
        return new ProcessResult(true, "ExecuteTriggerNotify process reduce");
    }

    @Override
    public ProcessResult process(TaskContext taskContext) throws Exception {
        log.info("并行处理器====process=====ExecuteTriggerNotify process");
        List<NotifyConfigVo> notifyConfigVos = operateBusiService.getTriggerConfigs();
        log.info("获取到的触发器配置信息：{}", notifyConfigVos);
        if (CollectionUtils.isNotEmpty(notifyConfigVos)) {
            Map<String,String> conditionMap = operateBusiService.getConditionMap();
            for (NotifyConfigVo notifyConfigVo : notifyConfigVos) {
                operateBusiService.executetTriggerConfig(notifyConfigVo,conditionMap);
            }
        }
        return new ProcessResult(true, "ExecuteTriggerNotify process success");
    }
}
