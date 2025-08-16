package com.transcend.plm.alm.demandmanagement.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.alm.demandmanagement.service.AlmDataDeliverQueueService;
import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 数据投递视图
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/7 10:59
 */
@Api(value = "Alm Data Deliver Queue Controller", tags = "ALM-需求管理-数据同步")
@RestController
@RequestMapping("/alm/data/deliver/queue")
@AllArgsConstructor
public class AlmDataDeliverQueueController {

    private final AlmDataDeliverQueueService almDataDeliverQueueService;

    @ApiOperation("数据补偿")
    @PostMapping("/compensate/{modelCode}")
    public TranscendApiResponse<Void> compensate(@PathVariable("modelCode") String modelCode,
                                                    @RequestBody ModelMixQo params) {
        almDataDeliverQueueService.compensateDeliver(modelCode, params);
        return TranscendApiResponse.success(null);
    }
}
