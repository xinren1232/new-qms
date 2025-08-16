package com.transcend.plm.configcenter.object.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectPermissionPo;
import com.transcend.plm.configcenter.api.model.object.qo.CfgObjectPermissionQo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Mapper
@Repository
public interface CfgObjectPermissionMapper  extends BaseMapper<CfgObjectPermissionPo> {

    List<CfgObjectPermissionPo> findByObjBid(String objBid);

    int logicalDeleteByBids(@Param("bids") Set<String> bids);

    int deleteAuthByList(@Param("list") List<String> modelCodeList);

    int bulkInsert(@Param("list") List<CfgObjectPermissionPo> list);

    int bulkUpdateByBid(@Param("list") List<CfgObjectPermissionPo> list);

    CfgObjectPermissionPo getByObjBid(@Param("objBid") String objBid);

    List<CfgObjectPermissionPo> findByCondition(CfgObjectPermissionQo qo);

    List<CfgObjectPermissionPo> listInModelCode(@Param("list") Set<String> modelCodeList);

    List<CfgObjectPermissionPo> findByModelCode(@Param("roleType") Byte roleType,
                                    @Param("lcCode") String lcCode,
                                    @Param("modelCodes") List<String> modelCodes,
                                    @Param("sysRoleSet") Set<String> sysRoleSet);
    List<CfgObjectPermissionPo> listByRoleCodeAndModelCode(@Param("modelCode") String modelCode,@Param("roleCodes") List<String> roleCodes);

    Set<String> listBidByModelCode(String modelCode);
}
