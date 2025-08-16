//package com.transcend.plm.configcenter.object.api;
//
//
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import org.springframework.web.bind.annotation.*;
//
//import javax.validation.Valid;
//import java.util.List;
//import java.util.Map;
//
///**
// * 对象模型Api
// *
// * @author jingfang.luo
// * @version: 1.0
// * @date 2022/11/24 11:40
// */
//@Api(tags = "对象模型Api", value = "对象模型Api")
//public interface ObjectModelApi {
//
//    @ApiOperation(value = "查询对象树")
//    @GetMapping("/mp-object-model/object/findTree")
//    ResponseVO<List<ObjectModelTreeVO>> findTree();
//
//    @ApiOperation(value = "查询->基类模型对象")
//    @GetMapping("/mp-object-model/object/listByRootModel")
//    ResponseVO<List<ObjectModelVO>> listByRootModel(@RequestParam String rootObject);
//
//    @ApiOperation(value = "增加对象")
//    @PostMapping("/mp-object-model/object/add")
//    ResponseVO<ObjectModelVO> add(@Valid @RequestBody ObjectAddParam objectAddParam);
//
//    @ApiOperation(value = "删除对象（级联删除所有子对象及对象的各类信息）")
//    @PostMapping("/mp-object-model/object/deleteWithChildrenAndInfo")
//    ResponseVO<Boolean> deleteWithChildrenAndInfo(@Valid @RequestBody ObjectDeleteParam objectDeleteParam);
//
//    @ApiOperation(value = "批量编辑 - 针对同级拖拉 && 树结构上名称编辑")
//    @PostMapping("/mp-object-model/object/edit")
//    ResponseVO<Boolean> edit(@Valid @RequestBody List<ObjectEditParam> objectEditParamList);
//
//    @ApiOperation(value = "查询单个对象的详情（包括对象基本信息，属性，生命周期，权限，视图...）")
//    @PostMapping("/mp-object-model/object/getOneObject")
//    ResponseVO<ObjectModelVO> getOne(@Valid @RequestBody ObjectGetOneParam objectGetOneParam);
//
//    @ApiOperation(value = "对象检出")
//    @PostMapping("/mp-object-model/object/checkout")
//    ResponseVO<Boolean> checkout(@RequestBody CheckoutParam checkoutParam);
//
//    @ApiOperation(value = "暂存")
//    @PostMapping("/mp-object-model/object/staging")
//    ResponseVO<Boolean> staging(@RequestBody StagingParam stagingParam);
//
//    @ApiOperation(value = "读取草稿")
//    @PostMapping("/mp-object-model/object/readDraft")
//    ResponseVO<ObjectModelVO> readDraft(@Valid @RequestBody ReadDraftParam readDraftParam);
//
//    @ApiOperation(value = "撤销对象检出")
//    @PostMapping("/mp-object-model/object/undoCheckout")
//    ResponseVO<Boolean> undoCheckout(@RequestBody UndoCheckoutParam undoCheckoutParam);
//
//    @ApiOperation(value = "对象检入")
//    @PostMapping("/mp-object-model/object/checkin")
//    ResponseVO<Boolean> checkin(@RequestBody CheckinParam checkinParam);
//
//    @ApiOperation(value = "新增对象和属性")
//    @PostMapping("/mp-object-model/object/addObjectAndAttr")
//    ResponseVO<ObjectModelVO> addObjectAndAttr(@Valid @RequestBody ObjectAndAttrAddParam objectAndAttrAddParam);
//
//    @ApiOperation(value = "新增对象和属性")
//    @PostMapping("/mp-object-model/object/editObjectAndAttr")
//    ResponseVO<ObjectModelVO> editObjectAndAttr(@RequestBody CheckinParam checkinParam);
//
//    /**
//     * 上面为基础接口
//     * ==================优--雅--的--分--割--线==================
//     * 下面为扩展接口
//     */
//
//    @ApiOperation(value = "查询对象列表（只有基本信息）")
//    @GetMapping("/mp-object-model/object/list")
//    ResponseVO<List<ObjectModelVO>> list();
//
//    @ApiOperation(value = "根据modelCode查询其本身及其所有子对象列表（只有基本信息）")
//    @GetMapping("/mp-object-model/object/{modelCode}/findChildrenListByModelCode")
//    ResponseVO<List<ObjectModelVO>> findChildrenListByModelCode(@PathVariable("modelCode") String modelCode);
//
//    @ApiOperation(value = "根据modelCodeList查询特定的对象List（只有基本信息）")
//    @PostMapping("/mp-object-model/object/findListByModelCodeList")
//    ResponseVO<List<ObjectModelVO>> findListByModelCodeList(@RequestBody List<String> modelCodeList);
//
//    @ApiOperation(value = "根据nameList查询对象List（只有基本信息）")
//    @PostMapping("/mp-object-model/object/findListByNameList")
//    ResponseVO<List<ObjectModelVO>> findListByNameList(@RequestBody List<String> nameList);
//
//    @ApiOperation(value = "根据nameList查询对象List（只有基本信息）")
//    @GetMapping("/mp-object-model/object/findLikeName")
//    ResponseVO<List<ObjectModelVO>> findLikeName(@RequestParam("name") String name);
//
//    @ApiOperation(value = "查询单个对象的详情（包括对象基本信息，属性）")
//    @PostMapping("/mp-object-model/object/getOneObjectJustAttr")
//    ResponseVO<ObjectModelVO> getOneJustAttr(@Valid @RequestBody ObjectGetOneParam objectGetOneParam);
//
//    @ApiOperation(value = "查询单个对象的详情（只有对象基本信息）")
//    @PostMapping("/mp-object-model/object/getOneJustObject")
//    ResponseVO<ObjectModelVO> getOneJustObject(@Valid @RequestBody ObjectGetOneParam objectGetOneParam);
//
//    @ApiOperation(value = "根据对象modelCode查询对象属性（包含父对象继承下来的属性）")
//    @GetMapping("/mp-object-model/attr/{modelCode}/findAttrsByModelCode")
//    ResponseVO<List<ObjectModelAttrVO>> findAttrsByModelCode(@PathVariable("modelCode") String modelCode);
//
//    @ApiOperation(value = "根据对象modelCodeList查询多对象属性（包含父对象继承下来的属性） - Map<modelCode, 对应的attrs>")
//    @PostMapping("/mp-object-model/attr/findAttrsByModelCodeList")
//    ResponseVO<Map<String, List<ObjectModelAttrVO>>> findAttrsByModelCodeList(@RequestBody List<String> modelCodeList);
//
//    /**
//     * 上面入参为modelCode的接口
//     * ==================优--雅--的--分--割--线==================
//     * 下面入参为bid的接口
//     */
//
//    @ApiOperation(value = "根据bid查询父对象的bidList（包含本身，从小到大排序）")
//    @GetMapping("/mp-object-model/object/{bid}/findParentBidListByBid")
//    ResponseVO<List<String>> findParentBidListByBid(@PathVariable("bid") String bid);
//
//    @ApiOperation(value = "根据bid查询其本身及其所有子对象列表（只有基本信息）")
//    @GetMapping("/mp-object-model/object/{bid}/findChildrenListByBid")
//    ResponseVO<List<ObjectModelVO>> findChildrenListByBid(@PathVariable("bid") String bid);
//
//    @ApiOperation(value = "根据对象的bid和属性bid查询单条属性")
//    @PostMapping("/mp-object-model/attr/findAttrsByBid")
//    ResponseVO<ObjectModelAttrVO> findAttrByBid(@RequestBody AttrFindParam attrFindParam);
//
//    @ApiOperation(value = "根据对象bid查询对象属性（包含父对象继承下来的属性）")
//    @GetMapping("/mp-object-model/attr/{bid}/findAttrsByObjectBid")
//    ResponseVO<List<ObjectModelAttrVO>> findAttrsByObjectBid(@PathVariable("bid") String bid);
//
//    @ApiOperation(value = "根据根节点bid查询出其以及子的所有属性（注意只能是根节点）")
//    @GetMapping("/mp-object-model/attr/{objBid}/findChildrenAttrsByObjectBid")
//    ResponseVO<List<ObjectModelAttrVO>> findChildrenAttrsByObjectBid(@PathVariable("objBid") String objBid);
//
//    @ApiOperation(value = "根据对象bid查询单个对象的详情（包括对象基本信息，属性，生命周期，权限，视图...）")
//    @GetMapping("/mp-object-model/object/{bid}/getOneObject")
//    ResponseVO<ObjectModelVO> getOneByBid(@PathVariable("bid") String bid);
//
//    @ApiOperation(value = "根据对象bid查询单个对象的详情（包括对象基本信息，属性）")
//    @GetMapping("/mp-object-model/object/{bid}/getOneObjectJustAttr")
//    ResponseVO<ObjectModelVO> getOneJustAttrByBid(@PathVariable("bid") String bid);
//
//    @ApiOperation(value = "根据对象bid查询单个对象的详情（只有对象基本信息）")
//    @GetMapping("/mp-object-model/object/{bid}/getOneJustObject")
//    ResponseVO<ObjectModelVO> getOneJustObjectByBid(@PathVariable("bid") String bid);
//
//    @ApiOperation(value = "根据bidList查询特定的对象List（只有对象基本信息）")
//    @PostMapping("/mp-object-model/object/findListByBidList")
//    ResponseVO<List<ObjectModelVO>> findListByBidList(@RequestBody List<String> bidList);
//
//    @ApiOperation(value = "根据对象bidList查询多对象属性（包含父对象继承下来的属性） - Map<objectBid, 对应的attrs>")
//    @PostMapping("/mp-object-model/attr/findAttrsByObjectBidList")
//    ResponseVO<Map<String, List<ObjectModelAttrVO>>> findAttrsByObjectBidList(@RequestBody List<String> objectBidList);
//
//    /**
//     * 历史对象相关接口
//     * ==================优--雅--的--分--割--线==================
//     * 下面入参为bid的接口
//     */
//    @ApiOperation(value = "查询历史 单个对象的详情（包括对象基本信息，属性）")
//    @GetMapping("/mp-object-model/object/{bid}/{version}/getOneHistoryJustAttrByBidAndVersion")
//    ResponseVO<ObjectModelVO> getOneHistoryJustAttrByBidAndVersion(@PathVariable("bid") String bid, @PathVariable("version") Integer version);
//}
