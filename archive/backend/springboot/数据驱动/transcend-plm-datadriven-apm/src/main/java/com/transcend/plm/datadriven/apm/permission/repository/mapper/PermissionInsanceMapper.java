package com.transcend.plm.datadriven.apm.permission.repository.mapper;

import com.transcend.plm.datadriven.api.model.config.TableDefinition;
import com.transcend.plm.datadriven.apm.permission.pojo.dto.PermissionInstanceDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author unknown
 */
@Mapper
public interface PermissionInsanceMapper {
    /**
     * updatePermissionBid
     *
     * @param table                 table
     * @param permissionInstanceDto permissionInstanceDto
     * @return {@link int}
     */
    int updatePermissionBid(@Param("table") TableDefinition table, @Param("permissionInstanceDto") PermissionInstanceDto permissionInstanceDto);

}
