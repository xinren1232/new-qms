package com.transcend.plm.configcenter.permission.infrastructure.repository.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.configcenter.common.util.CollectionUtils;
import com.transcend.plm.configcenter.permission.infrastructure.repository.mapper.PermissionPlmRuleMapper;
import com.transcend.plm.configcenter.permission.infrastructure.repository.po.PermissionPlmRule;
import com.transcend.plm.configcenter.permission.infrastructure.repository.service.PermissionPlmRuleService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class PermissionPlmRuleServiceImpl extends ServiceImpl<PermissionPlmRuleMapper, PermissionPlmRule>
        implements PermissionPlmRuleService {

    @Override
    public int deleteByRules(List<PermissionPlmRule> permissionPlmRules) {
        if (CollectionUtils.isEmpty(permissionPlmRules)) {
            return 0;
        }
        List<String> bids = permissionPlmRules.stream().map(PermissionPlmRule::getBid).collect(Collectors.toList());
        return deleteByBids(bids);
    }

    @Override
    public int deleteByBids(List<String> bids) {
        if (CollectionUtils.isEmpty(bids)) {
            return 0;
        }
        baseMapper.deleteByBids(bids);
        return 0;
    }
}




