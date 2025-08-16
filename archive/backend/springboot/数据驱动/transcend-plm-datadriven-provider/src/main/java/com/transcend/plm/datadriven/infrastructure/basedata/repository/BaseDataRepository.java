package com.transcend.plm.datadriven.infrastructure.basedata.repository;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.page.PageMethod;
import com.google.common.collect.Maps;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.dao.tool.PageResultTools;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.api.model.config.*;
import com.transcend.plm.datadriven.common.constant.DataBaseConstant;
import com.transcend.plm.datadriven.common.constant.TableTypeConstant;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.common.tool.CommonConstant;
import com.transcend.plm.datadriven.common.tool.QueryConveterTool;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.common.util.TableDefinitionUtil;
import com.transcend.plm.datadriven.domain.object.base.pojo.dto.UpdateRecordLog;
import com.transcend.plm.datadriven.domain.support.external.object.CfgObjectService;
import com.transcend.plm.datadriven.infrastructure.basedata.event.*;
import com.transcend.plm.datadriven.infrastructure.basedata.repository.mapper.BaseDataMapper;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基础数据仓库
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/5/5 14:50
 */
@Repository
public class BaseDataRepository<T extends MBaseData> {

    @Resource
    private BaseDataMapper<T> baseDataMapper;

    @Value("#{'${transcend.plm.apm.base.pagefilterAttr:text}'.split(',')}")
    private List<String> pageFilterAttrList;

    @Value("#{'${transcend.plm.apm.base.listfilterAttrCode:rich_text_content}'.split(',')}")
    private List<String> listFilterAttrCode;

    /**
     * spring事件发布器
     * 上层事件入口太多，导致数据变化事件未完成被监听，故需要在底层提供带有事务的事件发布器
     */
    @Resource
    private BaseDataEventPublisher eventPublisher;

    /**
     * 新增   后期分表策略再实现 TODO 如果attr 有重复，抛异常
     *
     * @param table
     * @param mBaseData
     * @return MBaseData
     */
    @Transactional(rollbackFor = Exception.class)
    public T add(TableDefinition table, T mBaseData) {
        // 表定义检查
        tableDifinitionCheck(table);
        //发布新增事件
        eventPublisher.publishEvent(new BaseDataAddEvent(table, mBaseData));
        // 初始化数据，创建人创建时间等
        initAddData(mBaseData);

        List<TableAttributeDefinition> tableAttributeDefinitions = table.getTableAttributeDefinitions();
        // 处理扩展字段
        List<TableAttributeDefinition> insertAttributeDefinitions = collectInsertProperty(table);
        collectExtInsertData(tableAttributeDefinitions, mBaseData);
        table.setTableAttributeDefinitions(insertAttributeDefinitions);
        // 移除非创建字段的property
        removePropertiesByMBaseData(table, mBaseData);
        // 不分表的实现
        if (DataBaseConstant.tableStrategyNone.equals(table.getStrategyCode())) {

            // 直接插入
            baseDataMapper.insert(table, mBaseData);
        }


        return mBaseData;
    }

    /**
     * @param mBaseData
     */
    private void initAddData(T mBaseData) {
        LocalDateTime now = LocalDateTime.now();
        if (StringUtil.isBlank(mBaseData.getBid())) {
            mBaseData.setBid(SnowflakeIdWorker.nextIdStr());
        }
        mBaseData.setDeleteFlag(DataBaseConstant.EXT_COLUMN_FLAG);
        if (StringUtil.isBlank(mBaseData.getCreatedBy())) {
            String jobNumber = SsoHelper.getJobNumber();
            mBaseData.setCreatedBy(jobNumber);
            mBaseData.setUpdatedBy(jobNumber);

        }
        if (ObjectUtils.isEmpty(mBaseData.get(TranscendModelBaseFields.CREATED_TIME))) {
            mBaseData.setCreatedTime(now);
        }
        mBaseData.setUpdatedTime(now);
    }

