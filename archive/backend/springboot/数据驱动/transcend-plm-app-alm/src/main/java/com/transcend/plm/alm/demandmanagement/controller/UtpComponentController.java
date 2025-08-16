package com.transcend.plm.alm.demandmanagement.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.alm.demandmanagement.entity.ao.UtpComponentTreeListAo;
import com.transcend.plm.alm.demandmanagement.entity.vo.UtpComponentTreeVo;
import com.transcend.plm.alm.demandmanagement.service.UtpComponentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据投递视图
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/7 10:59
 */
@Api(value = "Alm Utp component Controller", tags = "ALM-UTP-模块查询")
@RestController
@RequestMapping("/alm/utp/component")
@AllArgsConstructor
public class UtpComponentController {

    private final UtpComponentService utpComponentService;

    @ApiOperation("树列表")
    @PostMapping("treeList")
    public TranscendApiResponse<List<UtpComponentTreeVo>> treeList(
            @RequestBody UtpComponentTreeListAo ao) {
        return TranscendApiResponse.success(utpComponentService.getComponentTreeList(ao));
    }

    @ApiOperation("数据修正")
    @PostMapping("dataCorrection")
    public TranscendApiResponse<Void> dataCorrection() {
        utpComponentService.dataCorrection();
        return TranscendApiResponse.success(null);
    }


}
