package com.transcend.plm.datadriven.api.feign;

/**
 * TODO 描述
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/5/11 16:07
 * @since 1.0
 */

import com.transcend.framework.core.model.api.TranscendApiResponse;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.datadriven.api.feign.fallback.DatadrivenClientFactory;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.api.model.dto.CheckBoxDTO;
import com.transcend.plm.datadriven.api.model.dto.MObjectCheckDto;
import com.transcend.plm.datadriven.api.model.dto.PartialUpdateModelDto;
import com.transcend.plm.datadriven.api.model.dto.RelationCodeDTO;
import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transcend.plm.datadriven.api.model.qo.RelationCrossLevelQo;
import com.transcend.plm.datadriven.api.model.vo.DictViewVO;
import com.transcend.plm.datadriven.api.model.vo.OperationLogVo;
import com.transcend.plm.datadriven.api.model.vo.PlmDictViewVO;
import com.transsion.framework.dto.BaseRequest;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author jie.luo1
 * @Description: 配置对象feign
 * @date 2023年5月9日
 */
@FeignClient(name = "${transcend.datadriven.feign.name:transcend-plm-datadriven}", url = "${transcend.datadriven.feign.url:}",
        fallbackFactory = DatadrivenClientFactory.class)
public interface ObjectModelFeignClient {
    /**
     * add
     *
     * @param modelCode modelCode
     * @param mObject   mObject
     * @return {@link TranscendApiResponse<MObject>}
     */
    @ApiOperation("新增基本信息")
    @PostMapping("/data-driven/api/object-model/{modelCode}/add")
    TranscendApiResponse<MObject> add(@ApiParam("模型编码") @PathVariable String modelCode,
                                      @ApiParam("实例数据") @RequestBody MObject mObject);

    /**
     * batchLogicalDeleteByBids
     *
     * @param modelCode modelCode
     * @param bids      bids
     * @return {@link TranscendApiResponse<Boolean>}
     */
    @ApiOperation("批量逻辑删除")
    @PostMapping("/data-driven/api/object-model/{modelCode}/batchLogicalDeleteByBids")
    TranscendApiResponse<Boolean> batchLogicalDeleteByBids(@ApiParam("模型编码") @PathVariable String modelCode,
                                                           @RequestBody List<String> bids);

    /**
     * batchDeleteByBids
     *
     * @param modelCode modelCode
     * @param bids      bids
     * @return {@link TranscendApiResponse<Boolean>}
     */
    @ApiOperation("批量删除")
    @PostMapping("/data-driven/api/object-model/{modelCode}/batchDeleteByBids")
    TranscendApiResponse<Boolean> batchDeleteByBids(@ApiParam("模型编码") @PathVariable String modelCode,
                                                    @RequestBody List<String> bids);

    /**
     * saveOrUpdate
     *
     * @param modelCode modelCode
     * @param mObject   mObject
     * @return {@link TranscendApiResponse<MObject>}
     */
    @ApiOperation("新增或者更新基本信息")
    @PostMapping("/data-driven/api/object-model/{modelCode}/saveOrUpdate")
    TranscendApiResponse<MObject> saveOrUpdate(@ApiParam("模型编码") @PathVariable String modelCode,
                                               @ApiParam("实例数据") @RequestBody MObject mObject);

    /**
     * updatePartialContent
     *
     * @param modelCode   modelCode
     * @param bid         bid
     * @param partialData partialData
     * @return {@link TranscendApiResponse<Boolean>}
     */
    @ApiOperation("更新指定局部内容")
    @PostMapping("/data-driven/api/object-model/{modelCode}/updatePartialContent/{bid}")
    TranscendApiResponse<Boolean> updatePartialContent(@ApiParam("模型编码") @PathVariable String modelCode,
                                                       @PathVariable("bid") String bid,
                                                       @ApiParam("部分需要更新的property+实例数据") @RequestBody PartialUpdateModelDto partialData);