    /**
     * @param mBaseData
     */
    private void initUpdateData(T mBaseData) {
        String jobNumber = SsoHelper.getJobNumber();
        mBaseData.setUpdatedBy(jobNumber);
        mBaseData.setUpdatedTime(LocalDateTime.now());
    }

    /**
     * 表定义检查
     *
     * @param table 表定义信息
     */
    private static void tableDifinitionCheck(TableDefinition table) {
        if (table == null || StringUtils.isEmpty(table.getLogicTableName())) {
            throw new PlmBizException("", "表定义错误");
        }
    }


    /**
     * 新增-批量 TODO 如果attr 有重复，抛异常
     *
     * @param table
     * @param mBaseDataList
     * @return Boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean addBatch(TableDefinition table, List<T> mBaseDataList) {
        if (CollectionUtils.isEmpty(mBaseDataList)) {
            return Boolean.TRUE;
        }
        // 表定义检查
        tableDifinitionCheck(table);
        //发布批量新增事件
        eventPublisher.publishEvent(new BaseDataBatchAddEvent(table, mBaseDataList));
        // 初始化数据，创建人创建时间等
        initAddDatas(mBaseDataList);
        List<TableAttributeDefinition> tableAttributeDefinitions = table.getTableAttributeDefinitions();
        // 处理扩展字段
        List<TableAttributeDefinition> insertAttributeDefinitions = collectInsertProperty(table);
        collectExtInsertDatas(tableAttributeDefinitions, mBaseDataList);
        table.setTableAttributeDefinitions(insertAttributeDefinitions);
        // 不分表的实现
        if (DataBaseConstant.tableStrategyNone.equals(table.getStrategyCode())) {
            int count = baseDataMapper.insertBatch(table, mBaseDataList);
            return count > 0;
        }
        return false;
    }

    private void initAddDatas(List<T> mBaseDataList) {
        mBaseDataList.forEach(
                this::initAddData
        );
    }

    /**
     * 更新
     *
     * @return Boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(TableDefinition table, T mBaseData, List<QueryWrapper> wrappers) {
        //MBaseData oldData = getOneByProperty(table, wrappers);
        // 表定义检查
        tableDifinitionCheck(table);
        //发布更新事件
        eventPublisher.publishEvent(new BaseDataUpdateEvent(table, mBaseData, wrappers));
        // 初始化数据，创建人创建时间等
        initUpdateData(mBaseData);
        // 处理扩展字段数据
        collectExtUpdateData(table.getTableAttributeDefinitions(), mBaseData);
        // 移除非更新字段的property
        removePropertiesByMBaseData(table, mBaseData);
        int count = 0;
        // 不分表的实现
        if (DataBaseConstant.tableStrategyNone.equals(table.getStrategyCode())) {
            count = baseDataMapper.update(table, mBaseData, wrappers, null, null);
        }
        return count > 0;
    }

    /**
     * 更新
     *
     * @return Boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateBatch(TableDefinition table, List<BatchUpdateBO<T>> batchUpdateList) {
        // 表定义检查
        tableDifinitionCheck(table);
        //发布批量更新事件
        eventPublisher.publishEvent(new BaseDataBatchUpdateEvent(table, batchUpdateList));
        // 初始化数据，创建人创建时间等
        batchUpdateList.forEach(data -> {
            T mBaseDate = data.getBaseData();
            initUpdateData(mBaseDate);
            // 处理扩展字段数据
            collectExtUpdateData(table.getTableAttributeDefinitions(), mBaseDate);
            // 移除非更新字段的property
            removePropertiesByMBaseData(table, mBaseDate);
        });

        int count = 0;
        // 不分表的实现
        if (DataBaseConstant.tableStrategyNone.equals(table.getStrategyCode())) {
            count = baseDataMapper.updateBatch(table, batchUpdateList);
        }
        return count > 0;
    }


    private void recordUpdateLog(MBaseData oldData, MBaseData newData, String jobNumber, String tableName, String name) {
        List<UpdateRecordLog> updateRecordLogs = new ArrayList<>();
        List<String> notNeedFileds = new ArrayList<>();
        notNeedFileds.add(TranscendModelBaseFields.BID);
        notNeedFileds.add(TranscendModelBaseFields.TENANT_ID);
        notNeedFileds.add(TranscendModelBaseFields.CREATED_TIME);
        notNeedFileds.add(TranscendModelBaseFields.UPDATED_TIME);
        notNeedFileds.add(TranscendModelBaseFields.CREATED_BY);
        notNeedFileds.add(TranscendModelBaseFields.UPDATED_BY);
        notNeedFileds.add(TranscendModelBaseFields.ENABLE_FLAG);
        notNeedFileds.add(TranscendModelBaseFields.DELETE_FLAG);
        notNeedFileds.add(TranscendModelBaseFields.DATA_BID);
        ObjectVo objectVo = CfgObjectService.getInstance().getByModelCode(oldData.get(TranscendModelBaseFields.MODEL_CODE) + "");
        List<ObjectAttributeVo> attributes = objectVo.getAttributes();
        Map<String, String> attriMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        for (ObjectAttributeVo attr : attributes) {
            attriMap.put(attr.getCode(), attr.getName());
        }
        for (Map.Entry<String, Object> entry : newData.entrySet()) {
            Object oldValue = oldData.get(entry.getKey());
            if (!notNeedFileds.contains(entry.getKey()) && !StringUtil.equals(oldValue + "", entry.getValue() + "")) {
                //如果是ext就是json格式，需要特殊處理
                if ((entry.getValue() + "").startsWith("{") && (entry.getValue() + "").endsWith("}")) {
                    //json格式单独处理
                    Map<String, Object> newMap = JSONObject.parseObject(entry.getValue() + "");
                    Map<String, Object> oldMap = JSONObject.parseObject(oldValue + "");
                    for (Map.Entry<String, Object> entry1 : newMap.entrySet()) {
                        Object oldMapVal = oldMap.get(entry1.getKey());
                        if (!notNeedFileds.contains(entry1.getKey()) && !StringUtil.equals(oldMapVal + "", entry1.getValue() + "")) {
                            UpdateRecordLog updateRecordLog = new UpdateRecordLog();
                            updateRecordLog.setFiled(entry1.getKey());
                            updateRecordLog.setFiledName(attriMap.get(entry1.getKey()));
                            updateRecordLog.setOldValue(oldMapVal + "");
                            updateRecordLog.setNewValue(entry1.getValue() + "");
                            updateRecordLogs.add(updateRecordLog);
                        }
                    }
                } else {
                    UpdateRecordLog updateRecordLog = new UpdateRecordLog();
                    updateRecordLog.setFiled(entry.getKey());
                    updateRecordLog.setFiledName(attriMap.get(entry.getKey()));
                    updateRecordLog.setOldValue(oldValue + "");
                    updateRecordLog.setNewValue(entry.getValue() + "");
                    updateRecordLogs.add(updateRecordLog);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(updateRecordLogs)) {
            TableDefinition tableDefinition = TableDefinitionUtil.getUpdateRecordTableDefinition();
            // 实例数据
            MObject mBaseData = new MObject();
            mBaseData.setModelCode(oldData.get(TranscendModelBaseFields.MODEL_CODE) + "");
            mBaseData.setCreatedBy(jobNumber);
            mBaseData.setUpdatedBy(jobNumber);
            mBaseData.put("createdByName", name + "(" + jobNumber + ")");
            mBaseData.put(TranscendModelBaseFields.CREATED_BY, jobNumber);
            mBaseData.put("updateContent", JSONObject.toJSONString(updateRecordLogs));
            mBaseData.setBid(SnowflakeIdWorker.nextIdStr());
            mBaseData.put("tableName", tableName);
            add(tableDefinition, (T) mBaseData);
        }
    }


    /**
     * 移除非更新字段的property
     *
     * @param table     表定义
     * @param mBaseData 实例数据
     */
    private void removePropertiesByMBaseData(@NotNull TableDefinition table, @NotNull T mBaseData) {
        // 不包含则移除
        table.getTableAttributeDefinitions().removeIf(attr -> !mBaseData.containsKey(attr.getProperty()));
    }


