package com.transcend.plm.datadriven.domain.object.base;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.google.common.collect.ImmutableMap;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.common.constant.DataBaseConstant;
import com.transcend.plm.datadriven.common.tool.CommonConstant;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transsion.framework.dto.BaseRequest;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 基类对象-领域服务
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/5/5 14:50
 */
public abstract class AbstractObjectDomainService<T extends MBaseData> implements IObjectInsService<T> {


    /**
     * 协助子类初始化模型类，查询返回具体泛型的实体
     */
    @PostConstruct
    public void initModelClass(){
        setModelClass(currentMapperClass());
    }

    /**
     * @return {@link Class }<{@link T }>
     */
    public Class<T> getModelClass(){
        return this.modelClass;
    }

    /**
     * @param modelClass
     */
    public void setModelClass(Class<T> modelClass){
        this.modelClass = modelClass;
    }

    /**
     * 模型class
     */
    protected Class<T> modelClass;

    protected ModelService<T> modelService;

    @Override

    public T add(String modelCode, T mObject) {
        return modelService.add(modelCode, mObject);
    }

    /**
     * 获取子类定义的泛型类型
     *
     * @return Class<T>
     */
    protected Class<T> currentMapperClass() {
        return (Class<T>) ReflectionKit.getSuperClassGenericType(this.getClass(), AbstractObjectDomainService.class, 0);
    }

    /**
     * @param modelCode
     * @param mBaseDataList
     * @return {@link Boolean }
     */
    @Override
    public Boolean addBatch(String modelCode, List<T> mBaseDataList) {
        return modelService.addBatch(modelCode, mBaseDataList);
    }

    /**
     * @param modelCode
     * @param mBaseDataList
     * @return {@link Boolean }
     */
    @Override
    public Boolean addHisBatch(String modelCode, List<T> mBaseDataList) {
        return modelService.addHisBatch(modelCode, mBaseDataList);
    }

    /**
     * @param modelCode
     * @param mObject
     * @param wrappers
     * @return {@link Boolean }
     */
    public Boolean update(String modelCode, T mObject, List<QueryWrapper> wrappers) {
        return modelService.update(modelCode, mObject, wrappers,false);
    }

    /**
     * @param modelCode
     * @param wrappers
     * @return {@link Boolean }
     */
    public Boolean delete(String modelCode, List<QueryWrapper> wrappers) {
        return modelService.delete(modelCode, wrappers);
    }

    /**
     * @param modelCode
     * @param wrappers
     * @return {@link List }<{@link T }>
     */
    @Override
    public List<T> list(String modelCode, List<QueryWrapper> wrappers) {
        return list(modelCode, QueryCondition.of().setQueries(wrappers));
    }

    /**
     * @param modelCode
     * @param wrappers
     * @return {@link List }<{@link T }>
     */
    @Override
    public List<T> listIncludeDelete(String modelCode, List<QueryWrapper> wrappers) {
        List<MBaseData> list = modelService.listIncludeDelete(modelCode, QueryCondition.of().setQueries(wrappers));
        setStringList(list);
        if (CollectionUtils.isEmpty(list)) {
            return Lists.newArrayList();
        }
        return list.stream().map(this::convertSuperMBaseData).collect(Collectors.toList());
    }

    /**
     * @param modelCode
     * @param wrappers
     * @return {@link List }<{@link T }>
     */
    @Override
    public List<T> listHis(String modelCode, List<QueryWrapper> wrappers) {
        return listHis(modelCode, QueryCondition.of().setQueries(wrappers));
    }


    /**
     * @param modelCode
     * @param queryCondition
     * @return {@link List }<{@link T }>
     */
    @Override
    public List<T> list(String modelCode, QueryCondition queryCondition) {
        List<MBaseData> list = modelService.list(modelCode, queryCondition);
        setStringList(list);
        if (CollectionUtils.isEmpty(list)) {
            return Lists.newArrayList();
        }
        return list.stream().map(this::convertSuperMBaseData).collect(Collectors.toList());
    }

    @Override
    public List<T> signObjectRecursionTreeList(String modelCode, QueryCondition queryCondition) {
        List<MBaseData> list = modelService.signObjectRecursionTreeList(modelCode, queryCondition);
        setStringList(list);
        if (CollectionUtils.isEmpty(list)) {
            return Lists.newArrayList();
        }
        return list.stream().map(this::convertSuperMBaseData).collect(Collectors.toList());
    }

