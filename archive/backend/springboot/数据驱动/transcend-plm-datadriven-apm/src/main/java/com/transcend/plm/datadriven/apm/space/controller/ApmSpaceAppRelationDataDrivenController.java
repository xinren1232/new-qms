package com.transcend.plm.datadriven.apm.space.controller;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.datadriven.api.model.GroupListResult;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.MObjectTree;
import com.transcend.plm.datadriven.api.model.RelationMObject;
import com.transcend.plm.datadriven.api.model.relation.delete.StructureRelDel;
import com.transcend.plm.datadriven.api.model.relation.qo.QueryPathQo;
import com.transcend.plm.datadriven.api.model.relation.qo.RelationParentQo;
import com.transcend.plm.datadriven.api.model.relation.qo.RelationTreeQo;
import com.transcend.plm.datadriven.api.model.relation.vo.QueryPathVo;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.AddExpandAo;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.MObjectCopyAo;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.RelationDelAndRemParamAo;
import com.transcend.plm.datadriven.apm.space.model.ApmRelationMultiTreeAddParam;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.model.SpaceAppRelationAddParam;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmMultiTreeDto;
import com.transcend.plm.datadriven.apm.space.pojo.dto.MultiAppConfig;
import com.transcend.plm.datadriven.apm.space.service.IBaseApmSpaceAppRelationDataDrivenService;
import com.transcend.plm.datadriven.apm.tools.ParentBidHandler;
import com.transsion.framework.dto.BaseRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

/**
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/9/20 17:07
 * @since 1.0
 **/
@RestController
@Api(value = "Apm SpaceAppDataController", tags = "空间-应用关系数据驱动-控制器")
@RequestMapping(value = "/apm/space/{spaceBid}/app/{relationSpaceAppBid}/relation-data-driven")
public class ApmSpaceAppRelationDataDrivenController {

    @Resource
    private IBaseApmSpaceAppRelationDataDrivenService baseApmSpaceAppRelationDataDrivenService;

    @ApiOperation("新增或者选取已有数据")
    @PostMapping("/copyMObject")
    public TranscendApiResponse<Boolean> copyMObject(
            @ApiParam("新增目标数据") @RequestBody MObjectCopyAo mObjectCopyAo) {
        return TranscendApiResponse.success(baseApmSpaceAppRelationDataDrivenService.copyMObject(
                mObjectCopyAo
        ));
    }

    @PostMapping("/{source}/add")
    @ApiOperation("定制化新增接口")
    public TranscendApiResponse<Object> addExpand(@PathVariable("spaceBid") String spaceBid,
                                                  @PathVariable("relationSpaceAppBid") String relationSpaceAppBid,
                                                  @PathVariable("source") String source,
                                                  @RequestBody AddExpandAo addExpandAo){
        return TranscendApiResponse.success(
                baseApmSpaceAppRelationDataDrivenService.addExpand(spaceBid, addExpandAo.getSourceSpaceAppBid(), source, addExpandAo)
        );
    }


    @ApiOperation("删除绑定关系以及目标对象实例")
    @PostMapping("/batchDelete")
    public TranscendApiResponse<Boolean> batchDelete(
            @ApiParam("空间bid") @PathVariable("spaceBid") String spaceBid,
            @RequestBody RelationDelAndRemParamAo delAndRemParamAo) {
        return TranscendApiResponse.success(baseApmSpaceAppRelationDataDrivenService.batchDelete(
                spaceBid,
                delAndRemParamAo));
    }


    @ApiOperation("移除绑定关系")
    @PostMapping("/batchRemove")
    public TranscendApiResponse<Boolean> batchRemove(
            @ApiParam("空间bid") @PathVariable("spaceBid") String spaceBid,
            @RequestBody RelationDelAndRemParamAo delAndRemParamAo) {
        return TranscendApiResponse.success(baseApmSpaceAppRelationDataDrivenService.batchRemove(
                spaceBid,
                delAndRemParamAo));
    }

