package com.transcend.plm.datadriven.apm.task.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.datadriven.apm.task.ao.ApmTaskAO;
import com.transcend.plm.datadriven.apm.task.ao.ApmTaskDeleteAO;
import com.transcend.plm.datadriven.apm.task.ao.ApmTaskQueryAO;
import com.transcend.plm.datadriven.apm.task.service.ApmTaskApplicationService;
import com.transcend.plm.datadriven.apm.task.vo.ApmTaskNumVO;
import com.transcend.plm.datadriven.apm.task.vo.ApmTaskVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author yuanhu.huang <yuanhu.huang@transsion.com>
 * @version V1.0.0
 * @date 2023/10/08 14:07
 * @since 1.0
 **/
@RestController
@Api(value = "ApmTaskController", tags = "任务-控制器")
@RequestMapping(value ="/apm/task")
public class ApmTaskController {
    @Resource
    private ApmTaskApplicationService apmTaskApplicationService;

    @ApiOperation("新增或者修改流程模板")
    @PostMapping("/saveOrUpdate")
    public TranscendApiResponse<List<String>> saveOrUpdate(@RequestBody ApmTaskAO apmTaskAO){
        return TranscendApiResponse.success(apmTaskApplicationService.saveOrUpdateApmTask(apmTaskAO));
    }

    @ApiOperation("批量删除")
    @PostMapping("/delete")
    public TranscendApiResponse<Boolean> deleteByApmTaskDeleteAO(@RequestBody ApmTaskDeleteAO apmTaskDeleteAO){
        return TranscendApiResponse.success(apmTaskApplicationService.deleteByApmTaskDeleteAO(apmTaskDeleteAO));
    }

    @ApiOperation("根据任务状态查询当前登录人的任务")
    @GetMapping("/list/{taskState}")
    public TranscendApiResponse<List<ApmTaskVO>> listUserApmTasks(@ApiParam("taskState") @PathVariable("taskState") int taskState) {
        return TranscendApiResponse.success(apmTaskApplicationService.listUserApmTasks(taskState));
    }

    @ApiOperation("检查人员的任务是否完成")
    @PostMapping("/checkComplete")
    public TranscendApiResponse<Boolean> checkCurrentUserComplete(@RequestBody ApmTaskQueryAO apmTaskQueryAO){
        return TranscendApiResponse.success(apmTaskApplicationService.checkCurrentUserComplete(apmTaskQueryAO.getBizBid(),apmTaskQueryAO.getHandler(),apmTaskQueryAO.getTaskType()));
    }
    @ApiOperation("获取当前登录人任务数量")
    @GetMapping("/getTaskNum")
    public TranscendApiResponse<List<ApmTaskNumVO>> getTaskNum(){
        return TranscendApiResponse.success(apmTaskApplicationService.getTaskNum());
    }
}
