package com.transcend.plm.configcenter.object.controller;

import com.transcend.plm.configcenter.common.constant.CacheNameConstant;
import com.transcend.plm.configcenter.object.application.service.ICfgObjectAppService;
import com.transcend.plm.configcenter.api.model.object.dto.*;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectAttributeVo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectTreeVo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 对象控制层
 * 只能调用AppService，不能调用实体或者仓储
 *
 * @author jie.luo1
 * @version: 1.0
 * @date 2023/01/29 18:15
 */
@Slf4j
@Api(value = "CfgObject Controller", tags = "对象管理-控制器")
@Validated
@RestController
@RequestMapping(value = "/manager/cfg/object")
public class CfgObjectController {

    @Resource
    private ICfgObjectAppService objectModelAppService;

    @ApiOperation(value = "查询对象树以及锁信息")
    @GetMapping("treeAndLockInfo")
    public List<CfgObjectTreeVo> treeAndLockInfo() {
        return objectModelAppService.treeAndLockInfo();
    }


    @ApiOperation(value = "查询对象树")
    @GetMapping("tree")
    public List<CfgObjectTreeVo> tree() {
        return objectModelAppService.tree();
    }

    @ApiOperation(value = "查询->基类模型对象")
    @GetMapping("listByBaseModel")
    public List<CfgObjectVo> listByBaseModel(String baseModel) {
        return objectModelAppService.listByBaseModel(baseModel);
    }

    @ApiOperation(value = "增加对象")
    @PostMapping("add")
    public CfgObjectVo add(@RequestBody ObjectAddParam objectAddParam) {
        return objectModelAppService.add(objectAddParam);
    }

    @ApiOperation(value = "新增或修改对象")
    @PostMapping("saveOrUpdate")
    public CfgObjectVo saveOrUpdate(@RequestBody ObjectAddParam objectAddParam) {
        return objectModelAppService.saveOrUpdate(objectAddParam);
    }

    @ApiOperation(value = "删除对象（级联删除所有子对象及对象的各类信息）")
    @PostMapping("deleteWithChildrenAndInfo/{bid}")
    public Boolean deleteWithChildrenAndInfo(@PathVariable String bid) {
        return objectModelAppService.deleteWithChildrenAndInfo(bid);
    }

    @ApiOperation(value = "批量编辑 - 针对同级拖拉 && 树结构上名称编辑")
    @PostMapping("updatePosition")
    public Boolean updatePosition(@RequestBody List<ObjectPositionParam> objectPositionParamList) {
        return objectModelAppService.updatePosition(objectPositionParamList);
    }


    @ApiOperation(value = "查询单个对象的详情（包括对象基本信息，属性，生命周期，权限，视图...）")
    @PostMapping("getObjectAndAll/{bid}")
    public CfgObjectVo getOne(@PathVariable String bid) {
        return objectModelAppService.getObjectAndAll(bid);
    }

    @ApiOperation(value = "对象检出(并返回对象+属性)")
    @PostMapping("checkout/{bid}")
    public CfgObjectVo checkout(@PathVariable String bid) {
        objectModelAppService.checkout(bid);
        return objectModelAppService.getObjectAndAttribute(bid);
    }

    @ApiOperation(value = "暂存")
    @PostMapping("staging")
    public Boolean staging(@RequestBody StagingParam stagingParam) {
        return objectModelAppService.staging(stagingParam);
    }

    @ApiOperation(value = "读取草稿")
    @PostMapping("getAndAttributeDraft/{bid}")
    public CfgObjectVo readDraft(@PathVariable String bid) {
        return objectModelAppService.readDraft(bid);
    }


    @ApiOperation(value = "读取对象+属性数据（返回真实数据或者草稿数据）")
    @PostMapping("getObjectAndAttributeOrDraft/{bid}")
    public CfgObjectVo getObjectAndAttributeOrDraft(@PathVariable String bid) {
        return objectModelAppService.getObjectAndAttributeOrDraft(bid);
    }

    @ApiOperation(value = "撤销对象检出")
    @PostMapping("undoCheckout/{bid}")
    public Boolean undoCheckout(@PathVariable String bid) {
        return objectModelAppService.undoCheckout(bid);
    }

    @ApiOperation(value = "对象检入")
    @PostMapping("checkin")
    @CacheEvict(value = CacheNameConstant.OBJECT_MODEL, allEntries = true)
    public Boolean checkin(@RequestBody CheckinParam checkinParam) {
        return objectModelAppService.checkin(checkinParam);
    }

    @ApiOperation(value = "新增对象和属性")
    @PostMapping("addObjectAndAttr")
    public CfgObjectVo addObjectAndAttr(@RequestBody ObjectAndAttrAddParam objectAndAttrAddParam) {
        return objectModelAppService.addObjectAndAttr(objectAndAttrAddParam);
    }

