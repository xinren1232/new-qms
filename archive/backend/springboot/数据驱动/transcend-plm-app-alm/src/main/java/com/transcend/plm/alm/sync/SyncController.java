package com.transcend.plm.alm.sync;

import com.alibaba.fastjson.JSONObject;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * @author jinpeng.bai
 * @version v1.0.0
 * @description
 * @date 2024/07/27 11:24
 **/
@RestController
public class SyncController {


    @Autowired
    private SyncIrTaskService irTaskService;

    /*@PostMapping("/sync/rr/file")
    public void initFile(@RequestBody JSONObject params) {
        syncRRTaskService.initFiles(params);
    }

    @GetMapping("/sync/rr/desc")
    public void initDesc() {
        syncRRTaskService.initDescFiles();
    }

    @PostMapping("/sync/rr/data")
    public ProcessResult syncData(@RequestBody JSONObject jsonObject) throws Exception {
        Boolean result = syncRRTaskService.syncData(jsonObject);
        return new ProcessResult(true, "ExecuteTriggerNotify process success");
    }

    @PostMapping("/sync/rr/updateData")
    public Boolean updateData(@RequestBody JSONObject params) throws Exception {
        syncRRTaskService.createRrCode(params);
        return true;
    }*/

    @PostMapping("/sync/ir/data")
    public ProcessResult syncData(@RequestBody JSONObject jsonObject) throws Exception {
        Boolean result = irTaskService.syncData(jsonObject);
        return new ProcessResult(true, "ExecuteTriggerNotify process success");
    }

    @PostMapping("/sync/ir/notify")
    public ProcessResult notify2(@RequestBody JSONObject jsonObject) throws Exception {
        irTaskService.notify2(jsonObject.getString("instanceId"),jsonObject.getObject("empNos",List.class), jsonObject.getString("name"));
        return new ProcessResult(true, "ExecuteTriggerNotify process success");
    }

    @PostMapping("/sync/ir/addUser")
    public ProcessResult addUser(@RequestBody JSONObject jsonObject) throws Exception {
        irTaskService.addRoleUser();
        return new ProcessResult(true, "ExecuteTriggerNotify process success");
    }
}
