package com.transcend.plm.datadriven.configcenter.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.configcenter.api.model.object.dto.CfgViewRuleMatchDto;
import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import com.transcend.plm.datadriven.configcenter.cache.ConfigCenterCacheService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 实例驱动需要配置中心的控制器
 * <p>
 * <p>
 * 目的是为了把配置中心的配置数据在此做缓存，降低配置中心的压力
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/5/18 17:07
 */
@Api(value = "ConfigCenter Controller", tags = "配置中心-控制器")
@Validated
@RestController
@RequestMapping("/data-driven/api/config-center")
public class ConfigCenterController {

    @Resource
    private ConfigCenterCacheService configCenterCacheService;

    /**
     * @param dto
     * @return {@link TranscendApiResponse }<{@link CfgViewVo }>
     */
    @ApiOperation("获取最优匹配视图")
    @PostMapping("/getOptimalMatchView")
    public TranscendApiResponse<CfgViewVo> getOptimalMatchView(@RequestBody CfgViewRuleMatchDto dto) {

        return TranscendApiResponse.success(configCenterCacheService.getOptimalMatchView(dto));

    }

    /**
     * @param modelCode
     * @return {@link TranscendApiResponse }<{@link Map }<{@link String }, {@link Object }>>
     */
    @ApiOperation("获取默认配置")
    @PostMapping("/listDefaultConfigByModelCode/{modelCode}")
    public TranscendApiResponse<Map<String, Object>> listDefaultConfigByModelCode(@PathVariable String modelCode) {

        return TranscendApiResponse.success(configCenterCacheService.listDefaultConfigByModelCode(modelCode));

    }

    /**
     * @return {@link TranscendApiResponse }<{@link Boolean }>
     */
    @ApiOperation("测试")
    @GetMapping("/test")
    public TranscendApiResponse<Boolean> test() {
        return TranscendApiResponse.success(true);
    }


}
