package com.transcend.plm.alm.demandmanagement.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.alm.demandmanagement.service.SystemFeatureService;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.MObjectTree;
import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.service.IBaseApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.apm.tools.ParentBidHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author jinpeng.bai
 * @version v1.0.0
 * @description 需求管理定制业务控制器
 * @date 2024/06/21 11:03
 **/
@Api(value = "System Feature Controller", tags = "特性控制器")
@RestController
@RequestMapping(value = ("/alm/systemFeature/{spaceBid}/app/{spaceAppBid}"))
public class SystemFeatureController {

    @Resource
    private SystemFeatureService systemFeatureService;

    @ApiOperation("IR特性树接口")
    @PostMapping("/tree")
    public TranscendApiResponse<List<MObjectTree>> tree(@PathVariable("spaceBid") String spaceBid,
                                                        @PathVariable("spaceAppBid") String spaceAppBid,
                                                        @RequestBody ModelMixQo modelMixQo) {
        return TranscendApiResponse.success(ParentBidHandler.handleMObjectTree(systemFeatureService.tree(spaceBid,spaceAppBid, modelMixQo, true)));
    }

    @ApiOperation("查询有关联IR的tos特性")
    @PostMapping("/getSourceData")
    public TranscendApiResponse<List<MObjectTree>> getSourceData(@PathVariable("spaceBid") String spaceBid,
                                                             @PathVariable("spaceAppBid") String targetSpaceAppBid,
                                                             @RequestBody ModelMixQo modelMixQo) {
        return TranscendApiResponse.success(systemFeatureService.getSourceData(spaceBid, targetSpaceAppBid, modelMixQo));
    }


    @ApiOperation("查询二级特性下的三级特性")
    @PostMapping(value = {"/selectSF/tree",
            "/selectSF/tree/{parentBid}"})
    public TranscendApiResponse<List<MObjectTree>> selectSF(@PathVariable("spaceBid") String spaceBid,
                                                             @PathVariable("spaceAppBid") String targetSpaceAppBid,
                                                            @PathVariable(value = "parentBid",required = false) String parentBid) {
        return TranscendApiResponse.success(systemFeatureService.selectSF(spaceBid, targetSpaceAppBid, parentBid));
    }


    @ApiOperation("IR特性树接口")
    @PostMapping("/deleteIrRsfRelation")
    public TranscendApiResponse<List<MObjectTree>> deleteIrRsfRelation(@PathVariable("spaceBid") String spaceBid,
                                                        @PathVariable("spaceAppBid") String spaceAppBid,
                                                        @RequestBody List<String> mObjectList) {
        systemFeatureService.deleteIrRsfRelation(mObjectList);
        return null;
    }






}
