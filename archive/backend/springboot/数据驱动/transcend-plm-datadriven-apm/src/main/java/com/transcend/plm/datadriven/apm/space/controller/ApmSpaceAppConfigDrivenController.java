package com.transcend.plm.datadriven.apm.space.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.plm.configcenter.api.model.objectrelation.vo.CfgObjectRelationVo;
import com.transcend.plm.configcenter.api.model.view.dto.ViewQueryParam;
import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.api.model.config.ObjectVo;
import com.transcend.plm.datadriven.apm.flow.pojo.vo.ApmAppTabHeaderVO;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmAccessOperationAo;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmActionConfigUserVo;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmActionConfigVo;
import com.transcend.plm.datadriven.apm.space.model.view.AppViewModelDto;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmAppExportDto;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmAppExportTemplateDto;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmAppTabHeaderDto;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmSpaceAppCustomViewDto;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmAccessQo;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmMultiTreeAccessQo;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmSpaceTabQo;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmStateQo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.*;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmAppTabHeader;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmAppTabHeaderService;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppConfigDrivenService;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppConfigManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/9/20 17:07
 * @since 1.0
 **/
@RestController
@Api(value = "Apm SpaceAppDataDrivenController", tags = "空间-应用配置驱动-控制器")
@RequestMapping(value = "/apm/space/{spaceBid}/app/{spaceAppBid}/config-driven")
public class ApmSpaceAppConfigDrivenController {

    @Autowired
    private IApmSpaceAppConfigDrivenService apmSpaceAppConfigDrivenService;

    @Autowired
    private IApmSpaceAppConfigManageService apmSpaceAppConfigManageService;

    @Autowired
    private ApmAppTabHeaderService apmAppTabHeaderService;

    @ApiOperation("应用视图模式列表")
    @PostMapping("/listViewModel")
    public TranscendApiResponse<List<AppViewModelDto>> listViewModel(@PathVariable("spaceBid") String spaceBid,
                                                                     @PathVariable("spaceAppBid") String spaceAppBid) {
        return TranscendApiResponse.success(apmSpaceAppConfigDrivenService.listViewModel(spaceAppBid));
    }

    @ApiOperation("空间应用下的视图模式详情")
    @GetMapping("/viewModel/{viewModelCode}/get")
    public TranscendApiResponse<AppViewModelDto> viewModelGet(@PathVariable("spaceBid") String spaceBid,
                                                              @PathVariable("viewModelCode") String viewModelCode,
                                                              @PathVariable("spaceAppBid") String spaceAppBid) {
        return TranscendApiResponse.success(apmSpaceAppConfigDrivenService.getViewModelDetail(spaceAppBid, viewModelCode));
    }

    @ApiOperation("空间应用下的基础视图（一个）配置 查询")
    @GetMapping("/baseView/get")
    public TranscendApiResponse<CfgViewVo> baseViewGet(@PathVariable("spaceBid") String spaceBid,
                                                       @PathVariable("spaceAppBid") String spaceAppBid) {
        return TranscendApiResponse.success(apmSpaceAppConfigManageService.baseViewGet(spaceAppBid));
    }

    @ApiOperation("空间应用下的基础视图（一个）配置 查询")
    @PostMapping("/baseView/{type}/get")
    @Deprecated
    public TranscendApiResponse<CfgViewVo> baseViewGet(@PathVariable("spaceBid") String spaceBid,
                                                       @PathVariable("spaceAppBid") String spaceAppBid,
                                                       @PathVariable("type") String type) {
        return TranscendApiResponse.success(apmSpaceAppConfigManageService.baseViewGet(spaceAppBid, type));
    }

    @ApiOperation("根据参数查询视图")
    @PostMapping("/getView")
    public TranscendApiResponse<CfgViewVo> getView(@PathVariable("spaceBid") String spaceBid,
                                                   @PathVariable("spaceAppBid") String spaceAppBid,
                                                   @RequestBody ViewQueryParam param) {
        return TranscendApiResponse.success(apmSpaceAppConfigManageService.getView(param));
    }


    /**
     * 空间应用下的流程节点视图（一个）配置 查询
     */
    @ApiOperation("空间应用下的流程节点视图（一个）配置 查询")
    @GetMapping("/flowNodeView/{nodeBid}/action/{action}/get")
    public TranscendApiResponse<CfgViewVo> flowNodeViewGet(@PathVariable("spaceBid") String spaceBid,
                                                           @PathVariable("spaceAppBid") String spaceAppBid,
                                                           @PathVariable("action") String action,
                                                           @PathVariable("nodeBid") String nodeBid) {
        return TranscendApiResponse.success(apmSpaceAppConfigManageService.flowNodeViewGet(spaceAppBid, action, nodeBid));
    }


