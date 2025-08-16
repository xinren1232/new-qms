package com.transcend.plm.datadriven.apm.space.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.MObjectTree;
import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transcend.plm.datadriven.apm.dto.ProjectSetQueryDto;
import com.transcend.plm.datadriven.apm.space.service.IApmProjectSetService;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ProjectSetDemandViewVo;
import com.transsion.framework.dto.BaseRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author unknown
 */
@RestController
@Api(value = "ApmProjectSetController", tags = "项目集")
@RequestMapping(value = "/apm/projectSet")
public class ApmProjectSetController {
    @Resource
    private IApmProjectSetService iApmProjectSetService;

    @ApiOperation("获取所有项目集产品需求视图信息")
    @PostMapping("/getAllDemandBaseViews")
    public TranscendApiResponse<Map<String, ProjectSetDemandViewVo>> getAllDemandBaseViews(){
        return TranscendApiResponse.success(iApmProjectSetService.getAllDemandBaseViews());
    }

    @ApiOperation("项目集产品需求清单查询")
    @PostMapping("/listProjectSetDemands")
    public TranscendApiResponse<List<MObject>> listProjectSetDemands(){
        return TranscendApiResponse.success(iApmProjectSetService.listProjectSetDemands());
    }
    @ApiOperation("项目集产品需求清单查询")
    @PostMapping("/getProjectSetDemandPage")
    public TranscendApiResponse<PagedResult<MObject>> getProjectSetDemandPage(@RequestBody BaseRequest<ModelMixQo> mixQoBaseRequest){
        return TranscendApiResponse.success(iApmProjectSetService.getProjectSetDemandPage(mixQoBaseRequest));
    }

    @ApiOperation("项目集产品需求清单查询")
    @PostMapping("/getProjectSetPage")
    public TranscendApiResponse<PagedResult<MObject>> getProjectSetPage(@RequestBody ProjectSetQueryDto projectSetQueryDto) {
        return TranscendApiResponse.success(iApmProjectSetService.getProjectSetPage(projectSetQueryDto));
    }

    @ApiOperation("项目集产品需求清单查询")
    @PostMapping("/listFristProjectSetDemands")
    public TranscendApiResponse<List<MObject>> listFristProjectSetDemands(){
        return TranscendApiResponse.success(iApmProjectSetService.listFristProjectSetDemands());
    }
    @ApiOperation("单个项目集产品需求清单查询")
    @PostMapping("/getProjectDemandDetail")
    public TranscendApiResponse<List<MObjectTree>> getProjectDemandDetail(@RequestBody MObject mObject){
        return TranscendApiResponse.success(iApmProjectSetService.getProjectDemandDetail(mObject));
    }
}