    /**
     * 删除
     *
     * @return Boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(TableDefinition table, List<QueryWrapper> wrappers) {
        // 表定义检查
        tableDifinitionCheck(table);
        //发布删除事件
        eventPublisher.publishEvent(new BaseDataDeleteEvent(table, wrappers, false));

        // 不分表的实现
        if (DataBaseConstant.tableStrategyNone.equals(table.getStrategyCode())) {
            int count = baseDataMapper.delete(table, wrappers, null, null);
            return count > 0;
        }
        return false;
    }

    public void createTable(TableDto tableCreateDto) {
        if (CollectionUtils.isEmpty(tableCreateDto.getCreateCloumns())) {
            tableCreateDto.setCreateCloumns(getCloumnStr(tableCreateDto.getCloumnDtos()));
        }
        if (CollectionUtils.isEmpty(tableCreateDto.getCreateIndexs())) {
            tableCreateDto.setCreateIndexs(getIndexStr(tableCreateDto.getCloumnIndexs()));
        }
        baseDataMapper.createTable(tableCreateDto);
    }

    private List<String> getIndexStr(List<List<CloumnDto>> cloumnIndexs) {
        List<String> createIndexs = new ArrayList<>();
        for (List<CloumnDto> cloumnDtos : cloumnIndexs) {
            StringBuffer sb = new StringBuffer();
            sb.append("KEY `idx");
            StringBuffer cloStr = new StringBuffer();
            cloStr.append("(");
            for (int i = 0; i < cloumnDtos.size(); i++) {
                CloumnDto cloumnDto = cloumnDtos.get(i);
                sb.append("_").append(cloumnDto.getCloumn());
                if (i > 0) {
                    cloStr.append(",");
                }
                cloStr.append("`").append(cloumnDto.getCloumn()).append("`");
            }
            cloStr.append(") USING BTREE");
            sb.append("` ").append(cloStr);
            createIndexs.add(sb.toString());
        }
        return createIndexs;
    }

    private List<String> getCloumnStr(List<CloumnDto> cloumnDtos) {
        List<String> createCloumns = new ArrayList<>();
        for (CloumnDto cloumnDto : cloumnDtos) {
            StringBuffer sb = new StringBuffer();
            sb.append("`").append(cloumnDto.getCloumn()).append("` ").append(cloumnDto.getCloumnType());
            if (!cloumnDto.isNull()) {
                sb.append(" NOT NULL ");
            }
            if (StringUtil.isNotBlank(cloumnDto.getDefaultValue())) {
                sb.append(" DEFAULT ").append(cloumnDto.getDefaultValue());
            }
            if (StringUtil.isNotBlank(cloumnDto.getCloumnDesc())) {
                sb.append(" COMMENT '").append(cloumnDto.getCloumnDesc()).append("'");
            }
            createCloumns.add(sb.toString());
        }
        return createCloumns;
    }

    public void addTableCloumns(TableDto tableCreateDto) {
        if (CollectionUtils.isEmpty(tableCreateDto.getCreateCloumns())) {
            tableCreateDto.setCreateCloumns(getCloumnStr(tableCreateDto.getCloumnDtos()));
        }
        baseDataMapper.addTableCloumns(tableCreateDto);
    }

    /**
     * 逻辑删除
     *
     * @return Boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean logicalDelete(TableDefinition table, List<QueryWrapper> wrappers) {
        // 表定义检查
        tableDifinitionCheck(table);
        //发布删除事件
        eventPublisher.publishEvent(new BaseDataDeleteEvent(table, wrappers, true));

        // 不分表的实现
        if (DataBaseConstant.tableStrategyNone.equals(table.getStrategyCode())) {

            int count = baseDataMapper.logicalDelete(table, wrappers, null, null);
            return count > 0;
        }
        return false;
    }

    public Boolean batchLogicalDeleteByModeCodeAndSourceBid(Map<String, Set<String>> deleteParams) {
        return baseDataMapper.batchLogicalDeleteByModeCodeAndSourceBid(SsoHelper.getTenantCode(), deleteParams) > 0;
    }


    /**
     * 列表 TODO 如果attr 有重复，需要+1
     *
     * @return List<MBaseData>
     */
    public List<MBaseData> list(TableDefinition table, QueryCondition queryCondition) {
        // 默认条件： 查询未删除的实例数据
        List<QueryWrapper> modelQueries = queryCondition.getQueries();
        modelQueries = appendDefaultQueriesCondition(modelQueries);
        QueryConveterTool.column2Property(table, modelQueries, queryCondition.getOrders());
        // 解决多此使用是，多线程，或者其他场景更改值之后，会影响其他地方使用
        QueryCondition queryConditionNew = QueryConveterTool.column2Property(table, modelQueries, queryCondition.getOrders());
        table.setTableAttributeDefinitions(table.getTableAttributeDefinitions().stream().filter(attr -> !listFilterAttrCode.contains(attr.getColumnName())).collect(Collectors.toList()));
        return baseDataMapper.list(table,
                queryConditionNew.getQueries(),
                queryConditionNew.getOrders(), queryConditionNew.getJsonConditinStr(), queryCondition.getAdditionalSql()
        );
    }