    /**
     * 将MBaseData字符串是数组的转成数组对象
     *
     * @param list
     */
    private void setStringList(List<MBaseData> list){
       if(CollectionUtils.isNotEmpty(list)){
           for(MBaseData mBaseData:list){
               if(mBaseData != null){
                   setStrList(mBaseData);
               }
           }
       }
    }

    /**
     * @param modelCode
     * @param property
     * @param queryCondition
     * @return {@link List }<{@link Object }>
     */
    @Override
    public List<Object> listPropertyDistinct(String modelCode, String property, QueryCondition queryCondition) {
        return modelService.listPropertyDistinct(modelCode, property, queryCondition);
    }

    /**
     * @param modelCode
     * @param queryCondition
     * @return {@link List }<{@link T }>
     */
    @Override
    public List<T> listHis(String modelCode, QueryCondition queryCondition) {
        // 排序字段
        List<Order> orders = new ArrayList<>();
        Order order1 = Order.of().setProperty(BaseDataEnum.CREATED_TIME.getCode()).setDesc(true);
        orders.add(order1);
        queryCondition.setOrders(orders);
        List<MBaseData> list = modelService.listHis(modelCode, queryCondition);
        if (CollectionUtils.isEmpty(list)) {
            return Lists.newArrayList();
        }
        return list.stream().map(this::convertSuperMBaseData).collect(Collectors.toList());
    }

    /**
     * @param modelCode
     * @param property
     * @param value
     * @return {@link T }
     */
    @Override
    public T getOneByProperty(String modelCode, String property, String value) {
        MBaseData mBaseData = modelService.getOneByProperty(modelCode, property, value);
        if(mBaseData == null){
            return null;
        }
        setStrList(mBaseData);
        return convertSuperMBaseData(mBaseData);
    }

    @Override
    public T getOneByBid(String modelCode, String property, String value) {
        MBaseData mBaseData = modelService.getOneByBid(modelCode, property, value);
        if(mBaseData == null){
            return null;
        }
        setStrList(mBaseData);
        return convertSuperMBaseData(mBaseData);
    }

    @Override
    public T getOneByPropertyNotDelete(String modelCode, String property, String value) {
        MBaseData mBaseData = modelService.getOneByPropertyNotDelete(modelCode, property, value);
        if(mBaseData == null){
            return null;
        }
        setStrList(mBaseData);
        return convertSuperMBaseData(mBaseData);
    }

    /**
     * @param mBaseData
     */
    private void setStrList(MBaseData mBaseData){
        if(mBaseData != null){
            for(Map.Entry<String, Object> entry:mBaseData.entrySet()){
                if(entry.getValue() != null && entry.getValue() instanceof String){
                    if((entry.getValue()+"").startsWith(CommonConstant.OPEN_BRACKET) && (entry.getValue()+"").endsWith(CommonConstant.OPEN_RIGHT_BRACKET)){
                        try{
                            List<Object> str2List = JSONArray.parseArray(entry.getValue()+"");
                            mBaseData.put(entry.getKey(),str2List);
                        }catch (Exception e){

                        }
                    }
                }
            }
        }
    }

    @Override
    public T getHisOneByProperty(String modelCode, String property, String value) {
        MBaseData mBaseData = modelService.getHisOneByProperty(modelCode, property, value);
        if(mBaseData == null){
            return null;
        }
        return convertSuperMBaseData(mBaseData);
    }


    @Override
    public PagedResult<T> page(String modelCode, BaseRequest<QueryCondition> pageQo, boolean filterRichText) {
        PagedResult<MBaseData> page = modelService.page(modelCode, pageQo,false, filterRichText);
        return getPagedResult(page);
    }



    @Override
    public PagedResult<T> page(String modelCode, BaseRequest<QueryCondition> pageQo, boolean filterRichText, Set<String> resultFieldList) {
        PagedResult<MBaseData> page = modelService.page(modelCode, pageQo,false, filterRichText, resultFieldList);
        return getPagedResult(page);
    }

    /**
     * @param modelCode
     * @param pageQo
     * @param filterRichText
     * @return {@link PagedResult }<{@link T }>
     */
    @Override
    public PagedResult<T> hisPage(String modelCode, BaseRequest<QueryCondition> pageQo, boolean filterRichText) {
        PagedResult<MBaseData> page = modelService.page(modelCode, pageQo,true, filterRichText);
        return getPagedResult(page);
    }
    @NotNull
    private PagedResult<T> getPagedResult(PagedResult<MBaseData> page) {
        List<MBaseData> list = page.getData();
        setStringList(list);
        if (CollectionUtils.isEmpty(list)) {
            return (PagedResult<T>) page;
        }
        page.setData(
                list.stream().map(this::convertSuperMBaseData).collect(Collectors.toList())
        );
        return (PagedResult<T>) page;
    }

