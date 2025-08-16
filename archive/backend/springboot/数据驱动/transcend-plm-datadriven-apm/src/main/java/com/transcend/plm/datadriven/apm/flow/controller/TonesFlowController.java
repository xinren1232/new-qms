package com.transcend.plm.datadriven.apm.flow.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.datadriven.apm.flow.pojo.ao.ApmFlowTemplateAO;
import com.transcend.plm.datadriven.apm.flow.pojo.vo.ApmFlowTemplateVO;
import com.transcend.plm.datadriven.apm.flow.service.ITonesFlowService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author unknown
 */
@RestController
@Api(value = "TonesFlowController", tags = "tones-控制器")
@RequestMapping(value ="/apm/tones")
public class TonesFlowController {
    @Autowired
    private ITonesFlowService tonesFlowService;

    @PostMapping("/handleNodeState")
    public TranscendApiResponse<Boolean> handleNodeState(){
        return TranscendApiResponse.success(tonesFlowService.handleNodeState());
    }

    @PostMapping("/handleNodeHandler")
    public TranscendApiResponse<Boolean> handleNodeHandler(){
        return TranscendApiResponse.success(tonesFlowService.handleNodeHandler());
    }
}
