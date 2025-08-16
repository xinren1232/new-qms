package com.transcend.plm.datadriven.apm.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.configcenter.api.model.dictionary.dto.CfgOptionItemDto;
import com.transcend.plm.datadriven.common.dynamic.fields.DynamicFieldConverter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 动态字段视图
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/26 11:16
 */

@Slf4j
@RestController
@RequestMapping("apm/dynamic/field")
@AllArgsConstructor
@Api(tags = {"动态字段视图"})
public class DynamicFieldController {

    private DynamicFieldConverter dynamicFieldConverter;

    @ApiOperation("获取可配置动态字段列表")
    @GetMapping(value = "optionList")
    public TranscendApiResponse<List<CfgOptionItemDto>> optionList() {
        return TranscendApiResponse.success(dynamicFieldConverter.getOptionList());
    }

}