    @Override
    public int count(String modelCode, List<QueryWrapper> wrappers) {
        return modelService.count(modelCode, wrappers);
    }


    @Override
    public Boolean logicalDeleteByBid(String modelCode, String bid) {
        return modelService.logicalDeleteByBid(modelCode, bid);
    }

    @Override
    public Boolean logicalDelete(String modelCode, List<QueryWrapper> wrappers){
        return modelService.logicalDelete(modelCode,wrappers);
    }

    @Override
    public Boolean batchLogicalDeleteByBid(String modelCode, List<String> bids) {
        return modelService.batchLogicalDeleteByBid(modelCode, bids);
    }

    @Override
    public Boolean batchLogicalDeleteByModeCodeAndSourceBid(Map<String, Set<String>> deleteParams) {
        return modelService.batchLogicalDeleteByModeCodeAndSourceBid(deleteParams);
    }

    @Override
    public Boolean deleteByBid(String modelCode, String bid) {
        return modelService.deleteByBid(modelCode, bid);
    }

    @Override
    public Boolean deleteByWrappers(String modelCode, List<QueryWrapper> queryWrappers) {
        return modelService.deleteByWrappers(modelCode, queryWrappers);
    }

    @Override
    public Boolean batchDeleteByBids(String modelCode, List<String> bids) {
        return modelService.batchDeleteByBids(modelCode, bids);
    }

    /**
     * @param modelCode
     * @param bid
     * @param mObject
     * @return {@link Boolean }
     */
    @Override
    public Boolean updateByBid(String modelCode, String bid, T mObject) {
        return modelService.updateByBid(modelCode, bid, mObject);
    }

    /**
     * @param modelCode
     * @param bid
     * @param mObject
     * @return {@link Boolean }
     */
    @Override
    public Boolean updateHisByBid(String modelCode, String bid, T mObject) {
        return modelService.updateHisByBid(modelCode, bid, mObject);
    }

    @Override
    public T getByBid(String modelCode, String bid) {

        return this.getOneByBid(modelCode, DataBaseConstant.COLUMN_BID, bid);
    }

    @Override
    public T getByBidNotDelete(String modelCode, String bid) {
        return this.getOneByPropertyNotDelete(modelCode, DataBaseConstant.COLUMN_BID, bid);
    }

    @Override
    public T getHisByBid(String modelCode, String bid) {
        return this.getHisOneByProperty(modelCode, DataBaseConstant.COLUMN_BID, bid);
    }

    @Override
    public List<T> copyAndReset(String modelCode, String bid, ImmutableMap<String, Object> resetValues, String relBehavior) {
        return null;
    }

    @Override
    public T addHis(String modelCode, T mObject) {
       return modelService.addHis(modelCode, mObject);
    }

    @Override
    public Boolean updateByDataBid(String modelCode, String dataBid, T mObject) {
        return modelService.updateByDataBid(modelCode, dataBid, mObject);
    }

    @Override
    public Boolean updateHisByDataBid(String modelCode, String dataBid, T mObject) {
        return modelService.updateHisByDataBid(modelCode, dataBid, mObject);

    }

    @Override
    public T getByDataBid(String modelCode, String dataBid) {
        return this.getOneByProperty(modelCode, DataBaseConstant.COLUMN_DATA_BID, dataBid);
    }


    /**
     * @param modelCode
     * @param batchUpdateBoList
     * @param isHistory
     * @return {@link Boolean }
     */
    @Override
    public Boolean batchUpdateByQueryWrapper(String modelCode, List<BatchUpdateBO<T>> batchUpdateBoList, boolean isHistory) {
        return modelService.batchUpdate(modelCode, batchUpdateBoList, isHistory);
    }

    /**
     * @param voClass
     * @return {@link T }
     */
    private T getNewInstance(Class<T> voClass) {
        T t = null;
        try {
            t = voClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return t;
    }

    /**
     * 设置转换
     *
     * @param mBaseData MBaseData 转换为想要的类型
     * @return T
     */
    private T convertSuperMBaseData(MBaseData mBaseData) {
        Class<T> voClass = getModelClass();
        if (voClass.isInstance(MBaseData.class)) {
            return (T) mBaseData;
        }
        T t = getNewInstance(voClass);
        t.putAll(mBaseData);
        return t;
    }
}
