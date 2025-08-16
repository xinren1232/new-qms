package com.transcend.plm.datadriven.apm.permission.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.datadriven.apm.permission.repository.entity.PermissionPlmAddRuleItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author unknown
 * @Entity generator.domain.PermissionPlmAddRuleItem
 */
public interface PermissionPlmAddRuleItemMapper extends BaseMapper<PermissionPlmAddRuleItem> {
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
     * @param permissionPlmAddRuleItemNews permissionPlmAddRuleItemNews
     * @return {@link int}
     */
    int saveList(@Param("permissionPlmAddRuleItemNews") List<PermissionPlmAddRuleItem> permissionPlmAddRuleItemNews);
}




