package com.transcend.plm.datadriven.apm.permission.repository.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.datadriven.apm.permission.repository.entity.PermissionPlmAddRuleItem;
import com.transcend.plm.datadriven.apm.permission.repository.mapper.PermissionPlmAddRuleItemMapper;
import com.transcend.plm.datadriven.apm.permission.repository.service.PermissionPlmAddRuleItemService;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author unknown
 */
@Service
public class PermissionPlmAddRuleItemServiceImpl extends ServiceImpl<PermissionPlmAddRuleItemMapper, PermissionPlmAddRuleItem>
    implements PermissionPlmAddRuleItemService {

    @Override
    public void saveList(List<PermissionPlmAddRuleItem> permissionPlmAddRuleItemNews) {
        if (CollectionUtils.isNotEmpty(permissionPlmAddRuleItemNews)){
            int count  = baseMapper.saveList(permissionPlmAddRuleItemNews);
        }

    }
}




