package com.transcend.plm.datadriven.apm.space.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmAppViewConfigDto;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmAppViewConfigShereDto;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmAppViewConfigQo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmAppViewConfigVo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmAppViewUserRecord;
import com.transcend.plm.datadriven.apm.space.service.IApmAppViewConfigAppService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author unknown
 */
@RestController
@Api(value = "Apm SpaceAppDataController", tags = "空间-实例显示应用视图配置")
@RequestMapping(value = "/apm/appviewconfig")
public class ApmAppViewConfigController {

    @Resource
    private IApmAppViewConfigAppService apmAppViewConfigAppService;

    @ApiOperation("保存或者修改用户最后一次操作记录")
    @PostMapping("/saveOrUpdateUserRecord")
    public TranscendApiResponse<Boolean> saveOrUpdateUserRecord(@RequestBody ApmAppViewUserRecord apmAppViewUserRecord){
        return TranscendApiResponse.success(apmAppViewConfigAppService.saveApmAppViewUserRecord(apmAppViewUserRecord));
    }

    @ApiOperation("保存或者修改应用视图配置")
    @PostMapping("/saveOrUpdateAppViewConfig")
    public TranscendApiResponse<Boolean> saveOrUpdateAppViewConfig(@RequestBody ApmAppViewConfigDto apmAppViewConfigDto){
        return TranscendApiResponse.success(apmAppViewConfigAppService.saveAppViewConfig(apmAppViewConfigDto));
    }

    @ApiOperation("分享应用视图配置")
    @PostMapping("/shareAppViewConfig")
    public TranscendApiResponse<Boolean> shareAppViewConfig(@RequestBody ApmAppViewConfigShereDto apmAppViewConfigDto){
        return TranscendApiResponse.success(apmAppViewConfigAppService.shareAppView(apmAppViewConfigDto.getAppViewConfigBid(),apmAppViewConfigDto.getSpaceBidList()));
    }

    @ApiOperation("删除应用视图浏览记录")
    @PostMapping("/deleteRecord/{spaceAppBid}")
    public TranscendApiResponse<Boolean> deleteRecord(@PathVariable String spaceAppBid){
        return TranscendApiResponse.success(apmAppViewConfigAppService.deleteApmAppViewUserRecord(spaceAppBid));
    }

    @ApiOperation("删除应用视图配置")
    @PostMapping("/delete/{bid}")
    public TranscendApiResponse<Boolean> delete(@PathVariable String bid){
        return TranscendApiResponse.success(apmAppViewConfigAppService.deleteByBid(bid));
    }

    @ApiOperation("根据bid获取单个应用视图配置")
    @GetMapping("/get/{bid}")
    public TranscendApiResponse<ApmAppViewConfigVo> get(@PathVariable String bid){
        return TranscendApiResponse.success(apmAppViewConfigAppService.getViewByBid(bid));
    }

    @ApiOperation("保存或者修改应用视图配置")
    @PostMapping("/listViewConfigs")
    public TranscendApiResponse<List<ApmAppViewConfigVo>> listViewConfigs(@RequestBody ApmAppViewConfigQo apmAppViewConfigQo){
        return TranscendApiResponse.success(apmAppViewConfigAppService.listViewConfigs(apmAppViewConfigQo.getSpaceBid(), apmAppViewConfigQo.getSpaceAppBid(),apmAppViewConfigQo.getTabBid()));
    }

}
