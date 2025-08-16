package com.transcend.plm.datadriven.apm.flow.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author unknown
 * @Entity generator.domain.ApmFlowInstanceNode
 */
@Mapper
public interface ApmFlowInstanceNodeMapper extends BaseMapper<ApmFlowInstanceNode> {

    /**
     * 删除具有指定实例标识符的实例。
     *
     * @param instanceBid 实例标识符，不能为空。
     * @return 如果成功删除实例，则返回true；否则返回false。
     */
    boolean deleteByInstanceBid(String instanceBid);

    /**
     * 删除具有指定实例标识符列表的实例。
     *
     * @param instanceBids 实例标识符列表，不能为空。
     * @return 如果成功删除实例，则返回true；否则返回false。
     */
    Boolean deleteByInstanceBids(List<String> instanceBids);

    int completeNodeByBid(@Param("bid") String bid);
}




