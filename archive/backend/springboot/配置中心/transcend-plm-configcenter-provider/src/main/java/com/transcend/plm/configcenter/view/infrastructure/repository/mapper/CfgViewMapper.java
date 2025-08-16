package com.transcend.plm.configcenter.view.infrastructure.repository.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.configcenter.view.infrastructure.repository.po.CfgViewPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
@Mapper
public interface CfgViewMapper extends BaseMapper<CfgViewPo> {

    /**
     * 根据主键查询
     * @param bid
     * @return
     */
    CfgViewPo getByBid(String bid);

    /**
     * 根据主键查询
     * @param bid
     * @return
     */
    CfgViewPo getMetaModelsByBid(String bid);

    /**
     * 根据modelCode查询
     * @param modelCode
     * @return
     */
    CfgViewPo getByModelCode(String modelCode);

    /**
     * 根据modelCode查询
     * @param modelCode
     * @param tag
     * @return
     */
    CfgViewPo getByModelCodeAndTag(String modelCode, String tag);

    /**
     * 根据bid查询
     * @param bid
     * @return
     */
    int countByBid(String bid);
}
