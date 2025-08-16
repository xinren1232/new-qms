package com.transcend.plm.datadriven.apm.space.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.datadriven.apm.space.repository.po.MessageRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author unknown
 */
@Mapper
public interface MessageRecordMapper extends BaseMapper<MessageRecord> {
    /**
     * physicsRemoveByIds
     *
     * @param ids ids
     * @return {@link boolean}
     */
    boolean physicsRemoveByIds(@Param("ids") List<Long> ids);
}
