package com.transcend.plm.datadriven.apm.permission.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmSphere;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author peng.qin
 * @description 针对表【apm_domain】的数据库操作Mapper
 * @createDate 2023-09-20 16:15:29
 * @Entity com.transcend.plm.datadriven.apm.repository.entity.ApmDomain
 */
public interface ApmSphereMapper extends BaseMapper<ApmSphere> {

    /**
     * querySpaceSphereBidBySphereBid
     *
     * @param sphereBids sphereBids
     * @return List<String>
     */
    List<String> querySpaceSphereBidBySphereBid(@Param("bids") Set<String> sphereBids);

    /**
     * getSphereBidListByBid
     *
     * @param bid bid
     * @return List<String>
     */
    List<String> getSphereBidListByBid(@Param("bid") String bid);
}




