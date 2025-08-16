package com.transcend.plm.configcenter.object.infrastructure.repository.mapper;

import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectAttributePo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ObjectModelAttrHistoryMapper
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/05 18:23
 */
@Mapper
public interface ObjectModelAttrHistoryMapper {

    /**
     * 批量新增
     *
     * @param poList
     * @return
     * @version: 1.0
     * @date: 2022/12/7 17:05
     * @author: jingfang.luo
     */
    int bulkInsert(@Param("poList") List<CfgObjectAttributePo> poList);

    /**
     * 根据modelVersionCode列表查询属性
     * @param modelVersionCodeList modelVersionCodeList
     * @return
     * @version: 1.0
     * @date: 2023/1/7 16:20
     * @author: bin.yin
     */
    List<CfgObjectAttributePo> findByModelVersionCodeList(@Param("list") List<String> modelVersionCodeList);

}
