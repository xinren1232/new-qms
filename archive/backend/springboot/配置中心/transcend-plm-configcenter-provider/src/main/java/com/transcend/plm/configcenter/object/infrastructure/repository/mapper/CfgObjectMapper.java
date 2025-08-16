package com.transcend.plm.configcenter.object.infrastructure.repository.mapper;

import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ObjectModelMapper
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/11/24 15:59
 */
@Mapper
public interface CfgObjectMapper {

    /**
     * 查询所有对象
     *
     * @param
     * @return
     * @version: 1.0
     * @date: 2022/11/24 16:16
     * @author: jingfang.luo
     */
    List<CfgObjectPo> findAll();

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
     * 查询根节点的最大modelCode
     *
     * @param
     * @return
     * @version: 1.0
     * @date: 2022/12/8 18:01
     * @author: jingfang.luo
     */
    String findMaxModelCodeInRoot();

    /**
     * 根据父的modelCode查询子里面最大的modelCode
     *
     * @param parentCode
     * @return
     * @version: 1.0
     * @date: 2022/11/28 14:14
     * @author: jingfang.luo
     */
    String findMaxModelCodeByParentCode(@Param("parentCode") String parentCode);

    /**
     * 根据modelVersionCode删除对象 - 软删
     *
     * @param modelCode
     * @return
     * @version: 1.0
     * @date: 2022/11/29 10:21
     * @author: jingfang.luo
     */
    int delete(String modelCode);

    /**
     * 批量更新
     *
     * @param poList
     * @return
     * @version: 1.0
     * @date: 2022/11/29 23:34
     * @author: jingfang.luo
     */
    int bulkUpdate(@Param("poList") List<CfgObjectPo> poList);

    /**
     * 根据modelVersionCode查询对象
     *
     * @param modelCode
     * @return
     * @version: 1.0
     * @date: 2022/12/1 16:01
     * @author: jingfang.luo
     */
    CfgObjectPo find(String modelCode);

    /**
     * 根据objectBid查询对象
     *
     * @param objectBid
     * @return
     * @version: 1.0
     * @date: 2022/12/14 15:32
     * @author: jingfang.luo
     */
    CfgObjectPo getByObjectBid(String objectBid);

    /**
     * 更新子对象的父版本 - 应用于检入
     *
     * @param oldParentVersionCodeForChildren
     * @param parentModelVersionCodeForChildren
     * @param newParentVersion
     * @param jobNumber
     * @return
     * @version: 1.0
     * @date: 2022/12/7 11:26
     * @author: jingfang.luo
     */
    int updateChildren(@Param("oldParentVersionCodeForChildren") String oldParentVersionCodeForChildren,
                       @Param("parentModelVersionCodeForChildren") String parentModelVersionCodeForChildren,
                       @Param("newParentVersion") Integer newParentVersion,
                       @Param("jobNumber") String jobNumber);

    /**
     * 根据ParentModelVersionCode查询子对象
     *
     * @param parentMvc
     * @return
     * @version: 1.0
     * @date: 2022/12/8 18:33
     * @author: jingfang.luo
     */
    List<CfgObjectPo> findAllChildrenByParentMvc(String parentMvc);

    /**
     * 根据modelCode查询所有对象 - like modelCode%
     *
     * @param modelCode
     * @return
     * @version: 1.0
     * @date: 2022/12/13 14:17
     * @author: jingfang.luo
     */
    List<CfgObjectPo> findAllChildrenByModelCode(String modelCode);

    /**
     * 根据modelCode删除所有对象
     *
     * @param modelCodeList
     * @return
     * @version: 1.0
     * @date: 2022/12/13 14:29
     * @author: jingfang.luo
     */
    int deleteByModelCodes(@Param("list") List<String> modelCodeList);

    /**
     * 根据对象名称查询对象List
     *
     * @param nameList
     * @return
     * @version: 1.0
     * @date: 2022/12/19 17:40
     * @author: jingfang.luo
     */
    List<CfgObjectPo> findListByNameList(@Param("list") List<String> nameList);


    /**
     * 根据对象名称模糊查询对象List
     *
     * @param name
     * @return
     * @version: 1.0
     * @date: 2022/12/19 17:40
     * @author: jingfang.luo
     */
    List<CfgObjectPo> findLikeName(@Param("name") String name);

    List<CfgObjectPo> findLikeModelCode(@Param("modelCode") String modelCode);

    /**
     * 根据bidList查询modelCodeList
     *
     * @param bidList
     * @return
     * @version: 1.0
     * @date: 2022/12/21 11:37
     * @author: jingfang.luo
     */
    List<CfgObjectPo> findModelCodeListByObjectBidList(@Param("bidList") List<String> bidList);

    /**
     * 查询基类对象
     *
     * @param baseModel 基类对象
     * @return List<ObjectModelVO>
     */
    List<CfgObjectPo> findByBaseModel(@Param("baseModel") String baseModel);


    /**
     * 查询一个基类对象
     *
     * @param baseModel 基类对象
     * @return List<ObjectModelVO>
     */
    CfgObjectPo getOneByBaseModel(@Param("baseModel") String baseModel);

    /**
     * 根据modelCodeList查询对象List
     *
     * @param modelCodeList
     * @return
     * @version: 1.0
     * @date: 2023/1/9 16:45
     * @author: jingfang.luo
     */
    List<CfgObjectPo> findListByModelCodeList(@Param("modelCodeList") List<String> modelCodeList);

}
