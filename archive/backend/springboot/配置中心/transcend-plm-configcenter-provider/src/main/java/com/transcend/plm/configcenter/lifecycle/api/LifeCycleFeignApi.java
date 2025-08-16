package com.transcend.plm.configcenter.lifecycle.api;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.configcenter.api.model.lifecycle.vo.LifeCycleStateVo;
import com.transcend.plm.configcenter.lifecycle.application.service.ILifeCycleApplicationService;
import com.transcend.plm.configcenter.lifecycle.domain.service.LifeCycleStateService;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.TemplateDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.CfgLifeCycleTemplateNodePo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 生命周期API
 * @author yinbin
 * @version:
 * @date 2023/10/08 15:29
 */
@Api(value = "LifeCycle Controller", tags = "API-生命周期-控制器")
@Validated
@RestController
public class LifeCycleFeignApi{

    @Resource
    private LifeCycleStateService lifeCycleStateService;

    @Resource
    private ILifeCycleApplicationService lifeCycleApplicationService;

    @ApiOperation("根据code列表查询生命周期状态列表")
    @PostMapping("/api/manager/cfg/life-cycle/state/queryByCodes")
    public TranscendApiResponse<List<LifeCycleStateVo>> queryByCodes(@RequestBody List<String> codeList) {
        return TranscendApiResponse.success(lifeCycleStateService.queryByCodes(codeList));
    }

    @ApiOperation("根据model获取模板节点信息")
    @PostMapping("/api/manager/cfg/life-cycle/template/getTemplateNodesSorted")
    public List<CfgLifeCycleTemplateNodePo> getTemplateNodesOrderByLine(@RequestBody TemplateDto templateDto){
        return lifeCycleApplicationService.getTemplateNodesOrderByLine(templateDto);
    }
}
