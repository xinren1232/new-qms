package com.transcend.plm.datadriven.apm.space.service;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.api.model.dto.LifeCyclePromoteDto;
import com.transcend.plm.datadriven.api.model.dto.MoveGroupNodeParam;
import com.transcend.plm.datadriven.api.model.dto.MoveTreeNodeParam;
import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transcend.plm.datadriven.api.model.qo.ResourceQo;
import com.transcend.plm.datadriven.apm.dto.DeleteRelDto;
import com.transcend.plm.datadriven.apm.dto.StateDataDriveDto;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowDriveRelate;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.StateDataDriveAO;
import com.transcend.plm.datadriven.apm.space.model.ApmSpaceAppDataDrivenOperationFilterBo;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.pojo.dto.MultiObjectUpdateDto;
import com.transcend.plm.datadriven.apm.space.pojo.qo.*;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmLaneGroupData;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ResourceVo;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 基础对象数据驱动service
 * todo 目前将之前IApmSpaceAppDataDrivenService的方法全部迁移到此类中，后续需要在细分
 *
 * @author yinbin
 * @version:
 * @date 2023/12/18 16:57
 */
public interface IBaseApmSpaceAppDataDrivenService {
    /**
     * 新增
     *
     * @param spaceAppBid
     * @param mSpaceAppData
     * @return
     */
    MSpaceAppData add(String spaceAppBid, MSpaceAppData mSpaceAppData);

    /**
     * add
     *
     * @param spaceAppBid   spaceAppBid
     * @param mSpaceAppData mSpaceAppData
     * @param filterBo      filterBo
     * @return {@link MSpaceAppData}
     */
    MSpaceAppData add(String spaceAppBid, MSpaceAppData mSpaceAppData,
                      ApmSpaceAppDataDrivenOperationFilterBo filterBo);

    /**
     * 逻辑删除空间
     *
     * @param spaceAppBid spaceAppBid
     * @param bid         bid
     * @return boolean
     */
    boolean logicalDelete(String spaceAppBid, String bid);


    /**
     * 逻辑删除空间 携带操作空间Bid
     *
     * @param spaceAppBid   spaceAppBid
     * @param operateAppBid operateAppBid
     * @param bid           bid
     * @return boolean
     */
    boolean logicDeleteWithOperateAppBid(String spaceAppBid, String bid, String operateAppBid);

    /**
     * 更新
     *
     * @param spaceAppBid   spaceAppBid
     * @param bid           bid
     * @param mSpaceAppData mSpaceAppData
     * @return {@link boolean}
     */
    boolean update(String spaceAppBid, String bid, MSpaceAppData mSpaceAppData);

    /**
     * get
     *
     * @param spaceAppBid     spaceAppBid
     * @param bid             bid
     * @param checkPermission checkPermission
     * @return {@link MSpaceAppData}
     */
    MSpaceAppData get(String spaceAppBid, String bid, boolean checkPermission);

    MSpaceAppData getUpdatePropertyStatus(String spaceAppBid, String bid, boolean checkPermission);

    /**
     * listByModelMixQo
     *
     * @param spaceAppBid spaceAppBid
     * @param modelMixQo  modelMixQo
     * @return {@link List<MSpaceAppData>}
     */
    List<MSpaceAppData> listByModelMixQo(String spaceAppBid, ModelMixQo modelMixQo);


    /**
     * 批量查询
     *
     * @param spaceAppBid spaceAppBid
     * @param bids        bids
     * @return {@link List<MSpaceAppData>}
     */
    List<MSpaceAppData> batchGet(String spaceAppBid, List<String> bids);


    /**
     * 批量查询跨应用的数据
     *
     * @param bids bids
     * @return {@link Map<String, List<MSpaceAppData>>}
     */
    Map<String, List<MSpaceAppData>> batchSpaceAppMapGet(Map<String, List<String>> bids);


