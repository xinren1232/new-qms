package com.transcend.plm.datadriven.infrastructure.basedata.repository.mapper;

import com.transcend.plm.datadriven.api.model.BatchUpdateBO;
import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.api.model.Order;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.api.model.config.TableDefinition;
import com.transcend.plm.datadriven.api.model.config.TableDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author jie.luo <jie.luo1@transsion.com>
 * @version 1.0
 * @Program transcend-plm-datadriven
 * 基础数据驱动Mapper
 * @date 2023-02-22 10:12
 **/
@Mapper
public interface BaseDataMapper<T> {

    /**
     * 插入
     *
     * @param table     表定义信息
     * @param mBaseData 基础数据
     * @return int
     */
    int insert(@Param("table") TableDefinition table,
               @Param("mBaseData") T mBaseData);

    /**
     * 插入-批量
     *
     * @param table         表定义信息
     * @param mBaseDataList 批量数据信息
     * @return int
     */
    int insertBatch(@Param("table") TableDefinition table,
                    @Param("mBaseDataList") List<T> mBaseDataList);

    /**
     * 列表（内含limit 10000,防止数据量过大）
     *
     * @param table           表定义信息
     * @param wrappers        条件查询
     * @param orderQueries    排序查询
     * @param additionalSql   附加sql
     * @param jsonConditinStr json条件
     * @return List<MBaseData>
     */
    List<MBaseData> list(@Param("table") TableDefinition table,
                         @Param("queryWrappers") List<QueryWrapper> wrappers,
                         @Param("orderQueries") List<Order> orderQueries,
                         @Param("jsonConditinStr") String jsonConditinStr,
                         @Param("additionalSql") String additionalSql);

    /**
     * 递归查询列表（内含limit 10000,防止数据量过大）
     *
     * @param table           表定义信息
     * @param wrappers        条件查询
     * @param orderQueries    排序查询
     * @param additionalSql   附加sql
     * @param jsonConditinStr json条件
     * @return List<MBaseData>
     */
    List<MBaseData> signObjectRecursionTreeList(@Param("table") TableDefinition table,
                         @Param("queryWrappers") List<QueryWrapper> wrappers,
                         @Param("orderQueries") List<Order> orderQueries,
                         @Param("jsonConditinStr") String jsonConditinStr,
                         @Param("additionalSql") String additionalSql);

    /**
     * 去重列表（内含limit 1000,防止数据量过大）
     *
     * @param table           表定义信息
     * @param column          去重字段
     * @param wrappers        条件查询
     * @param orderQueries    排序查询
     * @param additionalSql   附加sql
     * @param jsonConditinStr json条件
     * @return List<MBaseData>
     */
    List<Object> listPropertyDistinct(@Param("table") TableDefinition table,
                                      @Param("column") String column,
                                      @Param("queryWrappers") List<QueryWrapper> wrappers,
                                      @Param("orderQueries") List<Order> orderQueries,
                                      @Param("jsonConditinStr") String jsonConditinStr,
                                      @Param("additionalSql") String additionalSql,
                                      @Param("limitStartNum") Integer limitStartNum,
                                      @Param("pageSize") Integer pageSize);

    /**
     * 计数
     *
     * @param table           表定义信息
     * @param wrappers        条件查询
     * @param additionalSql   附加sql
     * @param jsonConditinStr json条件
     * @return int
     */
    int count(@Param("table") TableDefinition table,
              @Param("queryWrappers") List<QueryWrapper> wrappers,
              @Param("jsonConditinStr") String jsonConditinStr,
              @Param("additionalSql") String additionalSql);

    /**
     * 给予分页使用，内部实现没有limit
     *
     * @param table           表定义信息
     * @param wrappers        条件查询
     * @param orderQueries    排序查询
     * @param additionalSql   附加sql
     * @param jsonConditinStr json条件
     * @return List<MBaseData>
     */
    List<MBaseData> listForPage(@Param("table") TableDefinition table,
                                @Param("queryWrappers") List<QueryWrapper> wrappers,
                                @Param("orderQueries") List<Order> orderQueries,@Param("jsonConditinStr") String jsonConditinStr,@Param("additionalSql") String additionalSql);

    /**
     * 物理删除-慎用
     *
     * @param table           表定义信息
     * @param wrappers        条件查询
     * @param additionalSql   附加sql
     * @param jsonConditinStr json条件
     * @return List<MBaseData>
     */
    int delete(@Param("table") TableDefinition table,
               @Param("queryWrappers") List<QueryWrapper> wrappers,
               @Param("jsonConditinStr") String jsonConditinStr,
               @Param("additionalSql") String additionalSql);

    /**
     * createTable
     *
     * @param tableCreateDto tableCreateDto
     */
    void createTable(@Param("tableCreateDto") TableDto tableCreateDto);

    /**
     * addTableCloumns
     *
     * @param tableCreateDto tableCreateDto
     */
    void addTableCloumns(@Param("tableCreateDto") TableDto tableCreateDto);

    /**
     * 逻辑删除
     *
     * @param table           表定义信息
     * @param wrappers        条件查询
     * @param additionalSql   附加sql
     * @param jsonConditinStr json条件
     * @return List<MBaseData>
     */
    int logicalDelete(@Param("table") TableDefinition table,
                      @Param("queryWrappers") List<QueryWrapper> wrappers,
                      @Param("jsonConditinStr") String jsonConditinStr,
                      @Param("additionalSql") String additionalSql);


    /**
     * batchLogicalDeleteByModeCodeAndSourceBid
     *
     * @param tenantCode   tenantCode
     * @param deleteParams deleteParams
     * @return {@link int}
     */
    int batchLogicalDeleteByModeCodeAndSourceBid(@Param("tenantCode") String tenantCode,
                                                 @Param("deleteParams") Map<String, Set<String>> deleteParams);

    /**
     * 更新
     *
     * @param table           表定义信息
     * @param wrappers        条件查询
     * @param mBaseData       实体数据
     * @param jsonConditinStr json条件
     * @param additionalSql   附加sql
     * @return 更新条数
     */
    int update(@Param("table") TableDefinition table,
               @Param("mBaseData") T mBaseData,
               @Param("queryWrappers") List<QueryWrapper> wrappers,
               @Param("jsonConditinStr") String jsonConditinStr,
               @Param("additionalSql") String additionalSql);

    /**
     * 批量更新
     *
     * @param table     table
     * @param mBaseData mBaseData
     * @return {@link int}
     */
    int updateBatch(@Param("table") TableDefinition table,
                    @Param("mBaseDataList") List<BatchUpdateBO<T>> mBaseData);
}
