package com.transcend.plm.configcenter.permission.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.configcenter.permission.infrastructure.repository.po.PermissionPlmOperationRuleItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity generator.domain.PermissionPlmOperationRuleItem
 */
public interface PermissionPlmOperationRuleItemMapper extends BaseMapper<PermissionPlmOperationRuleItem> {
    int deleteByBids(@Param("bids") List<String> bids);
    int deleteByPermissionBid(@Param("permissionBid") String permissionBid);

    int saveList(@Param("permissionPlmOperationRuleItemNews") List<PermissionPlmOperationRuleItem> permissionPlmOperationRuleItemNews);

}




