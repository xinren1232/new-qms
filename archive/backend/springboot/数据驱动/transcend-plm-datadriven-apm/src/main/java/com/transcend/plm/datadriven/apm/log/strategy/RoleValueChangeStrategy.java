package com.transcend.plm.datadriven.apm.log.strategy;

import com.google.common.collect.Lists;
import com.transcend.plm.configcenter.api.feign.CfgRoleFeignClient;
import com.transcend.plm.configcenter.api.model.role.vo.CfgRoleVo;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto;
import com.transcend.plm.datadriven.apm.log.model.dto.OperationLogCfgViewMetaDto;
import com.transcend.plm.datadriven.common.enums.ViewComponentEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 角色选择器值变化策略
 * @author yinbin
 * @version:
 * @date 2023/10/08 18:31
 */
@Component(ViewComponentEnum.ROLE_CONSTANT + AbstractValueChangeStrategy.STRATEGY_NAME_SUFFIX)
public class RoleValueChangeStrategy extends AbstractValueChangeStrategy {
    @Resource
    private CfgRoleFeignClient cfgRoleFeignClient;
    @Override
    public String getChangeValue(String spaceAppBid, Object oldValue, Object newValue, OperationLogCfgViewMetaDto cfgViewMetaDto) {
        boolean oldIsArray = isArray(oldValue);
        boolean newIsArray = Boolean.TRUE.equals(cfgViewMetaDto.getMultiple());
        List<String> oldValueList = Lists.newArrayList();
        List<String> newValueList = Lists.newArrayList();
        List<String> roleCodeList = Lists.newArrayList();
        // 判断是否是数组
        if (oldIsArray) {
            oldValueList = transferArrayValue(oldValue, String.class);
            roleCodeList.addAll(oldValueList);
        } else {
            roleCodeList.add(String.valueOf(oldValue));
        }
        if (newIsArray) {
            newValueList = transferArrayValue(newValue, String.class);
            roleCodeList.addAll(newValueList);
        } else {
            roleCodeList.add(String.valueOf(newValue));
        }
        Map<String, String> roleCodeAndNameMap = cfgRoleFeignClient.queryByCodes(roleCodeList).getCheckExceptionData().stream().collect(Collectors.toMap(CfgRoleVo::getCode, CfgRoleVo::getName, (k1, k2) -> k1));
        oldValue = transferDictValue(oldValue, oldValueList, oldIsArray, roleCodeAndNameMap);
        newValue = transferDictValue(newValue, newValueList, newIsArray, roleCodeAndNameMap);
        return super.getChangeValue(spaceAppBid, oldValue, newValue, cfgViewMetaDto);
    }
}
