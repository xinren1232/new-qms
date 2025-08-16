package com.transcend.plm.configcenter.object.infrastructure.repository.mapper;

import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectLockPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * ObjectLockMapper
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/11/25 11:31
 */
@Mapper
public interface ObjectLockMapper {

    int delete(CfgObjectLockPo record);

    int deleteByModelCodes(@Param("list") List<String> modelCodes);

    int insert(CfgObjectLockPo record);

    List<CfgObjectLockPo> findAll();

    List<CfgObjectLockPo> findByModelCodeAndInModelCodes(@Param("modelCode") String modelCode,
                                                         @Param("list") LinkedHashSet<String> modelCodes);

    CfgObjectLockPo findAdapterModelCode(@Param("modelCode") String modelCode,
                                         @Param("jobNumber") String jobNumber);
}
