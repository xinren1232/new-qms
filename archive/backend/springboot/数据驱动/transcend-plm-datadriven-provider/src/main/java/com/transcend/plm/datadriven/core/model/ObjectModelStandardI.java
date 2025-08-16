package com.transcend.plm.datadriven.core.model;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.api.model.object.vo.ObjectModelLifeCycleVO;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.api.model.dto.LifeCyclePromoteDto;
import com.transcend.plm.datadriven.api.model.dto.MObjectCheckDto;
import com.transcend.plm.datadriven.api.model.dto.ReviseDto;
import com.transcend.plm.datadriven.api.model.qo.RelationCrossLevelQo;
import com.transsion.framework.dto.BaseRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 增删改查接口
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/5/18 17:11
 * @since 1.0
 */
public interface ObjectModelStandardI<M extends MObject> extends StandardI<M> {

    /**
     * setMObjectDefaultValue
     *
     * @param mObject                mObject
     * @param objectModelLifeCycleVO objectModelLifeCycleVO
     * @param modelCode              modelCode
     */
    void setMObjectDefaultValue(MObject mObject, ObjectModelLifeCycleVO objectModelLifeCycleVO, String modelCode);

    /**
     * 新增方法
     *
     * @param modelCode 模型code
     * @param mObject   模型实例
     * @return MObject
     */
    M add(String modelCode, M mObject);


    /**
     * 逻辑删除方法
     *
     * @param modelCode 模型code
     * @param bid       实例bid
     * @return Boolean
     */
    Boolean logicalDelete(String modelCode, String bid);

    /**
     * 更新方法
     *
     * @param modelCode 模型code
     * @param mObject   模型实例
     * @param wrappers  查询条件
     * @return Boolean
     */
    Boolean update(String modelCode, M mObject, List<QueryWrapper> wrappers);

    /**
     * 根据bid列表批量更新 单个字段为相同值
     *
     * @param modelCode 模型code
     * @param mObject   模型实例
     * @param bids      bid列表
     * @return Boolean
     */
    Boolean batchUpdatePartialContentByIds(String modelCode, M mObject, List<String> bids);


    /**
     * 根据coding列表批量更新 单个字段为相同值
     *
     * @param modelCode 模型code
     * @param mObject   模型实例
     * @param codingList      coding列表
     * @return Boolean
     */
    Boolean batchUpdatePartialContentByCodingList(String modelCode, M mObject, List<String> codingList);

    /**
     * 批量更新数据 每条数据根据queryWrapper设置查询语句
     *
     * @param modelCode         modelCode
     * @param batchUpdateBoList batchUpdateBoList
     * @param isHistory         是否更新历史表
     * @return java.lang.Boolean
     */
    Boolean batchUpdateByQueryWrapper(String modelCode, List<BatchUpdateBO<M>> batchUpdateBoList, boolean isHistory);

    /**
     * 列表查询
     *
     * @param modelCode 模型code
     * @param wrappers  查询条件
     * @return Boolean
     */
    List<M> list(String modelCode, List<QueryWrapper> wrappers);

    /**
     * 列表查询
     *
     * @param modelCode      模型code
     * @param queryCondition 查询条件
     * @return Boolean
     */
    List<M> list(String modelCode, QueryCondition queryCondition);


    /**
     * 递归列表查询
     *
     * @param modelCode      模型code
     * @param queryCondition 查询条件
     * @return Boolean
     */
    List<M> signObjectRecursionTreeList(String modelCode, QueryCondition queryCondition);

    /**
     * listTree
     *
     * @param modelCode       modelCode
     * @param relationMObject relationMObject
     * @return {@link List<MObjectTree>}
     */
    List<MObjectTree> listTree(String modelCode, RelationMObject relationMObject);

    /**
     * selectTree
     *
     * @param modelCode       modelCode
     * @param relationMObject relationMObject
     * @return {@link List }<{@link MObjectTree }>
     */
    List<MObjectTree> selectTree(String modelCode, RelationMObject relationMObject);

    /**
     * listTree
     *
     * @param list            list
     * @param relationMObject relationMObject
     * @param isSelect        是否选择
     * @return {@link List }<{@link MObjectTree }>
     */
    List<MObjectTree> listTree(List<MObject> list, RelationMObject relationMObject, Boolean isSelect);

