package com.transcend.plm.datadriven.apm.space.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.datadriven.apm.space.model.view.AppViewModelDto;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmSpaceAppDto;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceObjectVo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceViewTreeVo;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceConfigManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/9/20 17:07
 * @since 1.0
 **/
@RestController
@Api(value = "Apm SpaceAppDataController", tags = "空间-配置管理-控制器")
@RequestMapping(value ="/apm/space/{spaceBid}/config-manage")
public class ApmSpaceConfigManageController {

    @Resource
    private IApmSpaceConfigManageService apmSpaceConfigManageService;

    /**
     * 空间应用新增 TODO
     */
    @ApiOperation("新增")
    @PostMapping("/app/{spaceAppBid}/add")
    public TranscendApiResponse<List<AppViewModelDto>> addApp(@PathVariable("spaceBid") String spaceBid,
                                                              @RequestBody ApmSpaceAppDto apmSpaceAppDto){
        return TranscendApiResponse.success(apmSpaceConfigManageService.addApp(spaceBid, apmSpaceAppDto));
    }

    /**
     * 空间应用列表 TODO
     * @param spaceBid 空间bid
     * @return
     */
    @ApiOperation("空间应用列表")
    @PostMapping("/app/list")
    public TranscendApiResponse<List<AppViewModelDto>> appList(@PathVariable("spaceBid") String spaceBid){
        return TranscendApiResponse.success(apmSpaceConfigManageService.appList(spaceBid));
    }

    @ApiOperation(value = "设置启用状态", httpMethod = "POST")
    @PostMapping("/app/{bid}/changeEnableFlag/{enableFlag}")
    public TranscendApiResponse<Boolean> appChangeEnableFlag(@PathVariable(name = "bid") String bid,
                                    @PathVariable(name = "enableFlag") Integer enableFlag) {
        return TranscendApiResponse.success(apmSpaceConfigManageService.appChangeEnableFlag(bid, enableFlag));
    }

    @ApiOperation("根据modelCode查询子对象列表信息和对应的空间应用信息")
    @GetMapping("/app/{modelCode}/listChildrenByModelCode")
    public TranscendApiResponse<List<ApmSpaceObjectVo>> listChildrenByModelCode(@PathVariable("spaceBid") String spaceBid,
                                                                                @PathVariable("modelCode") String modelCode) {
        return TranscendApiResponse.success(apmSpaceConfigManageService.listChildrenByModelCode(spaceBid, modelCode));
    }

    @ApiOperation("根据视图Bid查询同类模型的视图树")
    @GetMapping("/app/{viewBid}/sameModel/viewTree")
    public TranscendApiResponse<List<ApmSpaceViewTreeVo>> sameModelViewTree(@PathVariable("spaceBid") String spaceBid,
                                                                            @PathVariable("viewBid") String viewBid) {
        return TranscendApiResponse.success(apmSpaceConfigManageService.sameModelViewTree(viewBid));
    }

}