    /**
     * updatePartialContent
     *
     * @param modelCode modelCode
     * @param bid       bid
     * @param object    object
     * @return {@link TranscendApiResponse<Boolean>}
     */
    @ApiOperation("更新一个字段")
    @PostMapping("/data-driven/api/object-model/{modelCode}/update/{bid}/property")
    TranscendApiResponse<Boolean> updatePartialContent(@ApiParam("模型编码") @PathVariable String modelCode,
                                                       @PathVariable("bid") String bid,
                                                       @ApiParam("某个字段以及值") @RequestBody MObject object);

    /**
     * update
     *
     * @param modelCode modelCode
     * @param bid       bid
     * @param data      data
     * @return {@link TranscendApiResponse<Boolean>}
     */
    @ApiOperation("更新内容")
    @PostMapping("/data-driven/api/object-model/{modelCode}/update/{bid}")
    TranscendApiResponse<Boolean> update(@ApiParam("模型编码") @PathVariable String modelCode,
                                         @PathVariable("bid") String bid,
                                         @ApiParam("实例数据+条件") @RequestBody MObject data);

    /**
     * getByBid
     *
     * @param modelCode modelCode
     * @param bid       bid
     * @return {@link TranscendApiResponse<MObject>}
     */
    @GetMapping("/data-driven/api/object-model/{modelCode}/get/{bid}")
    @ApiOperation("查看详情")
    TranscendApiResponse<MObject> getByBid(@ApiParam("模型编码") @PathVariable String modelCode,
                                           @ApiParam("bid") @PathVariable("bid") String bid);

    /**
     * getHisByBid
     *
     * @param modelCode modelCode
     * @param bid       bid
     * @return {@link TranscendApiResponse<MObject>}
     */
    @GetMapping("/data-driven/api/object-model/{modelCode}/getHis/{bid}")
    @ApiOperation("查看历史详情")
    TranscendApiResponse<MObject> getHisByBid(@ApiParam("模型编码") @PathVariable String modelCode,
                                              @ApiParam("bid") @PathVariable("bid") String bid);

    /**
     * getOperationLog
     *
     * @param modelCode modelCode
     * @param dataBid   dataBid
     * @return {@link TranscendApiResponse<List<OperationLogVo>>}
     */
    @GetMapping("/data-driven/api/object-model/{modelCode}/getOperationLog/{dataBid}")
    @ApiOperation("查看草稿或者详情")
    TranscendApiResponse<List<OperationLogVo>> getOperationLog(@ApiParam("模型编码") @PathVariable("modelCode") String modelCode,
                                                               @ApiParam("dataBid") @PathVariable("dataBid") String dataBid);

    /**
     * getOrDraftByBid
     *
     * @param modelCode modelCode
     * @param bid       bid
     * @return {@link TranscendApiResponse<MObject>}
     */
    @GetMapping("/data-driven/api/object-model/{modelCode}/getOrDraftByBid/{bid}")
    @ApiOperation("查看草稿或者详情")
    TranscendApiResponse<MObject> getOrDraftByBid(@ApiParam("模型编码") @PathVariable String modelCode,
                                                  @ApiParam("bid") @PathVariable("bid") String bid);

    /**
     * ListHisByDataBid
     *
     * @param modelCode modelCode
     * @param dataBid   dataBid
     * @return {@link TranscendApiResponse<List<MVersionObject>>}
     */
    @GetMapping("/data-driven/api/object-model/{modelCode}/ListHisByDataBid/{dataBid}")
    @ApiOperation("查看历史版本记录")
    TranscendApiResponse<List<MVersionObject>> ListHisByDataBid(@ApiParam("模型编码") @PathVariable String modelCode,
                                                                @ApiParam("dataBid") @PathVariable("dataBid") String dataBid);

    /**
     * page
     *
     * @param modelCode modelCode
     * @param pageQo    pageQo
     * @return {@link TranscendApiResponse<PagedResult<MObject>>}
     */
    @ApiOperation("分页查询")
    @PostMapping("/data-driven/api/object-model/{modelCode}/page")
    TranscendApiResponse<PagedResult<MObject>> page(@ApiParam("模型编码") @PathVariable String modelCode,
                                                    @ApiParam("分页查询参数") @RequestBody BaseRequest<ModelMixQo> pageQo);

