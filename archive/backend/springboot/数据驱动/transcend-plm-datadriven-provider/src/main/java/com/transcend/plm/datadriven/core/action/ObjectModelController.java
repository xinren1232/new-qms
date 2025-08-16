package com.transcend.plm.datadriven.core.action;

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.api.feign.IpmTonesFeignClient;
import com.transcend.plm.datadriven.api.feign.MpProjectFeignClient;
import com.transcend.plm.datadriven.api.feign.ObjectModelFeignClient;
import com.transcend.plm.datadriven.api.feign.PdcFeignClient;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.api.model.dto.CheckBoxDTO;
import com.transcend.plm.datadriven.api.model.dto.MObjectCheckDto;
import com.transcend.plm.datadriven.api.model.dto.PartialUpdateModelDto;
import com.transcend.plm.datadriven.api.model.dto.RelationCodeDTO;
import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transcend.plm.datadriven.api.model.qo.ProjectQo;
import com.transcend.plm.datadriven.api.model.qo.RelationCrossLevelQo;
import com.transcend.plm.datadriven.api.model.vo.DictViewVO;
import com.transcend.plm.datadriven.api.model.vo.OperationLogVo;
import com.transcend.plm.datadriven.api.model.vo.PlmDictViewVO;
import com.transcend.plm.datadriven.api.model.vo.ResponseVO;
import com.transcend.plm.datadriven.common.tool.QueryConveterTool;
import com.transcend.plm.datadriven.common.util.ParentBidHandler;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transcend.plm.datadriven.domain.object.base.OperationLogService;
import com.transcend.plm.datadriven.domain.object.version.VersionObjectManageService;
import com.transsion.framework.dto.BaseRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 对象模型控制器
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/5/18 17:07
 * @since 1.0
 */
@Api(value = "CfgObject Controller", tags = "数据驱动-对象模型-控制器")
@Validated
@RestController
public class ObjectModelController extends AbstractModelController<ObjectModelStandardI> implements ObjectModelFeignClient {

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Resource
    private VersionObjectManageService versionObjectManageService;

    /**
     * @return {@link ObjectModelStandardI }
     */
    @Override
    public ObjectModelStandardI getAim() {
        return this.objectModelCrudI;
    }

    @Resource
    private OperationLogService operationLogService;
    @Autowired
    private IpmTonesFeignClient ipmTonesFeignClient;
    @Autowired
    private MpProjectFeignClient mpProjectFeignClient;
    @Autowired
    private PdcFeignClient pdcFeignClient;


    /**
     * @param modelCode
     * @param mObject
     * @return {@link TranscendApiResponse }<{@link MObject }>
     */
    @ApiOperation("新增基本信息")
    @Override
    public TranscendApiResponse<MObject> add(@ApiParam("模型编码") @PathVariable String modelCode,
                                             @ApiParam("实例数据") @RequestBody MObject mObject) {
        return TranscendApiResponse.success(objectModelCrudI.add(modelCode, mObject));
    }

    /**
     * @param modelCode
     * @param mObject
     * @return {@link TranscendApiResponse }<{@link MObject }>
     */
    @ApiOperation("新增或者更新基本信息")
    @Override
    public TranscendApiResponse<MObject> saveOrUpdate(@ApiParam("模型编码") @PathVariable String modelCode,
                                                      @ApiParam("实例数据") @RequestBody MObject mObject) {
        return TranscendApiResponse.success(objectModelCrudI.saveOrUpdate(modelCode, mObject));
    }

    /**
     * @param modelCode
     * @param bid
     * @param partialData
     * @return {@link TranscendApiResponse }<{@link Boolean }>
     */
    @ApiOperation("更新指定局部内容")
    @Override
    public TranscendApiResponse<Boolean> updatePartialContent(@ApiParam("模型编码") @PathVariable String modelCode,
                                                              @PathVariable("bid") String bid,
                                                              @ApiParam("部分需要更新的property+实例数据") @RequestBody PartialUpdateModelDto partialData) {
        /*移除非部分的数据*/
        MBaseData data = partialData.getData();
        Set<String> properties = partialData.getProperties();
        MObject newData = new MObject();
        properties.forEach(property->
            newData.put(property, data.get(property))
        );
        return TranscendApiResponse.success(
                objectModelCrudI.updateByBid(modelCode, bid, newData)
        );
    }

