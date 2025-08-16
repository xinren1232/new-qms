///*
//package com.transcend.plm.alm.sync;
//
//import com.alibaba.fastjson.JSONObject;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import tech.powerjob.worker.core.processor.ProcessResult;
//import tech.powerjob.worker.core.processor.TaskContext;
//import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
//
//import java.util.Optional;
//
//*/
///*
//
//@Component
//@Slf4j
//public class SyncRRTask implements BasicProcessor {
//    @Autowired
//    private SyncRRTaskService syncRRTaskService;
//
//
//    @Override
//    public ProcessResult process(TaskContext taskContext) throws Exception {
//        log.info("单机处理器====process=====SyncRRTask process");
//        JSONObject params = Optional.ofNullable(taskContext.getJobParams()).map(JSONObject::parseObject).orElse(new JSONObject());
//        Boolean result = syncRRTaskService.syncData(params);
//        log.info("单机处理器====process=====SyncRRTask process result:{}", result);
//        return new ProcessResult(true, "ExecuteTriggerNotify process success");
//    }
//}
//*/
