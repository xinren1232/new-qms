package com.transcend.plm.datadriven.apm.space.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmAppViewConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author unknown
 * @Entity generator.domain.ApmAppViewConfig
 */
public interface ApmAppViewConfigMapper extends BaseMapper<ApmAppViewConfig> {

    /**
     * 删除指定bid的记录。
     *
     * @param bid 要删除的记录的bid。
     * @return 删除的记录数。
     */
    int deleteByBid(@Param("bid") String bid);

    /**
     * 删除指定id列表的记录。
     *
     * @param ids 要删除的记录的id列表。
     * @return 删除的记录数。
     */
    int deleteByIds(@Param("ids") List<Integer> ids);

}




