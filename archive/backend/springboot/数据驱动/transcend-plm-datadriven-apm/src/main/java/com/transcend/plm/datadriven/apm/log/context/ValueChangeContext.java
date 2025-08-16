package com.transcend.plm.datadriven.apm.log.context;

import com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto;
import com.transcend.plm.datadriven.apm.log.model.dto.OperationLogCfgViewMetaDto;
import com.transcend.plm.datadriven.apm.log.strategy.AbstractValueChangeStrategy;
import com.transcend.plm.datadriven.common.enums.ViewComponentEnum;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 值变化上下文
 *
 * @author yinbin
 * @version:
 * @date 2023/10/08 11:33
 */
@Component
public class ValueChangeContext {

    @Resource
    private Map<String, AbstractValueChangeStrategy> valueChangeStrategyMap;

    public AbstractValueChangeStrategy getServiceName(OperationLogCfgViewMetaDto operationLogCfgViewMetaDto) {
        String type = "";
        if (Objects.nonNull(operationLogCfgViewMetaDto)) {
            type = operationLogCfgViewMetaDto.getType();
        }
        String strategy = ViewComponentEnum.getStrategy(type);
        //远程组件属性
        if (ViewComponentEnum.SELECT_CONSTANT.equals(type)
                && ObjectUtils.isEmpty(operationLogCfgViewMetaDto.getRemoteDictType())
                && ObjectUtils.allNotNull(operationLogCfgViewMetaDto.getProperties(), operationLogCfgViewMetaDto.getProperties().get("apiUrl"))){
            return valueChangeStrategyMap.get(ViewComponentEnum.REMOTE_DATA_TYPE + AbstractValueChangeStrategy.STRATEGY_NAME_SUFFIX);
        }
        AbstractValueChangeStrategy abstractValueChangeStrategy = valueChangeStrategyMap.get(strategy + AbstractValueChangeStrategy.STRATEGY_NAME_SUFFIX);
        return Optional.ofNullable(abstractValueChangeStrategy).orElse(valueChangeStrategyMap.get(AbstractValueChangeStrategy.DEFAULT_STRATEGY));
    }
}