    /**
     * 提供一个根据应用查生命周期状态的接口 TODO
     *
     * @param spaceBid
     * @param spaceAppBid
     * @return
     */
    /*@ApiOperation("空间应用下的获取生命周期")
    @PostMapping("/lifeCycle/get")
    public TranscendApiResponse<ObjectModelLifeCycleVO> getLifeCycle(@PathVariable("spaceBid") String spaceBid,
                                                                     @PathVariable("spaceAppBid") String spaceAppBid){
        return TranscendApiResponse.success(apmSpaceAppConfigManageService.getLifeCycle(spaceAppBid));
    }*/
    @ApiOperation("空间应用下的获取生命周期")
    @PostMapping("/lifeCycleState/get")
    public TranscendApiResponse<ApmLifeCycleStateVO> getLifeCycleState(@PathVariable("spaceBid") String spaceBid,
                                                                       @PathVariable("spaceAppBid") String spaceAppBid) {
        return TranscendApiResponse.success(apmSpaceAppConfigManageService.getLifeCycleState(spaceAppBid));
    }

    @ApiOperation("空间应用下的获取生命周期")
    @PostMapping("/lifeCycleState/getMultiLifeCycleState")
    public TranscendApiResponse<List<ApmStateVO>> getMultiLifeCycleState(@PathVariable("spaceBid") String spaceBid,
                                                                         @PathVariable("spaceAppBid") String spaceAppBid,
                                                                         @RequestBody List<String> spaceAppBids) {
        return TranscendApiResponse.success(apmSpaceAppConfigManageService.getMultiLifeCycleState(spaceAppBids));
    }

    @ApiOperation("空间应用下的获取生命周期")
    @PostMapping("/lifeCycleState/getByModelCode/{modelCode}")
    public TranscendApiResponse<ApmLifeCycleStateVO> getByModelCode(@PathVariable("spaceBid") String spaceBid,
                                                                    @PathVariable("modelCode") String modelCode) {
        return TranscendApiResponse.success(apmSpaceAppConfigManageService.getLifeCycleStateByModelCode(spaceBid, modelCode));
    }

    @ApiOperation("空间应用下的获取生命周期")
    @PostMapping("/lifeCycleState/listNext")
    public TranscendApiResponse<List<ApmStateVO>> listNextStates(@PathVariable("spaceBid") String spaceBid, @PathVariable("spaceAppBid") String spaceAppBid, @RequestBody ApmStateQo apmStateQo) {
        if (StringUtils.isEmpty(apmStateQo.getSpaceAppBid())) {
            apmStateQo.setSpaceAppBid(spaceAppBid);
        }
        return TranscendApiResponse.success(apmSpaceAppConfigManageService.listNextStates(apmStateQo));
    }

    /**
     * 空间应用下的获取关系Table
     *
     * @param spaceBid
     * @param spaceAppBid
     * @return
     */
    @ApiOperation("空间应用下的获取关系Tab")
    @PostMapping("/relation/listRelationTab")
    public TranscendApiResponse<List<CfgObjectRelationVo>> relationListTab(@PathVariable("spaceBid") String spaceBid,
                                                                           @PathVariable("spaceAppBid") String spaceAppBid) {
        return TranscendApiResponse.success(apmSpaceAppConfigManageService.relationListTab(spaceAppBid));
    }


    /**
     * 空间应用下的获取关系Table
     *
     * @param spaceBid
     * @param spaceAppBid
     * @return
     */
    @ApiOperation("空间应用下的获取关系Tab")
    @PostMapping("/relation/app/listRelationTabByPermission")
    public TranscendApiResponse<List<ApmObjectRelationAppVo>> listRelationTabByPermission(@PathVariable("spaceBid") String spaceBid,
                                                                                          @PathVariable("spaceAppBid") String spaceAppBid,
                                                                                          @RequestBody ApmSpaceTabQo apmSpaceTabQo) {
        return TranscendApiResponse.success(apmSpaceAppConfigDrivenService.listRelationTabByPermission(
                apmSpaceTabQo.setSpaceAppBid(spaceAppBid).setSpaceBid(spaceBid)
        ));
    }