    /**
     * @param modelCode
     * @param bid
     * @param object
     * @return {@link TranscendApiResponse }<{@link Boolean }>
     */
    @ApiOperation("更新一个字段")
    @Override
    public TranscendApiResponse<Boolean> updatePartialContent(@ApiParam("模型编码") @PathVariable String modelCode,
                                                              @PathVariable("bid") String bid,
                                                              @ApiParam("某个字段以及值") @RequestBody MObject object) {
        if (null == object || 1 != object.size()){
            return TranscendApiResponse.fail("必要条件：有且只有一个属性以及值");
        }
        return TranscendApiResponse.success(
                objectModelCrudI.updateByBid(modelCode, bid, object)
        );
    }

    /**
     * @param modelCode
     * @param bid
     * @param data
     * @return {@link TranscendApiResponse }<{@link Boolean }>
     */
    @ApiOperation("更新内容")
    @Override
    public TranscendApiResponse<Boolean> update(@ApiParam("模型编码") @PathVariable String modelCode,
                                                @PathVariable("bid") String bid,
                                                @ApiParam("实例数据+条件") @RequestBody MObject data) {
        return TranscendApiResponse.success(
                objectModelCrudI.updateByBid(modelCode, bid, data)
        );
    }

    /**
     * @param modelCode
     * @param bid
     * @return {@link TranscendApiResponse }<{@link MObject }>
     */
    @ApiOperation("查看详情")
    @Override
    public TranscendApiResponse<MObject> getByBid(@ApiParam("模型编码") @PathVariable String modelCode,
                                                  @ApiParam("bid") @PathVariable("bid") String bid) {
        return TranscendApiResponse.success(objectModelCrudI.getByBid(modelCode, bid));
    }

    /**
     * @param modelCode
     * @param bid
     * @return {@link TranscendApiResponse }<{@link MObject }>
     */
    @ApiOperation("查看历史详情")
    @Override
    public TranscendApiResponse<MObject> getHisByBid(@ApiParam("模型编码") @PathVariable String modelCode,
                                                  @ApiParam("bid") @PathVariable("bid") String bid) {
        return TranscendApiResponse.success(objectModelCrudI.getHisByBid(modelCode, bid));
    }

    /**
     * @param modelCode
     * @param dataBid
     * @return {@link TranscendApiResponse }<{@link List }<{@link OperationLogVo }>>
     */
    @Override
    public TranscendApiResponse<List<OperationLogVo>> getOperationLog(String modelCode, String dataBid) {
        return TranscendApiResponse.success(operationLogService.getOperationLogs(SsoHelper.getTenantCode(),modelCode,dataBid));
    }

    /**
     * @param modelCode
     * @param bid
     * @return {@link TranscendApiResponse }<{@link MObject }>
     */
    @ApiOperation("查看详情(草稿或者详情)")
    @Override
    public TranscendApiResponse<MObject> getOrDraftByBid(@ApiParam("模型编码") @PathVariable String modelCode,
                                                  @ApiParam("bid") @PathVariable("bid") String bid) {
        return TranscendApiResponse.success(objectModelCrudI.getOrDraftByBid(modelCode, bid));
    }

    /**
     * @param modelCode
     * @param dataBid
     * @return {@link TranscendApiResponse }<{@link List }<{@link MVersionObject }>>
     */
    @ApiOperation("查看历史版本记录")
    @Override
    public TranscendApiResponse<List<MVersionObject>> ListHisByDataBid(@ApiParam("模型编码") @PathVariable String modelCode,
                                                                       @ApiParam("dataBid") @PathVariable("dataBid") String dataBid) {
        return TranscendApiResponse.success(versionObjectManageService.getHisByDataBid(modelCode,dataBid));
    }