    /**
     * 列表查询
     *
     * @param spaceAppBid    spaceAppBid
     * @param queryCondition queryCondition
     * @return {@link List<MSpaceAppData>}
     */
    List<MSpaceAppData> list(String spaceAppBid, QueryCondition queryCondition);


    /**
     * 分页列表查询
     *
     * @param spaceAppBid    spaceAppBid
     * @param pageQo         pageQo
     * @param filterRichText filterRichText
     * @return {@link PagedResult<MSpaceAppData>}
     */
    PagedResult<MSpaceAppData> page(String spaceAppBid, BaseRequest<QueryCondition> pageQo, boolean filterRichText);

    /**
     * 分页列表查询
     *
     * @param spaceAppBid    spaceAppBid
     * @param pageQo         pageQo
     * @param filterRichText filterRichText
     * @return {@link PagedResult<MSpaceAppData>}
     */
    PagedResult<MSpaceAppData> pageWithoutPermission(String spaceAppBid, BaseRequest<QueryCondition> pageQo, boolean filterRichText);


    /**
     * 更新一个属性
     *
     * @param spaceAppBid   spaceAppBid
     * @param bid           bid
     * @param mSpaceAppData mSpaceAppData
     * @return {@link Boolean}
     */
    Boolean updatePartialContent(String spaceAppBid, String bid, MSpaceAppData mSpaceAppData);

    /**
     * updatePartialContent
     *
     * @param spaceAppBid   spaceAppBid
     * @param bid           bid
     * @param mSpaceAppData mSpaceAppData
     * @param isRecordLog   isRecordLog
     * @return {@link Boolean}
     */
    Boolean updatePartialContent(String spaceAppBid, String bid, MSpaceAppData mSpaceAppData, Boolean isRecordLog);


    /**
     * 分组列表
     *
     * @param spaceAppBid    spaceAppBid
     * @param groupProperty  groupProperty
     * @param queryCondition queryCondition
     * @return {@link GroupListResult<MSpaceAppData>}
     */
    GroupListResult<MSpaceAppData> groupList(String spaceAppBid, String groupProperty, QueryCondition queryCondition);


    /**
     * 简单树树查询(单一空间)
     *
     * @param spaceAppBid spaceAppBid
     * @param wrappers    wrappers
     * @return {@link List<MSpaceAppData>}
     */
    List<MSpaceAppData> sampleTree(String spaceAppBid, List<QueryWrapper> wrappers);


    /**
     * 单对象多空间树查询
     *
     * @param spaceBid       spaceBid
     * @param spaceAppBid    spaceAppBid
     * @param modelMixQo     modelMixQo
     * @param filterRichText filterRichText
     * @return {@link List<MSpaceAppData>}
     */
    List<MSpaceAppData> signObjectAndMultiSpaceTree(String spaceBid, String spaceAppBid, ModelMixQo modelMixQo, boolean filterRichText);

    /**
     * 单对象多空间树查询
     *
     * @param spaceBid       spaceBid
     * @param spaceAppBid    spaceAppBid
     * @param modelMixQo     modelMixQo
     * @param filterRichText filterRichText
     * @return {@link List<MSpaceAppData>}
     */
    List<MObjectTree> signObjectAndMultiSpaceTreeList(String spaceBid, String spaceAppBid, ModelMixQo modelMixQo, boolean filterRichText);


    /**
     * 移动树节点
     *
     * @param spaceAppBid        spaceAppBid
     * @param moveTreeNodeParams moveTreeNodeParams
     * @return {@link Boolean}
     */
    Boolean moveTreeNode(String spaceAppBid, List<MoveTreeNodeParam> moveTreeNodeParams);


    /**
     * 移动树节点
     *
     * @param spaceBid           spaceBid
     * @param modelCode          modelCode
     * @param moveTreeNodeParams moveTreeNodeParams
     * @return {@link Boolean}
     */
    Boolean moveTreeNodeByModelCode(String spaceBid, String modelCode, List<MoveTreeNodeParam> moveTreeNodeParams);


