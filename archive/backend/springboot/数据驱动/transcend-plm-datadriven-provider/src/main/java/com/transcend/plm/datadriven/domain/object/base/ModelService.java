package com.transcend.plm.datadriven.domain.object.base;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.api.model.config.TableDefinition;
import com.transcend.plm.datadriven.common.constant.DataBaseConstant;
import com.transcend.plm.datadriven.common.constant.VersionObjectConstant;
import com.transcend.plm.datadriven.common.tool.ObjectTools;
import com.transcend.plm.datadriven.common.tool.QueryConveterTool;
import com.transcend.plm.datadriven.infrastructure.basedata.repository.BaseDataRepository;
import com.transsion.framework.common.JacksonBuilder;
import com.transsion.framework.dto.BaseRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 基础模型实例服务，处理模型的增删改查、列表、分页等
 *
 * @Program transcend-plm-datadriven
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-05-24 15:00
 **/
@Slf4j
@Component
public class ModelService<T extends MBaseData> {

    @Resource
    protected BaseDataRepository<T> baseDataRepository;

    /**
     * 新增
     *
     * @param modelCode
     * @param mObject
     * @return
     */
    public T add(String modelCode, T mObject) {
        return this.add(modelCode, mObject, false);
    }

    /**
     * 新增历史
     *
     * @param modelCode
     * @param mObject
     * @return
     */
    public T addHis(String modelCode, T mObject) {
        return this.add(modelCode, mObject, true);
    }

    /**
     * @param modelCode
     * @param mObject
     * @param isHistory
     * @return {@link T }
     */
    private T add(String modelCode, T mObject, boolean isHistory) {
        // modelCode获取表定义信息
        TableDefinition table = ObjectTools.fillTableDefinition(modelCode);
        if (isHistory) {
            table.setTableNameSuffix(VersionObjectConstant.COLUMN_HIS);
        }
        return baseDataRepository.add(table, mObject);
    }


    /**
     * 批量新增
     *
     * @param modelCode
     * @param mBaseDataList
     * @return
     */
    public Boolean addBatch(String modelCode, List<T> mBaseDataList) {
        return this.addBatch(modelCode, mBaseDataList, false);
    }

    /**
     * @param modelCode
     * @param mBaseDataList
     * @return {@link Boolean }
     */
    public Boolean addHisBatch(String modelCode, List<T> mBaseDataList) {
        return this.addBatch(modelCode, mBaseDataList, true);
    }

    /**
     * @param modelCode
     * @param mBaseDataList
     * @param isHistory
     * @return {@link Boolean }
     */
    private Boolean addBatch(String modelCode, List<T> mBaseDataList, boolean isHistory) {
        // modelCode获取表定义信息
        TableDefinition table = ObjectTools.fillTableDefinition(modelCode);
        if (isHistory) {
            table.setTableNameSuffix(VersionObjectConstant.COLUMN_HIS);
        }
        return baseDataRepository.addBatch(table, mBaseDataList);
    }


    /**
     * 根据条件更新
     *
     * @param modelCode
     * @param mObject
     * @param wrappers
     * @param isHistory
     * @return
     */
    public Boolean update(String modelCode, T mObject, List<QueryWrapper> wrappers,boolean isHistory) {
        // 无更新内容，返回false
        if (null == mObject) {
            return Boolean.FALSE;
        }
        // 移除bid的更新，防止恶意更新bid
        T mObjectClone = (T) mObject.clone();
        mObjectClone.remove(RelationEnum.BID.getCode());
        // modelCode获取表定义信息
        TableDefinition table = ObjectTools.fillTableDefinition(modelCode);
        //是否是历史查询
        if (isHistory) {
            table.setTableNameSuffix(VersionObjectConstant.COLUMN_HIS);
        }
        return baseDataRepository.update(table, mObject, wrappers);
    }

    /**
     * 根据Bid更新
     *
     * @param modelCode
     * @param mObject
     * @return
     */
    public Boolean batchUpdateTree(String modelCode, List<BatchUpdateBO<T>> mObject) {
        // modelCode获取表定义信息
        TableDefinition table = ObjectTools.fillTableDefinition(modelCode);
        return baseDataRepository.updateBatch(table, mObject);
    }

