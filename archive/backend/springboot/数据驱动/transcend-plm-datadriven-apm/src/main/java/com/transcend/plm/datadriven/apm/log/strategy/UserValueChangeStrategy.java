package com.transcend.plm.datadriven.apm.log.strategy;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto;
import com.transcend.plm.datadriven.apm.log.model.dto.OperationLogCfgViewMetaDto;
import com.transcend.plm.datadriven.common.enums.ViewComponentEnum;
import com.transsion.framework.uac.model.dto.EmployeeDTO;
import com.transsion.framework.uac.model.dto.UserDTO;
import com.transsion.framework.uac.service.IUacUserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 人员选择器值变更策略
 * @author yinbin
 * @version:
 * @date 2023/10/08 15:06
 */
@Component(ViewComponentEnum.USER_CONSTANT + AbstractValueChangeStrategy.STRATEGY_NAME_SUFFIX)
public class UserValueChangeStrategy extends AbstractValueChangeStrategy {
    @Resource
    private IUacUserService uacUserService;
    @Override
    public String getChangeValue(String spaceAppBid, Object oldValue, Object newValue, OperationLogCfgViewMetaDto cfgViewMetaDto) {
        boolean oldIsArray = isArray(oldValue);
        boolean newIsArray = Boolean.TRUE.equals(cfgViewMetaDto.getMultiple());
        List<String> oldValueList = Lists.newArrayList();
        List<String> newValueList = Lists.newArrayList();
        Set<String> jobNumberSet = Sets.newHashSet();
        if (oldIsArray) {
            oldValueList = transferArrayValue(oldValue, String.class);
            jobNumberSet.addAll(oldValueList);
        } else {
            jobNumberSet.add(String.valueOf(oldValue));
        }
        if (newIsArray) {
            newValueList = transferArrayValue(newValue, String.class);
            jobNumberSet.addAll(newValueList);
        } else {
            jobNumberSet.add(String.valueOf(newValue));
        }
        Map<String, String> jobNumberAndNameMap = uacUserService.batchQueryByEmpNos(true, jobNumberSet).getData().stream()
                .collect(Collectors.toMap(EmployeeDTO::getEmployeeNo, UserDTO::getRealName));
        oldValue = transferDictValue(oldValue, oldValueList, oldIsArray, jobNumberAndNameMap);
        newValue = transferDictValue(newValue, newValueList, newIsArray, jobNumberAndNameMap);
        return super.getChangeValue(spaceAppBid, oldValue, newValue, cfgViewMetaDto);
    }
}