    /**
     * 保存或者更新
     *
     * @param modelCode 模型code
     * @param mObject   对象实例数据
     * @return MObject
     */
    M saveOrUpdate(String modelCode, M mObject);

    /**
     * 根据bid查询
     *
     * @param modelCode 模型code
     * @param bid       业务bid
     * @return MObject
     */
    M getByBid(String modelCode, String bid);

    M getByBidNotDelete(String modelCode, String bid);

    /**
     * 根据bid查询
     *
     * @param modelCode    modelCode
     * @param bid          业务bid
     * @param isObjVersion 是否查询历史版本
     * @return {@link M }
     */
    M getByBid(String modelCode, String bid, Boolean isObjVersion);

    /**
     * 根据bid查询历史版本
     *
     * @param modelCode modelCode
     * @param bid       业务bid
     * @return {@link M }
     */
    M getHisByBid(String modelCode, String bid);

    /**
     * 分页查询
     *
     * @param modelCode      模型code
     * @param pageQo         分页查询条件
     * @param filterRichText 是否过滤富文本
     * @return Boolean
     */
    PagedResult<M> page(String modelCode, BaseRequest<QueryCondition> pageQo, boolean filterRichText);


    /**
     * 分页查询(需要部分结果集)
     *
     * @param modelCode      模型code
     * @param pageQo         分页查询条件
     * @param filterRichText 是否过滤富文本
     * @return Boolean
     */
    PagedResult<M> page(String modelCode, BaseRequest<QueryCondition> pageQo, boolean filterRichText, Set<String> fields);

    /**
     * 查询关系需要选择的数据，需要过滤已选择的数据
     *
     * @param modelCode       modelCode
     * @param mObjectCheckDto mObjectCheckDto
     * @return {@link List }<{@link MObject }>
     */
    List<MObject> relationSelectList(String modelCode, MObjectCheckDto mObjectCheckDto);

    /**
     * 查询关系需要选择的数据，需要过滤已选择的数据
     *
     * @param list            list
     * @param mObjectCheckDto mObjectCheckDto
     * @return {@link List }<{@link MObject }>
     */
    List<MObject> relationSelectList(List<MObject> list, MObjectCheckDto mObjectCheckDto);

    /**
     * 批量新增F
     *
     * @param modelCode 模型code
     * @param mObjects  实例数据集
     * @return Boolean
     */
    Boolean addBatch(String modelCode, List<MObject> mObjects);

    /**
     * 逻辑删除
     *
     * @param modelCode modelCode
     * @param bid       bid
     * @return Boolean
     */
    Boolean logicalDeleteByBid(String modelCode, String bid);

    /**
     * 批量逻辑删除
     *
     * @param modelCode modelCode
     * @param bids      bid
     * @return Boolean
     */
    Boolean batchLogicalDeleteByBids(String modelCode, List<String> bids);

    /**
     * 删除关系
     *
     * @param modelCode  modelCode
     * @param sourceBid  sourceBid
     * @param targetBids targetBids
     * @return {@link Boolean }
     */
    Boolean deleteRel(String modelCode, String sourceBid, List<String> targetBids);

    /**
     * 删除
     *
     * @param modelCode modelCode
     * @param bid       bid
     * @return Boolean
     */
    Boolean deleteByBid(String modelCode, String bid);

    /**
     * 更新
     *
     * @param modelCode modelCode
     * @param bid       bid
     * @param mObject   mObject
     * @return {@link Boolean }
     */
    Boolean updateByBid(String modelCode, String bid, MObject mObject);

    /**
     * 检出
     *
     * @param modelCode modelCode
     * @param bid       bid
     * @return {@link MVersionObject }
     */
    MVersionObject checkOut(String modelCode, String bid);

    /**
     * 撤销检出
     *
     * @param modelCode modelCode
     * @param bid       bid
     * @return {@link MVersionObject }
     */
    MVersionObject cancelCheckOut(String modelCode, String bid);

    /**
     * 检入
     *
     * @param modelCode modelCode
     * @param mObject   mObject
     * @return {@link MVersionObject }
     */
    MVersionObject checkIn(String modelCode, MVersionObject mObject);


    /**
     * 批量删除
     *
     * @param modelCode modelCode
     * @param bids      bids
     * @return {@link Boolean }
     */
    Boolean batchDeleteByBids(String modelCode, List<String> bids);

    /**
     * 保存草稿
     *
     * @param mObject mObject
     * @return {@link Boolean }
     */
    Boolean saveDraft(MObject mObject);

