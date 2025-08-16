package com.transcend.plm.configcenter.object.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectViewRuleVo;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectViewRulePo;
import com.transcend.plm.configcenter.api.model.object.qo.CfgObjectViewRuleQo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

/**
 * @author luojie
 * @date 2023-05-23
 */
@Mapper
@Repository
public interface CfgObjectViewRuleMapper extends BaseMapper<CfgObjectViewRulePo> {
    /**
     * 批量更新
     * @param objBid
     * @return
     */
    List<CfgObjectViewRulePo> findByObjBid(String objBid);

    /**
     * 批量删除
     * @param bids
     * @return
     */
    int logicalDeleteByBids(@Param("bids") Set<String> bids);

    /**
     * 删除权限
     * @param modelCodeList
     * @return
     */
    int deleteAuthByList(@Param("list") List<String> modelCodeList);

    /**
     * 批量插入
     * @param list
     * @return
     */
    int bulkInsert(@Param("list") List<CfgObjectViewRulePo> list);

    /**
     * 批量更新
     * @param list
     * @return
     */
    int bulkUpdateByBid(@Param("list") List<CfgObjectViewRulePo> list);

    /**
     * 根据对象ID查询
     * @param objBid
     * @return
     */
    CfgObjectViewRulePo getByObjBid(@Param("objBid") String objBid);

    /**
     * 根据对象ID查询
     * @param qo
     * @return
     */
    List<CfgObjectViewRulePo> findByCondition(CfgObjectViewRuleQo qo);

    /**
     * 根据模型编码查询
     * @param modelCodeList
     * @return
     */
    List<CfgObjectViewRulePo> listInModelCode(@Param("list") Set<String> modelCodeList);

    /**
     * 根据模型编码查询
     * @param modelCodes
     * @param roleType
     * @param roleSet
     * @param lcCode
     * @param tag
     * @return
     */
    List<CfgObjectViewRulePo> listByCondition(@Param("modelCodes") @NotNull List<String> modelCodes,
                                              @Param("roleType") Byte roleType,
                                              @Param("roleCodeSet") Set<String> roleSet,
                                              @Param("lcCode") String lcCode,
                                              @Param("tag") String tag);

    /**
     * 根据模型编码查询
     * @param modelCode
     * @return
     */
    Set<String> listBidByModelCode(String modelCode);

    /**
     * 根据模型编码查询
     * @param modelCode
     * @param tag
     * @return
     */
    String listByModelCodeAndTag(@Param("modelCode") String modelCode, @Param("tag") String tag);

    /**
     * 根据视图编码查询
     * @param viewBids
     * @return
     */
    List<CfgObjectViewRulePo> listByViewBids(@Param("viewBids") List<String> viewBids);


}