    /**
     * 多对象树查询
     *
     * @param spaceBid        空间bid
     * @param spaceAppBid     空间应用bid
     * @param modelMixQo      查询条件
     * @param filterRichText  是否过滤富文本
     * @param checkPermission 是否校验权限
     * @return List<MObjectTree>
     * @version: 1.0
     * @date: 2023/10/25 11:07
     * @author: bin.yin
     */
    List<MObjectTree> multiTree(String spaceBid, String spaceAppBid, ApmMultiTreeModelMixQo modelMixQo,
                                boolean filterRichText, Boolean checkPermission);


    /**
     * multiTreeGroupBy
     *
     * @param spaceBid      spaceBid
     * @param spaceAppBid   spaceAppBid
     * @param groupProperty groupProperty
     * @param wrappers      wrappers
     * @param orders        orders
     * @return {@link GroupListResult<MSpaceAppData>}
     */
    GroupListResult<MSpaceAppData> multiTreeGroupBy(String spaceBid, String spaceAppBid, String groupProperty, List<QueryWrapper> wrappers, List<Order> orders);

    /**
     * 多对象树复制
     *
     * @param mObjectTree     mObjectTree
     * @param copyInstanceBid copyInstanceBid
     * @param spaceBid        spaceBid
     * @param spaceAppBid     spaceAppBid
     * @return {@link Boolean}
     */
    boolean copyMultiTree(MObjectTree mObjectTree, String copyInstanceBid, String spaceBid, String spaceAppBid);


    /**
     * 批量逻辑删除
     *
     * @param spaceAppBid spaceAppBid
     * @param bids        bids
     * @return {@link Boolean}
     */
    Boolean batchLogicalDeleteByBids(String spaceAppBid, List<String> bids);


    /**
     * 批量逻辑删除
     *
     * @param spaceAppBid spaceAppBid
     * @param bids        bids
     * @return {@link Boolean}
     */
    Boolean batchLogicalDeleteByBids(String spaceAppBid, List<String> bids, Boolean checkPermission);




    /**
     * 批量逻辑删除 - 带操作应用Bid
     *
     * @param spaceAppBid spaceAppBid
     * @param qo          qo
     * @return {@link Boolean}
     */
    Boolean batchLogicalDelete(String spaceAppBid, BatchLogicDelAndRemQo qo);

    /**
     * 删除关系
     *
     * @param dto         dto
     * @param spaceAppBid 空间应用bid
     * @return {@link Boolean}
     */
    Boolean deleteRel(String spaceAppBid, DeleteRelDto dto);

    /**
     * addBatch
     *
     * @param spaceBid       spaceBid
     * @param spaceAppBid    spaceAppBid
     * @param targetMObjects targetMObjects
     * @param filterBo       filterBo
     * @return {@link Boolean}
     */
    Boolean addBatch(String spaceBid, String spaceAppBid, List<MSpaceAppData> targetMObjects
            , ApmSpaceAppDataDrivenOperationFilterBo filterBo);

    /**
     * 分组移动以及更改分组值
     *
     * @param spaceAppBid         空间应用bid
     * @param groupProperty       分组属性
     * @param moveGroupNodeParams 移动分组数据
     * @return Boolean
     */
    Boolean moveGroupNode(String spaceAppBid, String groupProperty, List<MoveGroupNodeParam> moveGroupNodeParams);

    /**
     * 更新部分属性并完成流程节点
     *
     * @param spaceAppBid   spaceAppBid
     * @param bid           bid
     * @param nodeBid       nodeBid
     * @param mSpaceAppData mSpaceAppData
     * @return {@link Boolean}
     */
    Boolean updatePartialContentAndCompleteFlowNode(String spaceAppBid, String bid, String nodeBid, MSpaceAppData mSpaceAppData);


