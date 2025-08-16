package com.transcend.plm.datadriven.apm.permission.repository.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.datadriven.apm.permission.repository.entity.PermissionPlmRule;
import com.transcend.plm.datadriven.apm.permission.repository.mapper.PermissionPlmRuleMapper;
import com.transcend.plm.datadriven.apm.permission.repository.service.PermissionPlmRuleService;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author unknown
 */
@Service
public class PermissionPlmRuleServiceImpl extends ServiceImpl<PermissionPlmRuleMapper, PermissionPlmRule>
    implements PermissionPlmRuleService {

    @Override
    public void saveList(List<PermissionPlmRule> permissionPlmRuleNews) {
        if (CollectionUtils.isNotEmpty(permissionPlmRuleNews)){
            int count = baseMapper.saveList(permissionPlmRuleNews);
        }

    }
}




