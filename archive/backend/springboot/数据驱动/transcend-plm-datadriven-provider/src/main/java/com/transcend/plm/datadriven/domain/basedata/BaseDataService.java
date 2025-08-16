package com.transcend.plm.datadriven.domain.basedata;

import com.transcend.plm.datadriven.common.constant.DataBaseConstant;
import com.transcend.plm.datadriven.common.constant.VersionObjectConstant;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.datadriven.common.tool.ObjectTools;
import com.transcend.plm.datadriven.domain.support.external.table.CfgTableService;
import com.transcend.plm.datadriven.infrastructure.basedata.repository.BaseDataRepository;
import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.api.model.QueryCondition;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.api.model.config.TableDefinition;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 基础数据-领域服务
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/5/5 14:50
 */
@Component
public class BaseDataService<T extends MBaseData> {

    @Resource
    private BaseDataRepository<T> baseDataRepository;

    /**
     * 新增   后期分表策略再实现 TODO 如果attr 有重复，抛异常
     *
     * @param tableBid
     * @param mBaseData
     * @return MBaseData
     */
    public T add(String tableBid, T mBaseData) {
        // 通过tableBid获取table与attribute信息 TODO
        TableDefinition table = CfgTableService.getInstance().getTableDefinitionByBid(tableBid);
        return baseDataRepository.add(table, mBaseData);
    }

    /**
     * 新增-批量 TODO 如果attr 有重复，抛异常
     *
     * @param tableBid
     * @param mBaseDataList
     * @return Boolean
     */
    public Boolean addBatch(String tableBid, List<T> mBaseDataList) {
        // 通过tableBid获取table与attribute信息 TODO
        TableDefinition table = CfgTableService.getInstance().getTableDefinitionByBid(tableBid);
        return baseDataRepository.addBatch(table, mBaseDataList);
    }

    /**
     * 更新
     *
     * @param tableBid
     * @param mBaseData
     * @param wrappers
     * @return Boolean
     */
    public Boolean update(String tableBid, T mBaseData, List<QueryWrapper> wrappers) {
        // 通过tableBid获取table与attribute信息 TODO
        TableDefinition table = CfgTableService.getInstance().getTableDefinitionByBid(tableBid);
        return baseDataRepository.update(table, mBaseData, wrappers);
    }

    /**
     * 删除
     *
     * @param tableBid
     * @param wrappers
     * @return Boolean
     */
    public Boolean delete(String tableBid, List<QueryWrapper> wrappers) {
        // 通过tableBid获取table与attribute信息 TODO
        TableDefinition table = CfgTableService.getInstance().getTableDefinitionByBid(tableBid);
        return baseDataRepository.delete(table, wrappers);
    }

    /**
     * 逻辑删除
     *
     * @param tableBid
     * @param wrappers
     * @return Boolean
     */
    public Boolean logicalDelete(String tableBid, List<QueryWrapper> wrappers) {
        // 通过tableBid获取table与attribute信息 TODO
        TableDefinition table = CfgTableService.getInstance().getTableDefinitionByBid(tableBid);
        return baseDataRepository.logicalDelete(table, wrappers);
    }

    /**
     * 列表 TODO 如果attr 有重复，需要+1
     *
     * @param tableBid
     * @param wrappers
     * @return List<MBaseData>
     */
    public List<MBaseData> list(String tableBid, List<QueryWrapper> wrappers) {
        // modelCode获取表定义信息
        TableDefinition table = ObjectTools.fillTableDefinition(tableBid);
        return baseDataRepository.list(table, wrappers);
    }


    /**
     * 去重列表（内含limit 1000,防止数据量过大）
     *
     * @param tableBid
     * @param property
     * @param queryCondition
     * @return List<Object>
     */
    public List<Object> listPropertyDistinct(String tableBid, String property, QueryCondition queryCondition) {
        // modelCode获取表定义信息
        TableDefinition table = ObjectTools.fillTableDefinition(tableBid);
        return baseDataRepository.listPropertyDistinct(table, property, queryCondition);
    }

    /**
     * 获取一个
     *
     * @param tableBid
     * @param property
     * @param value
     * @return MBaseData
     */
    public MBaseData getOneByProperty(String tableBid, String property, String value) {
        // 通过tableBid获取table与attribute信息 TODO
        TableDefinition table = CfgTableService.getInstance().getTableDefinitionByBid(tableBid);
        return baseDataRepository.getOneByProperty(table, property, value);
    }

    public MBaseData getOneByPropertyNotDelete(String tableBid, String property, String value) {
        // 通过tableBid获取table与attribute信息 TODO
        TableDefinition table = CfgTableService.getInstance().getTableDefinitionByBid(tableBid);
        return baseDataRepository.getOneByPropertyNotDelete(table, property, value);
    }

    /**
     * 分页列表
     *
     * @param tableBid
     * @param pageQo
     * @param filterRichText
     * @return PagedResult<MBaseData>
     */
    public PagedResult<MBaseData> page(String tableBid, BaseRequest<QueryCondition> pageQo, boolean filterRichText) {
        // 通过tableBid获取table与attribute信息 TODO
        TableDefinition table = CfgTableService.getInstance().getTableDefinitionByBid(tableBid);
        return baseDataRepository.page(table, pageQo, filterRichText);
    }

    /**
     * 计数
     *
     * @param tableBid
     * @param wrappers
     * @return int
     */
    public int count(String tableBid, List<QueryWrapper> wrappers) {
        // 通过tableBid获取table与attribute信息 TODO
        TableDefinition table = CfgTableService.getInstance().getTableDefinitionByBid(tableBid);
        return baseDataRepository.count(table, wrappers);
    }

    /**
     * 列表 TODO 如果attr 有重复，需要+1
     *
     * @param tableBid
     * @param parentProperty
     * @param wrappers
     * @return List<MBaseData>
     */
    public List<MBaseData> tree(String tableBid, String parentProperty, List<QueryWrapper> wrappers) {
        // 通过tableBid获取table与attribute信息 TODO
        TableDefinition table = CfgTableService.getInstance().getTableDefinitionByBid(tableBid);
        return baseDataRepository.list(table, wrappers);
    }

    /**
     * 列表 TODO 如果attr 有重复，需要+1
     *
     * @param tableBid
     * @param wrappers
     * @return List<MBaseData>
     */
    public List<MBaseData> tree(String tableBid, List<QueryWrapper> wrappers) {
        // 通过tableBid获取table与attribute信息 TODO
        TableDefinition table = CfgTableService.getInstance().getTableDefinitionByBid(tableBid);
        return tree(tableBid, DataBaseConstant.PROPERTY_PARENT_ID, wrappers);
    }


    /**
     * @param tableBid
     * @param mObject
     */
    public void addHis(String tableBid, T mObject) {
        TableDefinition table = CfgTableService.getInstance().getTableDefinitionByBid(tableBid);
        table.setTableNameSuffix(VersionObjectConstant.COLUMN_HIS);
        baseDataRepository.add(table, mObject);
    }
}
