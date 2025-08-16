package com.transcend.plm.datadriven.apm.log.strategy;

import com.google.common.collect.Lists;
import com.transcend.plm.datadriven.apm.log.model.dto.OperationLogCfgViewMetaDto;
import com.transcend.plm.datadriven.apm.space.utils.RemoteDataUtils;
import com.transcend.plm.datadriven.common.enums.ViewComponentEnum;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author Qiu Yuhao
 * @Date 2024/2/1 9:35
 * @Describe
 */
@Component(ViewComponentEnum.CASCADER + AbstractValueChangeStrategy.STRATEGY_NAME_SUFFIX)
public class CasCaderSelectValueChangeStrategy extends AbstractValueChangeStrategy {

    @Override
    public String getChangeValue(String spaceAppBid, Object oldValue, Object newValue, OperationLogCfgViewMetaDto cfgViewMetaDto) {
        List<Object> oldValueList = new ArrayList<>();
        List<Object> newValueList = new ArrayList<>();
        if (isArray(oldValue)) {
            oldValueList = transferArrayValue(oldValue, Object.class);
        } else {
            oldValueList.add(oldValue);
        }
        if (isArray(newValue)) {
            newValueList = transferArrayValue(newValue, Object.class);
        } else {
            newValueList.add(newValue);
        }
        List<Object> oldValueListCp = Lists.newArrayList(oldValueList);
        List<Object> newValueListCp = Lists.newArrayList(newValueList);
        // 判断是否有变化 则直接用两个集合相互取差集 如果都为空则没有变化
        oldValueListCp.removeAll(newValueList);
        newValueListCp.removeAll(oldValueList);
        if (oldValueListCp.isEmpty() && newValueListCp.isEmpty()) {
            return null;
        }
        if (CollectionUtils.isNotEmpty(cfgViewMetaDto.getProperties())) {
            String oldValueResult = String.valueOf(oldValueList);
            String newValueResult = String.valueOf(newValueList);
            Map<String, Object> remoteData =
                    RemoteDataUtils.getRemoteData(cfgViewMetaDto.getProperties());
            try {
                oldValueResult = oldValueList.stream().map(item -> {
                    if (item instanceof List){
                        return ((List<?>) item).stream().map(k -> remoteData.containsKey(k.toString()) ? remoteData.get(k.toString()).toString() : k.toString()).collect(Collectors.joining(","));
                    }else {
                        return ObjectUtils.isEmpty(remoteData.get(item.toString())) ? item.toString() : remoteData.get(item.toString()).toString();
                    }
                }).collect(Collectors.joining(","));
            } catch (Exception ignored) {
            }

            try {
                newValueResult = newValueList.stream().map(item -> {
                    if (item instanceof List){
                        return ((List<?>) item).stream().map(k -> remoteData.containsKey(k.toString()) ? remoteData.get(k.toString()).toString() : k.toString()).collect(Collectors.joining(",","[","]"));
                    }else {
                        return ObjectUtils.isEmpty(remoteData.get(item.toString())) ? item.toString() : remoteData.get(item.toString()).toString();
                    }
                }).collect(Collectors.joining(","));
            } catch (Exception ignored) {
            }
            return super.getChangeValue(spaceAppBid, oldValueResult, newValueResult, cfgViewMetaDto);
        }
        return super.getChangeValue(spaceAppBid, oldValue, newValue, cfgViewMetaDto);
    }
}