    @ApiOperation("移除所有关联")
    @PostMapping("/removeAllRelation")
    public TranscendApiResponse<Boolean> removeAllRelation(@ApiParam("目标实例集合") @RequestBody SpaceAppRelationAddParam spaceAppRelationAddParam) {
        return TranscendApiResponse.success(baseApmSpaceAppRelationDataDrivenService.removeAllRelation(spaceAppRelationAddParam));
    }


    @ApiOperation("关系-树视图模式查看")
    @PostMapping("/tree")
    public TranscendApiResponse<List<MObjectTree>> tree(@PathVariable("spaceBid") String spaceBid,
                                                                       @PathVariable("relationSpaceAppBid") String spaceAppBid,
                                                                       @RequestBody RelationMObject relationMObject){
        return TranscendApiResponse.success(baseApmSpaceAppRelationDataDrivenService.tree(spaceBid, spaceAppBid, relationMObject, true));
    }

    @ApiOperation("关系-多对象树选取")
    @PostMapping("/multiTree/batchSelect")
    public TranscendApiResponse<Boolean> batchSelect(@ApiParam("空间bid") @PathVariable("spaceBid") String spaceBid,
                                                     @ApiParam("空间应用bid") @PathVariable("relationSpaceAppBid") String relationSpaceAppBid,
            @RequestBody ApmRelationMultiTreeAddParam apmRelationMultiTreeAddParam) {
        return TranscendApiResponse.success(baseApmSpaceAppRelationDataDrivenService.multiBatchSelect(spaceBid, relationSpaceAppBid, apmRelationMultiTreeAddParam));
    }

    @PostMapping("/listMultiTree")
    public TranscendApiResponse<List<MObjectTree>> listMultiTree(@PathVariable("spaceBid") String spaceBid,
                                                                 @PathVariable("relationSpaceAppBid") String spaceAppBid,
                                                                 @RequestBody ApmMultiTreeDto apmMultiTreeDto){
        if(StringUtils.isEmpty(apmMultiTreeDto.getSpaceBid())){
            apmMultiTreeDto.setSourceBid(spaceBid);
        }
        if(StringUtils.isEmpty(apmMultiTreeDto.getSpaceAppBid())){
            apmMultiTreeDto.setSpaceAppBid(spaceAppBid);
        }

        //保持与关联列表相同逻辑，顶层数据无需权限
        Optional.of(apmMultiTreeDto).map(ApmMultiTreeDto::getMultiAppTreeContent)
                .map(MultiAppConfig::getMultiAppTreeConfig).ifPresent(multiAppTreeConfig -> {
                    multiAppTreeConfig.setInstanceQueryPermission(false);
                });

        List<MObjectTree> data = ParentBidHandler.handleMObjectTree(baseApmSpaceAppRelationDataDrivenService.listMultiTree(apmMultiTreeDto, true));
        if(StringUtils.isNoneBlank(apmMultiTreeDto.getGroupProperty()) && StringUtils.isNoneBlank(apmMultiTreeDto.getGroupPropertyValue())){
            apmMultiTreeDto.setTree(data);
            return TranscendApiResponse.success(baseApmSpaceAppRelationDataDrivenService.listMultiTreeGroupBy(apmMultiTreeDto));
        }
        return TranscendApiResponse.success(data);
    }
    @PostMapping("/listMultiTreeKb")
    public TranscendApiResponse<GroupListResult<MSpaceAppData>> listMultiTreeKb(@PathVariable("spaceBid") String spaceBid,
                                                                 @PathVariable("relationSpaceAppBid") String spaceAppBid, @RequestBody ApmMultiTreeDto apmMultiTreeDto){
        if(StringUtils.isEmpty(apmMultiTreeDto.getSpaceBid())){
            apmMultiTreeDto.setSourceBid(spaceBid);
        }
        if(StringUtils.isEmpty(apmMultiTreeDto.getSpaceAppBid())){
            apmMultiTreeDto.setSpaceAppBid(spaceAppBid);
        }
        return TranscendApiResponse.success(baseApmSpaceAppRelationDataDrivenService.listMultiTreeGroupKb(apmMultiTreeDto, true));
    }