    /**
     * 批量更新 每条数据按queryWrapper设置查询语句
     *
     * @param modelCode modelCode
     * @param list      list
     * @param isHistory isHistory
     * @return java.lang.Boolean
     */
    public Boolean batchUpdate(String modelCode, List<BatchUpdateBO<T>> list, boolean isHistory) {
        // modelCode获取表定义信息
        TableDefinition table = ObjectTools.fillTableDefinition(modelCode);
        // 是否更新历史
        if (isHistory) {
            table.setTableNameSuffix(VersionObjectConstant.COLUMN_HIS);
        }
        return baseDataRepository.updateBatch(table, list);
    }

    /**
     * 根据条件进行删除
     *
     * @param modelCode
     * @param wrappers
     * @return
     */
    public Boolean delete(String modelCode, List<QueryWrapper> wrappers) {
        // modelCode获取表定义信息
        TableDefinition table = ObjectTools.fillTableDefinition(modelCode);
        return baseDataRepository.delete(table, wrappers);
    }

    /**
     * 根据条件删除历史表
     *
     * @param modelCode
     * @param wrappers
     * @return
     */
    public Boolean deleteHis(String modelCode, List<QueryWrapper> wrappers) {
        // modelCode获取表定义信息
        TableDefinition table = ObjectTools.fillTableDefinition(modelCode);
        table.setTableNameSuffix(VersionObjectConstant.COLUMN_HIS);
        return baseDataRepository.delete(table, wrappers);
    }

    /**
     * 逻辑删除
     *
     * @param modelCode
     * @param wrappers
     * @return
     */
    public Boolean logicalDelete(String modelCode, List<QueryWrapper> wrappers) {
        // modelCode获取表定义信息
        TableDefinition table = ObjectTools.fillTableDefinition(modelCode);
        return baseDataRepository.logicalDelete(table, wrappers);
    }

    /**
     * 根据sourceBidSet批量逻辑删除
     *
     * @param deleteParams
     * @return
     */
    public Boolean batchLogicalDeleteByModeCodeAndSourceBid(Map<String, Set<String>> deleteParams) {
        return baseDataRepository.batchLogicalDeleteByModeCodeAndSourceBid(deleteParams);
    }

    /**
     * 根据条件查询列表
     *
     * @param modelCode
     * @param queryCondition
     * @return
     */
    public List<MBaseData> list(String modelCode, QueryCondition queryCondition) {
        // modelCode获取表定义信息
        queryCondition.setQueries(
                QueryConveterTool.appendModelCodeQueriesAndCondition(queryCondition.getQueries(), modelCode)
        );
        TableDefinition table = ObjectTools.fillTableDefinition(modelCode);
        return baseDataRepository.list(table, queryCondition);
    }

    /**
     * 递归查询列表
     * @param modelCode
     * @param queryCondition
     * @return
     */
    public List<MBaseData> signObjectRecursionTreeList(String modelCode, QueryCondition queryCondition) {
        // modelCode获取表定义信息
        queryCondition.setQueries(
                QueryConveterTool.appendModelCodeQueriesAndCondition(queryCondition.getQueries(), modelCode)
        );
        TableDefinition table = ObjectTools.fillTableDefinition(modelCode);
        return baseDataRepository.signObjectRecursionTreeList(table, queryCondition);
    }
    /**
     * 根据条件查询列表，包含已经删除的
     *
     * @param modelCode
     * @param queryCondition
     * @return
     */
    public List<MBaseData> listIncludeDelete(String modelCode, QueryCondition queryCondition) {
        // modelCode获取表定义信息
        queryCondition.setQueries(
                QueryConveterTool.appendModelCodeQueriesAndCondition(queryCondition.getQueries(), modelCode)
        );
        TableDefinition table = ObjectTools.fillTableDefinition(modelCode);
        return baseDataRepository.listIncludeDelete(table, queryCondition);
    }

    /**
     * 根据某个属性进行查询
     *
     * @param modelCode
     * @param property
     * @param value
     * @return
     */
    public MBaseData getOneByProperty(String modelCode, String property, String value) {
        // modelCode获取表定义信息
        TableDefinition table = ObjectTools.fillTableDefinition(modelCode);
        MBaseData oneData = baseDataRepository.getOneByProperty(table, property, value);
        return null == oneData ? null : (T) oneData;
    }

