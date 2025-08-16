package com.transcend.plm.datadriven.apm.space.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.api.model.attribute.vo.CfgAttributeVo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectAttributeVo;
import com.transcend.plm.configcenter.api.model.objectrelation.vo.CfgObjectRelationVo;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewDto;
import com.transcend.plm.configcenter.api.model.view.dto.ViewQueryParam;
import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.relation.qo.CrossRelationPathChainQO;
import com.transcend.plm.datadriven.apm.space.model.view.AppViewModelDto;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmSpaceAppTabDto;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmSpaceTabQo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmCrossRelationVO;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmObjectRelationAppVo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.MultiViewMetaListVo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ViewMetaListVo;
import com.transcend.plm.datadriven.apm.space.service.ApmSpaceApplicationService;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppConfigManageService;
import com.transcend.plm.datadriven.common.tool.QueryConveterTool;
import com.transcend.plm.datadriven.domain.object.relation.RelationObjectManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/9/20 17:07
 * @since 1.0
 **/
@RestController
@Api(value = "Apm SpaceAppDataController", tags = "空间-应用配置管理-控制器")
@RequestMapping(value = "/apm/space/{spaceBid}/app/{spaceAppBid}/config-manage")
public class ApmSpaceAppConfigManageController {

    @Resource
    private IApmSpaceAppConfigManageService apmSpaceAppConfigManageService;

    @Resource
    private RelationObjectManageService relationObjectManageService;

    @Resource
    private ApmSpaceApplicationService apmSpaceApplicationService;

    /**
     * 空间应用下的基础视图的对象属性查询
     */
    @ApiOperation("空间应用下的基础视图的对象属性查询")
    @PostMapping("/baseView/listAttributes")
    public TranscendApiResponse<List<CfgObjectAttributeVo>> baseViewListAttributes(@PathVariable("spaceBid") String spaceBid,
                                                                                   @PathVariable("spaceAppBid") String spaceAppBid
    ) {
        return TranscendApiResponse.success(apmSpaceAppConfigManageService.baseViewListAttributes(spaceAppBid));
    }

    /**
     * 查询以目标对象为主的关系列表
     */
    @ApiOperation("查询以目标对象为主的关系列表")
    @PostMapping("/baseView/listTargetRelation")
    public TranscendApiResponse<List<CfgObjectRelationVo>> baseViewListTargetRelation(@PathVariable("spaceBid") String spaceBid,
                                                                                      @PathVariable("spaceAppBid") String spaceAppBid
    ) {
        return TranscendApiResponse.success(apmSpaceAppConfigManageService.baseViewListTargetRelation(spaceAppBid));
    }

    @ApiOperation("空间应用下的基础视图（一个）配置 查询")
    @PostMapping("/baseView/get")
    public TranscendApiResponse<CfgViewVo> baseViewGet(@PathVariable("spaceBid") String spaceBid,
                                                       @PathVariable("spaceAppBid") String spaceAppBid) {
        return TranscendApiResponse.success(apmSpaceAppConfigManageService.baseViewGet(spaceAppBid));
    }

