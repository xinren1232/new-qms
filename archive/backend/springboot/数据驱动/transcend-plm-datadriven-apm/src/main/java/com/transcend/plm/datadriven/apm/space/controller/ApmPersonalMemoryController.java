package com.transcend.plm.datadriven.apm.space.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.datadriven.apm.space.model.ApmPersonMemoryParam;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmPersonMemory;
import com.transcend.plm.datadriven.apm.space.service.IApmPersonalMemoryService;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 空间个人记忆功能
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/9/20 17:07
 * @since 1.0
 **/
@RestController
@Api(value = "Apm SpaceAppDataController", tags = "空间-个人记忆-控制器")
@RequestMapping(value = "/apm/personal/memory")
public class ApmPersonalMemoryController {

    @Resource
    private IApmPersonalMemoryService iApmPersonalMemoryService;

    @ApiOperation("新增")
    @PostMapping("/saveOrUpdate")
    public TranscendApiResponse<Boolean> saveOrUpdate(@RequestBody ApmPersonMemory apmPersonMemory) {
        checkNotNull(apmPersonMemory.getCategory(), apmPersonMemory.getCode());
        return TranscendApiResponse.success(iApmPersonalMemoryService.saveOrUpdate(apmPersonMemory));
    }

    @ApiOperation("局部更新内容")
    @PostMapping("/updatePartialContent")
    public TranscendApiResponse<Boolean> updatePartialContent(@RequestBody ApmPersonMemoryParam apmPersonMemoryParam) {
        checkNotNull(apmPersonMemoryParam.getCategory(), apmPersonMemoryParam.getCode());
        return TranscendApiResponse.success(iApmPersonalMemoryService.updatePartialContent(apmPersonMemoryParam));
    }

    @ApiOperation("获取个人记忆")
    @GetMapping("/get/{category}/{code}")
    public TranscendApiResponse<Map<String, Object>> get(
            @PathVariable("category") String category,
            @PathVariable("code") String code) {
        checkNotNull(category, code);
        return TranscendApiResponse.success(iApmPersonalMemoryService.get(ApmPersonMemoryParam.builder().category(category).code(code).build()));
    }

    @ApiOperation("获取个人记忆(按照json内容里边的key获取)")
    @GetMapping("/getPartialContent/{category}/{code}")
    public TranscendApiResponse<Map<String, Object>> get(
            @PathVariable("category") String category,
            @PathVariable("code") String code,
            @RequestBody List<String> keys ) {
        checkNotNull(category, code);
        return TranscendApiResponse.success(iApmPersonalMemoryService.getPartialContent(category, code, keys));
    }


    /**
     * category与code必填
     *
     * @param category category
     * @param code code
     */
    private static void checkNotNull(String category, String code) {
        if (StringUtils.isEmpty(category) || StringUtils.isEmpty(code)) {
            throw new PlmBizException("category与code必填！");
        }
    }
}
