package com.transcend.plm.datadriven.apm.log.strategy;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto;
import com.transcend.plm.datadriven.apm.log.model.dto.OperationLogCfgViewMetaDto;
import com.transcend.plm.datadriven.common.enums.ViewComponentEnum;
import com.transsion.framework.uac.model.dto.DepartmentDTO;
import com.transsion.framework.uac.model.request.DeptQueryRequest;
import com.transsion.framework.uac.model.request.UacRequest;
import com.transsion.framework.uac.service.IUacDeptService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yinbin
 * @version:
 * @date 2023/10/08 17:18
 */
@Component(ViewComponentEnum.DEPT_CONSTANT + AbstractValueChangeStrategy.STRATEGY_NAME_SUFFIX)
public class DeptValueChangeStrategy extends AbstractValueChangeStrategy {
    @Resource
    private IUacDeptService uacDeptService;

    @Override
    public String getChangeValue(String spaceAppBid, Object oldValue, Object newValue, OperationLogCfgViewMetaDto cfgViewMetaDto) {
        boolean oldIsArray = isArray(oldValue);
        boolean newIsArray = Boolean.TRUE.equals(cfgViewMetaDto.getMultiple());
        List<String> oldValueList = Lists.newArrayList();
        List<String> newValueList = Lists.newArrayList();
        Set<String> deptIdSet = Sets.newHashSet();
        if (oldIsArray) {
            oldValueList = transferArrayValue(oldValue, String.class);
            deptIdSet.addAll(oldValueList);
        } else {
            deptIdSet.add(String.valueOf(oldValue));
        }
        if (newIsArray) {
            newValueList = transferArrayValue(newValue, String.class);
            deptIdSet.addAll(newValueList);
        } else {
            deptIdSet.add(String.valueOf(newValue));
        }
        UacRequest<DeptQueryRequest> request = new UacRequest<>();
        DeptQueryRequest deptQueryRequest = DeptQueryRequest.create();
        deptQueryRequest.setDeptNos(deptIdSet);
        request.withCurrent(1).withSize(200).withSearchTotal(Boolean.FALSE).withParam(deptQueryRequest);
        Map<String, String> deptNoAndNameMap = uacDeptService.queryPages(request).getData().getDataList().stream()
                .collect(Collectors.toMap(DepartmentDTO::getDeptNo, DepartmentDTO::getName));
        oldValue = transferDictValue(oldValue, oldValueList, oldIsArray, deptNoAndNameMap);
        newValue = transferDictValue(newValue, newValueList, newIsArray, deptNoAndNameMap);
        return super.getChangeValue(spaceAppBid, oldValue, newValue, cfgViewMetaDto);
    }
}