    /**
     * 更新部分属性并完成流程节点
     *
     * @param spaceAppBid   spaceAppBid
     * @param bid           bid
     * @param nodeBid       nodeBid
     * @param mSpaceAppData mSpaceAppData
     * @param runEvent      是否执行事件
     * @return {@link Boolean}
     */
    Boolean updatePartialContentAndRollbackFlowNode(String spaceAppBid, String bid, String nodeBid, MSpaceAppData mSpaceAppData, boolean runEvent);


    /**
     * 操作配置的执行方法
     *
     * @param spaceAppBid   空间应用bid
     * @param instanceBid   实例bid
     * @param actionBid     操作Bid
     * @param mSpaceAppData mSpaceAppData
     * @return Boolean
     */
    MObject executeAction(String spaceAppBid, String instanceBid, String actionBid, MSpaceAppData mSpaceAppData);

    /**
     * 项目下人力资源数据统计
     *
     * @param resourceQo 查询条件
     * @return List<ResourceVo>
     */
    List<ResourceVo> getProjectResources(ResourceQo resourceQo);

    /**
     * 空间下人力资源数据统计
     *
     * @param spaceBid   空间bid
     * @param resourceQo 查询条件
     * @return List<ResourceVo>
     */
    List<ResourceVo> getSpaceResources(String spaceBid, ResourceQo resourceQo);

    /**
     * listLane
     *
     * @param spaceBid    spaceBid
     * @param spaceAppBid spaceAppBid
     * @param apmLaneQO   apmLaneQO
     * @return {@link ApmLaneGroupData}
     */
    ApmLaneGroupData listLane(String spaceBid, String spaceAppBid, ApmLaneQO apmLaneQO);

    /**
     * 组装状态驱动数据
     *
     * @param stateDataDriveAO   stateDataDriveAO
     * @param apmFlowDriveRelate apmFlowDriveRelate
     * @return {@link List < StateDataDriveDto>}
     */
    List<StateDataDriveDto> updateDriveState(StateDataDriveAO stateDataDriveAO, ApmFlowDriveRelate apmFlowDriveRelate);

    /**
     * 导入excel
     *
     * @param spaceAppBid 空间应用bid
     * @param file        excel文件
     * @param spaceBid    空间bid
     * @return List<MSpaceAppData>
     */
    List<MSpaceAppData> importExcel(String spaceAppBid, MultipartFile file, String spaceBid);

    /**
     * importExcelAndSave
     *
     * @param spaceAppBid spaceAppBid
     * @param file        file
     * @param spaceBid    spaceBid
     * @return {@link boolean}
     */
    boolean importExcelAndSave(String spaceAppBid, MultipartFile file, String spaceBid);


    /**
     * 检出
     *
     * @param spaceAppBid spaceAppBid
     * @param bid         bid
     * @return {@link MSpaceAppData}
     * @version: 1.0
     * @date: 2023/12/21 11:10
     * @author: bin.yin
     */
    MSpaceAppData checkOut(String spaceAppBid, String bid);

    /**
     * 批量检出 循环调用 待优化
     *
     * @param spaceAppBid spaceAppBid
     * @param bids        bids
     * @return java.util.List<java.util.Map < java.lang.String, com.transcend.plm.datadriven.apm.space.model.MSpaceAppData>>
     * @version: 1.0
     * @date: 2024/5/15 15:55
     * @author: bin.yin
     **/
    Map<String, MSpaceAppData> batchCheckOut(String spaceAppBid, List<String> bids);

    /**
     * 取消检出
     *
     * @param spaceAppBid spaceAppBid
     * @param bid         bid
     * @return {@link MSpaceAppData}
     * @version: 1.0
     * @date: 2023/12/21 11:10
     * @author: bin.yin
     */
    MSpaceAppData cancelCheckOut(String spaceAppBid, String bid);

    /**
     * 批量取消检出 循环调用 待优化
     *
     * @param spaceAppBid spaceAppBid
     * @param bids        bids
     * @return java.util.List<java.util.Map < java.lang.String, com.transcend.plm.datadriven.apm.space.model.MSpaceAppData>>
     * @version: 1.0
     * @date: 2024/5/15 15:58
     * @author: bin.yin
     **/
    Map<String, MSpaceAppData> batchCancelCheckOut(String spaceAppBid, List<String> bids);

