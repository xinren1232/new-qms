package com.transcend.plm.datadriven.apm.log.strategy;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto;
import com.transcend.plm.datadriven.apm.log.model.dto.OperationLogCfgViewMetaDto;
import com.transcend.plm.datadriven.common.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 值变化策略抽象类
 * @author yinbin
 * @version:
 * @date 2023/10/08 11:35
 */
public abstract class AbstractValueChangeStrategy {


    public static final String DEFAULT_STRATEGY = "default" + AbstractValueChangeStrategy.STRATEGY_NAME_SUFFIX;
    public static final String STRATEGY_NAME_SUFFIX = "ValueChangeStrategy";
    public static final String CHANGE_VALUE_TEMPLATE = "修改了「%s」:%s => %s";

    /**
     * 默认实现
     * @param spaceAppBid 空间应用bid
     * @param oldValue 旧值
     * @param newValue 新值
     * @param cfgViewMetaDto 视图元数据
     * @return  日志内容
     * @version: 1.0
     * @date: 2023/10/8 11:40
     * @author: bin.yin
     */
    public String getChangeValue(String spaceAppBid, Object oldValue, Object newValue, OperationLogCfgViewMetaDto cfgViewMetaDto) {
        String oldStrValue = Optional.ofNullable(oldValue).map(Object::toString).orElse("");
        String newStrValue = Optional.ofNullable(newValue).map(Object::toString).orElse("");
        if (Objects.equals(oldStrValue, newStrValue)) {
            return null;
        }
        return String.format(CHANGE_VALUE_TEMPLATE, cfgViewMetaDto.getLabel(), Objects.isNull(oldValue) ? "" : oldValue, newValue);
    }

    /**
     * 判断是否是数组
     * @param value value
     * @return boolean
     * @version: 1.0
     * @date: 2023/10/8 14:10
     * @author: bin.yin
     */
    public boolean isArray(Object value) {
        return value instanceof Collection;
    }
    public <T> List<T> transferArrayValue(Object value, Class<T> clazz) {
        List<T> result = JSON.parseArray(JSON.toJSONString(value), clazz);
        return CollectionUtils.isEmpty(result) ? Lists.newArrayList() : result;
    }

    /**
     * 下拉选项值转换
     * @param value 值
     * @param isArray 是否数组
     * @param dictMap 下拉选项
     * @return String
     */
    public String transferDictValue(Object value, List<String> valueList, boolean isArray, Map<String, String> dictMap) {
        String result;
        if (isArray) {
            result = valueList.stream().map(item -> dictMap.getOrDefault(item, item)).collect(Collectors.joining(","));
        } else {
            result = dictMap.getOrDefault(String.valueOf(value), Objects.isNull(value) ? "" : value.toString());
        }
        return result;
    }
}