    public List<MBaseData> getOne(TableDefinition table, QueryCondition queryCondition) {
        // 默认条件： 查询未删除的实例数据
        List<QueryWrapper> modelQueries = queryCondition.getQueries();
        modelQueries = appendDefaultQueriesCondition(modelQueries);
        QueryConveterTool.column2Property(table, modelQueries, queryCondition.getOrders());
        // 解决多此使用是，多线程，或者其他场景更改值之后，会影响其他地方使用
        QueryCondition queryConditionNew = QueryConveterTool.column2Property(table, modelQueries, queryCondition.getOrders());
        return baseDataMapper.list(table,
                queryConditionNew.getQueries(),
                queryConditionNew.getOrders(), queryConditionNew.getJsonConditinStr(), queryCondition.getAdditionalSql()
        );
    }

    public List<MBaseData> listNotDelete(TableDefinition table, QueryCondition queryCondition) {
        // 默认条件： 查询未删除的实例数据
        List<QueryWrapper> modelQueries = queryCondition.getQueries();
        QueryConveterTool.column2Property(table, modelQueries, queryCondition.getOrders());
        // 解决多此使用是，多线程，或者其他场景更改值之后，会影响其他地方使用
        QueryCondition queryConditionNew = QueryConveterTool.column2Property(table, modelQueries, queryCondition.getOrders());
        return baseDataMapper.list(table,
                queryConditionNew.getQueries(),
                queryConditionNew.getOrders(), queryConditionNew.getJsonConditinStr(), queryCondition.getAdditionalSql()
        );
    }


