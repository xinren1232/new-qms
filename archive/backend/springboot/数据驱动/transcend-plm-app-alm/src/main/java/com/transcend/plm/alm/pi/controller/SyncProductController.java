package com.transcend.plm.alm.pi.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.alm.demandmanagement.entity.ao.SelectAo;
import com.transcend.plm.alm.pi.dto.SyncDto;
import com.transcend.plm.alm.pi.service.SyncProductService;
import com.transcend.plm.datadriven.api.model.MObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author yuanhu.huang
 * @version v1.0.0
 * @description pi 产品同步
 * @date 2024/10/15 11:03
 **/
@Api(value = "Demand Management Controller", tags = "空间-需求管理-控制器")
@RestController
@RequestMapping(value = ("/api/app/pi/syncProduct"))
public class SyncProductController {
    @Resource
    private SyncProductService syncProductService;

    @ApiOperation("更新或者新增")
    @PostMapping("/saveOrUpdate")
    public TranscendApiResponse<Boolean> saveOrUpdate(@RequestBody SyncDto syncDto) {
        return TranscendApiResponse.success(syncProductService.saveOrUpdate(syncDto));
    }
}