    /**
     * @param modelCode
     * @param pageQo
     * @return {@link TranscendApiResponse }<{@link PagedResult }<{@link MObject }>>
     */
    @ApiOperation("分页查询")
    @Override
    public TranscendApiResponse<PagedResult<MObject>> page(@ApiParam("模型编码") @PathVariable String modelCode,
                                                           @ApiParam("分页查询参数") @RequestBody BaseRequest<ModelMixQo> pageQo) {
        return TranscendApiResponse.success(objectModelCrudI.page(modelCode, QueryConveterTool.convert(pageQo), true));
    }

    /**
     * @param modelCode
     * @param mObjectCheckDto
     * @return {@link TranscendApiResponse }<{@link List }<{@link MObject }>>
     */
    @Override
    public TranscendApiResponse<List<MObject>> listNeedCheckMObject(@ApiParam("模型编码") @PathVariable String modelCode, @ApiParam("查询参数") @RequestBody MObjectCheckDto mObjectCheckDto){
        return TranscendApiResponse.success(objectModelCrudI.relationSelectList(modelCode,mObjectCheckDto));
    }

    /**
     * 列表查询(关系选择)
     *
     * @param modelCode
     * @param mObjectCheckDto
     * @return
     */
    @Override
    public TranscendApiResponse<List<MObject>> relationSelectList(String modelCode, MObjectCheckDto mObjectCheckDto) {
        return TranscendApiResponse.success(objectModelCrudI.relationSelectList(modelCode,mObjectCheckDto));
    }

    /**
     * @param modelCode
     * @param source
     * @param mObjectCheckDto
     * @return {@link TranscendApiResponse }<{@link List }<{@link MObject }>>
     */
    @Override
    public TranscendApiResponse<List<MObject>> relationSelectListExpand(String modelCode, String source, MObjectCheckDto mObjectCheckDto) {
        return TranscendApiResponse.success(objectModelCrudI.relationSelectListExpand(modelCode, source, mObjectCheckDto));
    }

    /**
     * @param modelCode
     * @param relationMObject
     * @return {@link TranscendApiResponse }<{@link List }<{@link MObjectTree }>>
     */
    @ApiOperation("树查询")
    @Override
    public TranscendApiResponse<List<MObjectTree>> listTree(@ApiParam("模型编码") @PathVariable String modelCode,
                                                        @ApiParam("分页查询参数") @RequestBody RelationMObject relationMObject){
        return TranscendApiResponse.success(objectModelCrudI.listTree(modelCode, relationMObject));
    }

    /**
     * @param modelCode
     * @param relationMObject
     * @return {@link TranscendApiResponse }<{@link List }<{@link MObjectTree }>>
     */
    @ApiOperation("树查询(关系选择)")
    @Override
    public TranscendApiResponse<List<MObjectTree>> selectTree(@ApiParam("模型编码") @PathVariable String modelCode,
                                                        @ApiParam("分页查询参数") @RequestBody RelationMObject relationMObject){
        return TranscendApiResponse.success(objectModelCrudI.selectTree(modelCode, relationMObject));
    }

    /**
     * @param modelCode
     * @param mObjects
     * @return {@link TranscendApiResponse }<{@link Boolean }>
     */
    @ApiOperation("批量新增")
    @Override
    public TranscendApiResponse<Boolean> bulkAdd(@ApiParam("模型编码") @PathVariable String modelCode,
                                                 @RequestBody List<MObject> mObjects) {
        return TranscendApiResponse.success(objectModelCrudI.addBatch(modelCode, mObjects));
    }

    /**
     * @param modelCode
     * @param bid
     * @return {@link TranscendApiResponse }<{@link Boolean }>
     */
    @ApiOperation("逻辑删除")
    @Override
    public TranscendApiResponse<Boolean> logicalDeleteByBid(@ApiParam("模型编码") @PathVariable String modelCode,
                                                            @ApiParam("bid") @PathVariable("bid") String bid) {
        return TranscendApiResponse.success(objectModelCrudI.logicalDeleteByBid(modelCode, bid));
    }

