package com.transcend.plm.datadriven.apm.permission.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.datadriven.apm.permission.repository.entity.PermissionPlmOperationRuleItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author unknown
 * @Entity generator.domain.PermissionPlmOperationRuleItem
 */
public interface PermissionPlmOperationRuleItemMapper extends BaseMapper<PermissionPlmOperationRuleItem> {
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
     * @param permissionPlmOperationRuleItemNews permissionPlmOperationRuleItemNews
     * @return {@link int}
     */
    int saveList(@Param("permissionPlmOperationRuleItemNews") List<PermissionPlmOperationRuleItem> permissionPlmOperationRuleItemNews);
}




