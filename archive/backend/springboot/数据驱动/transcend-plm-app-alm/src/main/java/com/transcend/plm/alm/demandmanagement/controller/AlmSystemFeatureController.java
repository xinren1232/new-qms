package com.transcend.plm.alm.demandmanagement.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.alm.demandmanagement.entity.ao.SfTreeDataSyncCopyAo;
import com.transcend.plm.alm.demandmanagement.service.SystemFeatureTreeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据投递视图
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/14 10:59
 */
@Api(value = "Alm Data Deliver Queue Controller", tags = "ALM-需求管理-系统特性")
@RestController
@RequestMapping("/alm/system/feature")
@AllArgsConstructor
public class AlmSystemFeatureController {

    private final SystemFeatureTreeService systemFeatureTreeService;

    @ApiOperation("同步数据")
    @PostMapping("syncData")
    public TranscendApiResponse<Void> syncData(@RequestBody SfTreeDataSyncCopyAo params) {
        systemFeatureTreeService.syncData(params);
        return TranscendApiResponse.success(null);
    }
}
