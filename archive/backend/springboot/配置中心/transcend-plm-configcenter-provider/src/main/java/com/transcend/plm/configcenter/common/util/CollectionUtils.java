package com.transcend.plm.configcenter.common.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.transcend.plm.configcenter.common.core.ClassUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CollectionUtils {

    private CollectionUtils(){}


    /**
     * Return {@code true} if the supplied Collection is {@code null} or empty.
     * Otherwise, return {@code false}.
     * @param collection the Collection to check
     * @return whether the given Collection is empty
     */
    public static boolean isEmpty( Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }

    public static boolean isNotEmpty( Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * Return {@code true} if the supplied Map is {@code null} or empty.
     * Otherwise, return {@code false}.
     * @param map the Map to check
     * @return whether the given Map is empty
     */
    public static boolean isEmpty( Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }


    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    public static boolean isEmpty(Object[] array)
    {
        return array==null || array.length==0;
    }
    /**
     *  获取list中的size
     * @param  collection
     * @return size大小，空值返回为0
     * @version: 1.0
     * @date: 2020/8/26 14:40
     * @author: qihua.sun
     */
    public static int getListSize(Collection<?> collection){
        int defaultSize = 0;
        if(isEmpty(collection)){
            return defaultSize;
        }
        return collection.size();
    }

    /**
     *  list复制
     * @param sourceList sourceList
     * @param targetClazz targetClazz
     * @param ignoreProperties ignoreProperties
     * @return
     * @version: 1.0
     * @date: 2020/12/14 17:03
     * @author: huan.zhou
     */
    public static <T> List<T> copyList(List sourceList,Class<T> targetClazz,String... ignoreProperties){
        List<T> resultList = Lists.newArrayList();
        if(isEmpty(sourceList)){
            return resultList;
        }
        sourceList.forEach(obj->{
            T target = ClassUtils.newInstance(targetClazz);
            BeanUtils.copyProperties(obj,target,ignoreProperties);
            resultList.add(target);
        });
        return resultList;
    }

    /**
     *  list复制
     * @param sourceList sourceList
     * @param targetClazz targetClazz
     * @return
     * @version: 1.0
     * @date: 2020/12/14 17:03
     * @author: huan.zhou
     */
    public static <T> List<T> copyList(List sourceList,Class<T> targetClazz) throws BeansException {
        return copyList(sourceList,targetClazz,null);
    }

    /**
     *  list复制，忽略新增审计字段
     * @param sourceList sourceList
     * @param targetClazz targetClazz
     * @return
     * @version: 1.0
     * @date: 2020/12/14 17:03
     * @author: huan.zhou
     */
    public static <T> List<T> copyListForInsert(List sourceList,Class<T> targetClazz) throws BeansException {
        return copyList(sourceList,targetClazz,BeanConstant.INSERT_IGNORE_FIELD);
    }

    /**
     *  list复制,忽略更新字段
     * @param sourceList sourceList
     * @param targetClazz targetClazz
     * @return
     * @version: 1.0
     * @date: 2020/12/14 17:03
     * @author: huan.zhou
     */
    public static <T> List<T> copyListForUpdate(List sourceList,Class<T> targetClazz) throws BeansException {
        return copyList(sourceList,targetClazz, BeanConstant.UPDATE_IGNORE_FIELD);
    }

    /**
     * list去重
     * @param sourceList sourceList
     * @param distincter distincter
     * @return
     * @version: 1.0
     * @date: 2020/12/15 17:40
     * @author: huan.zhou
     */
    public static <T,R> List<T> distinctList(List<T> sourceList, Function<T,R> distincter){
        List<T> result = Lists.newArrayList();
        if(isEmpty(sourceList)){
            return result;
        }
        Map<R, T> map = Maps.newHashMap();
        sourceList.forEach(obj->{
            map.put(distincter.apply(obj),obj);
        });
        result.addAll(map.values());
        return result;
    }

    /**
     * needSortList 根据 ruleList的属性进行排序
     * 不在ruleList中的元素将会直接放在排序后的元素后面
     * @date: 2021/5/14 16:12
     * @author: Quanlin.zou
     */
    public static <T,R,E>  List<E> sort(List<E> needSortList, Function<E,R> needSortFunction, List<T> ruleList, Function<T,R> ruleFunction){
        List<E> returnList = Lists.newArrayList();
        List<E> notSortList = Lists.newArrayList();
        LinkedHashMap<R, List<E>> ruleMap = ruleList.stream().collect(Collectors.toMap(ruleFunction, e->{return Lists.<E>newArrayList();} , (e1, e2) -> e1, LinkedHashMap::new));
        needSortList.forEach(e->{
            if(ruleMap.containsKey(needSortFunction.apply(e))){
                List<E> e1 = ruleMap.get(needSortFunction.apply(e));
                e1.add(e);
            }else{
                notSortList.add(e);
            }
        });
        ruleMap.values().stream().filter(e-> CollectionUtils.isNotEmpty(e)).forEach(e->{
            returnList.addAll(e);
        });
        returnList.addAll(notSortList);
        return returnList;
    }
}
