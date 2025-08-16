package com.transcend.plm.datadriven.apm.statistics.repository.mapper;

import com.transcend.plm.datadriven.api.model.MObject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author jinpeng.bai
 * @version v1.0.0
 * @description 能效统计报表
 * @date 2023/10/30 16:57
 **/
@Mapper
public interface ApmSpaceEfficiencyReportMapper {
    /**
     * queryTeamThroughputChartByProjectBid
     *
     * @param bid bid
     * @return List<MObject>
     */
    List<MObject> queryTeamThroughputChartByProjectBid(@Param("bid") String bid);
}
