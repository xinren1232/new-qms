package com.transcend.plm.configcenter.permission.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.configcenter.permission.infrastructure.repository.po.PermissionPlmListRuleItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity generator.domain.PermissionPlmListRuleItem
 */
public interface PermissionPlmListRuleItemMapper extends BaseMapper<PermissionPlmListRuleItem> {
    int deleteByBids(@Param("bids") List<String> bids);

    int deleteByPermissionBid(@Param("permissionBid") String permissionBid);

    int saveList(@Param("permissionPlmListRuleItemListNews")List<PermissionPlmListRuleItem> permissionPlmListRuleItemListNews);

}




