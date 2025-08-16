package com.transcend.plm.datadriven.apm.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.datadriven.apm.task.ao.ApmTaskDeleteAO;
import com.transcend.plm.datadriven.apm.task.domain.ApmTask;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author unknown
 * @Entity generator.domain.ApmTask
 */
public interface ApmTaskMapper extends BaseMapper<ApmTask> {
    /**
     * deleteByTaskIds
     *
     * @param ids ids
     * @return {@link int}
     */
    int deleteByTaskIds(@Param("ids") List<Integer> ids);

    /**
     * deleteByBizBids
     *
     * @param taskType taskType
     * @param bizBids  bizBids
     * @return {@link int}
     */
    int deleteByBizBids(@Param("taskType") String taskType, @Param("bizBids") List<String> bizBids);

    /**
     * deleteByApmTaskDeleteAO
     *
     * @param apmTaskDeleteAO apmTaskDeleteAO
     * @return {@link int}
     */
    int deleteByApmTaskDeleteAO(@Param("apmTaskDeleteAO") ApmTaskDeleteAO apmTaskDeleteAO);

    /**
     * countNeedTask
     *
     * @param bizBid  bizBid
     * @param handler handler
     * @return {@link long}
     */
    long countNeedTask(@Param("bizBid") String bizBid, @Param("handler") String handler);

    /**
     * deleteNotComplete
     *
     * @param bizBid   bizBid
     * @param handler  handler
     * @param taskType taskType
     * @return {@link int}
     */
    int deleteNotComplete(@Param("bizBid") String bizBid, @Param("handler") String handler, @Param("taskType") String taskType);

}




