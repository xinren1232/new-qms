package com.transcend.plm.configcenter.permission.infrastructure.repository.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.configcenter.common.util.CollectionUtils;
import com.transcend.plm.configcenter.permission.infrastructure.repository.mapper.PermissionPlmAddRuleItemMapper;
import com.transcend.plm.configcenter.permission.infrastructure.repository.po.PermissionPlmAddRuleItem;
import com.transcend.plm.configcenter.permission.infrastructure.repository.po.PermissionPlmListRuleItem;
import com.transcend.plm.configcenter.permission.infrastructure.repository.service.PermissionPlmAddRuleItemService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class PermissionPlmAddRuleItemServiceImpl extends ServiceImpl<PermissionPlmAddRuleItemMapper, PermissionPlmAddRuleItem>
    implements PermissionPlmAddRuleItemService {

    @Override
    public int deleteByRules(List<PermissionPlmAddRuleItem> addRuleItems) {
        if (CollectionUtils.isEmpty(addRuleItems)) {
            return 0;
        }
        List<String> bids = addRuleItems.stream().map(PermissionPlmAddRuleItem::getBid).collect(Collectors.toList());
        return baseMapper.deleteByBids(bids);
    }

    @Override
    public int deleteByBids(List<String> bids) {
        if (CollectionUtils.isEmpty(bids)){
            return 0;
        }
        return baseMapper.deleteByBids(bids);
    }
}