    /**
     * 检入
     *
     * @param spaceAppBid spaceAppBid
     * @param mObject     mObject
     * @return {@link MSpaceAppData}
     * @version: 1.0
     * @date: 2023/12/21 11:11
     * @author: bin.yin
     */
    MSpaceAppData checkIn(String spaceAppBid, MVersionObject mObject);

    /**
     * 批量检入 循环调用 待优化
     *
     * @param spaceAppBid spaceAppBid
     * @param list        list
     * @return java.util.List<java.util.Map < java.lang.String, com.transcend.plm.datadriven.apm.space.model.MSpaceAppData>>
     * @version: 1.0
     * @date: 2024/5/15 15:59
     * @author: bin.yin
     **/
    List<MSpaceAppData> batchCheckin(String spaceAppBid, List<MVersionObject> list);

    /**
     * 暂存草稿
     *
     * @param spaceAppBid spaceAppBid
     * @param mObject     mObject
     * @return Boolean
     * @version: 1.0
     * @date: 2023/12/21 11:11
     * @author: bin.yin
     */
    Boolean saveDraft(String spaceAppBid, MVersionObject mObject);

    /**
     * 批量暂存草稿 循环实现 待优化
     *
     * @param spaceAppBid spaceAppBid
     * @param draftList   draftList
     * @return Boolean
     * @version: 1.0
     * @date: 2023/12/21 11:11
     * @author: bin.yin
     */
    Boolean batchSaveDraft(String spaceAppBid, List<MVersionObject> draftList);

    /**
     * 查询历史版本
     *
     * @param spaceAppBid    spaceAppBid
     * @param dataBid        dataBid
     * @param queryCondition queryCondition
     * @return List<MSpaceAppData>
     * @version: 1.0
     * @date: 2023/12/21 11:11
     * @author: bin.yin
     */
    List<MSpaceAppData> listByHistory(String spaceAppBid, String dataBid, QueryCondition queryCondition);

    /**
     * 查询历史版本
     *
     * @param spaceAppBid    spaceAppBid
     * @param dataBid        dataBid
     * @param queryCondition queryCondition
     * @param filterRichText 是否过滤富文本
     * @return PagedResult<MSpaceAppData>
     * @version: 1.0
     * @date: 2023/12/21 11:11
     * @author: bin.yin
     */
    PagedResult<MSpaceAppData> pageListByHistory(String spaceAppBid, String dataBid, BaseRequest<QueryCondition> queryCondition, boolean filterRichText);


    /**
     * 去重属性列表
     *
     * @param spaceAppBid      空间应用bid
     * @param distinctProperty 去重属性
     * @param queryCondition   查询条件
     * @return List<Object>
     */
    List<Object> listPropertyDistinct(String spaceAppBid, String distinctProperty, QueryCondition queryCondition);

    /**
     * 修订
     *
     * @param spaceAppBid spaceAppBid
     * @param bid         bid
     * @return MSpaceAppData
     */
    MSpaceAppData revise(String spaceAppBid, String bid);

    /**
     * 提升生命周期
     *
     * @param spaceBid    spaceBid
     * @param spaceAppBid spaceAppBid
     * @param promoteDto  promoteDto
     * @return MSpaceAppData
     */
    MSpaceAppData promote(String spaceBid, String spaceAppBid, LifeCyclePromoteDto promoteDto);

    /**
     * 查询泳道全对象数据
     *
     * @param spaceBid    spaceBid
     * @param spaceAppBid spaceAppBid
     * @param laneAllQo   laneAllQo
     * @return ApmLaneGroupData
     */
    ApmLaneGroupData listAllLane(String spaceBid, String spaceAppBid, ApmLaneAllQo laneAllQo);