    @PostMapping("/groupList/{groupProperty}")
    public TranscendApiResponse<GroupListResult<MSpaceAppData>> groupList(@PathVariable("groupProperty") String groupProperty, @RequestBody RelationMObject relationMObject){
        return TranscendApiResponse.success(baseApmSpaceAppRelationDataDrivenService.groupList(groupProperty, relationMObject));
    }

    /**
     * 导出excel
     */
    @PostMapping("/exportExcel/{type}")
    public void exportExcel(@PathVariable("spaceBid") String spaceBid,
                            @PathVariable("relationSpaceAppBid") String spaceAppBid,
                            @PathVariable("type") String type,@RequestBody RelationMObject relationMObject, HttpServletResponse response){
        baseApmSpaceAppRelationDataDrivenService.exportExcel(spaceAppBid,type,relationMObject, response);
    }
    @ApiOperation("查询关系目标实例列表")
    @PostMapping("/listRelationMObjects")
    @Deprecated
    public TranscendApiResponse<List<MObject>> listRelation(@PathVariable("spaceBid") String spaceBid,
                                                            @PathVariable("relationSpaceAppBid") String spaceAppBid,
                                                            @RequestBody RelationMObject relationMObject){
        return TranscendApiResponse.success(baseApmSpaceAppRelationDataDrivenService.listRelation(spaceBid, spaceAppBid, relationMObject));
    }

    @ApiOperation("分页查询关系目标实例列表")
    @PostMapping("/listRelationMObjectsPage")
    public TranscendApiResponse<PagedResult<MObject>> listRelationPage(@PathVariable("spaceBid") String spaceBid,
                                                                       @PathVariable("relationSpaceAppBid") String spaceAppBid,
                                                                       @RequestBody BaseRequest<RelationMObject> relationMObject){
        return TranscendApiResponse.success(baseApmSpaceAppRelationDataDrivenService.listRelationPage(spaceBid, spaceAppBid, relationMObject, false));
    }

    @ApiOperation("根据源bid递归查询父子结构关系树")
    @PostMapping("/listRelationTree")
    public TranscendApiResponse<List<MObjectTree>> listRelationTree(@PathVariable("spaceBid") String spaceBid,
                                                         @PathVariable("relationSpaceAppBid") String spaceAppBid,
                                                         @RequestBody RelationTreeQo qo){
        return TranscendApiResponse.success(baseApmSpaceAppRelationDataDrivenService.listRelationTree(spaceBid, spaceAppBid, qo));
    }

    @ApiOperation("根据源bid 查询一层父关系数据")
    @PostMapping("/listRelParent")
    public TranscendApiResponse<List<MObject>> listRelParent(@PathVariable("spaceBid") String spaceBid,
                                                         @PathVariable("relationSpaceAppBid") String spaceAppBid,
                                                         @RequestBody RelationParentQo relationParentQo){
        return TranscendApiResponse.success(baseApmSpaceAppRelationDataDrivenService.listRelParent(spaceBid, spaceAppBid, relationParentQo));
    }

    @ApiOperation("父子结构 - 移除")
    @PostMapping("/structure/batchRemoveRel")
    public TranscendApiResponse<Object> structureBatchRemoveRel(@PathVariable("spaceBid") String spaceBid,
                                                                @PathVariable("relationSpaceAppBid") String spaceAppBid,
                                                                @RequestBody StructureRelDel structureRelDel) {
        return TranscendApiResponse.success(baseApmSpaceAppRelationDataDrivenService.structureBatchRemoveRel(spaceBid, spaceAppBid, structureRelDel));
    }

    @ApiOperation("查询CAD文档根文档到指定文档的路径")
    @PostMapping("/queryCadPath")
    public TranscendApiResponse<List<QueryPathVo>> queryCadPath(@PathVariable("spaceBid") String spaceBid,
                                                                @PathVariable("relationSpaceAppBid") String spaceAppBid,
                                                                @RequestBody QueryPathQo queryPathQo) {
        return TranscendApiResponse.success(baseApmSpaceAppRelationDataDrivenService.queryCadPath(spaceBid, spaceAppBid, queryPathQo));
    }

}
