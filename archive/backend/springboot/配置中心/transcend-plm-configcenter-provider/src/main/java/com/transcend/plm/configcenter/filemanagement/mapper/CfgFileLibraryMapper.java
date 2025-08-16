package com.transcend.plm.configcenter.filemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.configcenter.filemanagement.domain.CfgFileLibrary;
import org.apache.ibatis.annotations.Param;

/**
 * @Entity generator.domain.CfgFileLibrary
 */
public interface CfgFileLibraryMapper extends BaseMapper<CfgFileLibrary> {
    int deleteByBid(@Param("bid") String bid);
}