    /**
     * listTree
     *
     * @param modelCode       modelCode
     * @param relationMObject relationMObject
     * @return {@link TranscendApiResponse<List<MObjectTree>>}
     */
    @ApiOperation("树查询")
    @PostMapping("/data-driven/api/object-model/{modelCode}/listTree")
    TranscendApiResponse<List<MObjectTree>> listTree(@ApiParam("模型编码") @PathVariable String modelCode,
                                                     @ApiParam("分页查询参数") @RequestBody RelationMObject relationMObject);

    /**
     * selectTree
     *
     * @param modelCode       modelCode
     * @param relationMObject relationMObject
     * @return {@link TranscendApiResponse<List<MObjectTree>>}
     */
    @ApiOperation("树查询(关系选择)")
    @PostMapping("/data-driven/api/object-model/{modelCode}/relation/selectTree")
    TranscendApiResponse<List<MObjectTree>> selectTree(@ApiParam("模型编码") @PathVariable String modelCode,
                                                       @ApiParam("分页查询参数") @RequestBody RelationMObject relationMObject);

    /**
     * listNeedCheckMObject
     *
     * @param modelCode       modelCode
     * @param mObjectCheckDto mObjectCheckDto
     * @return {@link TranscendApiResponse<List<MObject>>}
     * @Deprecated 后续用 selectTree 替换
     */
    @Deprecated
    @ApiOperation("需要选择的列表查询")
    @PostMapping("/data-driven/api/object-model/{modelCode}/listNeedCheckMObject")
    TranscendApiResponse<List<MObject>> listNeedCheckMObject(@ApiParam("模型编码") @PathVariable String modelCode, @ApiParam("查询参数") @RequestBody MObjectCheckDto mObjectCheckDto);

    /**
     * relationSelectList
     *
     * @param modelCode       modelCode
     * @param mObjectCheckDto mObjectCheckDto
     * @return {@link TranscendApiResponse<List<MObject>>}
     */
    @ApiOperation("列表查询(关系选择)")
    @PostMapping("/data-driven/api/object-model/{modelCode}/relation/selectList")
    TranscendApiResponse<List<MObject>> relationSelectList(@ApiParam("模型编码") @PathVariable String modelCode, @ApiParam("查询参数") @RequestBody MObjectCheckDto mObjectCheckDto);

    /**
     * relationSelectListExpand
     *
     * @param modelCode       modelCode
     * @param source          source
     * @param mObjectCheckDto mObjectCheckDto
     * @return {@link TranscendApiResponse<List<MObject>>}
     */
    @ApiOperation("列表查询-支持内外部数据源扩展(关系选择)")
    @PostMapping("/data-driven/api/object-model/{modelCode}/{source}/relation/selectListExpand")
    TranscendApiResponse<List<MObject>> relationSelectListExpand(@ApiParam("模型编码") @PathVariable String modelCode, @PathVariable String source, @ApiParam("查询参数") @RequestBody MObjectCheckDto mObjectCheckDto);

    /**
     * bulkAdd
     *
     * @param modelCode modelCode
     * @param mObjects  mObjects
     * @return {@link TranscendApiResponse<Boolean>}
     */
    @ApiOperation("批量新增")
    @PostMapping("/data-driven/api/object-model/{modelCode}/addBatch")
    TranscendApiResponse<Boolean> bulkAdd(@ApiParam("模型编码") @PathVariable String modelCode,
                                          @RequestBody List<MObject> mObjects);

    /**
     * logicalDeleteByBid
     *
     * @param modelCode modelCode
     * @param bid       bid
     * @return {@link TranscendApiResponse<Boolean>}
     */
    @PostMapping("/data-driven/api/object-model/{modelCode}/logicalDelete/{bid}")
    @ApiOperation("逻辑删除")
    TranscendApiResponse<Boolean> logicalDeleteByBid(@ApiParam("模型编码") @PathVariable String modelCode,
                                                     @ApiParam("bid") @PathVariable("bid") String bid);

    /**
     * addRelatons
     *
     * @param relationMObject relationMObject
     * @return {@link TranscendApiResponse<Boolean>}
     */
    @PostMapping("/data-driven/api/object-model/addRelatons")
    @ApiOperation("对象增加关系目标对象")
    TranscendApiResponse<Boolean> addRelatons(@RequestBody RelationMObject relationMObject);

