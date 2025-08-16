package com.transcend.plm.datadriven.apm.space.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmNotifyExecuteRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author unknown
 * @Entity generator.domain.ApmNotifyExecuteRecord
 */
@Mapper
public interface ApmNotifyExecuteRecordMapper extends BaseMapper<ApmNotifyExecuteRecord> {
    /**
     *
     * 方法描述
     * @param id id
     * @return 返回值
     */
    int deleteById(@Param("id") int id);

    /**
     *
     * 方法描述
     * @param endOfDayS endOfDayS
     * @return 返回值
     */
    List<ApmNotifyExecuteRecord> selectAllExecuteRecord(@Param("endOfDayS") String endOfDayS);

    /**
     *
     * 方法描述
     * @return 返回值
     */
    List<ApmNotifyExecuteRecord> selectImmediateExecuteRecord();

    /**
     *
     * 方法描述
     * @param nowTime nowTime
     * @param type type
     * @return 返回值
     */
    List<ApmNotifyExecuteRecord> selectExecuteRecordByParam(@Param("nowTime") String nowTime, @Param("type") String type);
}




