package com.transcend.plm.datadriven.apm.space.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author unknown
 * @Entity generator.domain.ApmSpace
 */
@Mapper
public interface ApmSpaceMapper extends BaseMapper<ApmSpace> {

    /**
     * physicsRemoveByBid
     *
     * @param bid bid
     * @return boolean
     */
    boolean physicsRemoveByBid(@Param("bid") String bid);

    int updateSphereBid(@Param("bid") String bid, @Param("sphereBid") String sphereBid);
}




