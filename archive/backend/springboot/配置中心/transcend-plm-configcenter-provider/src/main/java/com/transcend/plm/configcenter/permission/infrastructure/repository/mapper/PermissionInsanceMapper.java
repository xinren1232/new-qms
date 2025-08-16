package com.transcend.plm.configcenter.permission.infrastructure.repository.mapper;

import com.transcend.plm.configcenter.api.model.table.vo.CfgTableVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PermissionInsanceMapper {

    int updateDefaultPermissionBid(@Param("table") CfgTableVo table,@Param("appBids") List<String> appBids);

    int updatePermissionBid(@Param("table") CfgTableVo table, @Param("oldPermissionBids") List<String> oldPermissionBids, @Param("newPermissionBid") String newPermissionBid);

}
