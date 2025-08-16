package com.transcend.plm.datadriven.apm.permission.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.datadriven.apm.permission.repository.entity.PermissionPlmRule;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author unknown
 * @Entity generator.domain.PermissionPlmRule
 */
public interface PermissionPlmRuleMapper extends BaseMapper<PermissionPlmRule> {
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
     * @param permissionPlmRuleNews permissionPlmRuleNews
     * @return {@link int}
     */
    int saveList(@Param("permissionPlmRuleNews") List<PermissionPlmRule> permissionPlmRuleNews);
}




