package com.transcend.plm.configcenter.api.feign;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.configcenter.api.feign.fallback.DemoClientFactory;
import com.transcend.plm.configcenter.api.model.lifecycle.dto.TemplateDto;
import com.transcend.plm.configcenter.api.model.lifecycle.vo.CfgLifeCycleTemplateNodeVo;
import com.transcend.plm.configcenter.api.model.lifecycle.vo.LifeCycleStateVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 生命周期状态远程调用
 * @author yinbin
 * @version:
 * @date 2023/10/08 14:29
 */
@FeignClient(name = "${transcend.configcenter.feign.name:transcend-plm-configcenter}",
        url = "${transcend.configcenter.feign.url:}",
        fallbackFactory = DemoClientFactory.class)
public interface LifeCycleFeignClient {

    /**
     * 根据code列表查询生命周期状态列表
     * @param codeList code列表
     * @return List<LifeCycleStateVo>
     * @version: 1.0
     * @date: 2023/10/8 14:31
     * @author: bin.yin
     */
    @ApiOperation("根据code列表查询生命周期状态列表")
    @PostMapping("/api/manager/cfg/life-cycle/state/queryByCodes")
    TranscendApiResponse<List<LifeCycleStateVo>> queryByCodes(@RequestBody List<String> codeList);

    @ApiOperation("根据model获取模板节点信息")
    @PostMapping("/manager/cfg/life-cycle/template/getTemplateNodes")
    TranscendApiResponse<List<CfgLifeCycleTemplateNodeVo>> getTemplateNodes(@RequestBody TemplateDto templateDto);

    @ApiOperation("根据model获取模板节点信息")
    @PostMapping("/manager/cfg/life-cycle/template/getNextTemplateNodes")
    TranscendApiResponse<List<CfgLifeCycleTemplateNodeVo>> getNextTemplateNodes(@RequestBody TemplateDto templateDto);

    @ApiOperation("根据阶段生命周期模板获取关键节点")
    @PostMapping("/manager/cfg/life-cycle/template/getKeyPathNodes")
    TranscendApiResponse<List<CfgLifeCycleTemplateNodeVo>> getKeyPathNodes(@RequestBody TemplateDto templateDto);


    @ApiOperation("查询阶段生命周期状态所属关键节点")
    @PostMapping("/manager/cfg/life-cycle/template/getKeyPathNode")
    TranscendApiResponse<CfgLifeCycleTemplateNodeVo> getKeyPathNode(@RequestBody TemplateDto templateDto);


    @ApiOperation("根据model获取模板节点信息")
    @PostMapping("/api/manager/cfg/life-cycle/template/getTemplateNodesSorted")
    TranscendApiResponse<List<CfgLifeCycleTemplateNodeVo>> getTemplateNodesSorted(@RequestBody TemplateDto templateDto);
}