    /**
     * @param relationMObject
     * @return {@link TranscendApiResponse }<{@link Boolean }>
     */
    @Override
    public TranscendApiResponse<Boolean> addRelatons(@RequestBody RelationMObject relationMObject){
        return TranscendApiResponse.success(objectModelCrudI.addRelatons(relationMObject));
    }

    /**
     * @param relationMObject
     * @return {@link TranscendApiResponse }<{@link Boolean }>
     */
    @Override
    public TranscendApiResponse<Boolean> logicalDeleteRelations(@RequestBody RelationMObject relationMObject){
        return TranscendApiResponse.success(objectModelCrudI.logicalDeleteRelations(relationMObject));
    }

    /**
     * @param relationMObject
     * @return {@link TranscendApiResponse }<{@link Boolean }>
     */
    @Override
    public TranscendApiResponse<Boolean> deleteRelations(@RequestBody RelationMObject relationMObject){
        return TranscendApiResponse.success(objectModelCrudI.deleteRelations(relationMObject));
    }

    /**
     * @param modelCode
     * @param bid
     * @return {@link TranscendApiResponse }<{@link Boolean }>
     */
    @ApiOperation("删除")
    @Override
    public TranscendApiResponse<Boolean> deleteByBid(@ApiParam("模型编码") @PathVariable String modelCode,
                                                     @PathVariable("bid") String bid) {
        return TranscendApiResponse.success(objectModelCrudI.deleteByBid(modelCode, bid));
    }

    /**
     * @param modelCode
     * @param bids
     * @return {@link TranscendApiResponse }<{@link Boolean }>
     */
    @Override
    public TranscendApiResponse<Boolean> batchLogicalDeleteByBids(@ApiParam("模型编码") @PathVariable String modelCode,
                                                                  @RequestBody List<String> bids) {
        return TranscendApiResponse.success(objectModelCrudI.batchLogicalDeleteByBids(modelCode, bids));
    }

    /**
     * @param modelCode
     * @param bid
     * @return {@link TranscendApiResponse }<{@link MVersionObject }>
     */
    @ApiOperation("检出")
    @Override
    public TranscendApiResponse<MVersionObject> checkOut(@ApiParam("模型编码") @PathVariable String modelCode,
                                   @ApiParam("bid") @PathVariable("bid") String bid) {
        return TranscendApiResponse.success(objectModelCrudI.checkOut(modelCode, bid));
    }

    /**
     * @param modelCode
     * @param bids
     * @return {@link TranscendApiResponse }<{@link Boolean }>
     */
    @Override
    public TranscendApiResponse<Boolean> batchDeleteByBids(@ApiParam("模型编码") @PathVariable String modelCode,
                                                           @RequestBody List<String> bids) {
        return TranscendApiResponse.success(objectModelCrudI.batchDeleteByBids(modelCode, bids));
    }

    /**
     * @param modelCode
     * @param bid
     * @return {@link TranscendApiResponse }<{@link MVersionObject }>
     */
    @ApiOperation("取消检出")
    @Override
    public TranscendApiResponse<MVersionObject> cancelCheckOut(@ApiParam("模型编码") @PathVariable String modelCode, @ApiParam("bid") @PathVariable String bid) {
        return TranscendApiResponse.success(objectModelCrudI.cancelCheckOut(modelCode, bid));
    }

    /**
     * @param modelCode
     * @param mObject
     * @return {@link TranscendApiResponse }<{@link MVersionObject }>
     */
    @ApiOperation("检入")
    @Override
    public TranscendApiResponse<MVersionObject> checkIn(@ApiParam("模型编码") @PathVariable String modelCode,@ApiParam("实例数据") MVersionObject mObject) {
        return TranscendApiResponse.success(objectModelCrudI.checkIn(modelCode, mObject));
    }