    @ApiOperation(value = "新增对象和属性")
    @PostMapping("editObjectAndAttr")
    public CfgObjectVo editObjectAndAttr(@RequestBody CheckinParam checkinParam) {
        return objectModelAppService.editObjectAndAttr(checkinParam);
    }

    @ApiOperation(value = "查询对象列表（只有基本信息）")
    @GetMapping("list")
    public List<CfgObjectVo> list() {
        return objectModelAppService.list();
    }

    @ApiOperation(value = "根据modelCode查询其本身及其所有子对象列表（只有基本信息）")
    @GetMapping("/{modelCode}/listChildrenByModelCode")
    public List<CfgObjectVo> listChildrenByModelCode(@PathVariable String modelCode) {
        return objectModelAppService.listChildrenByModelCode(modelCode);
    }

    @ApiOperation(value = "根据modelCodeList查询特定的对象List（只有基本信息）")
    @PostMapping("listByModelCodes")
    public List<CfgObjectVo> listByModelCodes(@RequestBody List<String> modelCodes) {
        return objectModelAppService.listByModelCodes(modelCodes);
    }

    @ApiOperation(value = "根据nameList查询对象List（只有基本信息）")
    @PostMapping("listByNames")
    public List<CfgObjectVo> listByNames(@RequestBody List<String> nameList) {
        return objectModelAppService.listByNames(nameList);
    }

    @ApiOperation(value = "根据nameList查询对象List（只有基本信息）")
    @GetMapping("listLikeName")
    public List<CfgObjectVo> listLikeName(String name) {
        return objectModelAppService.listLikeName(name);
    }

    @ApiOperation(value = "查询单个对象的详情（包括对象基本信息，属性）")
    @PostMapping("getObjectAndAttribute/{bid}")
    public CfgObjectVo getObjectAndAttribute(@PathVariable String bid) {
        return objectModelAppService.getObjectAndAttribute(bid);
    }

    @ApiOperation(value = "查询单个对象的详情（包括对象基本信息，属性）")
    @PostMapping("getObjectAndAttributeByModelCode/{modelCode}")
    public CfgObjectVo getObjectAndAttributeByModelCode(@PathVariable String modelCode) {
        return objectModelAppService.getObjectAndAttributeByModelCode(modelCode);
    }

    @ApiOperation(value = "查询单个对象的详情（只有对象基本信息）")
    @PostMapping("get/{bid}")
    public CfgObjectVo get(@PathVariable String bid) {
        return objectModelAppService.get(bid);
    }

    @ApiOperation(value = "根据对象modelCode查询对象属性（包含父对象继承下来的属性）")
    @GetMapping("attribute/{modelCode}/listAttrsByModelCode")
    public List<CfgObjectAttributeVo> listAttrsByModelCode(@PathVariable String modelCode) {
        return objectModelAppService.listAttrsByModelCode(modelCode);
    }

    @ApiOperation(value = "根据对象modelCodeList查询多对象属性（包含父对象继承下来的属性） - Map<modelCode, 对应的attrs>")
    @PostMapping("attribute/listAttributesByModelCodes")
    public Map<String, List<CfgObjectAttributeVo>> listAttrsByModelCodes(@RequestBody List<String> modelCodeList) {
        return objectModelAppService.listAttributesByModelCodes(modelCodeList);
    }

    /**
     * 上面入参为modelCode的接口
     * ==================优--雅--的--分--割--线==================
     * 下面入参为bid的接口
     */


    public List<String> findParentBidListByBid(String bid) {
        return objectModelAppService.findParentBidListByBid(bid);
    }


    public List<CfgObjectVo> findChildrenListByBid(String bid) {
        return objectModelAppService.findChildrenListByBid(bid);
    }


    public CfgObjectAttributeVo findAttrByBid(AttrFindParam attrFindParam) {
        return objectModelAppService.findAttrByBid(attrFindParam);
    }


    public List<CfgObjectAttributeVo> findAttrsByObjectBid(String bid) {
        return objectModelAppService.findAttrsByBid(bid);
    }


    public List<CfgObjectAttributeVo> findChildrenAttrsByObjectBid(String objBid) {
        return objectModelAppService.findChildrenAttrsByObjectBid(objBid);
    }


    public CfgObjectVo getOneJustAttrByBid(String bid) {
        return objectModelAppService.getOneJustAttrByBid(bid);
    }


    public List<CfgObjectVo> findListByBidList(List<String> bidList) {
        return objectModelAppService.listByBids(bidList);
    }


    public Map<String, List<CfgObjectAttributeVo>> findAttrsByObjectBidList(List<String> objectBidList) {
        return objectModelAppService.findAttrsByObjectBidList(objectBidList);
    }


    public CfgObjectVo getOneHistoryJustAttrByBidAndVersion(String bid, Integer version) {
        return objectModelAppService.getOneHistoryByBidAndVersion(bid, version);
    }
}