    /**
     * 关系目标对象新增
     *
     * @param relationMObject relationMObject
     * @return {@link Boolean }
     */
    Boolean addRelatons(RelationMObject relationMObject);

    /**
     * 关系目标对象逻辑删除
     *
     * @param relationMObject relationMObject
     * @return {@link Boolean }
     */
    Boolean logicalDeleteRelations(RelationMObject relationMObject);

    /**
     * 关系目标对象逻辑删除
     *
     * @param relationMObject relationMObject
     * @return {@link Boolean }
     */
    Boolean logicalDeleteRelationsByTargetBids(RelationMObject relationMObject);


    /**
     * 查询源实例挂在关系实例数据
     *
     * @param relationMObject relationMObject
     * @return {@link List }<{@link MObject }>
     */
    List<MObject> listRelationMObjects(RelationMObject relationMObject);

    /**
     * 查询源实例挂在关系实例数据
     *
     * @param isVersion       只有当源和目标对象都是版本化对象时，才是true，这个字段决定查目标对象实例是否查历史表，源对象实例是否是历史版本决定查关系实例是否查历史表
     * @param relationMObject relationMObject
     * @return {@link List }<{@link MObject }>
     */
    List<MObject> listRelationMObjects(Boolean isVersion, RelationMObject relationMObject);

    /**
     * 查询源实例挂在关系实例数据
     *
     * @param isVersion       是否是历史版本
     * @param relationMObject relationMObject
     * @param spaceAppBid     spaceAppBid
     * @param filterRichText  是否过滤富文本
     * @return {@link PagedResult }<{@link MObject }>
     */
    PagedResult<MObject> listRelationMObjectsPage(Boolean isVersion, BaseRequest<RelationMObject> relationMObject, String spaceAppBid, boolean filterRichText);

    /**
     * 查询源实例挂在关系实例数据
     *
     * @param targetVersion    是否是历史版本
     * @param pageParam        pageParam
     * @param relationBehavior relationBehavior
     * @param spaceAppBid      spaceAppBid
     * @param filterRichText   是否过滤富文本
     * @return {@link PagedResult }<{@link MObject }>
     */
    PagedResult<MObject> listRelationMObjectsPage(Boolean targetVersion, BaseRequest<RelationMObject> pageParam, String relationBehavior, String spaceAppBid, boolean filterRichText);

    /**
     * 查询源实例挂在关系实例数据
     *
     * @param relationModelCode relationModelCode
     * @param targetModelCode   targetModelCode
     * @param mObject           mObject
     * @param sourceBid
     * @return {@link List }<{@link MObject }>
     */
    List<MObject> listNoVersionRelationMObjects(String relationModelCode, String targetModelCode, MObject mObject, String sourceBid);


    /**
     * listNotVersionRelationMObjects
     *
     * @param relationMObject relationMObject
     * @return {@link List<MObject>}
     */
    List<MObject> listNotVersionRelationMObjects(RelationMObject relationMObject);

    /**
     * 查询源实例挂在关系实例数据
     *
     * @param relationMObject relationMObject
     * @return {@link List }<{@link MObject }>
     */
    List<MObject> listOnlyRelationMObjects(RelationMObject relationMObject);

    /**
     * 查看详情(草稿或者详情)
     *
     * @param modelCode modelCode
     * @param bid       bid
     * @return {@link MVersionObject }
     */
    MVersionObject getOrDraftByBid(String modelCode, String bid);

    /**
     * 查询源实例挂在关系实例数据
     *
     * @param relationMObject relationMObject
     * @return {@link Boolean }
     */
    Boolean deleteRelations(RelationMObject relationMObject);

    /**
     * 查询源实例挂在关系实例数据
     *
     * @param modelCode      modelCode
     * @param groupProperty  groupProperty
     * @param queryCondition queryCondition
     * @return {@link Map }<{@link String },{@link List }<{@link MObject }>>
     */
    Map<String, List<MObject>> groupList(String modelCode, String groupProperty, QueryCondition queryCondition);

    /**
     * listTree
     *
     * @param modelCode modelCode
     * @param wrappers  查询条件
     * @return {@link List }<{@link MObjectTree }>
     */
    List<MObjectTree> tree(String modelCode, List<QueryWrapper> wrappers);

