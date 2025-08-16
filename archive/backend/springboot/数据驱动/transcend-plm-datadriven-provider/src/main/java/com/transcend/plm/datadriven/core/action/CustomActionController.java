package com.transcend.plm.datadriven.core.action;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 关系对象控制器
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/5/18 17:07
 * @since 1.0
 */
@Api(value = "自定义扩展action Controller", tags = "自定义扩展action-控制器")
@Validated
@RestController
@RequestMapping("/data-driven/api/custom-action")
public class CustomActionController {


    @ApiOperation("do")
    @PostMapping("/do")
    public TranscendApiResponse<Object> action(@RequestBody Map<String, String> data) {
        return null;

    }

}
