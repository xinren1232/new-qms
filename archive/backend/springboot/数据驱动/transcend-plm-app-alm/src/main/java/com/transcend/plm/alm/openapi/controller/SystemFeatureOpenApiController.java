package com.transcend.plm.alm.openapi.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.alm.demandmanagement.service.SystemFeatureTreeService;
import com.transcend.plm.alm.openapi.dto.AlmSystemFeatureDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统特性OpenAPI
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/27 16:54
 */
@Api(value = "Alm System Feature Open Api Controller", tags = "ALM-系统特性-OPEN API")
@RestController
@RequestMapping("/open/api/alm/system/feature")
@AllArgsConstructor
public class SystemFeatureOpenApiController {

    private final SystemFeatureTreeService systemFeatureTreeService;


    @ApiOperation("查询全局特性树")
    @GetMapping("tree")
    public TranscendApiResponse<List<AlmSystemFeatureDTO>> tree(
            @RequestParam(value = "searchKey", required = false) String searchKey) {
        return TranscendApiResponse.success(systemFeatureTreeService.getSystemFeatureTree(searchKey));
    }


    @ApiOperation("查询全局特性详情")
    @GetMapping("detail")
    public TranscendApiResponse<AlmSystemFeatureDTO> detail(@RequestParam("bid") String bid) {
        return TranscendApiResponse.success(systemFeatureTreeService.getSystemFeatureByBid(bid));
    }

}
