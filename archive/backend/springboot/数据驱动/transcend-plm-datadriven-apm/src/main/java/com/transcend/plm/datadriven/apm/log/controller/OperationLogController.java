package com.transcend.plm.datadriven.apm.log.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.datadriven.apm.log.OperationLogEsService;
import com.transcend.plm.datadriven.apm.log.model.dto.GenericLogAddParam;
import com.transcend.plm.datadriven.apm.log.model.dto.OperationLogAddParam;
import com.transcend.plm.datadriven.apm.log.model.dto.OperationLogEsData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 操作日志控制器
 * @author yinbin
 * @version:
 * @date 2023/10/07 14:20
 */
@Api(value = "OperationLog Controller", tags = "操作日志-控制器")
@RestController
public class OperationLogController {
    @Resource
    private OperationLogEsService operationLogEsService;
    @ApiOperation("新增操作日志 - 实例数据(会根据实例数据翻译组件的值)")
    @PostMapping("/data-driven/api/operation-log/save")
    public TranscendApiResponse<Boolean> save(@RequestBody @Valid OperationLogAddParam operationLogAddParam) {
        return TranscendApiResponse.success(operationLogEsService.save(operationLogAddParam,null));
    }

    @ApiOperation("新增操作日志 - 通用保存(只保存日志信息)")
    @PostMapping("/data-driven/api/operation-log/genericSave")
    public TranscendApiResponse<Boolean> genericSave(@RequestBody @Valid GenericLogAddParam genericLogAddParam) {
        return TranscendApiResponse.success(operationLogEsService.genericSave(genericLogAddParam));
    }

    @ApiOperation("查询操作日志")
    @GetMapping(value = {"/data-driven/api/operation-log/queryList/{spaceBid}/{type}/{bizId}",
                "/data-driven/api/operation-log/queryList/{spaceBid}/{bizId}",
                "/data-driven/api/operation-log/queryList/{bizId}"})
    public TranscendApiResponse<List<OperationLogEsData>> queryList(@PathVariable(required = false) String spaceBid, @PathVariable(required = false) String type, @PathVariable String bizId) {
        return TranscendApiResponse.success(operationLogEsService.queryList(spaceBid, type, bizId));
    }
}
