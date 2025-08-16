package com.transcend.plm.configcenter.object.infrastructure.repository.mapper;

import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectAttributePo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * ObjectModelAttrMapper
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/05 18:23
 */
@Mapper
public interface ObjectModelAttrMapper {

    /**
     * 根据ModelCodeList查询属性
     *
     * @param codeList
     * @return
     * @version: 1.0
     * @date: 2022/12/5 18:47
     * @author: jingfang.luo
     */
    List<CfgObjectAttributePo> findInModelCodeList(@Param("list") List<String> codeList);

    /**
     * 根据modelCode删除属性
     *
     * @param modelCode
     * @return
     * @version: 1.0
     * @date: 2022/12/7 11:59
     * @author: jingfang.luo
     */
    int delete(String modelCode);

    int deleteExcludeCodes(@Param("modelCode") String modelCode,@Param("list") List<String> codeList);

    /**
     * 根据modelCodeList删除属性
     *
     * @param modelCodeList
     * @return
     * @version: 1.0
     * @date: 2022/12/13 14:58
     * @author: jingfang.luo
     */
    int deleteByList(List<String> modelCodeList);

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
     * 根据对象的modelCode和属性bid查询单条属性
     *
     * @param modelCode
     * @param bid
     * @return
     * @version: 1.0
     * @date: 2022/12/14 15:44
     * @author: jingfang.luo
     */
    CfgObjectAttributePo findAttrByModelCodeAndBid(@Param("modelCode") String modelCode, @Param("bid") String bid);

    /**
     * 根据modelCode Like出所有属性
     *
     * @param modelCode
     * @return
     * @version: 1.0
     * @date: 2023/1/4 16:07
     * @author: jingfang.luo
     */
    List<CfgObjectAttributePo> findLikeModelCode(@Param("modelCode") String modelCode);

    /**
     * 批量更新发布状态
     */
    int batchUpdatePublishStatus(@Param("bidList") List<String> bidList, @Param("published") boolean published);

    List<CfgObjectAttributePo> listSupperAttr(Set<String> objectTypes);
}
