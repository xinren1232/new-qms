package com.transcend.plm.datadriven.apm.space.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.api.model.dto.LifeCyclePromoteDto;
import com.transcend.plm.datadriven.api.model.dto.MoveGroupNodeParam;
import com.transcend.plm.datadriven.api.model.dto.MoveTreeNodeParam;
import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transcend.plm.datadriven.api.model.qo.ResourceQo;
import com.transcend.plm.datadriven.apm.dto.DeleteRelDto;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.pojo.dto.MSpaceAppBatchUpdateDataByBidsDto;
import com.transcend.plm.datadriven.apm.space.pojo.dto.MultiObjectUpdateDto;
import com.transcend.plm.datadriven.apm.space.pojo.qo.*;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmLaneGroupData;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ResourceVo;
import com.transcend.plm.datadriven.apm.space.service.IAppDataService;
import com.transcend.plm.datadriven.apm.space.service.IBaseApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.apm.task.service.ApmTaskApplicationService;
import com.transcend.plm.datadriven.apm.tools.ParentBidHandler;
import com.transcend.plm.datadriven.common.tool.QueryConveterTool;
import com.transsion.framework.dto.BaseRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/9/20 17:07
 * @since 1.0
 **/
@RestController
@Api(value = "Apm SpaceAppDataController", tags = "空间-应用数据驱动-控制器")
@RequestMapping(value = "/apm/space/{spaceBid}/app/{spaceAppBid}/data-driven")
public class ApmSpaceAppDataDrivenController {

    @Resource
    private IBaseApmSpaceAppDataDrivenService iBaseApmSpaceAppDataDrivenService;

    @Resource
    private ApmTaskApplicationService apmTaskApplicationService;

    @Resource
    private IAppDataService appDataService;

    /**
     * 需要默认带出流程的应用bid
     */
    @Value("${transcend.alm.setDefaultFlow.spaceAppBids:1197585980276293632,1195047614899425280}")
    private String setDefaultFlowSpaceAppBids;

    @ApiOperation("新增")
    @PostMapping("/add")
    public TranscendApiResponse<MSpaceAppData> add(
            @PathVariable("spaceBid") String spaceBid,
            @PathVariable("spaceAppBid") String spaceAppBid,
            @RequestBody MSpaceAppData mSpaceAppData) {
        if (Objects.nonNull(mSpaceAppData)) {
            mSpaceAppData.remove(BaseDataEnum.ID.getCode());
        }
        mSpaceAppData.setSyanCheckFlow(true);
        MSpaceAppData resMspaceAppData =iBaseApmSpaceAppDataDrivenService.add(spaceAppBid, mSpaceAppData);
        /*if(resMspaceAppData.get(SpaceAppDataEnum.WORK_ITEM_TYPE.getCode()) != null){
            //检查流程是否开始
            CompletableFuture.runAsync(() -> runtimeService.runStartNode(resMspaceAppData.getBid(),spaceBid,spaceAppBid), SimpleThreadPool.getInstance());
            //重新查询实例
            //resMspaceAppData = iBaseApmSpaceAppDataDrivenService.get(spaceAppBid, resMspaceAppData.getBid(),false);
        }*/
        return TranscendApiResponse.success(resMspaceAppData);
    }

    @ApiOperation("是否默认设置流程")
    @GetMapping("/isSetDefaultFlow")
    public TranscendApiResponse<Boolean> isSetDefaultFlow(
            @PathVariable("spaceBid") String spaceBid,
            @PathVariable("spaceAppBid") String spaceAppBid) {
        return TranscendApiResponse.success(isSetDefaultFlow(spaceAppBid));
    }

    private boolean isSetDefaultFlow(String spaceAppBid) {
        if(StringUtils.isNotEmpty(setDefaultFlowSpaceAppBids)){
            List<String> list = Arrays.asList(setDefaultFlowSpaceAppBids.split(","));
            if(list.contains(spaceAppBid)){
                return true;
            }else {
                return false;
            }
        }
        return false;
    }

