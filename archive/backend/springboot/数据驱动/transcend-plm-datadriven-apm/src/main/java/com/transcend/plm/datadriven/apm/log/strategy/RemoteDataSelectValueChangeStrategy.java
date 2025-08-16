package com.transcend.plm.datadriven.apm.log.strategy;

import com.google.common.collect.Lists;
import com.transcend.plm.datadriven.apm.log.model.dto.OperationLogCfgViewMetaDto;
import com.transcend.plm.datadriven.apm.space.utils.RemoteDataUtils;
import com.transcend.plm.datadriven.common.enums.ViewComponentEnum;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author Qiu Yuhao
 * @Date 2024/2/1 9:35
 * @Describe
 */
@Component(ViewComponentEnum.REMOTE_DATA_TYPE + AbstractValueChangeStrategy.STRATEGY_NAME_SUFFIX)
public class RemoteDataSelectValueChangeStrategy extends AbstractValueChangeStrategy {

    @Override
    public String getChangeValue(String spaceAppBid, Object oldValue, Object newValue, OperationLogCfgViewMetaDto cfgViewMetaDto) {
        List<String> oldValueList = Lists.<String>newArrayList();
        List<String> newValueList = Lists.<String>newArrayList();
        if (isArray(oldValue)) {
            oldValueList = transferArrayValue(oldValue, String.class);
        } else {
            oldValueList.add(String.valueOf(oldValue));
        }
        if (isArray(newValue)) {
            newValueList = transferArrayValue(newValue, String.class);
        } else {
            newValueList.add(String.valueOf(newValue));
        }
        List<String> oldValueListCp = Lists.<String>newArrayList(oldValueList);
        List<String> newValueListCp = Lists.<String>newArrayList(newValueList);
        // 判断是否有变化 则直接用两个集合相互取差集 如果都为空则没有变化
        oldValueListCp.removeAll(newValueList);
        newValueListCp.removeAll(oldValueList);
        if (oldValueListCp.isEmpty() && newValueListCp.isEmpty()) {
            return null;
        }
        if (CollectionUtils.isNotEmpty(cfgViewMetaDto.getProperties())) {
            Map<String, Object> remoteData =
                    RemoteDataUtils.getRemoteData(cfgViewMetaDto.getProperties());
            String oldValueResult = oldValueList.stream().map(item -> ObjectUtils.isEmpty(remoteData.get(item)) ? item : remoteData.get(item).toString()).collect(Collectors.joining(","));
            String newValueResult = newValueList.stream().map(item -> ObjectUtils.isEmpty(remoteData.get(item)) ? item : remoteData.get(item).toString()).collect(Collectors.joining(","));
            return super.getChangeValue(spaceAppBid, oldValueResult, newValueResult, cfgViewMetaDto);
        }
        return super.getChangeValue(spaceAppBid, oldValue, newValue, cfgViewMetaDto);
    }
}
