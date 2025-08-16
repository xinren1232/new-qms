package com.transcend.plm.configcenter.filemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.configcenter.filemanagement.domain.CfgFileCopyRule;
import org.apache.ibatis.annotations.Param;

/**
 * @Entity generator.domain.CfgFileCopyRule
 */
public interface CfgFileCopyRuleMapper extends BaseMapper<CfgFileCopyRule> {

    int deleteByBid(@Param("bid") String bid);
}




