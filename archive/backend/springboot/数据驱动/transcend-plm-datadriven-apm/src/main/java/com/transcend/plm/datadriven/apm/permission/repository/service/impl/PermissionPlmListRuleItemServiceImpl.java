package com.transcend.plm.datadriven.apm.permission.repository.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowNodeDisplayCondition;
import com.transcend.plm.datadriven.apm.permission.repository.entity.PermissionPlmAddRuleItem;
import com.transcend.plm.datadriven.apm.permission.repository.entity.PermissionPlmListRuleItem;
import com.transcend.plm.datadriven.apm.permission.repository.mapper.PermissionPlmListRuleItemMapper;
import com.transcend.plm.datadriven.apm.permission.repository.service.PermissionPlmListRuleItemService;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author unknown
 */
@Service
public class PermissionPlmListRuleItemServiceImpl extends ServiceImpl<PermissionPlmListRuleItemMapper, PermissionPlmListRuleItem>
    implements PermissionPlmListRuleItemService {

    @Override
    public void saveList(List<PermissionPlmListRuleItem> permissionPlmListRuleItemListNews) {
        if (CollectionUtils.isNotEmpty(permissionPlmListRuleItemListNews)){
            int count  = baseMapper.saveList(permissionPlmListRuleItemListNews);
        }
    }

    @Override
    public List<String> listInstanceRoleCodes(String permissionBid) {
        List<String> instanceRoleCodes = new ArrayList<>();
        List<PermissionPlmListRuleItem> permissionPlmListRuleItems = list(Wrappers.<PermissionPlmListRuleItem>lambdaQuery().eq(PermissionPlmListRuleItem::getRoleType, CommonConst.PRI_ROLE_TYPE).eq(PermissionPlmListRuleItem::getPermissionBid, permissionBid));
        if(CollectionUtils.isNotEmpty(permissionPlmListRuleItems)){
            for(PermissionPlmListRuleItem permissionPlmListRuleItem : permissionPlmListRuleItems){
                instanceRoleCodes.add(permissionPlmListRuleItem.getRoleCode().substring(CommonConst.PRI_KEY.length()));
            }
        }
        return instanceRoleCodes;
    }


}