    public List<MBaseData> signObjectRecursionTreeList(TableDefinition table, QueryCondition queryCondition) {
        // 默认条件： 查询未删除的实例数据
        List<QueryWrapper> modelQueries = queryCondition.getQueries();
        modelQueries = appendDefaultQueriesCondition(modelQueries);
        QueryConveterTool.column2Property(table, modelQueries, queryCondition.getOrders());
        // 解决多此使用是，多线程，或者其他场景更改值之后，会影响其他地方使用
        QueryCondition queryConditionNew = QueryConveterTool.column2Property(table, modelQueries, queryCondition.getOrders());
        return baseDataMapper.signObjectRecursionTreeList(table,
                queryConditionNew.getQueries(),
                queryConditionNew.getOrders(), queryConditionNew.getJsonConditinStr(), queryCondition.getAdditionalSql()
        );
    }

    /**
     * 查询列表包含已经删除的数据
     *
     * @param table
     * @param queryCondition
     * @return
     */
    public List<MBaseData> listIncludeDelete(TableDefinition table, QueryCondition queryCondition) {
        // 默认条件： 查询未删除的实例数据
        List<QueryWrapper> modelQueries = queryCondition.getQueries();
        QueryConveterTool.column2Property(table, modelQueries, queryCondition.getOrders());
        // 解决多此使用是，多线程，或者其他场景更改值之后，会影响其他地方使用
        QueryCondition queryConditionNew = QueryConveterTool.column2Property(table, modelQueries, queryCondition.getOrders());
        return baseDataMapper.list(table,
                queryConditionNew.getQueries(),
                queryConditionNew.getOrders(), queryConditionNew.getJsonConditinStr(), queryCondition.getAdditionalSql()
        );
    }