    /**
     * @param mObject
     * @return {@link TranscendApiResponse }<{@link Boolean }>
     */
    @Override
    public TranscendApiResponse<Boolean> saveDraft(MObject mObject) {
        return TranscendApiResponse.success(objectModelCrudI.saveDraft(mObject));
    }

    /**
     * @param relationMObject
     * @return {@link TranscendApiResponse }<{@link List }<{@link MObject }>>
     */
    @Override
    public TranscendApiResponse<List<MObject>> listRelationMObjects(@RequestBody RelationMObject relationMObject) {
        return TranscendApiResponse.success(objectModelCrudI.listRelationMObjects(relationMObject));
    }

    /**
     * @param isVersion
     * @param relationMObject
     * @return {@link TranscendApiResponse }<{@link PagedResult }<{@link MObject }>>
     */
    @Override
    public TranscendApiResponse<PagedResult<MObject>> listRelationMObjectsWithVersionPage(Boolean isVersion, BaseRequest<RelationMObject> relationMObject){
        return TranscendApiResponse.success(objectModelCrudI.listRelationMObjectsPage(isVersion, relationMObject,null, true));
    }

    /**
     * @param isVersion
     * @param relationMObject
     * @return {@link TranscendApiResponse }<{@link List }<{@link MObject }>>
     */
    @Override
    public TranscendApiResponse<List<MObject>> listRelationMObjectsWithVersion(Boolean isVersion, RelationMObject relationMObject){
        return TranscendApiResponse.success(objectModelCrudI.listRelationMObjects(isVersion, relationMObject));
    }

    /**
     * @param modelCode
     * @param wrappers
     * @return {@link TranscendApiResponse }<{@link List }<{@link MObjectTree }>>
     */
    @Override
    public TranscendApiResponse<List<MObjectTree>> tree(@PathVariable String modelCode, @RequestBody List<QueryWrapper> wrappers) {
        return TranscendApiResponse.success(objectModelCrudI.tree(modelCode,wrappers));
    }

    /**
     * @param modelCode
     * @param treeList
     * @return {@link TranscendApiResponse }<{@link Boolean }>
     */
    @Override
    public TranscendApiResponse<Boolean> moveTreeNode(@PathVariable String modelCode, @RequestBody List<MObjectTree> treeList) {
        return TranscendApiResponse.success(objectModelCrudI.moveTreeNode(modelCode,treeList));
    }

    @Override
    public TranscendApiResponse<List<MObjectTree>> relationTree(@RequestBody RelationMObject relationMObject) {
        return TranscendApiResponse.success(ParentBidHandler.handleMObjectTree(objectModelCrudI.relationTree(relationMObject)));
    }


    /**
     * 关系下拉选项的分页查询（作为目标对象查询以源对象的可选集合）
     *
     * @param sourceModelCode
     * @param qo
     * @return
     */
    @Override
    public TranscendApiResponse<PagedResult<MObject>> relationTargetSelectPage(String sourceModelCode, RelationCrossLevelQo qo) {
        qo.setSourceModelCode(sourceModelCode);
        return TranscendApiResponse.success(objectModelCrudI.relationTargetSelectPage(qo));
    }

    @Override
    public TranscendApiResponse<List<CheckBoxDTO>> getCommonCheckBox(GetEnumRequest request) {
        return TranscendApiResponse.success(ipmTonesFeignClient.getCommonCheckBox(request).getData());
    }

    @Override
    public TranscendApiResponse<List<DictViewVO>> queryAllProjectName() {
        return TranscendApiResponse.success(mpProjectFeignClient.queryAllProjectName().getData());
    }

    @Override
    public TranscendApiResponse<List<PlmDictViewVO>> plmQueryDictByCodes(String code) {
        return TranscendApiResponse.success(pdcFeignClient.plmQueryDictByCode(code).getData());
    }

    @Override
    public TranscendApiResponse<Map<String, Object>> getDictRelationByCode(RelationCodeDTO dictRelationPO) {
        return TranscendApiResponse.success(pdcFeignClient.getDictRelationByCode(dictRelationPO).getData());
    }
}
