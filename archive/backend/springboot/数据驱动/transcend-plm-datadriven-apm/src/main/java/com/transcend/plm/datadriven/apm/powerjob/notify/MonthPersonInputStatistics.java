package com.transcend.plm.datadriven.apm.powerjob.notify;

import com.transcend.plm.datadriven.apm.permission.service.IApmRoleIdentityDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;

import javax.annotation.Resource;

/**
 * 项目人员月投入占比统计
 * @author haijun.ren
 */
@Component
@Slf4j
public class MonthPersonInputStatistics implements BasicProcessor {


    @Resource
    private IApmRoleIdentityDomainService apmRoleIdentityDomainService;

    @Override
    public ProcessResult process(TaskContext taskContext) throws Exception {
        log.info("单机处理器====process=====MonthPersonInputStatistics process");
        Boolean result = apmRoleIdentityDomainService.personMonthInputStat();
        log.info("单机处理器====process=====MonthPersonInputStatistics process result:{}", result);
        return new ProcessResult(true, "ExecuteTriggerNotify process success");
    }
}