    /**
     * 空间应用下的获取关系应用Tab TODO
     *
     * @param spaceBid
     * @param spaceAppBid
     * @return
     */
    @ApiOperation("空间应用下的获取应用关系Tab")
    @PostMapping("/relation/app/listRelationTab")
    public TranscendApiResponse<List<ApmObjectRelationAppVo>> relationAppListTab(@PathVariable("spaceBid") String spaceBid,
                                                                                 @PathVariable("spaceAppBid") String spaceAppBid) {
        return TranscendApiResponse.success(apmSpaceAppConfigDrivenService.relationAppListTab(spaceAppBid));
    }

    @ApiOperation("查询空间应用的操作配置权限")
    @GetMapping("/access/list")
    public TranscendApiResponse<List<ApmActionConfigVo>> listAccess(@PathVariable("spaceBid") String spaceBid,
                                                                    @PathVariable("spaceAppBid") String spaceAppBid) {
        return TranscendApiResponse.success(apmSpaceAppConfigDrivenService.queryAccessWithRoleBySphereBid(spaceAppBid));
    }

    @ApiOperation("新增操作配置权限")
    @PostMapping("/access/add")
    public TranscendApiResponse<Boolean> addAccess(@PathVariable("spaceBid") String spaceBid,
                                                   @PathVariable("spaceAppBid") String spaceAppBid,
                                                   @RequestBody ApmAccessOperationAo apmAccessOperationAo) {
        return TranscendApiResponse.success(apmSpaceAppConfigDrivenService.addAccess(apmAccessOperationAo, spaceAppBid));
    }

    @ApiOperation("修改操作配置权限")
    @PostMapping("/access/update")
    public TranscendApiResponse<Boolean> updateAccess(@PathVariable("spaceBid") String spaceBid,
                                                      @PathVariable("spaceAppBid") String spaceAppBid,
                                                      @RequestBody ApmAccessOperationAo apmAccessOperationAo) {
        return TranscendApiResponse.success(apmSpaceAppConfigDrivenService.updateAccess(apmAccessOperationAo, spaceAppBid));
    }

    @ApiOperation("删除操作配置权限")
    @PostMapping("/access/delete")
    public TranscendApiResponse<Boolean> deleteAccess(@PathVariable("spaceBid") String spaceBid,
                                                      @PathVariable("spaceAppBid") String spaceAppBid,
                                                      @RequestBody ApmAccessOperationAo apmAccessOperationAo) {
        return TranscendApiResponse.success(apmSpaceAppConfigDrivenService.deleteAccess(apmAccessOperationAo, spaceAppBid));
    }

    @ApiOperation("查询当前登录人的操作权限")
    @GetMapping("/access/currentUser")
    public TranscendApiResponse<List<ApmActionConfigUserVo>> currentUserAccess(@PathVariable("spaceBid") String spaceBid,
                                                                               @PathVariable("spaceAppBid") String spaceAppBid) {
        return TranscendApiResponse.success(apmSpaceAppConfigDrivenService.currentUserAccess(spaceAppBid));
    }

    @ApiOperation("查询当前登陆人实例的操作权限")
    @GetMapping("/access/currentUser/{instanceBid}")
    public TranscendApiResponse<List<ApmActionConfigUserVo>> currentUserAccess(@PathVariable("spaceBid") String spaceBid,
                                                                               @PathVariable("spaceAppBid") String spaceAppBid,
                                                                               @PathVariable("instanceBid") String instanceBid) {
        return TranscendApiResponse.success(apmSpaceAppConfigDrivenService.currentUserInstanceAccess(spaceAppBid, instanceBid));
    }


    @ApiOperation("查询当前登陆人实例的操作权限")
    @PostMapping("/access/currentUserAccess/multiTree/{instanceBid}")
    public TranscendApiResponse<List<ApmActionConfigUserVo>> currentUserPermissionAccessByMultiTree(@PathVariable("spaceBid") String spaceBid,
                                                                                                    @PathVariable("spaceAppBid") String spaceAppBid,
                                                                                                    @PathVariable("instanceBid") String instanceBid,
                                                                                                    @RequestBody ApmMultiTreeAccessQo query) {
        return TranscendApiResponse.success(apmSpaceAppConfigDrivenService.currentUserAccessByMultiTree(spaceBid, spaceAppBid, instanceBid, query));
    }

    @ApiOperation("查询当前登陆人实例的操作权限")
    @PostMapping("/access/currentUserAccess/{instanceBid}")
    public TranscendApiResponse<List<ApmActionConfigUserVo>> currentUserPermissionAccess(@PathVariable("spaceBid") String spaceBid,
                                                                                         @PathVariable("spaceAppBid") String spaceAppBid,
                                                                                         @PathVariable("instanceBid") String instanceBid,
                                                                                         @RequestBody ApmAccessQo apmAccessQo) {
        return TranscendApiResponse.success(apmSpaceAppConfigDrivenService.currentUserPermissionAccess(spaceAppBid, instanceBid, apmAccessQo));
    }


