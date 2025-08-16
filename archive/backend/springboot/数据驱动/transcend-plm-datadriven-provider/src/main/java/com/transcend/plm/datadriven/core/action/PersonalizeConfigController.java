package com.transcend.plm.datadriven.core.action;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.domain.datasource.pojo.dto.PersonalizeConfigDto;
import com.transcend.plm.datadriven.domain.object.base.PersonalizeConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Api(value = "PersonalizeConfigController", tags = "数据驱动-个性化配置")
@Validated
@RestController
@RequestMapping("/data-driven/api/personalize-config")
public class PersonalizeConfigController {
    @Resource
    private PersonalizeConfigService personalizeConfigService;

    /**
     * @param personalizeConfigDto
     * @return {@link TranscendApiResponse }<{@link Boolean }>
     */
    @ApiOperation("保存或者更新")
    @PostMapping("/saveOrUpdate")
    public TranscendApiResponse<Boolean> saveOrUpdate(@RequestBody PersonalizeConfigDto personalizeConfigDto) {
        return TranscendApiResponse.success(personalizeConfigService.saveOrUpdate(personalizeConfigDto));
    }

    /**
     * @param modelCode
     * @return {@link TranscendApiResponse }<{@link MBaseData }>
     */
    @GetMapping("/getByModelCode/{modelCode}")
    public TranscendApiResponse<MBaseData> getByModelCode(@ApiParam("模型编码") @PathVariable String modelCode){
        return TranscendApiResponse.success(personalizeConfigService.getByModelCode(modelCode));
    }
}
