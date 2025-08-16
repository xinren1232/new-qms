package com.transcend.plm.configcenter.object.infrastructure.repository.mapper;

import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ObjectHistoryMapper
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/11/24 15:59
 */
@Mapper
public interface ObjectHistoryMapper {

    /**
     * 新增对象
     *
     * @param po
     * @return
     * @version: 1.0
     * @date: 2022/11/28 10:22
     * @author: jingfang.luo
     */
    int insert(CfgObjectPo po);

    /**
     * 把最新的子数据复制一份到历史表
     *
     * @param parentModelVersionCodeForChildren
     * @return
     * @version: 1.0
     * @date: 2022/12/8 13:38
     * @author: jingfang.luo
     */
    int copyToHistory(@Param("parentModelVersionCodeForChildren") String parentModelVersionCodeForChildren);

    /**
     * 根据modelCode和bid来批量更新历史数据
     *
     * @param poList
     * @return
     * @version: 1.0
     * @date: 2022/12/9 13:48
     * @author: jingfang.luo
     */
    int batchUpdate(@Param("poList") List<CfgObjectPo> poList);

    /**
     * 通过modelCode 和 version 查询唯一历史对象
     * @param modelCode modelCode
     * @param version version
     * @return
     * @version: 1.0
     * @date: 2023/1/7 14:08
     * @author: bin.yin
     */
    CfgObjectPo findHistory(@Param("modelCode") String modelCode, @Param("version") Integer version);

}
