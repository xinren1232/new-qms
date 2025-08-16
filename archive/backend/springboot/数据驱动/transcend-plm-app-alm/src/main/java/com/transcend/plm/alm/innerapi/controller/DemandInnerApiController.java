package com.transcend.plm.alm.innerapi.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.alm.demandmanagement.service.DemandManagementService;
import com.transcend.plm.datadriven.api.model.MObjectTree;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统特性PLM系统内调用的API
 *
 * @author jie.luo1 <jie.luo1@transsion.com>
 * @version 1.0
 * createdAt 2025/5/27 16:54
 */
@Api(value = "Alm Demand Inner Api Controller", tags = "ALM-需求-INNER API")
@RestController
@RequestMapping("/inner/api/alm/demand/")
@AllArgsConstructor
public class DemandInnerApiController {

    private final DemandManagementService demandManagementService;


    @ApiOperation("查询需求IR/SR的数据")
    @GetMapping("/irsr/tree")
    public TranscendApiResponse<List<MObjectTree>> tree(
            @RequestParam(value = "projectName") String projectName) {
        return TranscendApiResponse.success(demandManagementService.searchDemandIrsrTree(projectName));
    }

}
