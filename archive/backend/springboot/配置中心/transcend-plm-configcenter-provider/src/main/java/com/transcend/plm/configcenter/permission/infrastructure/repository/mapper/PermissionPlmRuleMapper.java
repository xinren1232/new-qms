package com.transcend.plm.configcenter.permission.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.configcenter.permission.infrastructure.repository.po.PermissionPlmRule;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity generator.domain.PermissionPlmRule
 */
public interface PermissionPlmRuleMapper extends BaseMapper<PermissionPlmRule> {
    int deleteByPermissionBid(@Param("permissionBid") String permissionBid);
    int deleteByPermissionBids(@Param("permissionBids") List<String> permissionBids);

    int deleteByBids(@Param("bids") List<String> bids);

    int saveList(@Param("permissionPlmRuleNews") List<PermissionPlmRule> permissionPlmRuleNews);
}