    /**
     * moveTreeNode
     *
     * @param modelCode modelCode
     * @param treeList  treeList
     * @return {@link Boolean }
     */
    Boolean moveTreeNode(String modelCode, List<MObjectTree> treeList);

    /**
     * 获取关系对象树形列表
     *
     * @param relationMObject relationMObject
     * @return {@link List }<{@link MObjectTree }>
     */
    List<MObjectTree> relationTree(RelationMObject relationMObject);

    /**
     * 获取关系对象树形列表
     *
     * @param isVersion       是否是历史版本
     * @param relationMObject relationMObject
     * @param filterRichText  是否过滤富文本
     * @return {@link List }<{@link MObjectTree }>
     */
    List<MObjectTree> relationTree(Boolean isVersion, RelationMObject relationMObject, boolean filterRichText);

    /**
     * 获取关系对象树形列表
     *
     * @param relationModelCode 关系对象模型编码
     * @param sourceModelCode   源对象模型编码
     * @param targetBid         目标对象bid
     * @return 源对象列表
     */
    List<MObject> listSourceMObjects(String relationModelCode, String sourceModelCode, List<String> targetBid);

    /**
     * 获取历史版本关系对象树形列表
     *
     * @param relationModelCode relationModelCode
     * @param sourceModelCode   源对象模型编码
     * @param targetBid         目标对象bid
     * @return {@link List }<{@link MObject }>
     */
    List<MObject> listHisSourceMObjects(String relationModelCode, String sourceModelCode, List<String> targetBid);

    /**
     * 获取历史版本关系对象树形列表
     *
     * @param bidList   bidList
     * @param modelCode modelCode
     * @return {@link List }<{@link MObject }>
     */
    List<MObject> listByBids(List<String> bidList, String modelCode);

    /**
     * 获取历史版本关系对象树形列表
     *
     * @param bidList   bidList
     * @param modelCode modelCode
     * @return {@link List }<{@link MObject }>
     */
    List<MObject> listByBidsIncludeDelete(List<String> bidList, String modelCode);

    /**
     * 列表查询
     *
     * @param modelCode     modelCode
     * @param queryWrappers queryWrappers
     * @return {@link List }<{@link MObject }>
     */
    List<MObject> listByQueryWrapper(String modelCode, List<QueryWrapper> queryWrappers);

    /**
     * 列表查询
     *
     * @param modelCode      modelCode
     * @param queryCondition queryCondition
     * @return {@link List }<{@link MObject }>
     */
    List<MObject> listByQueryCondition(String modelCode, QueryCondition queryCondition);

    /**
     * 关系下拉选项的分页查询（作为目标对象查询以源对象的可选集合）
     *
     * @param qo 查询条件
     * @return 分页结果
     */
    PagedResult<MObject> relationTargetSelectPage(RelationCrossLevelQo qo);

    /**
     * 关系下拉选项的分页查询（作为来源（外部/内部）查询以目标对象的可选集合）
     *
     * @param modelCode       modelCode
     * @param source          对象编码
     * @param mObjectCheckDto 对象检查条件
     * @return {@link List }<{@link MObject }>
     */
    List<MObject> relationSelectListExpand(String modelCode, String source, MObjectCheckDto mObjectCheckDto);

    /**
     * 列表转树
     *
     * @param mObjectTreeList mObjectTreeList
     * @return java.util.List<com.transcend.plm.datadriven.api.model.MObjectTree>
     */
    List<MObjectTree> convertToTree(List<MObjectTree> mObjectTreeList);

    /**
     * 列表转树
     *
     * @param modelCode        modelCode
     * @param distinctProperty distinctProperty
     * @param queryCondition   queryCondition
     * @return {@link List }<{@link Object }>
     */
    List<Object> listPropertyDistinct(String modelCode, String distinctProperty, QueryCondition queryCondition);

    /**
     * 修订
     *
     * @param reviseDto reviseDto
     * @return {@link MVersionObject }
     */
    MVersionObject revise(ReviseDto reviseDto);

    /**
     * 提升
     *
     * @param promoteDto promoteDto
     * @return {@link MVersionObject }
     */
    MVersionObject promote(LifeCyclePromoteDto promoteDto);


    /**
     * 查询数量
     *
     * @param modelCode 模型编码
     * @param wrappers  查询条件
     * @return 数量
     */
    int count(String modelCode, List<QueryWrapper> wrappers);
}
