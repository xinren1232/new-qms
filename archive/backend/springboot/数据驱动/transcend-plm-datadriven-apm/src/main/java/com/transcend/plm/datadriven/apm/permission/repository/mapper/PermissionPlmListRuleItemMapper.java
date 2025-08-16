package com.transcend.plm.datadriven.apm.permission.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.datadriven.apm.permission.repository.entity.PermissionPlmAddRuleItem;
import com.transcend.plm.datadriven.apm.permission.repository.entity.PermissionPlmListRuleItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author unknown
 * @Entity generator.domain.PermissionPlmListRuleItem
 */
public interface PermissionPlmListRuleItemMapper extends BaseMapper<PermissionPlmListRuleItem> {
    /**
     * deleteByBids
     *
     * @param bids bids
     * @return {@link int}
     */
    int deleteByBids(@Param("bids") List<String> bids);

    /**
     * deleteByPermissionBid
     *
     * @param permissionBid permissionBid
     * @return {@link int}
     */
    int deleteByPermissionBid(@Param("permissionBid") String permissionBid);

    /**
     * saveList
     *
     * @param permissionPlmListRuleItemListNews permissionPlmListRuleItemListNews
     * @return {@link int}
     */
    int saveList(@Param("permissionPlmListRuleItemListNews") List<PermissionPlmListRuleItem> permissionPlmListRuleItemListNews);
}




