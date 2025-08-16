package com.transcend.plm.datadriven.apm.integration.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.datadriven.apm.integration.message.IInstanceOperateMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author yanjie
 * @Date 2024/1/12 14:21
 * @Version 1.0
 */

@RestController
@Api(value = "InstanceOperateMessageController", tags = "实例操作消息")
@RequestMapping(value = "/apm/integration/instance/operate")
public class InstanceOperateMessageController {

    @Resource
    IInstanceOperateMessageService iInstanceOperateMessageService;
    @ApiOperation("实例错误消息处理")
    @PostMapping("/failureMessageHandle")
    public TranscendApiResponse<Boolean> failureMessageHandle() {
        iInstanceOperateMessageService.failureMessageHandle();
        return TranscendApiResponse.success(Boolean.TRUE);
    }

}
