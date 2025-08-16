package com.transcend.plm.configcenter.table.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.configcenter.table.infrastructure.repository.po.CfgTableAttributePo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface CfgTableAttributeMapper extends BaseMapper<CfgTableAttributePo> {

    int blukUpdateByBid(@Param("items") List<CfgTableAttributePo> cfgTableAttributePos);

    List<CfgTableAttributePo> listTableAttributeByTableBids(@Param("tableBids") Set<String> bids,
                                                            @Param("enableFlag") Integer enableFlag);

}
