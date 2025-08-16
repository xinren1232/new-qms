package com.transcend.plm.configcenter.lifecycle.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.CfgLifeCycleStatePo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Entity generator.domain.CfgLifeCycleStatePo
 */
@Mapper
public interface CfgLifeCycleStateMapper extends BaseMapper<CfgLifeCycleStatePo> {

    int deleteByBid(@Param("bid") String bid);
}