    /**
     * 去重列表（内含limit 1000,防止数据量过大）
     *
     * @return List<Object>
     */
    public List<Object> listPropertyDistinct(TableDefinition table, String property, QueryCondition queryCondition) {
        // 默认条件： 查询未删除的实例数据
        List<QueryWrapper> modelQueries = queryCondition.getQueries();
        modelQueries = appendDefaultQueriesCondition(modelQueries);
        QueryConveterTool.column2Property(table, modelQueries, queryCondition.getOrders());
        // 解决多此使用是，多线程，或者其他场景更改值之后，会影响其他地方使用
        QueryCondition queryConditionNew = QueryConveterTool.column2Property(table, modelQueries, queryCondition.getOrders());
        // 属性转换为列名
        String column = QueryConveterTool.property2Column(table, property);
        if (StringUtil.isBlank(column)) {
            return Lists.newArrayList();
        }
        return baseDataMapper.listPropertyDistinct(table,
                column,
                queryConditionNew.getQueries(),
                queryConditionNew.getOrders(),
                queryConditionNew.getJsonConditinStr(),
                queryCondition.getAdditionalSql(),
                queryCondition.getLimitStartNum(),
                queryCondition.getPageSize()
        );
    }


    /**
     * 列表 TODO 如果attr 有重复，需要+1
     *
     * @return List<MBaseData>
     */
    public List<MBaseData> list(TableDefinition table, List<QueryWrapper> queries) {
        return list(table, QueryCondition.of().setQueries(queries));
    }

    public List<MBaseData> getOne(TableDefinition table, List<QueryWrapper> queries) {
        return getOne(table, QueryCondition.of().setQueries(queries));
    }

    public List<MBaseData> listNotDelete(TableDefinition table, List<QueryWrapper> queries) {
        return listNotDelete(table, QueryCondition.of().setQueries(queries));
    }

