package com.transcend.plm.datadriven.apm.powerjob.notify;

import com.transcend.plm.datadriven.apm.powerjob.notify.mapper.JobCommonMapper;
import com.transcend.plm.datadriven.common.spring.PlmContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.TaskResult;
import tech.powerjob.worker.core.processor.sdk.MapReduceProcessor;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author yuanhu.huang
 * @version v1.0.0
 * @description PI 人员任务处理
 * @date 2024/10/10 11:03
 **/
@Component
@Slf4j
public class TrEmployeeHandle implements MapReduceProcessor {

    /*@Autowired
    private CommonMapper commonMapper;*/

    @Override
    public ProcessResult reduce(TaskContext taskContext, List<TaskResult> list) {
        log.info("并行处理器====reduce=====TrEmployeeHandle reduce");

        // 所有 Task 执行结束后，reduce 将会被执行
        // taskResults 保存了所有子任务的执行结果
        // 用法举例，统计执行结果
        AtomicLong successCnt = new AtomicLong(0);
        list.forEach(tr -> {
            if (tr.isSuccess()) {
                successCnt.incrementAndGet();
            }
        });
        // 该结果将作为任务最终的执行结果
        return new ProcessResult(true, "success task num:" + successCnt.get());
    }

    @Override
    public ProcessResult process(TaskContext taskContext) throws Exception {
        JobCommonMapper commonMapper = PlmContextHolder.getBean(JobCommonMapper.class);
        commonMapper.deletePersonmgrBakData();
        commonMapper.insertPersonmgrBakData();
        commonMapper.deletePersonmgrData();
        commonMapper.insertPersonmgrData();
        return new ProcessResult(true, "TrEmployeeHandle process success");
    }
}
