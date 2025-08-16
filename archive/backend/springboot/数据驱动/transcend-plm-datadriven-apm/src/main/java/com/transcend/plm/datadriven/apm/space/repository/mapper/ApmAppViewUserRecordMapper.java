package com.transcend.plm.datadriven.apm.space.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmAppViewUserRecord;
import org.apache.ibatis.annotations.Param;

/**
 * @author unknown
 * @Entity generator.domain.ApmAppViewUserRecord
 */
public interface ApmAppViewUserRecordMapper extends BaseMapper<ApmAppViewUserRecord> {

    /**
     * 删除指定视图用户点击记录。
     *
     * @param spaceAppBid  应用BID
     * @param createdBy  创建人
     * @return 删除的记录数量
     */
    int delete(@Param("spaceAppBid") String spaceAppBid,@Param("createdBy")  String createdBy);

}