    public MBaseData getOneByBid(String modelCode, String property, String value) {
        // modelCode获取表定义信息
        TableDefinition table = ObjectTools.fillTableDefinition(modelCode);
        MBaseData oneData = baseDataRepository.getOneByBid(table, property, value);
        return null == oneData ? null : (T) oneData;
    }

    public MBaseData getOneByPropertyNotDelete(String modelCode, String property, String value) {
        // modelCode获取表定义信息
        TableDefinition table = ObjectTools.fillTableDefinition(modelCode);
        MBaseData oneData = baseDataRepository.getOneByPropertyNotDelete(table, property, value);
        return null == oneData ? null : (T) oneData;
    }

    /**
     * 获取单个历史实例数据
     *
     * @param modelCode
     * @param property
     * @param value
     * @return {@link MBaseData }
     */
    public MBaseData getHisOneByProperty(String modelCode, String property, String value) {
        // modelCode获取表定义信息
        TableDefinition table = ObjectTools.fillTableDefinition(modelCode);
        table.setTableNameSuffix(VersionObjectConstant.COLUMN_HIS);
        MBaseData oneData = baseDataRepository.getOneByProperty(table, property, value);
        return null == oneData ? null : (T) oneData;
    }


    /**
     * 根据条件分页
     *
     * @param modelCode
     * @param pageQo
     * @param isHistory
     * @param filterRichText
     * @return
     */
    public PagedResult<MBaseData> page(String modelCode, BaseRequest<QueryCondition> pageQo, boolean isHistory, boolean filterRichText) {
        return page(modelCode, pageQo,isHistory, filterRichText, null);
    }

    /**
     * 根据条件分页
     *
     * @param modelCode
     * @param pageQo
     * @param isHistory
     * @param filterRichText
     * @return
     */
    public PagedResult<MBaseData> page(String modelCode, BaseRequest<QueryCondition> pageQo, boolean isHistory, boolean filterRichText, Set<String> resultFieldSet) {
        // modelCode获取表定义信息
        TableDefinition table = ObjectTools.fillTableDefinition(modelCode, resultFieldSet);
        //是否是历史查询
        if (isHistory) {
            table.setTableNameSuffix(VersionObjectConstant.COLUMN_HIS);
        }
        // 填充modelCode过滤条件
        pageQo.getParam().setQueries(
                QueryConveterTool.appendModelCodeQueriesAndCondition(pageQo.getParam().getQueries(), modelCode)
        );
        return baseDataRepository.page(table, pageQo, filterRichText);
    }

    /**
     * 统计满足条件的数据
     *
     * @param modelCode
     * @param wrappers
     * @return
     */
    public int count(String modelCode, List<QueryWrapper> wrappers) {
        // 通过tableBid获取table与attribute信息 TODO
        TableDefinition table = ObjectTools.fillTableDefinition(modelCode);
        return baseDataRepository.count(table, wrappers);
    }

    /**
     * 逻辑删除
     *
     * @param modelCode
     * @param bid
     * @return
     */

    public Boolean logicalDeleteByBid(String modelCode, String bid) {
        QueryWrapper qo = new QueryWrapper();
        qo.eq(DataBaseConstant.COLUMN_BID, bid);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        return logicalDelete(modelCode, queryWrappers);
    }

    /**
     * 批量逻辑删除
     *
     * @param modelCode
     * @param bids
     * @return
     */
    public Boolean batchLogicalDeleteByBid(String modelCode, List<String> bids) {
        QueryWrapper qo = new QueryWrapper();
        qo.in(DataBaseConstant.COLUMN_BID, bids);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        return logicalDelete(modelCode, queryWrappers);
    }

    /**
     * 根据bid进行删除
     *
     * @param modelCode
     * @param bid
     * @return
     */
    public Boolean deleteByBid(String modelCode, String bid) {
        QueryWrapper qo = new QueryWrapper();
        qo.eq(DataBaseConstant.COLUMN_BID, bid);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        return delete(modelCode, queryWrappers);
    }

