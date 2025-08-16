package com.transcend.plm.datadriven.common.tool;

import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.Order;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 对象树工具类
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/12/5 13:55
 * @since 1.0
 */
public class ObjectTreeTools {

    @NotNull
    public static <T extends MObject> List<T> sortObjectList(List<Order> orders, List<T> metaList) {
        // 没有排序,直接返回
        if (CollectionUtils.isEmpty(orders)) {
            return metaList;
        }
        Stream<T> stream = metaList.stream();
        // 编写倒序遍历orders排序，保持与DB一致
        for (int i = orders.size() - 1; i >= 0; i--) {
            Order orderItem = orders.get(i);
            Comparator<Object> comparator = Boolean.TRUE.equals(orderItem.getDesc()) ? new UniversalComparator().reversed() : new UniversalComparator();
            stream = stream.sorted(Comparator.nullsLast((v1, v2) -> comparator.compare(v1.get(orderItem.getProperty()), v2.get(orderItem.getProperty()))));
        }
        metaList = stream.collect(Collectors.toList());
        return metaList;
    }

    /**
     * 增加按字典自定义的顺序排序
     * @param orders
     * @param metaList
     * @param dictSortMap
     * @return
     * @param <T>
     */
    public static  <T extends MObject> List<T> sortObjectList(List<Order> orders, List<T> metaList, Map<String, Map<String, Integer>> dictSortMap) {
        // 没有排序,直接返回
        if (CollectionUtils.isEmpty(orders)) {
            return metaList;
        }
        Stream<T> stream = metaList.stream();
        // 编写倒序遍历orders排序，保持与DB一致
        for (int i = orders.size() - 1; i >= 0; i--) {
            Order orderItem = orders.get(i);
            Comparator<Object> comparator = Boolean.TRUE.equals(orderItem.getDesc()) ? new UniversalComparator().reversed() : new UniversalComparator();
            stream = stream.sorted(Comparator.nullsLast((v1, v2) -> {
                String property = orderItem.getProperty();
                Object o1Value = v1.get(property);
                Object o2Value = v2.get(property);
                if (orderItem.getDesc() == null && StringUtils.isNotBlank(orderItem.getRemoteDictType()) && dictSortMap.containsKey(orderItem.getRemoteDictType())) {
                    if (ObjectUtils.isNotEmpty(o1Value)) {
                        o1Value = dictSortMap.get(orderItem.getRemoteDictType()).getOrDefault(o1Value.toString(),999);
                    }else {
                        o1Value = 999;
                    }
                    if (ObjectUtils.isNotEmpty(o2Value)) {
                        o2Value = dictSortMap.get(orderItem.getRemoteDictType()).getOrDefault(o2Value.toString(),999);
                    } else {
                        o2Value = 999;
                    }
                }
                return comparator.compare(o1Value, o2Value);
            }));
        }
        metaList = stream.collect(Collectors.toList());
        return metaList;
    }
}
