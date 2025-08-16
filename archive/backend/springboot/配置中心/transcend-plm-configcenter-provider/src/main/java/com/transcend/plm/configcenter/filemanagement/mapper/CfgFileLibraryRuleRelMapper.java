package com.transcend.plm.configcenter.filemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.configcenter.filemanagement.domain.CfgFileLibraryRuleRel;
import org.apache.ibatis.annotations.Param;

/**
 * @Entity generator.domain.CfgFileLibraryRuleRel
 */
public interface CfgFileLibraryRuleRelMapper extends BaseMapper<CfgFileLibraryRuleRel> {

    int deleteByBid(@Param("bid") String bid);
}