    /**
     * batchRemove
     *
     * @param spaceAppBid spaceAppBid
     * @param qo          qo
     * @return {@link Boolean}
     */
    Boolean batchRemove(String spaceAppBid, BatchLogicDelAndRemQo qo);

    /**
     * 批量移除中间关系
     *
     * @return
     */
    Boolean batchRemoveRelation(String spaceAppBid, BatchRemoveRelationQo qo);


    /**
     * 缓存最近10个用户
     *
     * @param spaceAppBid spaceAppBid
     * @param modelCode   modelCode
     * @param userIds     userIds
     * @return {@link List<Map<String, Object>>}
     */
    List<Map<String, Object>> userCache(String spaceAppBid, String modelCode, List<String> userIds);


    /**
     * 批量新增数据
     *
     * @param spaceBid          spaceBid
     * @param spaceAppBid       spaceAppBid
     * @param mSpaceAppDataList mSpaceAppDataList
     * @return {@link List<MSpaceAppData>}
     */
    List<MSpaceAppData> batchAdd(String spaceBid, String spaceAppBid, List<MSpaceAppData> mSpaceAppDataList);

    /**
     * 保存草稿数据
     *
     * @param spaceAppBid   spaceAppBid
     * @param mSpaceAppData mSpaceAppData
     * @return {@link MSpaceAppData}
     */
    MSpaceAppData saveCommonDraft(String spaceAppBid, MSpaceAppData mSpaceAppData);

    /**
     * addTonesData
     *
     * @param spaceAppBid   spaceAppBid
     * @param mSpaceAppData mSpaceAppData
     * @return {@link MSpaceAppData}
     */
    MSpaceAppData addTonesData(String spaceAppBid, MSpaceAppData mSpaceAppData);

    /**
     * batchAddForCad
     *
     * @param spaceBid          spaceBid
     * @param spaceAppBid       spaceAppBid
     * @param mSpaceAppDataList mSpaceAppDataList
     * @return {@link String}
     */
    String batchAddForCad(String spaceBid, String spaceAppBid, List<MSpaceAppData> mSpaceAppDataList);

    /**
     * batchAddForCadResult
     *
     * @param spaceBid    spaceBid
     * @param spaceAppBid spaceAppBid
     * @param resultId    resultId
     * @return {@link List<MSpaceAppData>}
     */
    List<MSpaceAppData> batchAddForCadResult(String spaceBid, String spaceAppBid, String resultId);

    /**
     * batchCheckinAsync
     *
     * @param spaceAppBid spaceAppBid
     * @param list        list
     * @return {@link String}
     */
    String batchCheckinAsync(String spaceAppBid, List<MVersionObject> list);

    /**
     * batchCheckinAsyncProgress
     *
     * @param spaceAppBid spaceAppBid
     * @param resultId    resultId
     * @return {@link Map<String,Integer>}
     */
    Map<String, Integer> batchCheckinAsyncProgress(String spaceAppBid, String resultId);

    /**
     * 批量更新部分内容
     *
     * @param spaceBid
     * @param spaceAppBid   spaceAppBid
     * @param mSpaceAppData 待更新数据
     * @param bids          待更新数据的bids
     * @return {@link Boolean}
     */
    Boolean batchUpdatePartialContentByBids(String spaceBid, String spaceAppBid, List<String> bids, MSpaceAppData mSpaceAppData);

    Boolean multiObjectPartialContent(String spaceAppBid, List<MultiObjectUpdateDto> multiObjectUpdateDtoList);

    /**
     * 批量检查权限
     * @param spaceBid
     * @param spaceAppBid
     * @param bids
     * @param operation
     * @return
     */
    Boolean batchOperationCheckPermission(String spaceBid, String spaceAppBid, List<String> bids, String operation);

    /**
     * 批量逻辑删除(根据视图模式)
     *
     * @param viewModel 视图模式
     * @param spaceAppBid spaceAppBid
     * @param bids        bids
     * @return
     */
    Boolean batchViewModelLogicalDeleteByBids(String viewModel, String spaceAppBid, List<String> bids);
}
