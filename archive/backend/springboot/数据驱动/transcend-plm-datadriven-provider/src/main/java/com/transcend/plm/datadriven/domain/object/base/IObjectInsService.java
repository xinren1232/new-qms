package com.transcend.plm.datadriven.domain.object.base;

import com.google.common.collect.ImmutableMap;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.datadriven.api.model.*;
import com.transsion.framework.dto.BaseRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
public interface IObjectInsService<T extends MBaseData> {

    /**
     * 单个新增
     *
     * @param modelCode       modelCode
     * @param mObjectRelation mObjectRelation
     * @return {@link T }
     */
    T add(String modelCode, T mObjectRelation);


    /**
     * 批量新增
     *
     * @param modelCode     modelCode
     * @param mBaseDataList mBaseDataList
     * @return {@link Boolean }
     */
    Boolean addBatch(String modelCode, List<T> mBaseDataList);

    /**
     * 批量新增历史数据
     *
     * @param modelCode     modelCode
     * @param mBaseDataList mBaseDataList
     * @return {@link Boolean }
     */
    Boolean addHisBatch(String modelCode, List<T> mBaseDataList);


    /**
     * 获取列表
     *
     * @param modelCode modelCode
     * @param wrappers  查询条件
     * @return {@link PagedResult }<{@link T }>
     */
    List<T> list(String modelCode, List<QueryWrapper> wrappers);

    /**
     * 获取列表 + 删除标记
     *
     * @param modelCode modelCode
     * @param wrappers  查询条件
     * @return {@link List }<{@link T }>
     */
    List<T> listIncludeDelete(String modelCode, List<QueryWrapper> wrappers);

    /**
     * 获取列表 + 排序
     *
     * @param modelCode      modelCode
     * @param queryCondition 查询条件
     * @return {@link List }<{@link T }>
     */
    List<T> list(String modelCode, QueryCondition queryCondition);


    /**
     * 递归获取列表 + 排序
     *
     * @param modelCode      modelCode
     * @param queryCondition 查询条件
     * @return {@link List }<{@link T }>
     */
    List<T> signObjectRecursionTreeList(String modelCode, QueryCondition queryCondition);

    /**
     * 获取去重字段集合
     *
     * @param modelCode      模型编码
     * @param property       字段名
     * @param queryCondition 查询条件
     * @return 去重字段集合
     */
    List<Object> listPropertyDistinct(String modelCode, String property, QueryCondition queryCondition);

    /**
     * 获取列表 + 排序
     *
     * @param modelCode      modelCode
     * @param queryCondition 查询条件
     * @return {@link List }<{@link T }>
     */
    List<T> listHis(String modelCode, QueryCondition queryCondition);


    /**
     * 获取列表 + 排序
     *
     * @param modelCode     modelCode
     * @param queryWrappers 查询条件
     * @return {@link List }<{@link T }>
     */
    List<T> listHis(String modelCode, List<QueryWrapper> queryWrappers);

    /**
     * 获取一个
     *
     * @param modelCode modelCode
     * @param property  字段名
     * @param value     值
     * @return {@link T }
     */
    T getOneByProperty(String modelCode, String property, String value);

    T getOneByBid(String modelCode, String property, String value);

    T getOneByPropertyNotDelete(String modelCode, String property, String value);

    /**
     * 获取一个历史数据
     *
     * @param modelCode modelCode
     * @param property  字段名
     * @param value     值
     * @return {@link T }
     */
    T getHisOneByProperty(String modelCode, String property, String value);

    /**
     * 分页查询
     *
     * @param modelCode      modelCode
     * @param pageQo         pageQo
     * @param filterRichText 是否过滤富文本
     * @return {@link PagedResult }<{@link T }>
     */
    PagedResult<T> page(String modelCode, BaseRequest<QueryCondition> pageQo, boolean filterRichText);
    /**
     * 分页查询
     *
     * @param modelCode      modelCode
     * @param pageQo         pageQo
     * @param filterRichText 是否过滤富文本
     * @return {@link PagedResult }<{@link T }>
     */
    PagedResult<T> page(String modelCode, BaseRequest<QueryCondition> pageQo, boolean filterRichText, Set<String> resultFieldList);

    /**
     * 分页查询
     *
     * @param modelCode modelCode
     * @param pageQo    pageQo
     * @param filterRichText 是否过滤富文本
     * @return {@link PagedResult }<{@link T }>
     */
    PagedResult<T> hisPage(String modelCode, BaseRequest<QueryCondition> pageQo, boolean filterRichText);