    /**
     * logicalDeleteRelations
     *
     * @param relationMObject relationMObject
     * @return {@link TranscendApiResponse<Boolean>}
     */
    @PostMapping("/data-driven/api/object-model/logicalDeleteRelations")
    @ApiOperation("对象删除关系目标对象")
    TranscendApiResponse<Boolean> logicalDeleteRelations(@RequestBody RelationMObject relationMObject);

    /**
     * deleteRelations
     *
     * @param relationMObject relationMObject
     * @return {@link TranscendApiResponse<Boolean>}
     */
    @PostMapping("/data-driven/api/object-model/deleteRelations")
    @ApiOperation("对象删除关系目标对象")
    TranscendApiResponse<Boolean> deleteRelations(@RequestBody RelationMObject relationMObject);

    /**
     * deleteByBid
     *
     * @param modelCode modelCode
     * @param bid       bid
     * @return {@link TranscendApiResponse<Boolean>}
     */
    @PostMapping("/data-driven/api/object-model/{modelCode}/delete/{bid}")
    @ApiOperation("删除")
    TranscendApiResponse<Boolean> deleteByBid(@ApiParam("模型编码") @PathVariable String modelCode,
                                              @PathVariable("bid") String bid);

    /**
     * checkOut
     *
     * @param modelCode modelCode
     * @param bid       bid
     * @return {@link TranscendApiResponse<MVersionObject>}
     */
    @PostMapping("/data-driven/api/object-model/{modelCode}/checkOut/{bid}")
    @ApiOperation("检出")
    TranscendApiResponse<MVersionObject> checkOut(@ApiParam("模型编码") @PathVariable String modelCode,
                                                  @ApiParam("bid") @PathVariable("bid") String bid);

    /**
     * cancelCheckOut
     *
     * @param modelCode modelCode
     * @param bid       bid
     * @return {@link TranscendApiResponse<MVersionObject>}
     */
    @PostMapping("/data-driven/api/object-model/{modelCode}/cancelCheckOut/{bid}")
    @ApiOperation("撤销检出")
    TranscendApiResponse<MVersionObject> cancelCheckOut(@ApiParam("模型编码") @PathVariable String modelCode,
                                                        @ApiParam("bid") @PathVariable("bid") String bid);

    /**
     * checkIn
     *
     * @param modelCode modelCode
     * @param mObject   mObject
     * @return {@link TranscendApiResponse<MVersionObject>}
     */
    @PostMapping("/data-driven/api/object-model/{modelCode}/checkIn")
    @ApiOperation("检入")
    TranscendApiResponse<MVersionObject> checkIn(@ApiParam("模型编码") @PathVariable String modelCode, @ApiParam("实例数据") @RequestBody MVersionObject mObject);


    /**
     * 编写一个方法，用于保存草稿数据，传入DraftVO并校验bid和modelCode不能为空，返回DraftVO
     * saveDraft
     *
     * @param mObject mObject
     * @return {@link TranscendApiResponse<Boolean>}
     */
    @PostMapping("/data-driven/api/object-model/{modelCode}/saveDraft")
    @ApiOperation("保存草稿")
    TranscendApiResponse<Boolean> saveDraft(@ApiParam("实例数据") @RequestBody MObject mObject);

    /**
     * tree
     *
     * @param modelCode modelCode
     * @param wrappers  wrappers
     * @return {@link TranscendApiResponse<List<MObjectTree>>}
     */
    @PostMapping("/data-driven/api/object-model/{modelCode}/tree")
    @ApiOperation("根据模型编码获取树形结构")
    TranscendApiResponse<List<MObjectTree>> tree(@PathVariable String modelCode, @RequestBody List<QueryWrapper> wrappers);

    /**
     * moveTreeNode
     *
     * @param modelCode modelCode
     * @param treeList  treeList
     * @return {@link TranscendApiResponse<Boolean>}
     */
    @PostMapping("/data-driven/api/object-model/{modelCode}/moveTreeNode")
    @ApiOperation("根据模型编码获取树形结构")
    TranscendApiResponse<Boolean> moveTreeNode(@PathVariable String modelCode, @RequestBody List<MObjectTree> treeList);