    /**
     * 获取一个
     *
     * @return MBaseData
     */
    public MBaseData getOneByProperty(TableDefinition table, String property, String value) {
        QueryWrapper qo = new QueryWrapper();
        qo.eq(property, value);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        List<MBaseData> list = list(table, queryWrappers);
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    public MBaseData getOneByBid(TableDefinition table, String property, String value) {
        QueryWrapper qo = new QueryWrapper();
        qo.eq(property, value);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        List<MBaseData> list = getOne(table, queryWrappers);
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }


    public MBaseData getOneByPropertyNotDelete(TableDefinition table, String property, String value) {
        QueryWrapper qo = new QueryWrapper();
        qo.eq(property, value);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        List<MBaseData> list = listNotDelete(table, queryWrappers);
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    public MBaseData getOneByProperty(TableDefinition table, List<QueryWrapper> queryWrappers) {
        List<MBaseData> list = list(table, queryWrappers);
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    /**
     * 分页列表
     *
     * @return PagedResult<MBaseData>
     */
    public PagedResult<MBaseData> page(TableDefinition table, BaseRequest<QueryCondition> pageQo, boolean filterRichText) {
        PageMethod.startPage(pageQo.getCurrent(), pageQo.getSize());
        QueryCondition param = pageQo.getParam();
        // 防止空指针
        if (param == null) {
            param = new QueryCondition();
        }
        List<QueryWrapper> modelQueries = param.getQueries();
        // 默认条件： 查询已删除的实例数据
        modelQueries = appendDefaultQueriesCondition(modelQueries);
        // 解决多此使用是，多线程，或者其他场景更改值之后，会影响其他地方使用
        QueryCondition queryConditionNew = QueryConveterTool.column2Property(table, modelQueries, param.getOrders());
        //过滤富文本字段
        if (filterRichText) {
            table.setTableAttributeDefinitions(table.getTableAttributeDefinitions().stream().filter(attr -> !pageFilterAttrList.contains(attr.getType())).collect(Collectors.toList()));
        } else {
            table.setTableAttributeDefinitions(table.getTableAttributeDefinitions());
        }
        List<MBaseData> pageList = baseDataMapper.listForPage(table, queryConditionNew.getQueries(), queryConditionNew.getOrders(), queryConditionNew.getJsonConditinStr(), param.getAdditionalSql());
        PageInfo<MBaseData> pageInfo = new PageInfo<>(pageList);
        return PageResultTools.create(pageInfo);
    }

    /**
     * 追加默认查询条件
     *
     * @param modelQueries
     */
    private List<QueryWrapper> appendDefaultQueriesCondition(List<QueryWrapper> modelQueries) {
        return QueryConveterTool.appendDeleteFlagQueriesAndCondition(modelQueries);
    }

    /**
     * 计数
     *
     * @return int
     */
    public int count(TableDefinition table, List<QueryWrapper> wrappers) {
        // 表定义检查
        tableDifinitionCheck(table);
        wrappers = appendDefaultQueriesCondition(wrappers);
        // 解决多此使用是，多线程，或者其他场景更改值之后，会影响其他地方使用
        QueryCondition queryConditionNew = QueryConveterTool.column2Property(table, wrappers, null);
        // 不分表的实现
        if (DataBaseConstant.tableStrategyNone.equals(table.getStrategyCode())) {
            return baseDataMapper.count(table, queryConditionNew.getQueries(), queryConditionNew.getJsonConditinStr(), null);
        }

        return 0;
    }

    /**
     * 收集插入的属性
     *
     * @param table 表定义
     * @return List<TableAttributeDefinition>
     */
    private List<TableAttributeDefinition> collectInsertProperty(TableDefinition table) {
        List<TableAttributeDefinition> attributeDefinitions = Lists.newArrayList();
        // 补充ext
        List<TableAttributeDefinition> tableAttributeDefinitions = table.getTableAttributeDefinitions();
        // 收集需要插入的字段
        tableAttributeDefinitions.stream()
                .filter(attr -> DataBaseConstant.BASE_COLUMN_FLAG.equals(attr.getBaseFlag()))
                .forEach(attributeDefinitions::add);
        attributeDefinitions.stream().filter(attr -> TranscendModelBaseFields.EXT.equals(attr.getProperty())).forEach(attr -> attr.setBaseFlag(DataBaseConstant.EXT_COLUMN_FLAG));
        return attributeDefinitions;
    }

    /**
     * 收集插入扩展的数据
     *
     * @param attributeDefinitions 表属性定义
     * @param mBaseDataList        数据集
     */
    private void collectExtInsertDatas(List<TableAttributeDefinition> attributeDefinitions,
                                       List<T> mBaseDataList) {

//        a b c   e_a ,e_b
        // 遍历数据
        mBaseDataList.forEach(mBaseData -> {
            // 收集扩展字段
            Map<String, String> extPropertyMap = mBaseData.get(DataBaseConstant.COLUMN_EXT) == null ? Maps.newHashMap() :
                    JSONObject.parseObject(JSONObject.toJSONString(mBaseData.get(DataBaseConstant.COLUMN_EXT)), new TypeReference<Map<String, String>>() {
                    });
            // 收集扩展字段
            attributeDefinitions.stream()
                    .filter(attr -> !DataBaseConstant.BASE_COLUMN_FLAG.equals(attr.getBaseFlag()))
                    .filter(attr -> mBaseData.get(attr.getProperty()) != null)
                    .forEach(attr -> {
                        if (!DataBaseConstant.COLUMN_EXT.equals(attr.getProperty())) {
                            Object value = mBaseData.get(attr.getProperty());
                            String type = attr.getType();
                            // value格式化转换
                            value = convertDataFormat(value, type);
                            extPropertyMap.put(attr.getColumnName(), value.toString());
                        }
                    });
            //增加处理默认值逻辑
            attributeDefinitions.stream()
                    .filter(attr -> StringUtil.isNotBlank(attr.getDefaultValue()))
                    .forEach(attr -> {
                        Object value = mBaseData.get(attr.getProperty());
                        if (value != null && StringUtil.isNotBlank(value.toString())) {
                            return;
                        }
                        String type = attr.getType();
                        // value格式化转换
                        value = convertDataFormat(attr.getDefaultValue(), type);
                        mBaseData.put(attr.getProperty(), value);
                    });
            // 收集到ext中
            mBaseData.put(DataBaseConstant.COLUMN_EXT, extPropertyMap);
        });

    }


    /**
     * 收集更新扩展的数据
     *
     * @param attributeDefinitions 表属性定义
     * @param mBaseDataList        数据集
     */
    private void collectExtUpdateDatas(List<TableAttributeDefinition> attributeDefinitions,
                                       List<MBaseData> mBaseDataList) {
        // 遍历数据
        mBaseDataList.forEach(mBaseData ->
                // 收集扩展字段
                attributeDefinitions.stream()
                        .filter(attr -> !DataBaseConstant.BASE_COLUMN_FLAG.equals(attr.getBaseFlag()))
                        .filter(attr -> mBaseData.get(attr.getProperty()) != null)
                        .forEach(attr -> {
                            Object value = mBaseData.get(attr.getProperty());
                            String type = attr.getType();
                            String property = attr.getProperty();
                            // value格式化转换
                            value = convertDataFormat(value, type);
                            mBaseData.put(property, value);
                        })
        );

    }

    /**
     * 收集更新扩展的数据
     *
     * @param attributeDefinitions 表属性定义
     * @param mBaseData            数据
     */
    private void collectExtUpdateData(List<TableAttributeDefinition> attributeDefinitions,
                                      MBaseData mBaseData) {
        List<MBaseData> mBaseDataList = Lists.newArrayList();
        mBaseDataList.add(mBaseData);
        collectExtUpdateDatas(attributeDefinitions, mBaseDataList);

    }

    /**
     * // TODO 暂时用toString,适配后期再处理
     *
     * @param value 转换的值
     * @param type  类型
     * @return Object
     */
    private static Object convertDataFormat(Object value, String type) {
        if (TableTypeConstant.DATE.equals(type) && value instanceof Date) {
            value = ((Date) value).getTime();
        }
        return value.toString();
    }

    /**
     * 收集插入扩展的数据
     *
     * @param attributeDefinitions 表属性定义
     * @param mBaseData            数据集
     */
    private void collectExtInsertData(List<TableAttributeDefinition> attributeDefinitions,
                                      T mBaseData) {
        List<T> mBaseDataList = Lists.newArrayList();
        mBaseDataList.add(mBaseData);
        collectExtInsertDatas(attributeDefinitions, mBaseDataList);
    }


}

