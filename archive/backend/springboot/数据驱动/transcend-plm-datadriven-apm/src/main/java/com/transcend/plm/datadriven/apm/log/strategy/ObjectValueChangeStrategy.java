package com.transcend.plm.datadriven.apm.log.strategy;

import com.google.common.collect.Lists;
import com.transcend.plm.configcenter.api.feign.CfgObjectFeignClient;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto;
import com.transcend.plm.datadriven.apm.log.model.dto.OperationLogCfgViewMetaDto;
import com.transcend.plm.datadriven.common.enums.ViewComponentEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 对象选择器值变化策略
 * @author yinbin
 * @version:
 * @date 2023/10/08 18:30
 */
@Component(ViewComponentEnum.OBJECT_CONSTANT + AbstractValueChangeStrategy.STRATEGY_NAME_SUFFIX)
public class ObjectValueChangeStrategy extends AbstractValueChangeStrategy {

    @Resource
    private CfgObjectFeignClient cfgObjectFeignClient;

    @Override
    public String getChangeValue(String spaceAppBid, Object oldValue, Object newValue, OperationLogCfgViewMetaDto cfgViewMetaDto) {
        boolean oldIsArray = isArray(oldValue);
        boolean newIsArray = Boolean.TRUE.equals(cfgViewMetaDto.getMultiple());
        List<String> oldValueList = Lists.newArrayList();
        List<String> newValueList = Lists.newArrayList();
        List<String> modelCodeList = Lists.newArrayList();
        if (oldIsArray) {
            oldValueList = transferArrayValue(oldValue, String.class);
            modelCodeList.addAll(oldValueList);
        } else {
            modelCodeList.add(String.valueOf(oldValue));
        }
        if (newIsArray) {
            newValueList = transferArrayValue(newValue, String.class);
            modelCodeList.addAll(newValueList);
        } else {
            modelCodeList.add(String.valueOf(newValue));
        }
        Map<String, String> roleCodeAndNameMap = cfgObjectFeignClient.listByModelCodes(modelCodeList).getCheckExceptionData().stream().collect(Collectors.toMap(CfgObjectVo::getModelCode, CfgObjectVo::getName, (k1, k2) -> k1));
        oldValue = transferDictValue(oldValue, oldValueList, oldIsArray, roleCodeAndNameMap);
        newValue = transferDictValue(newValue, newValueList, newIsArray, roleCodeAndNameMap);
        return super.getChangeValue(spaceAppBid, oldValue, newValue, cfgViewMetaDto);
    }
}