    /**
     * relationTree
     *
     * @param relationMObject relationMObject
     * @return {@link TranscendApiResponse<List<MObjectTree>>}
     */
    @PostMapping("/data-driven/api/object-model/relationTree")
    @ApiOperation("根据模型编码获取树形结构")
    TranscendApiResponse<List<MObjectTree>> relationTree(
            @RequestBody RelationMObject relationMObject);

    /**
     * listRelationMObjects
     *
     * @param relationMObject relationMObject
     * @return {@link TranscendApiResponse<List<MObject>>}
     */
    @PostMapping("/data-driven/api/object-model/listRelationMObjects")
    @ApiOperation("查询对象关系实例数据")
    TranscendApiResponse<List<MObject>> listRelationMObjects(@RequestBody RelationMObject relationMObject);

    /**
     * listRelationMObjectsWithVersionPage
     *
     * @param isVersion       isVersion
     * @param relationMObject relationMObject
     * @return {@link TranscendApiResponse<PagedResult<MObject>>}
     */
    @PostMapping("/data-driven/api/object-model/listRelationMObjects/{isVersion}/page")
    @ApiOperation("分页查询对象关系实例数据")
    TranscendApiResponse<PagedResult<MObject>> listRelationMObjectsWithVersionPage(@ApiParam("是否版本对象") @PathVariable Boolean isVersion,
                                                                                   @RequestBody BaseRequest<RelationMObject> relationMObject);

    /**
     * listRelationMObjectsWithVersion
     *
     * @param isVersion       isVersion
     * @param relationMObject relationMObject
     * @return {@link TranscendApiResponse<List<MObject>>}
     */
    @PostMapping("/data-driven/api/object-model/listRelationMObjects/{isVersion}")
    @ApiOperation("查询对象关系实例数据")
    TranscendApiResponse<List<MObject>> listRelationMObjectsWithVersion(@ApiParam("是否版本对象") @PathVariable Boolean isVersion,
                                                                        @RequestBody RelationMObject relationMObject);

    /**
     * relationTargetSelectPage
     *
     * @param sourceModelCode sourceModelCode
     * @param qo              qo
     * @return {@link TranscendApiResponse<PagedResult<MObject>>}
     */
    @ApiOperation("关系下拉选项的分页查询（作为目标对象查询以源对象的可选集合）")
    @PostMapping("/data-driven/api/object-model/relation/target-select/{sourceModelCode}/page")
    TranscendApiResponse<PagedResult<MObject>> relationTargetSelectPage(@ApiParam("模型编码") @PathVariable String sourceModelCode,
                                                                        @ApiParam("向上寻址的信息") @RequestBody RelationCrossLevelQo qo);

    /**
     * getCommonCheckBox
     *
     * @param request request
     * @return {@link TranscendApiResponse<List<CheckBoxDTO>>}
     */
    @ApiOperation("查询所有软件版本号的")
    @PostMapping("/ipm-tones/project/info/getCommonCheckBox")
    TranscendApiResponse<List<CheckBoxDTO>> getCommonCheckBox(@RequestBody GetEnumRequest request);

    /**
     * queryAllProjectName
     *
     * @return {@link TranscendApiResponse<List<DictViewVO>>}
     */
    @ApiOperation("查询所有机型")
    @GetMapping("/mp-project/queryAllProjectName")
    TranscendApiResponse<List<DictViewVO>> queryAllProjectName();

    /**
     * plmQueryDictByCodes
     *
     * @param code code
     * @return {@link TranscendApiResponse<List<PlmDictViewVO>>}
     */
    @ApiOperation("查所有字典")
    @GetMapping("/pdc/dict/plmQueryDictByCodes")
    TranscendApiResponse<List<PlmDictViewVO>> plmQueryDictByCodes(@RequestParam("code") String code);

    /**
     * getDictRelationByCode
     *
     * @param dictRelationPO dictRelationPO
     * @return {@link TranscendApiResponse<Map<String, Object>>}
     */
    @ApiOperation("根据软件版本查询相关数据")
    @PostMapping("/pdc/dictRelation/getDictRelationByCode")
    TranscendApiResponse<Map<String, Object>> getDictRelationByCode(@RequestBody RelationCodeDTO dictRelationPO);
}