    /**
     * @param modelCode
     * @param queryWrappers
     * @return {@link Boolean }
     */
    public Boolean deleteByWrappers(String modelCode, List<QueryWrapper> queryWrappers){
        return delete(modelCode, queryWrappers);
    }

    /**
     * 根据bid批量删除
     *
     * @param modelCode
     * @param bids
     * @return
     */
    public Boolean batchDeleteByBids(String modelCode, List<String> bids) {
        QueryWrapper qo = new QueryWrapper();
        qo.in(DataBaseConstant.COLUMN_BID, bids);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        return delete(modelCode, queryWrappers);
    }

    /**
     * 根据bid进行更新
     *
     * @param modelCode
     * @param bid
     * @param mObject
     * @return
     */
    public Boolean updateByBid(String modelCode, String bid, T mObject) {
        // 没有数据直接返回
        if (null == mObject) {
            return Boolean.FALSE;
        }
        QueryWrapper qo = new QueryWrapper();
        qo.eq(DataBaseConstant.COLUMN_BID, bid);
        if (null != mObject.getBid() && !bid.equals(mObject.getBid())) {
            log.error("更新的bid : ["+bid+"] 与 被更新的数据: [" + JacksonBuilder.toString(mObject) + "]不一致 ");
        }
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        return update(modelCode, mObject, queryWrappers,false);
    }

    /**
     * 根据bid进行更新历史数据
     *
     * @param modelCode
     * @param bid
     * @param mObject
     * @return
     */
    public Boolean updateHisByBid(String modelCode, String bid, T mObject) {
        QueryWrapper qo = new QueryWrapper();
        qo.eq(DataBaseConstant.COLUMN_BID, bid);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        return update(modelCode, mObject, queryWrappers,true);
    }

    /**
     * 根据bid查询
     *
     * @param modelCode
     * @param bid
     * @return
     */
    public MBaseData getByBid(String modelCode, String bid) {
        return getOneByProperty(modelCode, DataBaseConstant.COLUMN_BID, bid);
    }

    public MBaseData getByBidNotDelete(String modelCode, String bid) {
        return getOneByPropertyNotDelete(modelCode, DataBaseConstant.COLUMN_BID, bid);
    }

    /**
     * @param modelCode
     * @param dataBid
     * @param mObject
     * @return {@link Boolean }
     */
    public Boolean updateByDataBid(String modelCode, String dataBid, T mObject) {
        QueryWrapper qo = new QueryWrapper();
        qo.eq(DataBaseConstant.COLUMN_DATA_BID, dataBid);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        return update(modelCode, mObject, queryWrappers,false);
    }

    /**
     * @param modelCode
     * @param dataBid
     * @param mObject
     * @return {@link Boolean }
     */
    public Boolean updateHisByDataBid(String modelCode, String dataBid, T mObject) {
        QueryWrapper qo = new QueryWrapper();
        qo.eq(DataBaseConstant.COLUMN_DATA_BID, dataBid);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        return update(modelCode, mObject, queryWrappers,true);
    }

    /**
     * @param modelCode
     * @param queryCondition
     * @return {@link List }<{@link MBaseData }>
     */
    public List<MBaseData> listHis(String modelCode, QueryCondition queryCondition) {
        return list(modelCode, queryCondition, true);
    }

    /**
     * 查询列表
     *
     * @param modelCode
     * @param queryCondition
     * @param isHistory
     * @return
     */
    private List<MBaseData> list(String modelCode, QueryCondition queryCondition, boolean isHistory) {
        TableDefinition table = ObjectTools.fillTableDefinition(modelCode);
        if (isHistory) {
            table.setTableNameSuffix(VersionObjectConstant.COLUMN_HIS);
        }
        return baseDataRepository.list(table, queryCondition);
    }

    /**
     * @param modelCode
     * @param property
     * @param queryCondition
     * @return {@link List }<{@link Object }>
     */
    public List<Object> listPropertyDistinct(String modelCode, String property, QueryCondition queryCondition) {
        TableDefinition table = ObjectTools.fillTableDefinition(modelCode);
        return baseDataRepository.listPropertyDistinct(table, property, queryCondition);
    }
}