    @ApiOperation("获取详情")
    @GetMapping("/get/{bid}")
    public TranscendApiResponse<MSpaceAppData> get(@PathVariable("spaceBid") String spaceBid,
                                                   @PathVariable("spaceAppBid") String spaceAppBid,
                                                   @PathVariable("bid") String bid) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.get(spaceAppBid, bid,true));
    }

    @ApiOperation("获取详情")
    @GetMapping("/getDetail/{bid}")
    public TranscendApiResponse<MSpaceAppData> getDetail(@PathVariable("spaceBid") String spaceBid,
                                                   @PathVariable("spaceAppBid") String spaceAppBid,
                                                   @PathVariable("bid") String bid) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.get(spaceAppBid, bid,false));
    }


    @ApiOperation("修改部分字段")
    @PostMapping("/update/{bid}/property")
    public TranscendApiResponse<Boolean> updatePartialContent(
            @PathVariable("spaceBid") String spaceBid,
            @PathVariable("spaceAppBid") String spaceAppBid,
            @PathVariable("bid") String bid,
            @RequestBody MSpaceAppData mSpaceAppData) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.updatePartialContent(spaceAppBid, bid, mSpaceAppData)
        );
    }


    @ApiOperation("批量修改部分字段(不需要权限，再data中补充)")
    @PostMapping("/batch-update/property")
    public TranscendApiResponse<Boolean> batchUpdatePartialContent(
            @PathVariable("spaceBid") String spaceBid,
            @PathVariable("spaceAppBid") String spaceAppBid,
            @RequestBody MSpaceAppBatchUpdateDataByBidsDto spaceAppBatchUpdateDataByBidsDto) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.batchUpdatePartialContentByBids(spaceBid, spaceAppBid,spaceAppBatchUpdateDataByBidsDto.getBids(), spaceAppBatchUpdateDataByBidsDto.getData())
        );
    }

    @ApiOperation("批量修改权限校验")
    @PostMapping("/batch-operation/checkPermission/{operation}")
    public TranscendApiResponse<Boolean> batchUpdateCheckPermission(
            @PathVariable("spaceBid") String spaceBid,
            @PathVariable("spaceAppBid") String spaceAppBid,
            @PathVariable("operation") String operation,
            @RequestBody  List<String> bids) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.batchOperationCheckPermission(spaceBid, spaceAppBid, bids, operation)
        );
    }

    @ApiOperation("多对象批量编辑")
    @PostMapping("/multiObject/property")
    public TranscendApiResponse<Boolean> multiObjectPartialContent(
            @PathVariable("spaceBid") String spaceBid,
            @PathVariable("spaceAppBid") String spaceAppBid,
            @RequestBody List<MultiObjectUpdateDto> multiObjectUpdateDtoList) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.multiObjectPartialContent(spaceAppBid, multiObjectUpdateDtoList)
        );
    }

    @ApiOperation("逻辑删除")
    @PostMapping("/logicDelete/{bid}")
    public TranscendApiResponse<Boolean> logicDelete(@PathVariable("spaceBid") String spaceBid,
                                                             @PathVariable("spaceAppBid") String spaceAppBid,
                                                             @ApiParam("bid") @PathVariable("bid") String bid
    ) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.logicalDelete(spaceAppBid, bid));
    }


    @ApiOperation("逻辑删除 - 带操作应用Bid")
    @PostMapping("/logicDelete/{bid}/{operateAppBid}")
    public TranscendApiResponse<Boolean> logicDeleteWithBelongAppBid(
            @PathVariable("spaceBid") String spaceBid,
            @PathVariable("spaceAppBid") String spaceAppBid,
            @ApiParam("bid") @PathVariable("bid") String bid,
            @ApiParam("operateAppBid") @PathVariable("operateAppBid") String operateAppBid
    ) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.logicDeleteWithOperateAppBid(spaceAppBid, bid, operateAppBid));
    }

    @ApiOperation("批量逻辑删除")
    @PostMapping("/batchLogicalDelete")
    public TranscendApiResponse<Boolean> batchLogicalDeleteByBids(
            @PathVariable("spaceBid") String spaceBid,
            @PathVariable("spaceAppBid") String spaceAppBid,
            @RequestBody BatchLogicDelAndRemQo qo
    ) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.batchLogicalDelete(spaceAppBid, qo));
    }

    @ApiOperation("批量移除 - 多对象树")
    @PostMapping("/batchRemove")
    public TranscendApiResponse<Boolean> batchRemove(
            @PathVariable("spaceBid") String spaceBid,
            @PathVariable("spaceAppBid") String spaceAppBid,
            @RequestBody BatchLogicDelAndRemQo qo
    ) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.batchRemove(spaceAppBid, qo));
    }

    @ApiOperation("批量移除 - 多对象树")
    @PostMapping("/multi-tree/batchRemove")
    public TranscendApiResponse<Boolean> batchRemoveMultiTree(
            @PathVariable("spaceBid") String spaceBid,
            @PathVariable("spaceAppBid") String spaceAppBid,
            @RequestBody BatchLogicDelAndRemQo qo
    ) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.batchRemove(spaceAppBid, qo));
    }

    @ApiOperation("批量移除-中间关系")
    @PostMapping("/batchRemoveRelation")
    public TranscendApiResponse<Boolean> batchRemoveRelation(
            @PathVariable("spaceBid") String spaceBid,
            @PathVariable("spaceAppBid") String spaceAppBid,
            @RequestBody BatchRemoveRelationQo qo
    ) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.batchRemoveRelation(spaceAppBid, qo));
    }

    @ApiOperation("检查是否有新增权限")
    @PostMapping("/validAddPermission")
    public TranscendApiResponse<Boolean> validAddPermission(
            @PathVariable("spaceBid") String spaceBid, @PathVariable("spaceAppBid") String spaceAppBid, @RequestBody Map<String,String> appData) {
        return TranscendApiResponse.success(appDataService.validAddPermission(spaceBid, spaceAppBid, appData));
    }



    @ApiOperation("批量逻辑删除")
    @PostMapping("/batchLogicalDeleteByBids")
    public TranscendApiResponse<Boolean> batchLogicalDeleteByBids(@PathVariable("spaceBid") String spaceBid,
                                                                  @PathVariable("spaceAppBid") String spaceAppBid,
                                                                  @RequestBody List<String> bids
    ) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.batchLogicalDeleteByBids(spaceAppBid, bids));
    }



    @ApiOperation("批量逻辑删除")
    @PostMapping("/{viewModel}/batchLogicalDeleteByBids")
    public TranscendApiResponse<Boolean> batchLogicalDeleteByBids(@PathVariable("spaceBid") String spaceBid,
                                                                  @PathVariable("spaceAppBid") String spaceAppBid,
                                                                  @PathVariable("viewModel") String viewModel,
                                                                  @RequestBody List<String> bids
    ) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.batchViewModelLogicalDeleteByBids(viewModel, spaceAppBid, bids));
    }

    @ApiOperation("批量逻辑删除")
    @PostMapping("/deleteRel")
    public TranscendApiResponse<Boolean> deleteRel(@PathVariable("spaceBid") String spaceBid,
                                                                  @PathVariable("spaceAppBid") String spaceAppBid,
                                                                  @RequestBody DeleteRelDto deleteRelDto
    ) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.deleteRel(spaceAppBid,deleteRelDto));
    }

    @ApiOperation("分页列表")
    @PostMapping("/page")
    public TranscendApiResponse<PagedResult<MSpaceAppData>> page(@PathVariable("spaceBid") String spaceBid,
                                                                 @PathVariable("spaceAppBid") String spaceAppBid,
                                                                 @RequestBody BaseRequest<ModelMixQo> pageQo) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.page(spaceAppBid, QueryConveterTool.convertFitterNullValue(pageQo), true));
    }

    @ApiOperation("分页列表")
    @PostMapping("/pageWithoutPermission")
    public TranscendApiResponse<PagedResult<MSpaceAppData>> pageWithoutPermission(@PathVariable("spaceBid") String spaceBid,
                                                                 @PathVariable("spaceAppBid") String spaceAppBid,
                                                                 @RequestBody BaseRequest<ModelMixQo> pageQo) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.pageWithoutPermission(spaceAppBid, QueryConveterTool.convertFitterNullValue(pageQo), true));
    }

    @ApiOperation("泳道列表")
    @PostMapping("/lane/list")
    public TranscendApiResponse<ApmLaneGroupData> pageLane(@PathVariable("spaceBid") String spaceBid,
                                                                        @PathVariable("spaceAppBid") String spaceAppBid,
                                                                        @RequestBody ApmLaneQO apmLaneQO) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.listLane(spaceBid,spaceAppBid, apmLaneQO));
    }

    @ApiOperation("泳道列表-查询全部")
    @PostMapping("/lane/list/all")
    public TranscendApiResponse<ApmLaneGroupData> listAllLane(@PathVariable("spaceBid") String spaceBid,
                                                                    @PathVariable("spaceAppBid") String spaceAppBid,
                                                                    @RequestBody ApmLaneAllQo laneAllQo) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.listAllLane(spaceBid,spaceAppBid,laneAllQo));
    }

    @ApiOperation("列表")
    @PostMapping("/list")
    @Deprecated
    public TranscendApiResponse<List<MSpaceAppData>> list(@PathVariable("spaceBid") String spaceBid,
                                                          @PathVariable("spaceAppBid") String spaceAppBid,
                                                          @RequestBody ModelMixQo modelMixQo) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.list(spaceAppBid, QueryConveterTool.convert(modelMixQo)));
    }

    @ApiOperation("去重属性列表")
    @PostMapping("/listPropertyDistinct/{distinctProperty}")
    public TranscendApiResponse<List<Object>> listPropertyDistinct(@PathVariable("spaceBid") String spaceBid,
                                                          @PathVariable("spaceAppBid") String spaceAppBid,
                                                          @PathVariable("distinctProperty") String distinctProperty,
                                                          @RequestBody ModelMixQo modelMixQo) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.listPropertyDistinct(spaceAppBid, distinctProperty, QueryConveterTool.convert(modelMixQo)));
    }

    /**
     *  未来支持单个属性分组的排序存储，目前暂时只用sort排序
     * @param spaceBid
     * @param spaceAppBid
     * @param groupProperty
     * @param modelMixQo
     * @return
     */
    @ApiOperation("分组列表接口")
    @PostMapping("/groupList/{groupProperty}")
    public TranscendApiResponse<GroupListResult<MSpaceAppData>> groupList(@PathVariable("spaceBid") String spaceBid,
                                                                          @PathVariable("spaceAppBid") String spaceAppBid,
                                                                          @PathVariable("groupProperty") String groupProperty,
                                                                          @RequestBody ModelMixQo modelMixQo) {
        return TranscendApiResponse.success(
                iBaseApmSpaceAppDataDrivenService.groupList(
                        spaceAppBid,
                        groupProperty,
                        QueryConveterTool.convert(modelMixQo))
        );
    }

    /**
     * 批量更新分组移动接口（包括排序） TODO 未来支持单个属性分组的排序存储，目前暂时只用sort排序
     * @param spaceBid
     * @param spaceAppBid
     * @param groupProperty
     * @param moveGroupNodeParams
     * @return
     */
    @ApiOperation("批量更新分组移动接口（包括排序）")
    @PostMapping("/moveGroupNode/{groupProperty}")
    public TranscendApiResponse<Boolean> moveGroupNode(@PathVariable("spaceBid") String spaceBid,
                                                      @PathVariable("spaceAppBid") String spaceAppBid,
                                                      @PathVariable("groupProperty") String groupProperty,
                                                      @RequestBody List<MoveGroupNodeParam> moveGroupNodeParams) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.moveGroupNode(spaceAppBid, groupProperty, moveGroupNodeParams));
    }

    @ApiOperation("树接口")
    @PostMapping("/tree")
    public TranscendApiResponse<List<MSpaceAppData>> tree(@PathVariable("spaceBid") String spaceBid,
                                                                     @PathVariable("spaceAppBid") String spaceAppBid,
                                                                     @RequestBody ModelMixQo modelMixQo) {
        return TranscendApiResponse.success(ParentBidHandler.handleMSpaceAppData(iBaseApmSpaceAppDataDrivenService.signObjectAndMultiSpaceTree(spaceBid,spaceAppBid, modelMixQo, true)));
    }

    @ApiOperation("项目下人力资源统计")
    @PostMapping("/getProjectResources")
    public TranscendApiResponse<List<ResourceVo>> getProjectResources(@PathVariable("spaceBid") String spaceBid,
                                                                      @PathVariable("spaceAppBid") String spaceAppBid,
                                                                      @RequestBody ResourceQo resourceQo) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.getProjectResources(resourceQo));
    }

    @ApiOperation("空间下人力资源统计")
    @PostMapping("/getSpaceResources")
    public TranscendApiResponse<List<ResourceVo>> getSpaceResources(@PathVariable("spaceBid") String spaceBid,
                                                                    @PathVariable("spaceAppBid") String spaceAppBid,
                                                                    @RequestBody ResourceQo resourceQo) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.getSpaceResources(spaceBid, resourceQo));
    }

    @ApiOperation("多对象树接口")
    @PostMapping("/multiTree")
    public TranscendApiResponse<List<MObjectTree>> multiTree(@PathVariable("spaceBid") String spaceBid,
                                                             @PathVariable("spaceAppBid") String spaceAppBid,
                                                             @RequestBody(required = false) ApmMultiTreeModelMixQo modelMixQo) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.multiTree(spaceBid, spaceAppBid,
                modelMixQo, true, true));
    }

    @ApiOperation("多对象树接口")
    @PostMapping("/multiTreeWithoutPermission")
    public TranscendApiResponse<List<MObjectTree>> multiTreeWithoutPermission(@PathVariable("spaceBid") String spaceBid,
                                                                              @PathVariable("spaceAppBid") String spaceAppBid,
                                                                              @RequestBody(required = false) ApmMultiTreeModelMixQo modelMixQo) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.multiTree(spaceBid, spaceAppBid, modelMixQo,
                true, false));
    }

    @ApiOperation("多对象树分组接口")
    @PostMapping("/multiTreeKb/{groupProperty}")
    public TranscendApiResponse<GroupListResult<MSpaceAppData>> multiTreeKb(@PathVariable("spaceBid") String spaceBid,
                                                                          @PathVariable("spaceAppBid") String spaceAppBid,
                                                                          @PathVariable("groupProperty") String groupProperty,
                                                                          @RequestBody ModelMixQo modelMixQo) {
        List<QueryWrapper> wrapperList;
        if (Objects.isNull(modelMixQo)) {
            wrapperList = QueryConveterTool.convert(Lists.newArrayList(), null);
        } else {
            wrapperList = QueryConveterTool.convert(modelMixQo.getQueries(), modelMixQo.getAnyMatch());
        }
        wrapperList.add(new QueryWrapper(Boolean.TRUE).setSqlRelation(" and "));
        List<Order> orders = modelMixQo.getOrders();
        // 史诗-特性-用户故事-子任务
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.multiTreeGroupBy(spaceBid, spaceAppBid, groupProperty, wrapperList,orders));

    }
    @ApiOperation("多对象树复制接口")
    @PostMapping("/{copyInstanceBid}/copyMultiTree")
    public TranscendApiResponse<Boolean> copyMultiTree(@PathVariable("spaceBid") String spaceBid,
                                                             @PathVariable("spaceAppBid") String spaceAppBid,
                                                             @PathVariable("copyInstanceBid") String copyInstanceBid,
                                                             @RequestBody MObjectTree mObjectTree) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.copyMultiTree(mObjectTree,copyInstanceBid,spaceBid,spaceAppBid));
    }
    @ApiOperation("多对象树接口V2")
    @PostMapping("/multiTree/V2")
    public TranscendApiResponse<List<MObjectTree>> multiTreeV2(@PathVariable("spaceBid") String spaceBid,
                                                               @PathVariable("spaceAppBid") String spaceAppBid,
                                                               @RequestBody(required = false) ApmMultiTreeModelMixQo modelMixQo) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.multiTree(spaceBid, spaceAppBid, modelMixQo,
                true, false));
    }

    @ApiOperation("批量更新树移动接口（包括排序，拖动父bid）")
    @PostMapping("/moveTreeNode")
    public TranscendApiResponse<Boolean> moveTreeNode(@PathVariable("spaceBid") String spaceBid,
                                                      @PathVariable("spaceAppBid") String spaceAppBid,
                                                      @RequestBody List<MoveTreeNodeParam> moveTreeNodeParams) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.moveTreeNode(spaceAppBid, moveTreeNodeParams));
    }
    @ApiOperation("批量更新树移动接口（包括排序，拖动父bid）")
    @PostMapping("/moveTreeNodeByModelCode/{modelCode}")
    public TranscendApiResponse<Boolean> moveTreeNodeByModelCode(@PathVariable("spaceBid") String spaceBid,
                                                                 @PathVariable("spaceAppBid") String spaceAppBid,
                                                                 @RequestBody List<MoveTreeNodeParam> moveTreeNodeParams, @PathVariable("modelCode") String modelCode) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.moveTreeNodeByModelCode(spaceBid,modelCode, moveTreeNodeParams));
    }

    @PostMapping(value = "/update/{bid}/completeFlowNode/{nodeBid}")
    @ApiOperation("完成节点任务+更新部分数据")
    public TranscendApiResponse<Boolean> updatePartialContentAndCompleteFlowNode(@PathVariable("spaceBid") String spaceBid,
                                            @PathVariable("spaceAppBid") String spaceAppBid,
                                            @PathVariable("bid") String bid,
                                            @PathVariable("nodeBid") String nodeBid,
                                            @RequestBody MSpaceAppData mSpaceAppData){
        mSpaceAppData.put("specialCount", apmTaskApplicationService.checkNeedDealTask(nodeBid, SsoHelper.getJobNumber()));
        boolean res = iBaseApmSpaceAppDataDrivenService.updatePartialContentAndCompleteFlowNode(spaceAppBid, bid, nodeBid, mSpaceAppData);
        //强制更新节点状态
        apmTaskApplicationService.updateFlowNodeStatus(spaceAppBid, bid, nodeBid, mSpaceAppData);
        return TranscendApiResponse.success(
                res
        );
    }

    @PostMapping(value = "/update/{bid}/rollbackFlowNode/{nodeBid}")
    @ApiOperation("回滚节点任务+更新部分数据")
    public TranscendApiResponse<Boolean> updatePartialContentAndRollbackFlowNode(@PathVariable("spaceBid") String spaceBid,
                                                                                 @PathVariable("spaceAppBid") String spaceAppBid,
                                                                                 @PathVariable("bid") String bid,
                                                                                 @PathVariable String nodeBid,
                                                                                 @RequestBody MSpaceAppData mSpaceAppData){
        return TranscendApiResponse.success(
                iBaseApmSpaceAppDataDrivenService.updatePartialContentAndRollbackFlowNode(spaceAppBid, bid, nodeBid, mSpaceAppData,true)
        );
    }

    @PostMapping(value = "/operation/{instanceBid}/{actionBid}")
    @ApiOperation("执行操作方法")
    public TranscendApiResponse<MObject> executeAction(@PathVariable("spaceBid") String spaceBid,
                                                       @PathVariable("spaceAppBid") String spaceAppBid,
                                                       @PathVariable("instanceBid") String instanceBid,
                                                       @PathVariable("actionBid") String actionBid,
                                                       @RequestBody MSpaceAppData mSpaceAppData){
        return TranscendApiResponse.success(
                iBaseApmSpaceAppDataDrivenService.executeAction(spaceAppBid, instanceBid, actionBid, mSpaceAppData)
        );
    }

    /**
     * 导入excel
     */
    @PostMapping("/importExcel")
    @ApiOperation("导入excel")
    public TranscendApiResponse<List<MSpaceAppData>> importExcel(@PathVariable("spaceBid") String spaceBid,
                                                                 @PathVariable("spaceAppBid") String spaceAppBid,
                                                                 @ApiParam("导入数据")MultipartFile file) {
        return TranscendApiResponse.success(
                iBaseApmSpaceAppDataDrivenService.importExcel(spaceAppBid, file, spaceBid)
        );
    }

    @PostMapping("/importExcelAndSave")
    @ApiOperation("导入excel")
    public TranscendApiResponse<Boolean> importExcelAndSave(@PathVariable("spaceBid") String spaceBid,
                                                                 @PathVariable("spaceAppBid") String spaceAppBid,
                                                                 @ApiParam("导入数据")MultipartFile file) {
        return TranscendApiResponse.success(
                iBaseApmSpaceAppDataDrivenService.importExcelAndSave(spaceAppBid, file, spaceBid)
        );
    }


    @ApiOperation("检出")
    @PostMapping("/checkout/{bid}")
    public TranscendApiResponse<MSpaceAppData> checkOut(@PathVariable("spaceBid") String spaceBid,
                                                         @PathVariable("spaceAppBid") String spaceAppBid,
                                                         @ApiParam("bid") @PathVariable("bid") String bid) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.checkOut(spaceAppBid, bid));
    }

    @ApiOperation("批量检出")
    @PostMapping("/batchCheckout")
    public TranscendApiResponse<Map<String, MSpaceAppData>> batchCheckout(@PathVariable("spaceBid") String spaceBid,
                                                                                @PathVariable("spaceAppBid") String spaceAppBid,
                                                                                @ApiParam("bids") @RequestBody List<String> bids) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.batchCheckOut(spaceAppBid, bids));
    }

    @ApiOperation("取消检出")
    @PostMapping("/cancelCheckout/{bid}")
    public TranscendApiResponse<MSpaceAppData> cancelCheckOut(@PathVariable("spaceBid") String spaceBid,
                                                               @PathVariable("spaceAppBid") String spaceAppBid,
                                                               @ApiParam("bid") @PathVariable String bid) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.cancelCheckOut(spaceAppBid, bid));
    }

    @ApiOperation("批量取消检出")
    @PostMapping("/batchCancelCheckout")
    public TranscendApiResponse<Map<String, MSpaceAppData>> batchCancelCheckout(@PathVariable("spaceBid") String spaceBid,
                                                                   @PathVariable("spaceAppBid") String spaceAppBid,
                                                                   @ApiParam("bids") @RequestBody List<String> bids) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.batchCancelCheckOut(spaceAppBid, bids));
    }

    @ApiOperation("检入")
    @PostMapping("/checkin")
    public TranscendApiResponse<MSpaceAppData> checkIn(@PathVariable("spaceBid") String spaceBid,
                                                        @PathVariable("spaceAppBid") String spaceAppBid,
                                                        @ApiParam("实例数据") @RequestBody MVersionObject mObject) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.checkIn(spaceAppBid, mObject));
    }

    @ApiOperation("批量检入")
    @PostMapping("/batchCheckin")
    public TranscendApiResponse<List<MSpaceAppData>> batchCheckin(@PathVariable("spaceBid") String spaceBid,
                                                       @PathVariable("spaceAppBid") String spaceAppBid,
                                                       @ApiParam("实例数据列表") @RequestBody List<MVersionObject> list) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.batchCheckin(spaceAppBid, list));
    }

    @ApiOperation("批量检入异步")
    @PostMapping("/batchCheckinAsync")
    public TranscendApiResponse<String> batchCheckinAsync(@PathVariable("spaceBid") String spaceBid,
                                                                  @PathVariable("spaceAppBid") String spaceAppBid,
                                                                  @ApiParam("实例数据列表") @RequestBody List<MVersionObject> list) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.batchCheckinAsync(spaceAppBid, list));
    }

    @ApiOperation("查询批量异步检入进度")
    @GetMapping("/batchCheckinAsyncProgress/{resultId}")
    public TranscendApiResponse<Map<String, Integer>> batchCheckinAsyncProgress(@PathVariable("spaceBid") String spaceBid,
                                                                  @PathVariable("spaceAppBid") String spaceAppBid,
                                                                  @PathVariable("resultId") String resultId) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.batchCheckinAsyncProgress(spaceAppBid, resultId));
    }

    @ApiOperation("暂存")
    @PostMapping("/saveDraft")
    public TranscendApiResponse<Boolean> saveDraft(@PathVariable("spaceBid") String spaceBid,
                                                   @PathVariable("spaceAppBid") String spaceAppBid,
                                                   @ApiParam("草稿数据") @RequestBody MVersionObject mVersionObject) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.saveDraft(spaceAppBid, mVersionObject));
    }

    @ApiOperation("批量暂存")
    @PostMapping("/batchSaveDraft")
    public TranscendApiResponse<Boolean> batchSaveDraft(@PathVariable("spaceBid") String spaceBid,
                                                   @PathVariable("spaceAppBid") String spaceAppBid,
                                                   @ApiParam("草稿数据") @RequestBody List<MVersionObject> draftList) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.batchSaveDraft(spaceAppBid, draftList));
    }

    @ApiOperation("列表查询历史版本")
    @PostMapping("/listByHistory/{dataBid}")
    public TranscendApiResponse<List<MSpaceAppData>> listByHistory(@PathVariable("spaceBid") String spaceBid,
                                                                   @PathVariable("spaceAppBid") String spaceAppBid,
                                                                   @PathVariable("dataBid") String dataBid,
                                                                   @RequestBody ModelMixQo modelMixQo) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.listByHistory(spaceAppBid, dataBid, QueryConveterTool.convert(modelMixQo)));
    }

    @ApiOperation("分页查询历史版本")
    @PostMapping("/pageListByHistory/{dataBid}")
    public TranscendApiResponse<PagedResult<MSpaceAppData>> pageListByHistory(@PathVariable("spaceBid") String spaceBid,
                                                                       @PathVariable("spaceAppBid") String spaceAppBid,
                                                                       @PathVariable("dataBid") String dataBid,
                                                                       @RequestBody BaseRequest<ModelMixQo> modelMixQo) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.pageListByHistory(spaceAppBid, dataBid, QueryConveterTool.convert(modelMixQo), true));
    }

    @ApiOperation("修订")
    @PostMapping("/revise/{bid}")
    public TranscendApiResponse<MSpaceAppData> revise(@PathVariable("spaceBid") String spaceBid,
                                                       @PathVariable("spaceAppBid") String spaceAppBid,
                                                       @PathVariable("bid") String  bid) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.revise(spaceAppBid,  bid));
    }

    @ApiOperation("提升")
    @PostMapping("/promote")
    public TranscendApiResponse<MSpaceAppData> promote(@PathVariable("spaceBid") String spaceBid,
                                                       @PathVariable("spaceAppBid") String spaceAppBid,
                                                       @ApiParam("生命周期数据") @RequestBody LifeCyclePromoteDto promoteDto) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.promote(spaceBid, spaceAppBid, promoteDto));
    }

    @ApiOperation("空间应用用户缓存（最近10个用户）")
    @PostMapping("/{modelCode}/user/cache")
    public TranscendApiResponse<List<Map<String, Object>>> userCache(@PathVariable("spaceBid") String spaceBid,
                                                        @PathVariable("spaceAppBid") String spaceAppBid,
                                                        @PathVariable("modelCode") String modelCode,
                                                        @RequestBody List<String> userIds) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.userCache(spaceAppBid, modelCode, userIds));
    }

    @ApiOperation("批量新增")
    @PostMapping("/batchAdd")
    public TranscendApiResponse<List<MSpaceAppData>> batchAdd(@PathVariable("spaceBid") String spaceBid,
                                                              @PathVariable("spaceAppBid") String spaceAppBid,
                                                              @RequestBody List<MSpaceAppData> mSpaceAppDataList) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.batchAdd(spaceBid, spaceAppBid, mSpaceAppDataList));
    }

    @ApiOperation("批量新增-CAD使用")
    @PostMapping("/batchAddForCad")
    public TranscendApiResponse<String> batchAddForCad(@PathVariable("spaceBid") String spaceBid,
                                                              @PathVariable("spaceAppBid") String spaceAppBid,
                                                              @RequestBody List<MSpaceAppData> mSpaceAppDataList) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.batchAddForCad(spaceBid, spaceAppBid, mSpaceAppDataList));
    }

    @ApiOperation("批量新增获取执行结果-CAD使用")
    @GetMapping("/batchAddForCad/{resultId}")
    public TranscendApiResponse<List<MSpaceAppData>> batchAddForCadResult(@PathVariable("spaceBid") String spaceBid,
                                                                                      @PathVariable("spaceAppBid") String spaceAppBid,
                                                                                      @PathVariable("resultId") String resultId) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.batchAddForCadResult(spaceBid, spaceAppBid, resultId));
    }

    @ApiOperation("保存草稿")
    @PostMapping("/saveCommonDraft")
    public TranscendApiResponse<MSpaceAppData> saveCommonDraft(@PathVariable("spaceBid") String spaceBid,
                                                   @PathVariable("spaceAppBid") String spaceAppBid,
                                                   @ApiParam("草稿数据") @RequestBody MSpaceAppData mSpaceAppData) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.saveCommonDraft(spaceAppBid, mSpaceAppData));
    }

    @ApiOperation("获取详情")
    @PostMapping("/{bid}/instanceEvent/afterChange")
    public TranscendApiResponse<MSpaceAppData> getUpdatePropertyStatus(@PathVariable("spaceBid") String spaceBid,
                                                   @PathVariable("spaceAppBid") String spaceAppBid,
                                                   @PathVariable("bid") String bid) {
        return TranscendApiResponse.success(iBaseApmSpaceAppDataDrivenService.getUpdatePropertyStatus(spaceAppBid, bid,true));
    }
}