    @ApiOperation("查询当前登陆人操作权限")
    @PostMapping("/access/currentUserPermissionAccess")
    public TranscendApiResponse<List<ApmActionConfigUserVo>> currentUserPermissionAccess(@PathVariable("spaceBid") String spaceBid,
                                                                                         @PathVariable("spaceAppBid") String spaceAppBid) {
        return TranscendApiResponse.success(apmSpaceAppConfigDrivenService.currentUserPermissionAccess(spaceAppBid));
    }


    /**
     * 查看应用视图模式列表
     *
     * @param spaceBid
     * @param spaceAppBid
     * @return
     */
    @ApiOperation("查看应用视图模式列表(按权限)")
    @RequestMapping("/listViewModelByPermission")
    public TranscendApiResponse<List<AppViewModelDto>> listViewModelByPermission(@PathVariable("spaceBid") String spaceBid,
                                                                                 @PathVariable("spaceAppBid") String spaceAppBid,
                                                                                 @RequestParam(value = "inverseQuery", required = false) Boolean inverseQuery,
                                                                                 @RequestParam(value = "entrance", required = false) String entrance,
                                                                                 @RequestParam(value = "tabBid", required = false) String tabBid) {
        return TranscendApiResponse.success(apmSpaceAppConfigDrivenService.listViewModelByPermission(spaceBid, spaceAppBid, entrance, tabBid, inverseQuery));
    }


    @ApiOperation("通过模版空间下发到所有通过复制创建的空间")
    @GetMapping("/access/distribute")
    public TranscendApiResponse<Boolean> distributeByTemplateSpace(@PathVariable("spaceBid") String spaceBid,
                                                                   @PathVariable("spaceAppBid") String spaceAppBid) {
        return TranscendApiResponse.success(apmSpaceAppConfigDrivenService.distributeAccessByTemplateSpace(spaceBid, spaceAppBid));
    }


    /**
     * 自定义视图-保存 TODO
     *
     * @param spaceBid
     * @param spaceAppBid
     * @return
     */
    @ApiOperation("自定义视图-保存")
    @PostMapping("/customView/saveOrUpdate")
    public TranscendApiResponse<Boolean> customViewSaveOrUpdate(@PathVariable("spaceBid") String spaceBid,
                                                                @PathVariable("spaceAppBid") String spaceAppBid,
                                                                @RequestBody ApmSpaceAppCustomViewDto apmSpaceAppCustomViewDto) {
        return TranscendApiResponse.success(apmSpaceAppConfigDrivenService.customViewSaveOrUpdate(spaceAppBid, apmSpaceAppCustomViewDto));
    }


    /**
     * 自定义视图--列表查询 TODO
     *
     * @param spaceBid
     * @param spaceAppBid
     * @return
     */
    @ApiOperation("自定义视图-列表查询")
    @PostMapping("/customView/list")
    public TranscendApiResponse<List<ApmSpaceAppCustomViewVo>> customViewList(@PathVariable("spaceBid") String spaceBid,
                                                                              @PathVariable("spaceAppBid") String spaceAppBid) {
        return TranscendApiResponse.success(apmSpaceAppConfigDrivenService.customViewPermissionList(spaceAppBid));
    }

    /**
     * 自定义视图--详情查询 TODO
     *
     * @param spaceBid
     * @param spaceAppBid
     * @return
     */
    @ApiOperation("自定义视图-详情查询")
    @PostMapping("/customView/{customViewBid}/get")
    public TranscendApiResponse<ApmSpaceAppCustomViewVo> customViewGet(@PathVariable("spaceBid") String spaceBid,
                                                                       @PathVariable("spaceAppBid") String spaceAppBid,
                                                                       @PathVariable("customViewBid") String costomViewBid) {
        return TranscendApiResponse.success(apmSpaceAppConfigDrivenService.customViewGet(costomViewBid));
    }

    /**
     * 查询泳道标签关系
     */
    @ApiOperation("查询泳道标签关系")
    @GetMapping("/laneTab/list")
    public TranscendApiResponse<List<ApmLaneTabVO>> laneTabList(@PathVariable("spaceBid") String spaceBid,
                                                                @PathVariable("spaceAppBid") String spaceAppBid,
                                                                @RequestParam(value = "tabBid", required = false) String tabBid) {
        return TranscendApiResponse.success(apmSpaceAppConfigDrivenService.laneTabList(spaceBid, spaceAppBid, tabBid));
    }