    @ApiOperation("空间应用下的基础视图（一个）配置 添加/覆盖保存")
    @PostMapping("/baseView/saveOrUpdate")
    public TranscendApiResponse<CfgViewVo> baseViewSaveOrUpdate(@PathVariable("spaceBid") String spaceBid,
                                                                @PathVariable("spaceAppBid") String spaceAppBid,
                                                                @RequestBody Map<String, Object> content) {
        return TranscendApiResponse.success(apmSpaceAppConfigManageService.baseViewSaveOrUpdate(spaceAppBid, content));
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
     * 空间应用下的基础视图（一个）配置 添加/覆盖保存
     * @param spaceBid
     * @param spaceAppBid
     * @param cfgViewDto
     * @return
     */
    @ApiOperation("视图新增更新")
    @PostMapping("/saveOrUpdateView")
    public TranscendApiResponse<CfgViewVo> saveOrUpdateView(@PathVariable("spaceBid") String spaceBid,
                                                            @PathVariable("spaceAppBid") String spaceAppBid,
                                                            @RequestBody CfgViewDto cfgViewDto) {
        return TranscendApiResponse.success(apmSpaceAppConfigManageService.saveOrUpdateView(cfgViewDto));
    }

    @ApiOperation("查询空间应用下的视图列表")
    @GetMapping("/listView/{belongBid}")
    public TranscendApiResponse<List<CfgViewVo>> listView(@PathVariable("spaceBid") String spaceBid,
                                                       @PathVariable("spaceAppBid") String spaceAppBid,
                                                       @PathVariable("belongBid") String belongBid) {
        return TranscendApiResponse.success(apmSpaceAppConfigManageService.listView(belongBid));
    }

    @ApiOperation("空间应用下的基础视图（一个）配置 添加/覆盖保存")
    @PostMapping("/baseView/{type}/saveOrUpdate")
    public TranscendApiResponse<CfgViewVo> baseViewSaveOrUpdate(@PathVariable("spaceBid") String spaceBid,
                                                                @PathVariable("spaceAppBid") String spaceAppBid,
                                                                @PathVariable("type") String type,
                                                                @RequestBody Map<String, Object> content) {
        return TranscendApiResponse.success(apmSpaceAppConfigManageService.baseViewSaveOrUpdate(spaceAppBid, type, content));
    }

    /**
     * 空间应用下的流程节点视图（一个）配置 查询
     */
    @ApiOperation("空间应用下的流程节点视图（一个）配置 查询")
    @GetMapping("/flowNodeView/{nodeBid}/action/{action}/get")
    @Deprecated
    public TranscendApiResponse<CfgViewVo> flowNodeViewGet(@PathVariable("spaceBid") String spaceBid,
                                                           @PathVariable("spaceAppBid") String spaceAppBid,
                                                           @PathVariable("action") String action,
                                                           @PathVariable("nodeBid") String nodeBid) {
        return TranscendApiResponse.success(apmSpaceAppConfigManageService.flowNodeViewGet(spaceAppBid, action, nodeBid));
    }

    @ApiOperation("空间应用下的流程节点视图（一个）配置 查询")
    @GetMapping("/allFlowView/{flowBid}/{version}/get")
    public TranscendApiResponse<List<Object>> getAllFlowView(@PathVariable("spaceBid") String spaceBid,
                                                                     @PathVariable("spaceAppBid") String spaceAppBid,
                                                                     @PathVariable("flowBid") String flowBid,
                                                                     @PathVariable("version") String version) {
        return TranscendApiResponse.success(apmSpaceAppConfigManageService.getAllFlowView(spaceAppBid, flowBid, version));
    }

    /**
     * 空间应用下的流程节点视图（一个）配置 添加/覆盖保存
     *
     * @param spaceBid
     * @param spaceAppBid
     * @param nodeBid
     * @param content
     * @return
     */
    @ApiOperation("空间应用下的流程节点视图（一个）配置 添加/覆盖保存")
    @PostMapping("/flowNodeView/{nodeBid}/action/{action}/saveOrUpdate")
    public TranscendApiResponse<CfgViewVo> flowNodeViewSaveOrUpdate(@PathVariable("spaceBid") String spaceBid,
                                                                    @PathVariable("spaceAppBid") String spaceAppBid,
                                                                    @PathVariable("nodeBid") String nodeBid,
                                                                    @PathVariable("action") String action,
                                                                    @RequestBody Map<String, Object> content) {
        return TranscendApiResponse.success(apmSpaceAppConfigManageService.flowNodeViewSaveOrUpdate(spaceAppBid, nodeBid, action, content));
    }

    /**
     * 查看应用视图模式列表
     *
     * @param spaceBid
     * @param spaceAppBid
     * @return
     */
    @ApiOperation("查看应用视图模式列表")
    @PostMapping("/listViewModel")
    public TranscendApiResponse<List<AppViewModelDto>> viewModelUpdatePartialContent(@PathVariable("spaceBid") String spaceBid,
                                                                                     @PathVariable("spaceAppBid") String spaceAppBid) {
        return TranscendApiResponse.success(apmSpaceAppConfigManageService.listViewModel(spaceAppBid));
    }

    @ApiOperation("空间应用下的视图模式详情")
    @GetMapping("/viewModel/{viewModelCode}/get")
    public TranscendApiResponse<AppViewModelDto> viewModelGet(@PathVariable("spaceBid") String spaceBid,
                                                              @PathVariable("viewModelCode") String viewModelCode,
                                                              @PathVariable("spaceAppBid") String spaceAppBid) {
        return TranscendApiResponse.success(apmSpaceAppConfigManageService.getViewModelDetail(spaceAppBid, viewModelCode));
    }

    /**
     * 空间应用下的 某个视图模式（启用禁用）
     *
     * @param spaceBid
     * @param spaceAppBid
     * @param viewModelCode
     * @param enableFlag
     * @return
     */
    @ApiOperation("空间应用下的 某个视图模式（启用禁用）")
    @PostMapping("/viewModel/{viewModelCode}/changeEnableFlag/{enableFlag}")
    public TranscendApiResponse<Boolean> viewModelChangeEnableFlag(@PathVariable("spaceBid") String spaceBid,
                                                                   @PathVariable("spaceAppBid") String spaceAppBid,
                                                                   @PathVariable("viewModelCode") String viewModelCode,
                                                                   @PathVariable(name = "enableFlag") Integer enableFlag) {
        return TranscendApiResponse.success(apmSpaceAppConfigManageService.viewModelChangeEnableFlag(spaceAppBid, viewModelCode, enableFlag));
    }


    /**
     * 空间应用下的 某个视图模式 更新配置内容
     *
     * @param spaceBid
     * @param spaceAppBid
     * @param viewModelCode
     * @param configContent
     * @return
     */
    @ApiOperation("空间应用下的 某个视图模式 更新配置内容")
    @PostMapping("/viewModel/{viewModelCode}/updatePartialContent")
    public TranscendApiResponse<Boolean> viewModelUpdatePartialContent(@PathVariable("spaceBid") String spaceBid,
                                                                       @PathVariable("spaceAppBid") String spaceAppBid,
                                                                       @PathVariable("viewModelCode") String viewModelCode,
                                                                       @RequestBody Map<String, Object> configContent
    ) {
        return TranscendApiResponse.success(apmSpaceAppConfigManageService.viewModelUpdatePartialContent(spaceAppBid, viewModelCode, configContent));
    }

    /**
     * 空间应用下的 某个视图模式 更新配置内容
     *
     * @param spaceBid
     * @param spaceAppBid
     * @param viewModelCode
     * @param appViewModelDto
     * @return
     */
    @ApiOperation("空间应用下的 某个视图模式 更新配置内容")
    @PostMapping("/viewModel/{viewModelCode}/update")
    public TranscendApiResponse<Boolean> viewModelUpdate(@PathVariable("spaceBid") String spaceBid,
                                                         @PathVariable("spaceAppBid") String spaceAppBid,
                                                         @PathVariable("viewModelCode") String viewModelCode,
                                                         @RequestBody AppViewModelDto appViewModelDto
    ) {
        return TranscendApiResponse.success(apmSpaceAppConfigManageService.viewModelUpdate(spaceAppBid, viewModelCode, appViewModelDto));
    }

    /**
     * 批量存储视图模式信息
     *
     * @param spaceBid
     * @param spaceAppBid
     * @param appViewModelDtos
     * @return
     */
    @ApiOperation("批量存储视图模式信息")
    @PostMapping("/batchSaveViewModel")
    public TranscendApiResponse<Boolean> viewModelUpdatePartialContent(@PathVariable("spaceBid") String spaceBid,
                                                                       @PathVariable("spaceAppBid") String spaceAppBid,
                                                                       @RequestBody List<AppViewModelDto> appViewModelDtos
    ) {
        return TranscendApiResponse.success(apmSpaceAppConfigManageService.batchSaveViewModel(spaceAppBid, appViewModelDtos));
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
                                                                                 @PathVariable("spaceAppBid") String spaceAppBid,
                                                                                 @RequestBody ApmSpaceTabQo apmSpaceTabQo){
        return TranscendApiResponse.success(apmSpaceAppConfigManageService.relationAppListTab(apmSpaceTabQo.setSpaceAppBid(spaceAppBid)));
    }

    /**
     * 保存空间应用下的获取应用关系Tab配置
     *
     * @param spaceBid
     * @param spaceAppBid
     * @param apmSpaceAppTabDto
     * @return
     */
    @ApiOperation("保存空间应用下的获取应用关系Tab配置")
    @PostMapping("/relation/app/saveRelationTab")
    public TranscendApiResponse<Boolean> saveApmSpaceAppTab(@PathVariable("spaceBid") String spaceBid,
                                                            @PathVariable("spaceAppBid") String spaceAppBid, @RequestBody ApmSpaceAppTabDto apmSpaceAppTabDto) {
        return TranscendApiResponse.success(apmSpaceAppConfigManageService.saveApmSpaceAppTab(spaceBid, spaceAppBid, apmSpaceAppTabDto));
    }

    /**
     * 保存空间应用下的获取应用关系Tab配置
     *
     * @param sourceModelCode
     * @return
     */
    @ApiOperation("以源和目标对象查看跨层级关系列表")
    @GetMapping("/relation/app/listCrossRelationByModelCode/sourceModelCode/{sourceModelCode}")
    public TranscendApiResponse<List<ApmCrossRelationVO>> listCrossRelationByModelCode(@PathVariable("spaceBid") String spaceBid,
                                                                                       @PathVariable("spaceAppBid") String spaceAppBid,
                                                                                       @PathVariable("sourceModelCode") String sourceModelCode) {
        return TranscendApiResponse.success(apmSpaceAppConfigManageService.listCrossRelationByModelCode(spaceBid, spaceAppBid, sourceModelCode));
    }

    /**
     * 下载应用的导入模板
     */
    @ApiOperation("下载应用的导入模板")
    @GetMapping("/downloadImportTemplate/{type}")
    public void downloadImportTemplate(@PathVariable("spaceBid") String spaceBid,
                                       @PathVariable("spaceAppBid") String spaceAppBid,
                                       @PathVariable("type") String type, HttpServletResponse response) {
        apmSpaceAppConfigManageService.downloadImportTemplate(spaceAppBid,type,response);
    }

    /**
     * 跨层级查询源对象的实例集合 强浮动以bid作为关联
     */
    @ApiOperation("跨层级查询源对象的实例集合 强浮动以bid作为关联")
    @PostMapping("/pageCrossRelationInstance")
    public TranscendApiResponse<PagedResult<MObject>> pageCrossRelationInstance(@PathVariable("spaceBid") String spaceBid,
                                                                                @PathVariable("spaceAppBid") String spaceAppBid,
                                                                                @RequestBody @Validated CrossRelationPathChainQO qo) {
        qo.setSpaceBid(spaceBid);
        qo.setCurrentSourceModelCode(apmSpaceApplicationService.getModelCodeBySpaceAppBid(spaceAppBid));
        return TranscendApiResponse.success(
                relationObjectManageService.pageCrossRelationInstance(
                        qo, QueryConveterTool.convert(qo.getPageQo())
                ));
    }

    @ApiOperation("空间应用视图 获取单个属性的配置")
    @GetMapping("/getAttributeConfigByCode/{code}")
    public TranscendApiResponse<CfgAttributeVo> getAttributeConfigByCode(@PathVariable("spaceBid") String spaceBid,
                                                                         @PathVariable("spaceAppBid") String spaceAppBid,
                                                                         @PathVariable String code) {
        return TranscendApiResponse.success(apmSpaceAppConfigManageService.getAttributeConfigByCode(code));
    }

    @ApiOperation("空间应用 tab管理 通过应用id 查询关系列表")
    @GetMapping("/listRelationByModelCode")
    public TranscendApiResponse<List<CfgObjectRelationVo>> listRelationByModelCode(@PathVariable("spaceBid") String spaceBid,
                                                                                   @PathVariable("spaceAppBid") String spaceAppBid) {
        return TranscendApiResponse.success(apmSpaceAppConfigManageService.listRelationByModelCode(spaceBid, spaceAppBid));
    }

    @ApiOperation("获取视图的元数据列表")
    @GetMapping("/view/metaList")
    public TranscendApiResponse<MultiViewMetaListVo> getViewMetaList(
            @PathVariable("spaceBid") String spaceBid,
            @PathVariable("spaceAppBid") String spaceAppBid,
            @RequestParam(value = "currentSpaceBid", required = false) String currentSpaceBid,
            @RequestParam(value = "viewModelType", required = false) String viewModelType) {
        return TranscendApiResponse.success(
                apmSpaceAppConfigManageService.getViewMetaList(spaceBid, spaceAppBid, currentSpaceBid, viewModelType)
        );
    }

    @ApiOperation("获取视图的元数据列表")
    @GetMapping("/view/listViewMetaList")
    public TranscendApiResponse<List<ViewMetaListVo>> listViewMetaList(
            @PathVariable("spaceBid") String spaceBid,
            @PathVariable("spaceAppBid") String spaceAppBid,
            @RequestParam(value = "currentSpaceBid", required = false) String currentSpaceBid,
            @RequestParam(value = "viewModelType", required = false) String viewModelType) {
        return TranscendApiResponse.success(
                apmSpaceAppConfigManageService.listViewMetaList(spaceBid, spaceAppBid, currentSpaceBid, viewModelType)
        );
    }
}
