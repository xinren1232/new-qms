package com.transcend.plm.configcenter.permission.infrastructure.repository.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.configcenter.common.util.CollectionUtils;
import com.transcend.plm.configcenter.permission.infrastructure.repository.mapper.PermissionPlmOperationRuleItemMapper;
import com.transcend.plm.configcenter.permission.infrastructure.repository.po.PermissionPlmOperationRuleItem;
import com.transcend.plm.configcenter.permission.infrastructure.repository.po.PermissionPlmRule;
import com.transcend.plm.configcenter.permission.infrastructure.repository.service.PermissionPlmOperationRuleItemService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class PermissionPlmOperationRuleItemServiceImpl extends ServiceImpl<PermissionPlmOperationRuleItemMapper, PermissionPlmOperationRuleItem>
    implements PermissionPlmOperationRuleItemService {


    @Override
    public int deleteByRules(List<PermissionPlmOperationRuleItem> operationRuleItems) {
        if (CollectionUtils.isEmpty(operationRuleItems)) {
            return 0;
        }
        List<String> bids = operationRuleItems.stream().map(PermissionPlmOperationRuleItem::getBid).collect(Collectors.toList());
        return deleteByBids(bids);
    }

    @Override
    public int deleteByBids(List<String> bids) {
        if (CollectionUtils.isEmpty(bids)){
            return 0;
        }
        return baseMapper.deleteByBids(bids);
    }
}




