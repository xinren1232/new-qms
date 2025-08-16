package com.transcend.plm.configcenter.object.infrastructure.repository.mapper;

import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectLifeCyclePo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * ObjectLifeCycleMapper
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/01 11:14
 */
@Mapper
public interface ObjectModelLifeCycleMapper {

    /**
     * 根据modelCode查询生命周期
     *
     * @param modelCode
     * @return
     * @version: 1.0
     * @date: 2022/12/1 14:33
     * @author: jingfang.luo
     */
    CfgObjectLifeCyclePo find(String modelCode);

    List<CfgObjectLifeCyclePo> list(@Param("list") LinkedHashSet<String> list);

    /**
     * 根据modelCode删除对象生命周期配置
     *
     * @param modelCode
     * @return
     * @version: 1.0
     * @date: 2022/12/12 11:08
     * @author: jingfang.luo
     */
    int delete(String modelCode);

    /**
     * 根据modelCodeList删除对象生命周期配置
     *
     * @param modelCodeList
     * @return
     * @version: 1.0
     * @date: 2022/12/13 15:13
     * @author: jingfang.luo
     */
    int deleteByModelCodeList(@Param("list") List<String> modelCodeList);

    /**
     * 新增对象生命周期配置
     *
     * @param po
     * @return
     * @version: 1.0
     * @date: 2022/12/12 11:09
     * @author: jingfang.luo
     */
    int insert(CfgObjectLifeCyclePo po);

    /**
     * 更新对象生命周期配置
     *
     * @param po
     * @return
     * @version: 1.0
     * @date: 2022/12/12 18:43
     * @author: jingfang.luo
     */
    int update(CfgObjectLifeCyclePo po);

    /**
     * 根据modelCodeList查询生命周期
     *
     * @param modelCodeList
     * @return
     * @version: 1.0
     * @date: 2022/12/16 17:19
     * @author: jingfang.luo
     */
    List<CfgObjectLifeCyclePo> findLifecycleInModelCodeList(@Param("list") List<String> modelCodeList);

}
