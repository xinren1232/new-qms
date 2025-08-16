package com.transcend.plm.configcenter.permission.infrastructure.repository.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.configcenter.common.util.CollectionUtils;
import com.transcend.plm.configcenter.permission.infrastructure.repository.mapper.PermissionPlmListRuleItemMapper;
import com.transcend.plm.configcenter.permission.infrastructure.repository.po.PermissionPlmListRuleItem;
import com.transcend.plm.configcenter.permission.infrastructure.repository.po.PermissionPlmOperationRuleItem;
import com.transcend.plm.configcenter.permission.infrastructure.repository.service.PermissionPlmListRuleItemService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class PermissionPlmListRuleItemServiceImpl extends ServiceImpl<PermissionPlmListRuleItemMapper, PermissionPlmListRuleItem>
    implements PermissionPlmListRuleItemService {


    @Override
    public int deleteByRules(List<PermissionPlmListRuleItem> listRuleItems) {
        if (CollectionUtils.isEmpty(listRuleItems)) {
            return 0;
        }
        List<String> bids = listRuleItems.stream().map(PermissionPlmListRuleItem::getBid).collect(Collectors.toList());
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




