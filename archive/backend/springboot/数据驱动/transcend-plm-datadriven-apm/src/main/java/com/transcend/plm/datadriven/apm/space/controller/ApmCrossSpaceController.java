package com.transcend.plm.datadriven.apm.space.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.MObjectTree;
import com.transcend.plm.datadriven.api.model.RelationMObject;
import com.transcend.plm.datadriven.api.model.dto.MObjectCheckDto;
import com.transcend.plm.datadriven.apm.space.service.ICrossSpaceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author unknown
 */
@RestController
@Api(value = "Apm ApmCrossSpaceController", tags = "跨空间-应用数据驱动-控制器")
@RequestMapping(value = "/apm/crossSpace/data-driven")
public class ApmCrossSpaceController {

    @Resource
    private ICrossSpaceService crossSpaceService;
    @ApiOperation("列表查询-支持内外部数据源扩展(关系选择)")
    @PostMapping("/relation/{spaceBid}/{modelCode}/{source}/selectListExpand")
    TranscendApiResponse<List<MObject>> relationSelectListExpand(@PathVariable("spaceBid") String spaceBid,
                                                                 @ApiParam("模型编码") @PathVariable String modelCode, @PathVariable String source, @ApiParam("查询参数") @RequestBody MObjectCheckDto mObjectCheckDto){
        return TranscendApiResponse.success(crossSpaceService.relationSelectListExpand(spaceBid,modelCode, source,mObjectCheckDto));
    }

    @ApiOperation("树查询")
    @PostMapping("/relation/{spaceBid}/{modelCode}/selectTree")
    TranscendApiResponse<List<MObjectTree>> selectTree(@PathVariable("spaceBid") String spaceBid,
                                                                     @ApiParam("模型编码") @PathVariable String modelCode, @ApiParam("查询参数") @RequestBody RelationMObject relationMObject){
        return TranscendApiResponse.success(crossSpaceService.selectTree(spaceBid,modelCode, relationMObject));
    }
}