    /**
     * 统计数量
     *
     * @param modelCode modelCode
     * @param wrappers  查询条件
     * @return {@link Integer }
     */
    int count(String modelCode, List<QueryWrapper> wrappers);


    /**
     * 逻辑删除
     *
     * @param modelCode modelCode
     * @param bid       bid
     * @return {@link Boolean }
     */
    Boolean logicalDeleteByBid(String modelCode, String bid);

    /**
     * 逻辑删除
     *
     * @param modelCode modelCode
     * @param wrappers  查询条件
     * @return {@link Boolean }
     */
    Boolean logicalDelete(String modelCode, List<QueryWrapper> wrappers);

    /**
     * 批量删除
     *
     * @param modelCode modelCode
     * @param bids      bids
     * @return {@link Boolean }
     */
    Boolean batchLogicalDeleteByBid(String modelCode, List<String> bids);

    /**
     * 根据sourceBidSet批量逻辑删除
     * <p>
     * <p>
     * key:modelCode  value:sourceBidSet
     *
     * @param deleteParams modelCode和sourceBidSet
     * @return {@link Boolean }
     */
    Boolean batchLogicalDeleteByModeCodeAndSourceBid(Map<String, Set<String>> deleteParams);

    /**
     * 根据bid删除
     *
     * @param modelCode modelCode
     * @param bid       bid
     * @return {@link Boolean }
     */
    Boolean deleteByBid(String modelCode, String bid);

    /**
     * 根据条件删除
     *
     * @param modelCode     modelCode
     * @param queryWrappers 查询条件
     * @return {@link Boolean }
     */
    Boolean deleteByWrappers(String modelCode, List<QueryWrapper> queryWrappers);

    /**
     * 批量删除
     *
     * @param modelCode modelCode
     * @param bids      bids
     * @return {@link Boolean }
     */
    Boolean batchDeleteByBids(String modelCode, List<String> bids);


    /**
     * 根据bid更新数据
     *
     * @param modelCode modelCode
     * @param bid       bid
     * @param mObject   mObject
     * @return {@link Boolean }
     */
    Boolean updateByBid(String modelCode, String bid, T mObject);

    /**
     * 根据bid更新历史数据
     *
     * @param modelCode modelCode
     * @param bid       bid
     * @param mObject   mObject
     * @return {@link Boolean }
     */
    Boolean updateHisByBid(String modelCode, String bid, T mObject);

    /**
     * 根据bid获取实例数据
     *
     * @param modelCode modelCode
     * @param bid       bid
     * @return {@link T }
     */
    T getByBid(String modelCode, String bid);

    T getByBidNotDelete(String modelCode, String bid);

    /**
     * 根据bid获取历史实例数据
     *
     * @param modelCode modelCode
     * @param bid       bid
     * @return {@link T }
     */
    T getHisByBid(String modelCode, String bid);

    /**
     * 根据bid复制数据，并且重置更新部分值
     *
     * @param modelCode   modelCode
     * @param bid         bid
     * @param resetValues 需要重置的值
     * @param relBehavior 关联行为
     * @return {@link List<T> }
     */
    List<T> copyAndReset(String modelCode, String bid, ImmutableMap<String, Object> resetValues, String relBehavior);

    /**
     * 新增历史数据
     *
     * @param modelCode     modelCode
     * @param versionObject versionObject
     * @return {@link T }
     */
    T addHis(String modelCode, T versionObject);

    /**
     * 根据dataBid更新数据
     *
     * @param modelCode modelCode
     * @param dataBid   dataBid
     * @param mObject   mObject
     * @return {@link Boolean}
     */
    Boolean updateByDataBid(String modelCode, String dataBid, T mObject);

    /**
     * 变更历史数据
     *
     * @param modelCode modelCode
     * @param dataBid   dataBid
     * @param mObject   mObject
     * @return {@link Boolean }
     */
    Boolean updateHisByDataBid(String modelCode, String dataBid, T mObject);

    /**
     * 根据bid获取数据
     *
     * @param modelCode modelCode
     * @param dataBid   dataBid
     * @return {@link T }
     */
    T getByDataBid(String modelCode, String dataBid);

    /**
     * batchUpdateByQueryWrapper
     *
     * @param modelCode         modelCode
     * @param batchUpdateBoList batchUpdateBoList
     * @param isHistory         isHistory
     * @return {@link Boolean}
     */
    Boolean batchUpdateByQueryWrapper(String modelCode, List<BatchUpdateBO<T>> batchUpdateBoList, boolean isHistory);
}