    @ApiOperation("空间应用下的视图模式详情")
    @PostMapping("/saveOrUpateAppTabHeader")
    public TranscendApiResponse<Boolean> saveOrUpateAppTabHeader(@RequestBody ApmAppTabHeaderDto apmAppTabHeaderDto) {
        return TranscendApiResponse.success(apmAppTabHeaderService.saveOrUpdate(apmAppTabHeaderDto));
    }

    @ApiOperation("查询当前登陆人实例的操作权限")
    @GetMapping("/getApmAppTabHeader/{bizBid}")
    public TranscendApiResponse<ApmAppTabHeader> getApmAppTabHeader(@PathVariable("bizBid") String bizBid) {
        return TranscendApiResponse.success(apmAppTabHeaderService.getApmAppTabHeader(bizBid));
    }

    @ApiOperation("查询当前登陆人实例的操作权限")
    @GetMapping("/getApmAppTabHeaderVo/{bizBid}/{code}")
    public TranscendApiResponse<ApmAppTabHeaderVO> getApmAppTabHeaderVo(@PathVariable("bizBid") String bizBid, @PathVariable("code") String code) {
        return TranscendApiResponse.success(apmAppTabHeaderService.getApmAppTabHeaderVO(bizBid, code));
    }

    @ApiOperation("查询当前登陆人实例的操作权限")
    @GetMapping("/object/baseInfo")
    public TranscendApiResponse<ObjectVo> objectBaseInfo(@PathVariable("spaceAppBid") String spaceAppBid) {
        return TranscendApiResponse.success(apmSpaceAppConfigDrivenService.objectBaseInfo(spaceAppBid));
    }

    @ApiOperation("空间应用下的通用导出新增或更新模板")
    @PostMapping("/export/template/saveOrUpdate")
    public TranscendApiResponse<ApmAppExportTemplateVo> exportTemplateSaveOrUpdate(@PathVariable("spaceBid") String spaceBid,
                                                                                   @PathVariable("spaceAppBid") String spaceAppBid,
                                                                                   @RequestBody ApmAppExportTemplateDto apmAppExportTemplateDto) {
        return TranscendApiResponse.success(apmSpaceAppConfigDrivenService.exportTemplateSaveOrUpdate(apmAppExportTemplateDto));
    }

    @ApiOperation("空间应用下的通用导出删除模板")
    @PostMapping("/export/template/delete/{bid}")
    public TranscendApiResponse<Boolean> exportTemplateDelete(@PathVariable("spaceBid") String spaceBid,
                                                              @PathVariable("spaceAppBid") String spaceAppBid,
                                                              @PathVariable("bid") String bid) {
        return TranscendApiResponse.success(apmSpaceAppConfigDrivenService.exportTemplateDelete(bid));
    }

    @ApiOperation("空间应用下的通用导出查询模板")
    @PostMapping("/export/template/query")
    public TranscendApiResponse<List<ApmAppExportTemplateVo>> queryExportTemplate(@PathVariable("spaceBid") String spaceBid,
                                                                                  @PathVariable("spaceAppBid") String spaceAppBid,
                                                                                  @RequestBody ApmAppExportTemplateDto apmAppExportTemplateDto) {
        return TranscendApiResponse.success(apmSpaceAppConfigDrivenService.queryExportTemplate(apmAppExportTemplateDto));
    }

    @ApiOperation("空间应用下的通用导出")
    @PostMapping("/export")
    public void export(@PathVariable("spaceBid") String spaceBid,
                       @PathVariable("spaceAppBid") String spaceAppBid,
                       @RequestBody ApmAppExportDto apmAppExportDto,
                       HttpServletResponse response) throws IOException {
        apmSpaceAppConfigDrivenService.export(apmAppExportDto, response);
    }


    @ApiOperation("查询阶段组件")
    @PostMapping("/lifeCycleState/phase/{instanceBid}")
    public TranscendApiResponse<List<CfgLifeCycleTemplateKeyPathNodeVo>> listPhaseLifeCycleState(@PathVariable("spaceBid") String spaceBid,
                                                                                                 @PathVariable("spaceAppBid") String spaceAppBid,
                                                                                                 @PathVariable("instanceBid") String instanceBid) {
        return TranscendApiResponse.success(apmSpaceAppConfigManageService.listPhaseLifeCycleState(spaceAppBid, instanceBid));
    }


}
