package com.transcend.plm.datadriven.apm.permission.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.datadriven.apm.permission.repository.entity.PermissionPlmRuleCondition;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author unknown
 * @Entity generator.domain.PermissionPlmRuleCondition
 */
public interface PermissionPlmRuleConditionMapper extends BaseMapper<PermissionPlmRuleCondition> {
    /**
     * deleteByPermissionBid
     *
     * @param permissionBid permissionBid
     * @return {@link int}
     */
    int deleteByPermissionBid(@Param("permissionBid") String permissionBid);

    List<PermissionPlmRuleCondition> selectByPermissionBidList(@Param("permissionBidList") List<String> permissionBidList);
}




